package cn.reid.springbatchdemo.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringBootTest
class ExamScoreJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldProcessExamScoreFileCorrectly() throws Exception {
        // 准备测试参数
        JobParameters params = new JobParametersBuilder()
                .addString("fileType", "exam_score")
                .addString("filePath", "src/test/resources/data/exam-score-test.dat")
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();

        // 异步启动 Job
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        Long jobExecutionId = execution.getJobId();

        // 等待 Job 完成（超时 30 秒，轮询间隔 500ms）
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            execution = jobExplorer.getJobExecution(jobExecutionId);
            if (execution != null && execution.getEndTime() != null) {
                break;
            }
            Thread.sleep(500);
        }

        // 验证 Job 成功完成
        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());

        // 验证 Step 统计
        var stepExecutions = execution.getStepExecutions();
        stepExecutions.forEach(stepExecution -> {
            assertEquals(6, stepExecution.getReadCount());    // 读取 6 条
            assertEquals(3, stepExecution.getWriteCount());   // 写入 3 条
            long filterCount = stepExecution.getReadCount()
                    - stepExecution.getWriteCount()
                    - stepExecution.getSkipCount();
            assertEquals(3, filterCount);                     // 过滤 3 条
        });

        // 验证数据库中的数据
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_exam_score ORDER BY student_no");

        assertEquals(3, rows.size());

        // 验证第一条数据
        Map<String, Object> row1 = rows.get(0);
        assertEquals("TEST001", row1.get("student_no"));
        assertEquals("FINAL", row1.get("exam_type"));

        // 验证第二条数据
        Map<String, Object> row2 = rows.get(1);
        assertEquals("TEST002", row2.get("student_no"));
        assertEquals("MIDTERM", row2.get("exam_type"));

        // 验证第三条数据
        Map<String, Object> row3 = rows.get(2);
        assertEquals("TEST003", row3.get("student_no"));
        assertEquals("QUIZ", row3.get("exam_type"));
        assertEquals("N", row3.get("passed"));
    }
}
