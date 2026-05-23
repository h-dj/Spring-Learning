package cn.reid.springbatchdemo.dto;

import lombok.Data;

@Data
public class FileProcessResponse {

    private Long jobExecutionId;
    private String jobName;
    private String fileType;
    private String filePath;
    private String status;
    private String message;

    public static FileProcessResponse success(Long jobExecutionId, String jobName, String fileType, String filePath) {
        FileProcessResponse r = new FileProcessResponse();
        r.setJobExecutionId(jobExecutionId);
        r.setJobName(jobName);
        r.setFileType(fileType);
        r.setFilePath(filePath);
        r.setStatus("STARTED");
        r.setMessage("Job started asynchronously");
        return r;
    }

    public static FileProcessResponse failed(String message) {
        FileProcessResponse r = new FileProcessResponse();
        r.setJobExecutionId(-1L);
        r.setStatus("FAILED");
        r.setMessage(message);
        return r;
    }
}
