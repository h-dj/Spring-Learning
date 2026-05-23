package cn.reid.springbatchdemo.config;

import cn.reid.springbatchdemo.entity.Student;
import cn.reid.springbatchdemo.listener.FileProcessingMetricsListener;
import cn.reid.springbatchdemo.mapper.StudentFieldSetMapper;
import cn.reid.springbatchdemo.processor.StudentProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class FileJobConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Student> studentReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<Student> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1); // 跳过表头

        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames(new String[]{
                "studentNo", "name", "gender", "birthDate",
                "phone", "email", "className", "enrollmentYear", "status"
        });
        tokenizer.setStrict(false); // 允许行尾缺少字段

        StudentFieldSetMapper fieldSetMapper = new StudentFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public ThreadPoolTaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("batch-launcher-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public JobLauncher asyncJobLauncher(JobRepository jobRepository, ThreadPoolTaskExecutor batchTaskExecutor) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(batchTaskExecutor);
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Bean
    public JdbcBatchItemWriter<Student> studentWriter(DataSource dataSource) {
        JdbcBatchItemWriter<Student> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("""
                INSERT INTO t_student (student_no, name, gender, birth_date, phone, email, class_name, enrollment_year, status)
                VALUES (:studentNo, :name, :gender, :birthDate, :phone, :email, :className, :enrollmentYear, :status)
                """);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }

    @Bean
    public Step fileStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<Student> studentReader,
            StudentProcessor studentProcessor,
            JdbcBatchItemWriter<Student> studentWriter,
            FileProcessingMetricsListener listener) {

        return new StepBuilder("fileStep", jobRepository)
                .<Student, Student>chunk(100, transactionManager)
                .reader(studentReader)
                .processor(studentProcessor)
                .writer(studentWriter)
                .listener(listener)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }

    @Bean
    public Job fileJob(JobRepository jobRepository, Step fileStep) {
        return new JobBuilder("fileJob", jobRepository)
                .start(fileStep)
                .build();
    }
}
