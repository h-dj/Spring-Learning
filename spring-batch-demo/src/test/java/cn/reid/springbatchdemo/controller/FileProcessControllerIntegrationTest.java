package cn.reid.springbatchdemo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileProcessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLauncher jobLauncher;

    @Autowired
    private Job fileJob;

    @Test
    @DisplayName("正常请求应返回 HTTP 200, status=STARTED")
    void shouldReturnStartedOnValidRequest() throws Exception {
        JobExecution mockExecution = mock(JobExecution.class);
        JobInstance mockInstance = mock(JobInstance.class);

        when(mockExecution.getJobId()).thenReturn(1L);
        when(mockExecution.getJobInstance()).thenReturn(mockInstance);
        when(mockInstance.getJobName()).thenReturn("fileJob");
        when(jobLauncher.run(eq(fileJob), any(JobParameters.class))).thenReturn(mockExecution);

        mockMvc.perform(post("/api/file/process")
                        .param("fileType", "student")
                        .param("filePath", "data/student.dat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.jobExecutionId").value(1))
                .andExpect(jsonPath("$.fileType").value("student"))
                .andExpect(jsonPath("$.filePath").value("data/student.dat"))
                .andExpect(jsonPath("$.message").value("Job started asynchronously"));
    }

    @Test
    @DisplayName("缺少必要参数应返回 HTTP 400")
    void shouldReturnBadRequestWhenMissingParams() throws Exception {
        mockMvc.perform(post("/api/file/process")
                        .param("fileType", "student"))
                .andExpect(status().isBadRequest());
    }
}
