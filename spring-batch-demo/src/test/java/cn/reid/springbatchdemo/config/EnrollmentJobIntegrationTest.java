package cn.reid.springbatchdemo.config;

import org.junit.jupiter.api.DisplayName;
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
class EnrollmentJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("选课文件处理流程：6 条读取 3 条写入 3 条过滤")
    void shouldProcessEnrollmentFileCorrectly() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("fileType", "enrollment")
                .addString("filePath", "src/test/resources/data/enrollment-test.dat")
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        Long jobExecutionId = execution.getJobId();

        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            execution = jobExplorer.getJobExecution(jobExecutionId);
            if (execution != null && execution.getEndTime() != null) {
                break;
            }
            Thread.sleep(500);
        }

        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());

        var stepExecutions = execution.getStepExecutions();
        stepExecutions.forEach(stepExecution -> {
            assertEquals(6, stepExecution.getReadCount());
            assertEquals(3, stepExecution.getWriteCount());
            long filterCount = stepExecution.getReadCount()
                    - stepExecution.getWriteCount()
                    - stepExecution.getSkipCount();
            assertEquals(3, filterCount);
        });

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_enrollment ORDER BY student_no");

        assertEquals(3, rows.size());

        Map<String, Object> row1 = rows.get(0);
        assertEquals("TEST001", row1.get("student_no"));
        assertEquals("ENROLLED", row1.get("status"));
        assertEquals("B", row1.get("final_grade"));

        Map<String, Object> row2 = rows.get(1);
        assertEquals("TEST002", row2.get("student_no"));
        assertEquals("ENROLLED", row2.get("status"));
        assertEquals("A", row2.get("final_grade"));

        Map<String, Object> row3 = rows.get(2);
        assertEquals("TEST003", row3.get("student_no"));
        assertEquals("COMPLETED", row3.get("status"));
        assertEquals("C", row3.get("final_grade"));
    }
}
