package cn.reid.springbatchdemo.config;

import cn.reid.springbatchdemo.entity.ExamScore;
import cn.reid.springbatchdemo.listener.FileProcessingMetricsListener;
import cn.reid.springbatchdemo.mapper.ExamScoreFieldSetMapper;
import cn.reid.springbatchdemo.monitor.ChunkMetricsChunkListener;
import cn.reid.springbatchdemo.monitor.ItemTimingListener;
import cn.reid.springbatchdemo.monitor.MonitoringFacade;
import cn.reid.springbatchdemo.monitor.SkipCollectorListener;
import cn.reid.springbatchdemo.processor.ExamScoreProcessor;
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
public class ExamScoreStepConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<ExamScore> examScoreReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<ExamScore> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);

        DefaultLineMapper<ExamScore> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer("|");
        tokenizer.setNames(new String[]{
                "studentNo", "courseCode", "score", "examDate",
                "examType", "creditPoints", "rank", "passed", "comments", "gradedBy"
        });
        tokenizer.setStrict(false);

        ExamScoreFieldSetMapper fieldSetMapper = new ExamScoreFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<ExamScore> examScoreWriter(
            DataSource dataSource,
            @Value("#{jobParameters['fileType']}") String fileType,
            ResourceLoader resourceLoader) throws IOException {

        var resource = resourceLoader.getResource("classpath:sql/" + fileType + "-insert.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        JdbcBatchItemWriter<ExamScore> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql(sql);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setAssertUpdates(false);
        return writer;
    }

    @Bean
    public Step examScoreStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<ExamScore> examScoreReader,
            ExamScoreProcessor examScoreProcessor,
            JdbcBatchItemWriter<ExamScore> examScoreWriter,
            FileProcessingMetricsListener listener,
            ItemTimingListener timingListener,
            SkipCollectorListener skipListener,
            MonitoringFacade monitoringFacade) {

        return new StepBuilder("examScoreStep", jobRepository)
                .<ExamScore, ExamScore>chunk(500, transactionManager)
                .reader(examScoreReader)
                .processor(examScoreProcessor)
                .writer(examScoreWriter)
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
