# Chapter 7: Item Processors

## Overview
Learn about ItemProcessor for data transformation, filtering, and validation.

## 7.1 Processor Overview

### What is ItemProcessor?
- Transforms items between reader and writer
- Filters unwanted items (return null)
- Validates data
- Enriches data

```
ItemReader ──▶ ItemProcessor ──▶ ItemWriter
             (Transform)
             (Filter)
             (Validate)
             (Enrich)
```

### Basic Processor
```java
@Bean
public ItemProcessor<Customer, Customer> basicProcessor() {
    return item -> {
        // Transform
        item.setFirstName(item.getFirstName().toUpperCase());
        item.setLastName(item.getLastName().toUpperCase());
        return item;
    };
}
```

## 7.2 Filtering Items

### Simple Filter
```java
@Bean
public ItemProcessor<Customer, Customer> filterProcessor() {
    return item -> {
        // Return null to filter out
        if ("INACTIVE".equals(item.getStatus())) {
            return null;
        }
        return item;
    };
}
```

### Multiple Filters
```java
@Bean
public ItemProcessor<Customer, Customer> compositeFilter() {
    List<ItemProcessor<Customer, Customer>> filters = Arrays.asList(
        item -> "ACTIVE".equals(item.getStatus()) ? item : null,
        item -> item.getEmail().contains("@company.com") ? item : null,
        item -> item.getAge() >= 18 ? item : null
    );

    return filters.stream()
        .reduce(ItemProcessor.identity(), (p1, p2) -> p1.andThen(p2));
}
```

## 7.3 Validation

### Bean Validation
```java
@Bean
public ItemProcessor<Customer, Customer> validatingProcessor() {
    return item -> {
        // Manual validation
        List<String> errors = new ArrayList<>();

        if (item.getFirstName() == null || item.getFirstName().isBlank()) {
            errors.add("First name is required");
        }
        if (item.getLastName() == null || item.getLastName().isBlank()) {
            errors.add("Last name is required");
        }
        if (item.getEmail() == null || !item.getEmail().contains("@")) {
            errors.add("Invalid email: " + item.getEmail());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation failed: " + String.join(", ", errors));
        }

        return item;
    };
}
```

### Using javax.validation
```java
@Bean
public ItemProcessor<Customer, Customer> beanValidationProcessor() {
    return item -> {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Customer>> violations =
            validator.validate(item);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }

        return item;
    };
}
```

### Custom Validator
```java
@Component
public class CustomerValidator implements Validator<Customer> {

    @Override
    public boolean supports(Class<?> clazz) {
        return Customer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Customer customer) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (customer.getEmail() != null &&
            !customer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.add("Invalid email format: " + customer.getEmail());
        }

        if (customer.getAge() != null && customer.getAge() < 0) {
            errors.add("Age cannot be negative: " + customer.getAge());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }
}

@Bean
public ItemProcessor<Customer, Customer> customValidationProcessor(
        CustomerValidator validator) {
    return item -> {
        validator.validate(item);
        return item;
    };
}
```

## 7.4 Data Transformation

### Field Transformation
```java
@Bean
public ItemProcessor<Customer, Customer> transformationProcessor() {
    return item -> {
        // Name transformation
        item.setFirstName(toProperCase(item.getFirstName()));
        item.setLastName(toProperCase(item.getLastName()));

        // Phone normalization
        if (item.getPhone() != null) {
            item.setPhone(normalizePhone(item.getPhone()));
        }

        // Date parsing
        if (item.getBirthDate() != null && item.getBirthDate() instanceof String) {
            try {
                LocalDate birthDate = LocalDate.parse(
                    (String) item.getBirthDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                item.setBirthDate(birthDate);
            } catch (DateTimeParseException e) {
                throw new RuntimeException(
                    "Invalid date format: " + item.getBirthDate());
            }
        }

        return item;
    };
}

private String toProperCase(String text) {
    if (text == null || text.isBlank()) return text;
    return text.substring(0, 1).toUpperCase() +
           text.substring(1).toLowerCase();
}

private String normalizePhone(String phone) {
    return phone.replaceAll("[^0-9]", "");
}
```

### Type Conversion
```java
@Bean
public ItemProcessor<Map<String, Object>, Customer> conversionProcessor() {
    return map -> {
        Customer customer = new Customer();

        customer.setId(Long.parseLong(map.get("id").toString()));
        customer.setFirstName((String) map.get("first_name"));
        customer.setLastName((String) map.get("last_name"));
        customer.setEmail((String) map.get("email"));
        customer.setStatus((String) map.get("status"));
        customer.setCreatedAt(LocalDateTime.parse(
            map.get("created_at").toString(),
            DateTimeFormatter.ISO_DATE_TIME));

        return customer;
    };
}
```

