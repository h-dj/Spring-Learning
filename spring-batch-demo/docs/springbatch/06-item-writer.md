# Chapter 6: Item Writers

## Overview
Master different ItemWriter implementations for writing to databases, files, and message systems.

## 6.1 Writer Types Overview

```
ItemWriter
├── FlatFileItemWriter (Text files)
│   ├── DelimitedLineAggregator
│   └── FormattedLineAggregator
├── XmlItemWriter (XML files)
├── JsonItemWriter (JSON files)
├── JdbcBatchItemWriter (JDBC batch)
├── JpaItemWriter (JPA entities)
├── HibernateItemWriter (Hibernate)
├── MongoItemWriter (MongoDB)
├── RabbitItemWriter (RabbitMQ)
├── KafkaItemWriter (Kafka)
└── Custom ItemWriter
```

## 6.2 FlatFileItemWriter

### Delimited File Writer
```java
@Bean
@StepScope
public FlatFileItemWriter<Customer> delimitedWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new FlatFileItemWriterBuilder<Customer>()
        .name("delimitedWriter")
        .resource(new FileSystemResource(filePath))
        .delimited()
        .delimiter(",")
        .names("id", "firstName", "lastName", "email")
        .headerCallback(writer -> writer.write("id,firstName,lastName,email"))
        .footerCallback(writer -> writer.write(
            "Total records: " + /* dynamic value */ "0"))
        .build();
}
```

### Fixed Width Writer
```java
@Bean
@StepScope
public FlatFileItemWriter<Customer> fixedWidthWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new FlatFileItemWriterBuilder<Customer>()
        .name("fixedWidthWriter")
        .resource(new FileSystemResource(filePath))
        .fixedLength()
        .names("id", "firstName", "lastName")
        .columns(new Range(1, 10), new Range(11, 30),
                 new Range(31, 50))
        .build();
}
```

### Formatted Writer
```java
@Bean
@StepScope
public FlatFileItemWriter<Customer> formattedWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new FlatFileItemWriterBuilder<Customer>()
        .name("formattedWriter")
        .resource(new FileSystemResource(filePath))
        .formatted()
        .format("%-10d %-20s %-20s %-30s")
        .names("id", "firstName", "lastName", "email")
        .headerCallback(writer -> writer.write(
            String.format("%-10s %-20s %-20s %-30s",
                "ID", "First Name", "Last Name", "Email")))
        .build();
}
```

### Custom Line Aggregator
```java
@Bean
@StepScope
public FlatFileItemWriter<Customer> customWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new FlatFileItemWriterBuilder<Customer>()
        .name("customWriter")
        .resource(new FileSystemResource(filePath))
        .lineAggregator(new CustomLineAggregator())
        .headerCallback(writer -> writer.write("ID|FIRST_NAME|LAST_NAME|EMAIL"))
        .build();
}

public class CustomLineAggregator implements LineAggregator<Customer> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String aggregate(Customer item) {
        return String.format("%d|%s|%s|%s",
            item.getId(),
            item.getFirstName(),
            item.getLastName(),
            item.getEmail());
    }
}
```

## 6.3 XML ItemWriter

### XML Writer Configuration
```java
@Bean
@StepScope
public StaxEventItemWriter<Customer> xmlWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new StaxEventItemWriterBuilder<Customer>()
        .name("xmlWriter")
        .resource(new FileSystemResource(filePath))
        .rootTagName("customers")
        .addFragmentRootElements("customer")
        .marshaller(new Jaxb2Marshaller() {{
            setClassesToBeBound(Customer.class);
        }})
        .build();
}
```

### XML with Header/Footer
```java
@Bean
@StepScope
public StaxEventItemWriter<Customer> xmlWriterWithHeader(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new StaxEventItemWriterBuilder<Customer>()
        .name("xmlWriterWithHeader")
        .resource(new FileSystemResource(filePath))
        .rootTagName("Customers")
        .addFragmentRootElements("Customer")
        .headerCallback(writer -> {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("<Customers xmlns=\"http://example.com\">");
        })
        .footerCallback(writer -> {
            writer.write("</Customers>");
        })
        .marshaller(new Jaxb2Marshaller() {{
            setClassesToBeBound(Customer.class);
        }})
        .build();
}
```

## 6.4 JSON ItemWriter

```java
@Bean
@StepScope
public JsonItemWriter<Customer> jsonWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new JsonItemReaderBuilder<Customer>()
        .name("jsonWriter")
        .resource(new FileSystemResource(filePath))
        .jsonObjectWriter(new JacksonJsonObjectWriter<>(Customer.class))
        .build();
}
```

