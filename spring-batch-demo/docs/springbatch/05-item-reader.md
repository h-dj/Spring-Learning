# Chapter 5: Item Readers

## Overview
Master different ItemReader implementations for reading from databases, files, and APIs.

## 5.1 Reader Types Overview

```
ItemReader
├── FlatFileItemReader (Text files)
│   ├── LineMapper
│   └── FieldSetMapper
├── XmlItemReader (XML files)
├── JsonItemReader (JSON files)
├── JdbcCursorItemReader (Database cursor)
├── JdbcPagingItemReader (Database paging)
├── JpaItemReader (JPA entities)
├── MongoItemReader (MongoDB)
├── RestItemReader (REST APIs)
└── Custom ItemReader
```

## 5.2 FlatFileItemReader

### Reading from Text File
```java
@Bean
@StepScope
public FlatFileItemReader<String> flatFileReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new FlatFileItemReaderBuilder<String>()
        .name("flatFileReader")
        .resource(new FileSystemResource(filePath))
        .encoding("UTF-8")
        .linesToSkip(1)  // Skip header
        .build();
}
```

### Reading with Delimiter
```java
@Bean
@StepScope
public FlatFileItemReader<Customer> delimitedReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new FlatFileItemReaderBuilder<Customer>()
        .name("customerReader")
        .resource(new FileSystemResource(filePath))
        .delimited()
        .names("id", "firstName", "lastName", "email", "phone")
        .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {{
            setTargetType(Customer.class);
        }})
        .linesToSkip(1)
        .build();
}
```

### Fixed Width Reader
```java
@Bean
@StepScope
public FlatFileItemReader<FixedWidthCustomer> fixedWidthReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new FlatFileItemReaderBuilder<FixedWidthCustomer>()
        .name("fixedWidthReader")
        .resource(new FileSystemResource(filePath))
        .fixedLength()
        .names("id", "firstName", "lastName", "email")
        .columns(new Range(1, 10), new Range(11, 30),
                 new Range(31, 50), new Range(51, 100))
        .fieldSetMapper(new BeanWrapperFieldSetMapper<FixedWidthCustomer>() {{
            setTargetType(FixedWidthCustomer.class);
        }})
        .build();
}
```

### Custom LineMapper
```java
@Bean
@StepScope
public FlatFileItemReader<Customer> customLineMapperReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new FlatFileItemReaderBuilder<Customer>()
        .name("customReader")
        .resource(new FileSystemResource(filePath))
        .lineMapper(new CustomLineMapper())
        .linesToSkip(1)
        .build();
}

public class CustomLineMapper implements LineMapper<Customer> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Customer mapLine(String line, int lineNumber) throws Exception {
        // Parse JSON format
        return objectMapper.readValue(line, Customer.class);
    }
}
```

## 5.3 XML ItemReader

### Adding Dependency
```xml
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-integration</artifactId>
</dependency>
```

### Reading XML
```java
@Bean
@StepScope
public StaxEventItemReader<Customer> xmlReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new StaxEventItemReaderBuilder<Customer>()
        .name("xmlReader")
        .resource(new FileSystemResource(filePath))
        .addFragmentRootElements("customer")
        .unmarshaller(new Jaxb2Unmarshaller(Customer.class))
        .build();
}
```

### Custom XML Mapping
```java
@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {

    @XmlElement
    private Long id;

    @XmlElement
    private String firstName;

    @XmlElement
    private String lastName;

    @XmlElement
    private String email;

    // Getters and setters
}
```

## 5.4 JSON ItemReader

### Reading JSON
```java
@Bean
@StepScope
public JsonItemReader<Customer> jsonReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new JsonItemReaderBuilder<Customer>()
        .name("jsonReader")
        .resource(new FileSystemResource(filePath))
        .jsonObjectReader(new JacksonJsonObjectReader<>(Customer.class))
        .build();
}
```

### JSON Array File
```json
[
  {"id": 1, "firstName": "John", "lastName": "Doe", "email": "john@example.com"},
  {"id": 2, "firstName": "Jane", "lastName": "Smith", "email": "jane@example.com"}
]
```

### JSON Lines Format
```java
@Bean
@StepScope
public JsonItemReader<Customer> jsonLinesReader(
        @Value("#{jobParameters['input.file']}") String filePath) {

    return new JsonItemReaderBuilder<Customer>()
        .name("jsonLinesReader")
        .resource(new FileSystemResource(filePath))
        .jsonObjectReader(new JacksonJsonObjectReader<>(Customer.class) {
            {
                setUseJsonLines(true);
            }
        })
        .build();
}
```

## 5.5 JdbcCursorItemReader