### Enrichment
```java
@Bean
public ItemProcessor<Customer, EnrichedCustomer> enrichmentProcessor(
        CategoryRepository categoryRepository) {

    return item -> {
        EnrichedCustomer enriched = new EnrichedCustomer(item);

        // Add category based on email domain
        String domain = item.getEmail().substring(
            item.getEmail().indexOf("@") + 1);
        Category category = categoryRepository.findByDomain(domain);
        enriched.setCategory(category);

        // Add full name
        enriched.setFullName(item.getFirstName() + " " + item.getLastName());

        // Add timestamp
        enriched.setProcessedAt(LocalDateTime.now());

        return enriched;
    };
}
```

## 7.5 CompositeItemProcessor

### Multiple Processors
```java
@Bean
public CompositeItemProcessor<Customer, Customer> compositeProcessor(
        ItemProcessor<Customer, Customer> validator,
        ItemProcessor<Customer, Customer> transformer,
        ItemProcessor<Customer, Customer> enricher) {

    CompositeItemProcessor<Customer, Customer> processor =
        new CompositeItemProcessor<>();

    processor.setDelegates(Arrays.asList(
        validator,
        transformer,
        enricher
    ));

    return processor;
}
```

### Builder Pattern
```java
@Bean
public ItemProcessor<Customer, Customer> chainedProcessor() {
    return item -> item
        .transform(this::validateBasicInfo)
        .transform(this::normalizeData)
        .transform(this::enrichData);
}
```

## 7.6 Filtering with Listseners

```java
@Component
public class FilteringProcessor implements ItemProcessor<String, String> {

    private final MeterRegistry meterRegistry;

    @Override
    public String process(String item) throws Exception {
        if (shouldFilter(item)) {
            meterRegistry.counter("items.filtered").increment();
            return null;
        }
        return item;
    }

    private boolean shouldFilter(String item) {
        // Filtering logic
        return item.startsWith("X");
    }
}
```

## 7.7 Processor with Stateful

### Stateful Processing
```java
@Component
@StepScope
public class DeduplicationProcessor implements ItemProcessor<Customer, Customer> {

    private final Set<Long> processedIds = ConcurrentHashMap.newKeySet();

    @Override
    public Customer process(Customer item) throws Exception {
        if (processedIds.contains(item.getId())) {
            return null;  // Filter duplicate
        }
        processedIds.add(item.getId());
        return item;
    }
}
```

### Aggregation Processor
```java
@Component
@StepScope
public class AggregationProcessor implements ItemProcessor<Transaction, Summary> {

    private Map<String, Summary> summaries = new ConcurrentHashMap<>();

    @Override
    public Summary process(Transaction item) throws Exception {
        // Accumulate data
        summaries.compute(item.getCategory(), (k, existing) -> {
            if (existing == null) {
                return new Summary(item.getCategory(), item.getAmount(), 1);
            }
            existing.addAmount(item.getAmount());
            existing.incrementCount();
            return existing;
        });

        // Return null - we're aggregating, not outputting directly
        return null;
    }

    public Map<String, Summary> getSummaries() {
        return summaries;
    }
}
```

## 7.8 Processor with Error Handling

```java
@Bean
public ItemProcessor<String, String> errorHandlingProcessor() {
    return item -> {
        try {
            return processItem(item);
        } catch (DataProcessingException e) {
            // Log error or send to error channel
            System.err.println("Failed to process: " + item);
            return null;
        }
    };
}
```

### Using Retry
```java
@Bean
public ItemProcessor<Customer, Customer> retryableProcessor() {
    ItemProcessor<Customer, Customer> delegate = item -> {
        // Processing that might fail
        return externalService.process(item);
    };

    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));

    return new RetryItemProcessorBuilder<Customer>()
        .name("retryableProcessor")
        .processor(delegate)
        .retryTemplate(retryTemplate)
        .build();
}
```

## 7.9 Processor Examples

### Data Cleansing Processor
```java
@Component
public class DataCleansingProcessor implements ItemProcessor<RawCustomer, CleanCustomer> {

    @Override
    public CleanCustomer process(RawCustomer raw) throws Exception {
        CleanCustomer clean = new CleanCustomer();

        // Trim whitespace
        clean.setFirstName(trim(raw.getFirstName()));
        clean.setLastName(trim(raw.getLastName()));
        clean.setEmail(trim(raw.getEmail()).toLowerCase());

        // Normalize phone
        clean.setPhone(normalizePhone(raw.getPhone()));

        // Parse and validate date
        clean.setBirthDate(parseDate(raw.getBirthDate()));

        // Validate email format
        if (!isValidEmail(clean.getEmail())) {
            throw new ValidationException("Invalid email: " + clean.getEmail());
        }

        // Generate unique ID
        clean.setId(generateId(clean));

        // Set processing timestamp
        clean.setProcessedAt(LocalDateTime.now());

        return clean;
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9+]", "");
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date: " + dateStr);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private Long generateId(CleanCustomer customer) {
        return (customer.getFirstName().hashCode() ^
                customer.getLastName().hashCode()) & 0xFFFFFFFFL;
    }
}
```

