package cn.reid.springbatchdemo.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class FileTypeDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String fileType = jobExecution.getJobParameters().getString("fileType");
        if (fileType == null) {
            return new FlowExecutionStatus("UNKNOWN");
        }
        return switch (fileType) {
            case "student" -> new FlowExecutionStatus("STUDENT");
            case "course" -> new FlowExecutionStatus("COURSE");
            case "class_group" -> new FlowExecutionStatus("CLASS_GROUP");
            case "exam_score" -> new FlowExecutionStatus("EXAM_SCORE");
            case "enrollment" -> new FlowExecutionStatus("ENROLLMENT");
            case "teacher" -> new FlowExecutionStatus("TEACHER");
            default -> new FlowExecutionStatus("UNKNOWN");
        };
    }
}
