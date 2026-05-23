package cn.reid.springbatchdemo.controller;

import cn.reid.springbatchdemo.dto.FileProcessResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileProcessController {

    private final JobLauncher jobLauncher;
    private final Job fileJob;

    public FileProcessController(JobLauncher jobLauncher, Job fileJob) {
        this.jobLauncher = jobLauncher;
        this.fileJob = fileJob;
    }

    @PostMapping("/process")
    public ResponseEntity<FileProcessResponse> processFile(
            @RequestParam String fileType,
            @RequestParam String filePath) {

        JobParameters params = new JobParametersBuilder()
                .addString("fileType", fileType)
                .addString("filePath", filePath)
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            var execution = jobLauncher.run(fileJob, params);
            return ResponseEntity.ok(FileProcessResponse.success(
                    execution.getJobId(),
                    execution.getJobInstance().getJobName(),
                    fileType,
                    filePath
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(FileProcessResponse.failed(e.getMessage()));
        }
    }
}