### JSON Array Writer
```java
@Bean
@StepScope
public JsonItemWriter<List<Customer>> jsonArrayWriter(
        @Value("#{jobParameters['output.file']}") String filePath) {

    return new JsonItemWriterBuilder<List<Customer>>()
        .name("jsonArrayWriter")
        .resource(new FileSystemResource(filePath))
        .jsonObjectWriter(new JacksonJsonObjectWriter<>(List.class) {{
            setPrettyPrint(true);
        }})
        .build();
}
```

## 6.5 JdbcBatchItemWriter

### Basic JDBC Writer
```java
@Bean
public JdbcBatchItemWriter<Customer> jdbcWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("jdbcWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, first_name, last_name, email) " +
             "VALUES (?, ?, ?, ?)")
        .itemPreparedStatementSetter(new CustomerPreparedStatementSetter())
        .build();
}

public class CustomerPreparedStatementSetter
        implements ItemPreparedStatementSetter<Customer> {

    @Override
    public void setValues(Customer item, PreparedStatement ps)
            throws SQLException {
        ps.setLong(1, item.getId());
        ps.setString(2, item.getFirstName());
        ps.setString(3, item.getLastName());
        ps.setString(4, item.getEmail());
    }
}
```

### Using BeanPropertySqlParameterSource
```java
@Bean
public JdbcBatchItemWriter<Customer> beanPropertyWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("beanPropertyWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, first_name, last_name, email) " +
             "VALUES (:id, :firstName, :lastName, :email)")
        .itemSqlParameterSourceProvider(
            new BeanPropertyItemSqlParameterSourceProvider<>())
        .build();
}
```

### Upsert Writer (MySQL)
```java
@Bean
public JdbcBatchItemWriter<Customer> upsertWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("upsertWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, first_name, last_name, email) " +
             "VALUES (?, ?, ?, ?) " +
             "ON DUPLICATE KEY UPDATE " +
             "first_name = VALUES(first_name), " +
             "last_name = VALUES(last_name), " +
             "email = VALUES(email)")
        .itemPreparedStatementSetter(new CustomerPreparedStatementSetter())
        .build();
}
```

### PostgreSQL Upsert
```java
@Bean
public JdbcBatchItemWriter<Customer> postgresUpsertWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("postgresUpsertWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, first_name, last_name, email) " +
             "VALUES (?, ?, ?, ?) " +
             "ON CONFLICT (id) DO UPDATE SET " +
             "first_name = EXCLUDED.first_name, " +
             "last_name = EXCLUDED.last_name, " +
             "email = EXCLUDED.email")
        .itemPreparedStatementSetter(new CustomerPreparedStatementSetter())
        .build();
}
```

### Named Parameter Writer
```java
@Bean
public JdbcBatchItemWriter<Customer> namedParamWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("namedParamWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, firstName, lastName, email) " +
             "VALUES (:id, :firstName, :lastName, :email)")
        .itemSqlParameterSourceProvider(
            new BeanPropertyItemSqlParameterSourceProvider<>())
        .build();
}
```

## 6.6 JpaItemWriter

```java
@Bean
public JpaItemWriter<Customer> jpaWriter(
        EntityManagerFactory entityManagerFactory) {

    return new JpaItemWriterBuilder<Customer>()
        .name("jpaWriter")
        .entityManagerFactory(entityManagerFactory)
        .usePersist(false)  // Use merge instead of persist
        .build();
}
```

### With EntityManager
```java
@Bean
public JpaItemWriter<Customer> customJpaWriter(
        EntityManagerFactory entityManagerFactory) {

    return new JpaItemWriterBuilder<Customer>()
        .name("customJpaWriter")
        .entityManagerFactory(entityManagerFactory)
        .build();
}
```

### Flushing After Write
```java
@Component
public class FlushingItemWriter<T> implements ItemWriter<T> {

    private final EntityManagerFactory entityManagerFactory;
    private EntityManager em;

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        if (em == null || !em.isOpen()) {
            em = entityManagerFactory.createEntityManager();
        }

        em.getTransaction().begin();
        try {
            for (T item : chunk) {
                em.merge(item);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
```

## 6.7 MongoItemWriter

```java
@Bean
public MongoItemWriter<Customer> mongoWriter(
        MongoTemplate mongoTemplate) {

    return new MongoItemWriterBuilder<Customer>()
        .name("mongoWriter")
        .template(mongoTemplate)
        .collection("customers")
        .build();
}
```

### Dynamic Collection
```java
@Bean
@StepScope
public MongoItemWriter<Customer> dynamicCollectionWriter(
        MongoTemplate mongoTemplate,
        @Value("#{jobParameters['collection']}") String collection) {

    return new MongoItemWriterBuilder<Customer>()
        .name("dynamicCollectionWriter")
        .template(mongoTemplate)
        .collection(collection)
        .build();
}
```