### Basic JDBC Reader
```java
@Bean
public JdbcCursorItemReader<Customer> jdbcCursorReader(
        DataSource dataSource) {

    return new JdbcCursorItemReaderBuilder<Customer>()
        .name("jdbcCursorReader")
        .dataSource(dataSource)
        .sql("SELECT id, first_name, last_name, email FROM customers")
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .fetchSize(100)
        .build();
}
```

### Parameterized SQL
```java
@Bean
@StepScope
public JdbcCursorItemReader<Order> parameterizedJdbcReader(
        DataSource dataSource,
        @Value("#{jobParameters['status']}") String status) {

    return new JdbcCursorItemReaderBuilder<Order>()
        .name("parameterizedJdbcReader")
        .dataSource(dataSource)
        .sql("SELECT * FROM orders WHERE status = ?")
        .rowMapper(new BeanPropertyRowMapper<>(Order.class))
        .preparedStatementSetter(new ArgumentPreparedStatementSetter(
            new Object[]{status}))
        .fetchSize(50)
        .build();
}
```

### Using Named Parameters
```java
@Bean
public JdbcCursorItemReader<Order> namedParamReader(
        DataSource dataSource) {

    return new JdbcCursorItemReaderBuilder<Order>()
        .name("namedParamReader")
        .dataSource(dataSource)
        .sql("SELECT * FROM orders WHERE status = :status AND created_at > :since")
        .rowMapper(new BeanPropertyRowMapper<>(Order.class))
        .build();
}
```

## 5.6 JdbcPagingItemReader

### Paging Reader
```java
@Bean
public JdbcPagingItemReader<Customer> jdbcPagingReader(
        DataSource dataSource,
        PagingQueryProvider queryProvider) {

    Map<String, Order> sortKeys = new HashMap<>();
    sortKeys.put("id", Order.ASCENDING);

    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("jdbcPagingReader")
        .dataSource(dataSource)
        .queryProvider(queryProvider)
        .parameterValues(Collections.singletonMap("status", "ACTIVE"))
        .pageSize(100)
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .build();
}

@Bean
public SqlPagingQueryProviderFactoryBean pagingQueryProvider(
        DataSource dataSource) {

    SqlPagingQueryProviderFactoryBean provider =
        new SqlPagingQueryProviderFactoryBean();
    provider.setDataSource(dataSource);
    provider.setSelectClause("SELECT id, first_name, last_name, email");
    provider.setFromClause("FROM customers");
    provider.setWhereClause("WHERE status = :status");
    provider.setSortKey("id");
    return provider;
}
```

### MySQL Paging
```java
@Bean
public JdbcPagingItemReader<Customer> mysqlPagingReader(
        DataSource dataSource) {

    MySqlPagingQueryProvider provider = new MySqlPagingQueryProvider();
    provider.setSelectClause("SELECT id, first_name, last_name, email");
    provider.setFromClause("FROM customers");
    provider.setWhereClause("WHERE status = :status");
    provider.setSortKey("id");

    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("mysqlPagingReader")
        .dataSource(dataSource)
        .queryProvider(provider)
        .parameterValues(Collections.singletonMap("status", "ACTIVE"))
        .pageSize(100)
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .build();
}
```

### PostgreSQL Paging
```java
@Bean
public JdbcPagingItemReader<Customer> postgresPagingReader(
        DataSource dataSource) {

    PostgresPagingQueryProvider provider =
        new PostgresPagingQueryProvider();
    provider.setSelectClause("SELECT id, first_name, last_name, email");
    provider.setFromClause("FROM customers");
    provider.setWhereClause("WHERE status = :status");
    provider.setSortKey("id");

    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("postgresPagingReader")
        .dataSource(dataSource)
        .queryProvider(provider)
        .parameterValues(Collections.singletonMap("status", "ACTIVE"))
        .pageSize(100)
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .build();
}
```

## 5.7 JpaItemReader

```java
@Bean
public JpaItemReader<Customer> jpaReader(
        EntityManagerFactory entityManagerFactory) {

    return new JpaItemReaderBuilder<Customer>()
        .name("jpaReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT c FROM Customer c WHERE c.status = :status")
        .parameterValues(Collections.singletonMap("status", "ACTIVE"))
        .pageSize(50)
        .build();
}
```

### Named Query
```java
@Bean
public JpaItemReader<Order> namedQueryReader(
        EntityManagerFactory entityManagerFactory) {

    return new JpaItemReaderBuilder<Order>()
        .name("namedQueryReader")
        .entityManagerFactory(entityManagerFactory)
        .namedQuery("Order.findByStatus")
        .parameterValues(Collections.singletonMap("status", "PENDING"))
        .pageSize(100)
        .build Dynamic Query
```();
}
```

###java
@Bean
@StepScope
public JpaItemReader<Customer> dynamicJpaReader(
        EntityManagerFactory entityManagerFactory,
        @Value("#{jobParameters['status']}") String status) {

    return new JpaItemReaderBuilder<Customer>()
        .name("dynamicJpaReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT c FROM Customer c WHERE c.status = :status")
        .parameterValues(Collections.singletonMap("status", status))
        .pageSize(50)
        .build();
}
```

