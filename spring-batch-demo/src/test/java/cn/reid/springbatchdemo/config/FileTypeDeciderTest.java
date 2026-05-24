package cn.reid.springbatchdemo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FileTypeDeciderTest {

    private FileTypeDecider decider;
    private JobExecution jobExecution;

    @BeforeEach
    void setUp() {
        decider = new FileTypeDecider();
    }

    private void givenFileType(String fileType) {
        JobParameters params = new JobParametersBuilder()
                .addString("fileType", fileType)
                .toJobParameters();
        jobExecution = new JobExecution(1L, params);
        jobExecution.setJobInstance(new JobInstance(1L, "fileJob"));
    }

    @Test
    @DisplayName("fileType=student → STUDENT")
    void testStudent() {
        givenFileType("student");
        assertEquals(new FlowExecutionStatus("STUDENT"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("fileType=course → COURSE")
    void testCourse() {
        givenFileType("course");
        assertEquals(new FlowExecutionStatus("COURSE"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("fileType=class_group → CLASS_GROUP")
    void testClassGroup() {
        givenFileType("class_group");
        assertEquals(new FlowExecutionStatus("CLASS_GROUP"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("fileType=exam_score → EXAM_SCORE")
    void testExamScore() {
        givenFileType("exam_score");
        assertEquals(new FlowExecutionStatus("EXAM_SCORE"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("fileType=enrollment → ENROLLMENT")
    void testEnrollment() {
        givenFileType("enrollment");
        assertEquals(new FlowExecutionStatus("ENROLLMENT"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("fileType=teacher → TEACHER")
    void testTeacher() {
        givenFileType("teacher");
        assertEquals(new FlowExecutionStatus("TEACHER"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("未知 fileType → UNKNOWN")
    void testUnknown() {
        givenFileType("nonexistent");
        assertEquals(new FlowExecutionStatus("UNKNOWN"), decider.decide(jobExecution, null));
    }

    @Test
    @DisplayName("无 fileType 参数 → UNKNOWN")
    void testMissing() {
        jobExecution = new JobExecution(1L, new JobParameters(Collections.emptyMap()));
        jobExecution.setJobInstance(new JobInstance(1L, "fileJob"));
        assertEquals(new FlowExecutionStatus("UNKNOWN"), decider.decide(jobExecution, null));
    }
}
