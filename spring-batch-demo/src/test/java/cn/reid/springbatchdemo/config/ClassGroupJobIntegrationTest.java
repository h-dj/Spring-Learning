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
class ClassGroupJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldProcessClassGroupFileCorrectly() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("fileType", "class_group")
                .addString("filePath", "src/test/resources/data/class-group-test.dat")
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
                "SELECT * FROM t_class_group ORDER BY class_no");

        assertEquals(3, rows.size());

        Map<String, Object> row1 = rows.get(0);
        assertEquals("CS101", row1.get("class_no"));
        assertEquals("ACTIVE", row1.get("status"));

        Map<String, Object> row2 = rows.get(1);
        assertEquals("CS102", row2.get("class_no"));
        assertEquals("ACTIVE", row2.get("status"));

        Map<String, Object> row3 = rows.get(2);
        assertEquals("PHY", row3.get("class_no"));
        assertEquals("ACTIVE", row3.get("status"));
    }
}
