package cn.reid.springbatchdemo.config;

import cn.reid.springbatchdemo.entity.Teacher;
import cn.reid.springbatchdemo.listener.FileProcessingMetricsListener;
import cn.reid.springbatchdemo.mapper.TeacherFieldSetMapper;
import cn.reid.springbatchdemo.monitor.ChunkMetricsChunkListener;
import cn.reid.springbatchdemo.monitor.ItemTimingListener;
import cn.reid.springbatchdemo.monitor.MonitoringFacade;
import cn.reid.springbatchdemo.monitor.SkipCollectorListener;
import cn.reid.springbatchdemo.processor.TeacherProcessor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
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
public class TeacherStepConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<Teacher> teacherReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<Teacher> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);

        DefaultLineMapper<Teacher> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames(new String[]{
                "teacherNo", "name", "gender", "title", "degree", "department",
                "phone", "email", "hireDate", "salaryLevel", "isAdvisor", "maxCourses", "status"
        });
        tokenizer.setStrict(false);

        TeacherFieldSetMapper fieldSetMapper = new TeacherFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Teacher> teacherWriter(
            DataSource dataSource,
            @Value("#{jobParameters['fileType']}") String fileType,
            ResourceLoader resourceLoader) throws IOException {

        var resource = resourceLoader.getResource("classpath:sql/" + fileType + "-insert.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        JdbcBatchItemWriter<Teacher> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql(sql);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setAssertUpdates(false);
        return writer;
    }

    @Bean
    public Step teacherStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<Teacher> teacherReader,
            TeacherProcessor teacherProcessor,
            JdbcBatchItemWriter<Teacher> teacherWriter,
            FileProcessingMetricsListener listener,
            ItemTimingListener timingListener,
            SkipCollectorListener skipListener,
            MonitoringFacade monitoringFacade) {

        return new StepBuilder("teacherStep", jobRepository)
                .<Teacher, Teacher>chunk(500, transactionManager)
                .reader(teacherReader)
                .processor(teacherProcessor)
                .writer(teacherWriter)
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