## 5.8 MongoItemReader

### Adding Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### MongoDB Reader
```java
@Bean
public MongoItemReader<Customer> mongoReader(
        MongoTemplate mongoTemplate) {

    Query query = new Query();
    query.addCriteria(Criteria.where("status").is("ACTIVE"));
    query.with(Sort.by(Sort.Direction.ASC, "id"));

    return new MongoItemReaderBuilder<Customer>()
        .name("mongoReader")
        .template(mongoTemplate)
        .query(query)
        .sort(Collections.singletonMap("_id", Sort.Direction.ASC))
        .targetType(Customer.class)
        .build();
}
```

### Dynamic Query
```java
@Bean
@StepScope
public MongoItemReader<Customer> dynamicMongoReader(
        MongoTemplate mongoTemplate,
        @Value("#{jobParameters['status']}") String status,
        @Value("#{jobParameters['city']}") String city) {

    Criteria criteria = Criteria.where("status").is(status);
    if (city != null && !city.isBlank()) {
        criteria = criteria.and("address.city").is(city);
    }

    Query query = new Query(criteria);
    query.with(Sort.by(Sort.Direction.ASC, "id"));

    return new MongoItemReaderBuilder<Customer>()
        .name("dynamicMongoReader")
        .template(mongoTemplate)
        .query(query)
        .targetType(Customer.class)
        .build();
}
```

## 5.9 REST ItemReader

```java
@Bean
@StepScope
public RestItemReader<Customer> restReader(
        RestTemplate restTemplate,
        @Value("#{jobParameters['api.url']}") String apiUrl) {

    return new RestItemReaderBuilder<Customer>()
        .name("restReader")
        .restTemplate(restTemplate)
        .uri(apiUrl)
        .httpMethod(HttpMethod.GET)
        .responseType(Customer[].class)
        .build();
}
```

### Paginated REST API
```java
@Component
@StepScope
public class PaginatedRestItemReader<T> implements ItemReader<T> {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final int pageSize;
    private final Class<T[]> responseType;
    private int currentPage = 0;
    private T[] currentBatch;
    private int currentIndex = 0;

    public PaginatedRestItemReader(RestTemplate restTemplate,
                                   String apiUrl,
                                   int pageSize,
                                   Class<T[]> responseType) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.pageSize = pageSize;
        this.responseType = responseType;
    }

    @Override
    public T read() {
        if (currentBatch == null || currentIndex >= currentBatch.length) {
            currentBatch = fetchNextPage();
            currentIndex = 0;
            if (currentBatch == null || currentBatch.length == 0) {
                return null;
            }
        }
        return currentBatch[currentIndex++];
    }

    private T[] fetchNextPage() {
        String url = String.format("%s?page=%d&size=%d",
            apiUrl, currentPage++, pageSize);
        ResponseEntity<T[]> response = restTemplate.exchange(
            url, HttpMethod.GET, null, responseType);
        return response.getBody();
    }
}
```

## 5.10 Custom ItemReader

### Simple Custom Reader
```java
public class GeneratorItemReader implements ItemReader<Integer> {

    private int current = 0;
    private final int maxValue;

    public GeneratorItemReader(int maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public Integer read() {
        if (current < maxValue) {
            return current++;
        }
        return null;  // Signal end of data
    }
}
```

### Database Reader with State
```java
public class StateTrackingReader implements ItemReader<Transaction> {

    private final JdbcTemplate jdbcTemplate;
    private long lastProcessedId = 0;
    private static final int BATCH_SIZE = 100;

    public StateTrackingReader(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction read() {
        List<Transaction> transactions = jdbcTemplate.query(
            "SELECT * FROM transactions WHERE id > ? ORDER BY id LIMIT ?",
            new BeanPropertyRowMapper<>(Transaction.class),
            lastProcessedId, BATCH_SIZE);

        if (transactions.isEmpty()) {
            return null;
        }

        Transaction transaction = transactions.get(0);
        lastProcessedId = transaction.getId();
        return transaction;
    }
}
```

### Kafka Reader
```java
public class KafkaItemReader implements ItemReader<String> {

    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private final Duration timeout;

    public KafkaItemReader(Map<String, Object> config,
                           String topic) {
        this.consumer = new KafkaConsumer<>(config);
        this.topic = topic;
        this.timeout = Duration.ofSeconds(1);
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public String read() {
        ConsumerRecords<String, String> records =
            consumer.poll(timeout);

        if (records.isEmpty()) {
            return null;
        }

        ConsumerRecord<String, String> record = records.iterator().next();
        return record.value();
    }
}
```

