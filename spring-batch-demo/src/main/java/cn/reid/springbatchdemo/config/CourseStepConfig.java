package cn.reid.springbatchdemo.config;

import cn.reid.springbatchdemo.entity.Course;
import cn.reid.springbatchdemo.listener.FileProcessingMetricsListener;
import cn.reid.springbatchdemo.mapper.CourseFieldSetMapper;
import cn.reid.springbatchdemo.monitor.ChunkMetricsChunkListener;
import cn.reid.springbatchdemo.monitor.ItemTimingListener;
import cn.reid.springbatchdemo.monitor.MonitoringFacade;
import cn.reid.springbatchdemo.monitor.SkipCollectorListener;
import cn.reid.springbatchdemo.processor.CourseProcessor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class CourseStepConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Course> courseReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<Course> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1); // 跳过表头

        DefaultLineMapper<Course> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames(new String[]{
                "courseCode", "courseName", "credits", "courseType",
                "department", "teacher", "maxStudents", "hours", "status", "description"
        });
        tokenizer.setStrict(false); // 允许行尾缺少字段

        CourseFieldSetMapper fieldSetMapper = new CourseFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Course> courseWriter(
            DataSource dataSource,
            @Value("#{jobParameters['fileType']}") String fileType,
            ResourceLoader resourceLoader) throws IOException {

        var resource = resourceLoader.getResource("classpath:sql/" + fileType + "-insert.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        JdbcBatchItemWriter<Course> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql(sql);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setAssertUpdates(false);
        return writer;
    }

    @Bean
    public Step courseStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<Course> courseReader,
            CourseProcessor courseProcessor,
            JdbcBatchItemWriter<Course> courseWriter,
            FileProcessingMetricsListener listener,
            ItemTimingListener timingListener,
            SkipCollectorListener skipListener,
            MonitoringFacade monitoringFacade) {

        return new StepBuilder("courseStep", jobRepository)
                .<Course, Course>chunk(500, transactionManager)
                .reader(courseReader)
                .processor(courseProcessor)
                .writer(courseWriter)
                .listener(listener)
                .listener((ItemReadListener<Object>) timingListener)
                .listener((ItemProcessListener<Object, Object>) timingListener)
                .listener((ItemWriteListener<Object>) timingListener)
                .listener(skipListener)
                .listener(new ChunkMetricsChunkListener(timingListener, skipListener, listener, monitoringFacade))
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }
}