## 6.8 RabbitMQ Writer

### Adding Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### RabbitMQ Writer
```java
@Bean
public RabbitItemWriter<Customer> rabbitWriter(
        RabbitTemplate rabbitTemplate) {

    return new RabbitItemWriterBuilder<Customer>()
        .name("rabbitWriter")
        .rabbitTemplate(rabbitTemplate)
        .exchange("batch.exchange")
        .routingKey("customer.created")
        .build();
}
```

### Message Converter
```java
@Bean
public RabbitItemWriter<Customer> jsonRabbitWriter(
        RabbitTemplate rabbitTemplate) {

    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

    return new RabbitItemWriterBuilder<Customer>()
        .name("jsonRabbitWriter")
        .rabbitTemplate(rabbitTemplate)
        .exchange("batch.exchange")
        .routingKey("customer.created")
        .build();
}
```

## 6.9 Kafka ItemWriter

### Adding Dependency
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### Kafka Writer
```java
@Bean
@StepScope
public KafkaItemWriter<String, Customer> kafkaWriter(
        KafkaTemplate<String, Customer> kafkaTemplate,
        @Value("#{jobParameters['topic']}") String topic) {

    return new KafkaItemWriterBuilder<String, Customer>()
        .name("kafkaWriter")
        .kafkaTemplate(kafkaTemplate)
        .topic(topic)
        .build();
}
```

### Custom Kafka Writer
```java
public class CustomKafkaItemWriter implements ItemWriter<Customer> {

    private final Producer<String, Customer> producer;
    private final String topic;
    private final String bootstrapServers;

    public CustomKafkaItemWriter(String topic, String bootstrapServers) {
        this.topic = topic;
        this.bootstrapServers = bootstrapServers;
        this.producer = new KafkaProducer<>(
            Collections.singletonMap(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
            new StringSerializer<>(),
            new JsonSerializer<>());
    }

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        for (Customer customer : chunk) {
            ProducerRecord<String, Customer> record =
                new ProducerRecord<>(topic,
                    customer.getId().toString(), customer);
            producer.send(record);
        }
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
```

## 6.10 CompositeItemWriter

### Multiple Writers
```java
@Bean
public CompositeItemWriter<Customer> compositeWriter(
        JdbcBatchItemWriter<Customer> jdbcWriter,
        FlatFileItemWriter<Customer> fileWriter) {

    return new CompositeItemWriterBuilder<Customer>()
        .name("compositeWriter")
        .writers(jdbcWriter, fileWriter)
        .build();
}
```

### Conditional Writer
```java
@Bean
public CompositeItemWriter<Customer> conditionalWriter(
        JdbcBatchItemWriter<Customer> jdbcWriter,
        FlatFileItemWriter<Customer> fileWriter) {

    List<ItemWriter<? super Customer>> delegates =
        new ArrayList<>();
    delegates.add(jdbcWriter);

    if (Boolean.parseBoolean(System.getProperty("write.to.file"))) {
        delegates.add(fileWriter);
    }

    return new CompositeItemWriterBuilder<Customer>()
        .name("conditionalWriter")
        .writers(delegates)
        .build();
}
```

## 6.11 Custom ItemWriter

### Simple Custom Writer
```java
public class LoggingItemWriter implements ItemWriter<String> {

    private final Logger logger = LoggerFactory.getLogger(LoggingItemWriter.class);

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String item : chunk) {
            logger.info("Writing item: {}", item);
        }
        System.out.println("Wrote " + chunk.size() + " items");
    }
}
```

### Batching Writer
```java
public class BatchingItemWriter implements ItemWriter<Customer> {

    private final CustomerRepository repository;
    private final int batchSize;

    public BatchingItemWriter(CustomerRepository repository,
                              int batchSize) {
        this.repository = repository;
        this.batchSize = batchSize;
    }

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        List<Customer> items = new ArrayList<>(chunk);
        for (int i = 0; i < items.size(); i += batchSize) {
            List<Customer> batch = items.subList(
                i, Math.min(i + batchSize, items.size()));
            repository.saveAll(batch);
        }
    }
}
```

### Deduplication Writer
```java
public class DeduplicationWriter implements ItemWriter<Customer> {

    private final Set<Long> seenIds = ConcurrentHashMap.newKeySet();
    private final JdbcBatchItemWriter<Customer> delegate;

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        List<Customer> uniqueItems = chunk.getItems().stream()
            .filter(c -> seenIds.add(c.getId()))
            .collect(Collectors.toList());

        if (!uniqueItems.isEmpty()) {
            delegate.write(new Chunk<>(uniqueItems));
        }
    }
}
```