## 5.11 Reader Configuration Best Practices

### @StepScope for Dynamic Parameters
```java
@Bean
@StepScope
public ItemReader<Customer> dynamicReader(
        DataSource dataSource,
        @Value("#{jobParameters['status']}") String status) {

    return new JdbcCursorItemReaderBuilder<Customer>()
        .name("dynamicReader")
        .dataSource(dataSource)
        .sql("SELECT * FROM customers WHERE status = ?")
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .preparedStatementSetter(new ArgumentPreparedStatementSetter(
            new Object[]{status}))
        .build();
}
```

### Reader with Retry
```java
@Bean
public RetryableItemReader<Customer> retryableReader(
        DataSource dataSource) {

    ItemReader<Customer> delegate = new JdbcCursorItemReaderBuilder<Customer>()
        .name("retryableReader")
        .dataSource(dataSource)
        .sql("SELECT * FROM customers")
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .build();

    return new RetryableItemReaderBuilder<Customer>()
        .name("retryableReader")
        .delegate(delegate)
        .retryLimit(3)
        .retryableExceptions(ConnectException.class)
        .build();
}
```

### Reader with Skip
```java
@Bean
public SkipableItemReader<Customer> skipableReader(
        DataSource dataSource) {

    ItemReader<Customer> delegate = new JdbcCursorItemReaderBuilder<Customer>()
        .name("skipableReader")
        .dataSource(dataSource)
        .sql("SELECT * FROM customers")
        .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
        .build();

    return new SkipableItemReaderBuilder<Customer>()
        .name("skipableReader")
        .delegate(delegate)
        .skipLimit(10)
        .skip(SQLException.class)
        .build();
}
```

## 5.12 Practice Scenario

### Scenario: Multi-source Data Import
```java
@Configuration
public class MultiSourceImportJobConfig {

    @Bean
    public Job multiSourceImportJob(JobRepository jobRepository,
                                    Step csvImportStep,
                                    Step dbImportStep) {
        return new JobBuilder("multiSourceImportJob", jobRepository)
            .start(csvImportStep)
            .next(dbImportStep)
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> csvReader(
            @Value("#{jobParameters['csv.file']}") String filePath) {

        return new FlatFileItemReaderBuilder<Customer>()
            .name("csvReader")
            .resource(new FileSystemResource(filePath))
            .delimited()
            .names("id", "firstName", "lastName", "email")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {{
                setTargetType(Customer.class);
            }})
            .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Customer> dbReader(
            DataSource dataSource,
            @Value("#{jobParameters['status']}") String status) {

        return new JdbcCursorItemReaderBuilder<Customer>()
            .name("dbReader")
            .dataSource(dataSource)
            .sql("SELECT id, first_name, last_name, email FROM customers WHERE status = ?")
            .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
            .preparedStatementSetter(new ArgumentPreparedStatementSetter(
                new Object[]{status}))
            .build();
    }
}
```

## 5.13 Summary

| Reader | Use Case | Key Configuration |
|--------|----------|-------------------|
| FlatFileItemReader | CSV, fixed-width files | `resource`, `delimited()`, `names()` |
| StaxEventItemReader | XML files | `resource`, `addFragmentRootElements()` |
| JsonItemReader | JSON files | `resource`, `jsonObjectReader()` |
| JdbcCursorItemReader | Database cursor | `dataSource`, `sql`, `rowMapper` |
| JdbcPagingItemReader | Large databases | `dataSource`, `queryProvider`, `pageSize` |
| JpaItemReader | JPA entities | `entityManagerFactory`, `queryString` |
| MongoItemReader | MongoDB | `template`, `query`, `targetType` |
| RestItemReader | REST APIs | `restTemplate`, `uri`, `responseType` |
| Custom | Any data source | Implements `ItemReader<T>` |

## 5.14 Next Steps

- [Chapter 6: Item Writers](06-item-writer.md)
- Learn about database, file, and message writers
- Implement batch inserts and updates

## Exercises

### Exercise 1: CSV to Database
Create a job that:
1. Reads customer data from CSV file
2. Transforms (uppercase names)
3. Writes to database

### Exercise 2: Database Paging
Implement a paging reader:
1. Read from large table (1M+ rows)
2. Use proper sorting and pagination
3. Monitor memory usage

### Exercise 3: Custom Reader
Create a reader that:
1. Reads from multiple files
2. Combines data into single stream
3. Tracks processed files

---
*Duration: 2 hours*