### Business Logic Processor
```java
@Component
public class OrderProcessingProcessor implements ItemProcessor<Order, Order> {

    private final PricingService pricingService;
    private final InventoryService inventoryService;

    @Override
    public Order process(Order order) throws Exception {
        // Validate order
        validateOrder(order);

        // Apply pricing rules
        applyPricingRules(order);

        // Check inventory
        checkInventory(order);

        // Calculate totals
        calculateTotals(order);

        // Set processing metadata
        order.setProcessedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PROCESSED);

        return order;
    }

    private void validateOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException("Order has no items");
        }
        if (order.getCustomerId() == null) {
            throw new ValidationException("Customer ID is required");
        }
    }

    private void applyPricingRules(Order order) {
        for (OrderItem item : order.getItems()) {
            BigDecimal price = pricingService.getPrice(item.getProductId());
            item.setUnitPrice(price);

            // Apply discounts
            if (order.isPremiumCustomer()) {
                item.setDiscount(price.multiply(BigDecimal.valueOf(0.1)));
            }
        }
    }

    private void checkInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            int available = inventoryService.getStock(item.getProductId());
            if (item.getQuantity() > available) {
                throw new InsufficientInventoryException(
                    "Insufficient stock for product: " + item.getProductId());
            }
        }
    }

    private void calculateTotals(Order order) {
        BigDecimal subtotal = order.getItems().stream()
            .map(item -> item.getUnitPrice()
                .subtract(item.getDiscount())
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08));
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0
            ? BigDecimal.ZERO : BigDecimal.valueOf(10);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShipping(shipping);
        order.setTotal(subtotal.add(tax).add(shipping));
    }
}
```

## 7.10 Processor Performance

### Parallel Processing
```java
@Bean
public ItemProcessor<Customer, Customer> parallelProcessor(
        ItemProcessor<Customer, Customer> delegate,
        @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {

    return new ThreadSafeItemProcessorWrapper<>(
        new AsyncItemProcessor<>(delegate, taskExecutor));
}
```

### Caching
```java
@Bean
public ItemProcessor<Product, Product> cachingProcessor(
        ProductService productService) {

    return item -> {
        Product cached = productService.getCached(item.getId());
        if (cached != null) {
            return cached;
        }
        return productService.process(item);
    };
}
```

## 7.11 Practice Scenario

### Scenario: Order Processing Pipeline
```java
@Configuration
public class OrderProcessingJobConfig {

    @Bean
    public Job orderProcessingJob(JobRepository jobRepository,
                                  Step processingStep) {
        return new JobBuilder("orderProcessingJob", jobRepository)
            .start(processingStep)
            .build();
    }

    @Bean
    public Step processingStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               ItemReader<Order> reader,
                               ItemProcessor<Order, Order> processor,
                               ItemWriter<Order> writer) {

        return new StepBuilder("processingStep", jobRepository)
            .<Order, Order>chunk(50, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(new OrderProcessingListener())
            .build();
    }

    @Bean
    public ItemProcessor<Order, Order> orderProcessor(
            OrderValidator validator,
            OrderEnricher enricher,
            OrderTransformer transformer) {

        CompositeItemProcessor<Order, Order> processor =
            new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(
            validator,
            enricher,
            transformer
        ));

        return processor;
    }
}

@Component
class OrderValidator implements ItemProcessor<Order, Order> {
    @Override
    public Order process(Order order) throws Exception {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException("Order has no items");
        }
        if (order.getCustomerId() == null) {
            throw new ValidationException("Customer ID is required");
        }
        return order;
    }
}

@Component
class OrderEnricher implements ItemProcessor<Order, Order> {
    @Autowired
    private CustomerService customerService;

    @Override
    public Order process(Order order) throws Exception {
        Customer customer = customerService.getCustomer(order.getCustomerId());
        order.setCustomer(customer);
        order.setPriority(customer.getTier().getPriority());
        return order;
    }
}

@Component
class OrderTransformer implements ItemProcessor<Order, Order> {
    @Override
    public Order process(Order order) throws Exception {
        // Apply business rules
        if (order.getPriority() > 5) {
            order.setRushProcessing(true);
        }
        // Calculate totals
        order.calculateTotals();
        return order;
    }
}
```

## 7.12 Summary

| Processor Type | Purpose | Example |
|----------------|---------|---------|
| Filtering | Remove unwanted items | `return null` to filter |
| Validation | Check data integrity | Bean Validation, custom |
| Transformation | Convert data format | Date parsing, type cast |
| Enrichment | Add derived data | Lookup, aggregation |
| Composition | Chain processors | `CompositeItemProcessor` |

## 7.13 Next Steps

- [Chapter 8: Listeners](08-listeners.md)
- Learn about Job, Step, and Chunk listeners
- Implement custom listeners

## Exercises

### Exercise 1: Data Validation
Create a processor that:
1. Validates customer email format
2. Normalizes phone numbers
3. Calculates age from birth date
4. Throws ValidationException for invalid data

### Exercise 2: Deduplication
Create a processor that:
1. Tracks processed IDs
2. Filters duplicate records
3. Logs statistics

### Exercise 3: Enrichment
Create a processor that:
1. Looks up related data from database
2. Enriches items with related data
3. Uses caching for lookups

---
*Duration: 1 hour*