## 6.12 Writer Configuration Best Practices

### Using @StepScope
```java
@Bean
@StepScope
public ItemWriter<Customer> dynamicWriter(
        DataSource dataSource,
        @Value("#{jobParameters['write.mode']}") String mode) {

    if ("database".equals(mode)) {
        return new JdbcBatchItemWriterBuilder<Customer>()
            .dataSource(dataSource)
            .sql("INSERT INTO customers...")
            .build();
    } else {
        return new FlatFileItemWriterBuilder<Customer>()
            .resource(new FileSystemResource("output.csv"))
            .build();
    }
}
```

### Fault-tolerant Writer
```java
@Bean
public JdbcBatchItemWriter<Customer> faultTolerantWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("faultTolerantWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers...")
        .itemPreparedStatementSetter(new CustomerPreparedStatementSetter())
        .assertUpdates(true)
        .build();
}
```

### Performance Optimization
```java
@Bean
public JdbcBatchItemWriter<Customer> optimizedWriter(
        DataSource dataSource) {

    return new JdbcBatchItemWriterBuilder<Customer>()
        .name("optimizedWriter")
        .dataSource(dataSource)
        .sql("INSERT INTO customers (id, first_name, last_name, email) " +
             "VALUES (?, ?, ?, ?)")
        .itemPreparedStatementSetter(new CustomerPreparedStatementSetter())
        .build();
}
```

## 6.13 Practice Scenario

### Scenario: Data Export Pipeline
```java
@Configuration
public class DataExportJobConfig {

    @Bean
    public Job dataExportJob(JobRepository jobRepository,
                             Step exportStep) {
        return new JobBuilder("dataExportJob", jobRepository)
            .start(exportStep)
            .build();
    }

    @Bean
    public Step exportStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          ItemReader<Customer> reader,
                          ItemProcessor<Customer, Customer> processor,
                          ItemWriter<Customer> writer) {

        return new StepBuilder("exportStep", jobRepository)
            .<Customer, Customer>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(new ExportStepListener())
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Customer> exportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
            .name("exportReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM customers WHERE status = 'ACTIVE'")
            .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
            .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Customer> exportWriter(
            @Value("#{jobParameters['output.format']}") String format,
            @Value("#{jobParameters['output.file']}") String filePath) {

        if ("csv".equalsIgnoreCase(format)) {
            return new FlatFileItemWriterBuilder<Customer>()
                .name("csvWriter")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("id", "firstName", "lastName", "email")
                .headerCallback(writer ->
                    writer.write("ID,FirstName,LastName,Email"))
                .build();
        } else {
            return new JsonItemWriterBuilder<Customer>()
                .name("jsonWriter")
                .resource(new FileSystemResource(filePath))
                .jsonObjectWriter(new JacksonJsonObjectWriter<>(Customer.class))
                .build();
        }
    }
}
```

## 6.14 Summary

| Writer | Use Case | Key Configuration |
|--------|----------|-------------------|
| FlatFileItemWriter | CSV, fixed-width files | `resource`, `delimited()`, `names()` |
| StaxEventItemWriter | XML files | `resource`, `rootTagName`, `marshaller` |
| JsonItemWriter | JSON files | `resource`, `jsonObjectWriter()` |
| JdbcBatchItemWriter | JDBC batch insert | `dataSource`, `sql`, `itemPreparedStatementSetter` |
| JpaItemWriter | JPA entities | `entityManagerFactory` |
| MongoItemWriter | MongoDB | `template`, `collection` |
| RabbitItemWriter | RabbitMQ | `rabbitTemplate`, `exchange`, `routingKey` |
| KafkaItemWriter | Kafka | `kafkaTemplate`, `topic` |
| CompositeItemWriter | Multiple outputs | `.writers(writer1, writer2)` |

## 6.15 Next Steps

- [Chapter 7: Item Processors](07-item-processor.md)
- Learn about data transformation and filtering
- Implement validation and enrichment

## Exercises

### Exercise 1: Multi-format Export
Create a job that:
1. Reads from database
2. Exports to both CSV and JSON formats
3. Uses CompositeItemWriter

### Exercise 2: Upsert Writer
Implement a writer that:
1. Handles inserts and updates
2. Uses database-specific UPSERT syntax
3. Reports whether each was insert or update

### Exercise 3: Message Queue Writer
Create a writer that:
1. Sends items to Kafka topic
2. Includes metadata in message
3. Handles producer errors gracefully

---
*Duration: 2 hours*
