package com.interview.master.springboot.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ============================================================
 * APACHE KAFKA COMPLETE INTERVIEW GUIDE
 * ============================================================
 *
 * Q: What is Apache Kafka?
 * Distributed event streaming platform (originally by LinkedIn, now Apache).
 * Used for: high-throughput messaging, event sourcing, stream processing,
 *           data pipelines, microservices communication.
 *
 * WHY KAFKA? Problems it solves:
 * Traditional messaging (RabbitMQ, JMS): point-to-point, messages deleted after consumption.
 * Kafka: distributed log, messages retained for configurable period, multiple consumers,
 *        replay ability, very high throughput (millions of msg/sec).
 *
 * ─────────────────────────────────────────────────────────────
 * KAFKA ARCHITECTURE (Core Components):
 * ─────────────────────────────────────────────────────────────
 *
 * TOPIC
 * - Named category/feed for messages (like a database table)
 * - Messages are appended to the log (immutable, sequential writes = very fast)
 * - Retention: time-based (default 7 days) or size-based
 *
 * PARTITION
 * - Topic is split into N partitions for parallelism
 * - Each partition is an ordered, immutable sequence
 * - Partition assignment: key-based (same key → same partition) or round-robin
 * - More partitions = higher throughput but more overhead
 *
 * OFFSET
 * - Unique sequential ID for each message within a partition
 * - Consumer tracks its position via offset
 * - Offset is PER consumer group PER partition
 *
 * BROKER
 * - Kafka server that stores topic partitions
 * - Cluster: multiple brokers
 * - Controller broker: elected leader for admin operations
 *
 * REPLICATION
 * - Each partition has 1 leader + N-1 followers (replicas)
 * - All reads/writes go to leader
 * - Followers replicate from leader (ISR: In-Sync Replicas)
 * - replication.factor=3 → 1 leader + 2 followers
 * - min.insync.replicas=2 → at least 2 replicas must acknowledge write
 *
 * ZOOKEEPER (legacy) / KRAFT (Kafka 3.x+)
 * - ZooKeeper: external coordinator (broker discovery, leader election, config)
 * - KRaft (Kafka Raft): built-in consensus, removes ZooKeeper dependency (Kafka 3.3+)
 *
 * PRODUCER
 * - Sends records to topic
 * - acks=0  : fire and forget (fastest, no guarantee)
 * - acks=1  : wait for leader ack (moderate - can lose if leader crashes before replication)
 * - acks=all: wait for all ISR acks (slowest, strongest guarantee)
 * - Batching: linger.ms, batch.size (accumulate records before send)
 * - Compression: snappy, gzip, lz4, zstd (reduces network I/O)
 * - Idempotent producer: enable.idempotence=true (exactly-once within session)
 *
 * CONSUMER
 * - Subscribes to topics, reads records
 * - Consumer Group: multiple consumers sharing a topic's partitions
 *   - Each partition consumed by exactly one consumer in a group
 *   - Multiple groups can consume same topic independently
 * - Rebalancing: triggered when consumer joins/leaves group
 * - auto.offset.reset: latest (skip old), earliest (read from beginning)
 * - Commit: auto commit (may reprocess or lose) vs manual commit
 *
 * DELIVERY GUARANTEES:
 * At Most Once  - messages may be lost, never duplicated (commit before processing)
 * At Least Once - messages may be duplicated, never lost (commit after processing, DEFAULT)
 * Exactly Once  - no loss, no duplication (transactions + idempotent consumer - hardest)
 *
 * ─────────────────────────────────────────────────────────────
 * KAFKA vs RabbitMQ:
 * ─────────────────────────────────────────────────────────────
 * Kafka:
 * - Pull-based (consumer pulls messages)
 * - Persistent log (message retained even after consumption)
 * - Replay supported
 * - Very high throughput
 * - Best for: event streaming, analytics, log aggregation
 *
 * RabbitMQ:
 * - Push-based (broker pushes to consumer)
 * - Message deleted after consumption
 * - Complex routing (exchanges: direct, topic, fanout, headers)
 * - Lower latency for simple messaging
 * - Best for: task queues, work distribution, simple pub/sub
 *
 * ┌─────────────────────┬────────────────────┬────────────────────────┐
 * │ Feature             │ Kafka              │ RabbitMQ               │
 * ├─────────────────────┼────────────────────┼────────────────────────┤
 * │ Model               │ Pull (consumer)    │ Push (broker)          │
 * │ Persistence         │ Log (configurable) │ Deleted after consume  │
 * │ Replay              │ Yes                │ No                     │
 * │ Throughput          │ Very High          │ High                   │
 * │ Ordering            │ Per partition      │ Per queue              │
 * │ Consumer Groups     │ Yes                │ Competing consumers    │
 * │ Routing             │ Topic/Partition    │ Exchange types         │
 * └─────────────────────┴────────────────────┴────────────────────────┘
 *
 * ─────────────────────────────────────────────────────────────
 * KAFKA STREAMS:
 * ─────────────────────────────────────────────────────────────
 * Client library for stream processing within Kafka.
 * - Stateless ops: filter, map, flatMap
 * - Stateful ops: count, reduce, aggregate, join, windowing
 * - KStream: unbounded stream of records
 * - KTable: changelog stream (like a table - latest value per key)
 * - GlobalKTable: replicated across all instances
 *
 * ─────────────────────────────────────────────────────────────
 * SPRING KAFKA:
 * ─────────────────────────────────────────────────────────────
 * @EnableKafka                    - enables Kafka listener infrastructure
 * @KafkaListener(topics, groupId) - consume messages
 * KafkaTemplate<K,V>              - send messages
 * ProducerFactory, ConsumerFactory - create producers/consumers
 * ConcurrentKafkaListenerContainerFactory - configure listener container
 *
 * ─────────────────────────────────────────────────────────────
 * KAFKA COMMON INTERVIEW QUESTIONS:
 * ─────────────────────────────────────────────────────────────
 * Q: How to ensure message ordering in Kafka?
 *    A: Use same partition key → all messages with same key go to same partition
 *       (partitions are ordered). Global ordering across partitions is not guaranteed.
 *
 * Q: What happens when consumer is slower than producer?
 *    A: Consumer lag builds up. Monitor with kafka-consumer-groups.sh.
 *       Solution: more partitions + more consumers, or scale consumer instances.
 *
 * Q: What is a dead letter topic (DLT)?
 *    A: Topic where failed messages are sent after max retry attempts.
 *       Allows reprocessing without blocking main topic.
 *
 * Q: How to handle poison pill messages?
 *    A: Messages that always fail. Use DLT + error handler. Log and skip.
 *
 * Q: What is Log Compaction?
 *    A: Kafka retains only latest value per key (like a KTable snapshot).
 *       Used for event sourcing state stores. cleanup.policy=compact
 *
 * Q: Kafka transaction support?
 *    A: transactional.id on producer enables atomic writes across partitions.
 *       Consumer sets isolation.level=read_committed to see only committed msgs.
 *
 * Q: What is __consumer_offsets?
 *    A: Internal Kafka topic that stores consumer group offset commits.
 *       Replaced ZooKeeper-based offset storage in older Kafka versions.
 */

// =========================================================
// TOPIC CONSTANTS
// =========================================================

class KafkaTopics {
    public static final String EMPLOYEE_CREATED      = "employee-created";
    public static final String EMPLOYEE_UPDATED      = "employee-updated";
    public static final String EMPLOYEE_DELETED      = "employee-deleted";
    public static final String EMPLOYEE_EVENTS       = "employee-events";     // all events on one topic
    public static final String EMPLOYEE_CREATED_DLT  = "employee-created.DLT"; // dead letter topic
    public static final String NOTIFICATION_EVENTS   = "notification-events";

    private KafkaTopics() {} // utility class - no instantiation
}

// =========================================================
// 1. EMPLOYEE CREATED EVENT - Kafka Message Payload (POJO)
// =========================================================

/**
 * KAFKA MESSAGE PAYLOAD:
 * - Must be serializable/deserializable (Jackson JSON, Avro, Protobuf)
 * - Keep events immutable where possible
 * - Include: eventId (UUID), timestamp, eventType, payload
 * - Version events for backward/forward compatibility
 *
 * Interview Q: What serializer/deserializer to use?
 * StringSerializer/StringDeserializer: simple strings
 * JsonSerializer/JsonDeserializer (spring-kafka): Java objects as JSON
 * Avro + Schema Registry: schema evolution, compact binary (best for production)
 */
class EmployeeCreatedEvent implements Serializable {

    // Event metadata
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String version;       // event schema version for compatibility

    // Business payload
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private String role;
    private java.math.BigDecimal salary;

    // For producer tracking - which service sent this
    private String sourceService;

    public EmployeeCreatedEvent() {}

    public EmployeeCreatedEvent(Long employeeId, String firstName, String lastName,
                                String email, Long departmentId, String role) {
        this.eventId       = java.util.UUID.randomUUID().toString();
        this.eventType     = "EMPLOYEE_CREATED";
        this.timestamp     = LocalDateTime.now();
        this.version       = "1.0";
        this.employeeId    = employeeId;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.email         = email;
        this.departmentId  = departmentId;
        this.role          = role;
        this.sourceService = "employee-service";
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public java.math.BigDecimal getSalary() { return salary; }
    public void setSalary(java.math.BigDecimal salary) { this.salary = salary; }

    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }

    @Override
    public String toString() {
        return "EmployeeCreatedEvent{" +
               "eventId='" + eventId + "'" +
               ", employeeId=" + employeeId +
               ", firstName='" + firstName + "'" +
               ", email='" + email + "'" +
               ", timestamp=" + timestamp +
               "}";
    }
}

/**
 * Employee Updated Event - separate event type for update operations
 */
class EmployeeUpdatedEvent implements Serializable {
    private String eventId;
    private String eventType = "EMPLOYEE_UPDATED";
    private LocalDateTime timestamp;
    private Long employeeId;
    private Map<String, Object> changedFields; // field -> new value
    private String updatedBy;

    public EmployeeUpdatedEvent() {}

    public EmployeeUpdatedEvent(Long employeeId, Map<String, Object> changedFields, String updatedBy) {
        this.eventId       = java.util.UUID.randomUUID().toString();
        this.timestamp     = LocalDateTime.now();
        this.employeeId    = employeeId;
        this.changedFields = changedFields;
        this.updatedBy     = updatedBy;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Map<String, Object> getChangedFields() { return changedFields; }
    public void setChangedFields(Map<String, Object> changedFields) { this.changedFields = changedFields; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}

// =========================================================
// 2. KAFKA PRODUCER CONFIGURATION
// =========================================================

/**
 * PRODUCER CONFIGURATION:
 * ─────────────────────────────────────────────────────────────
 * ProducerFactory: creates Kafka Producer instances (thread-safe, shared)
 * KafkaTemplate: high-level API to send messages (wraps ProducerFactory)
 *
 * Key producer configs:
 * bootstrap.servers       - Kafka broker addresses
 * key.serializer          - How to serialize message keys
 * value.serializer        - How to serialize message values
 * acks                    - Acknowledgment level (0, 1, all)
 * retries                 - Number of retry attempts on failure
 * linger.ms               - Wait time to batch messages (default 0 = no batching)
 * batch.size              - Max bytes in a batch (default 16KB)
 * buffer.memory           - Total memory for buffering (default 32MB)
 * enable.idempotence      - Ensure exactly-once delivery within producer session
 * compression.type        - none, gzip, snappy, lz4, zstd
 */
@Configuration
class KafkaProducerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    /**
     * ProducerFactory for String key + String value.
     * Use this for simple string messages.
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");          // wait for all ISR acks
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);           // retry up to 3 times
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // exactly-once per session

        // Performance settings
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);         // wait 5ms to batch
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);    // 16KB batch
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // compress batches

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for String key + String value.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    /**
     * ProducerFactory for String key + Object (JSON-serialized) value.
     * JsonSerializer converts Java objects to JSON automatically.
     */
    @Bean
    public ProducerFactory<String, Object> jsonProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // JsonSerializer: add type info header so consumer can deserialize correctly
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate for JSON object messages.
     * Use this for sending POJOs like EmployeeCreatedEvent.
     */
    @Bean(name = "jsonKafkaTemplate")
    public KafkaTemplate<String, Object> jsonKafkaTemplate() {
        return new KafkaTemplate<>(jsonProducerFactory());
    }

    /**
     * Strongly typed ProducerFactory for EmployeeCreatedEvent specifically.
     * Best practice: one factory per event type for strict typing.
     */
    @Bean
    public ProducerFactory<String, EmployeeCreatedEvent> employeeEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "employeeKafkaTemplate")
    public KafkaTemplate<String, EmployeeCreatedEvent> employeeKafkaTemplate() {
        return new KafkaTemplate<>(employeeEventProducerFactory());
    }
}

// =========================================================
// 3. KAFKA CONSUMER CONFIGURATION
// =========================================================

/**
 * CONSUMER CONFIGURATION:
 * ─────────────────────────────────────────────────────────────
 * ConsumerFactory: creates Kafka Consumer instances
 * ConcurrentKafkaListenerContainerFactory: creates listener containers
 *   - concurrency = number of threads (max = number of partitions)
 *
 * Key consumer configs:
 * bootstrap.servers       - Kafka broker addresses
 * group.id                - Consumer group identifier
 * key.deserializer        - How to deserialize message keys
 * value.deserializer      - How to deserialize message values
 * auto.offset.reset       - latest (default, skip history) or earliest (read all)
 * enable.auto.commit      - true: auto commit offsets; false: manual commit
 * auto.commit.interval.ms - How often to auto-commit (if enabled)
 * max.poll.records        - Max records returned per poll()
 * session.timeout.ms      - Consumer considered dead if no heartbeat within this time
 * heartbeat.interval.ms   - How often consumer sends heartbeat to broker
 */
@EnableKafka
@Configuration
class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID          = "employee-service-group";

    // -------------------------------------------------------
    // ConsumerFactory for String key + String value
    // -------------------------------------------------------
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Manual commit (we control when offset is committed)
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Performance tuning
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * ConcurrentKafkaListenerContainerFactory for String messages.
     * concurrency(3) means 3 threads, each consuming from different partitions.
     * Max concurrency = number of partitions in the topic.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setConcurrency(3);   // 3 consumer threads per listener

        // MANUAL_IMMEDIATE: commit offset immediately when Acknowledgment.acknowledge() is called
        // MANUAL: commit on next poll after acknowledge() (slightly more efficient)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    // -------------------------------------------------------
    // ConsumerFactory for String key + EmployeeCreatedEvent value (JSON)
    // -------------------------------------------------------
    @Bean
    public ConsumerFactory<String, EmployeeCreatedEvent> employeeEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // JsonDeserializer configuration:
        // Trust all packages (dev only) or specific package for security
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.interview.master.springboot.kafka");
        // Use the exact type (don't rely on type header)
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EmployeeCreatedEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new JsonDeserializer<>(EmployeeCreatedEvent.class)
        );
    }

    @Bean(name = "employeeEventListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeCreatedEvent>
            employeeEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmployeeCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(employeeEventConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // -------------------------------------------------------
    // Dead Letter Topic (DLT) Error Handler
    // -------------------------------------------------------

    /**
     * DefaultErrorHandler with Dead Letter Publishing Recoverer.
     * After maxAttempts retries, failed message is sent to the DLT topic.
     * DLT topic name convention: <original-topic>.DLT
     *
     * FixedBackOff(interval, maxAttempts):
     *   interval=1000ms, maxAttempts=3 → retry 3 times with 1s delay between each.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Recoverer: send failed message to DLT after all retries exhausted
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Retry 3 times with 1 second interval, then send to DLT
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Don't retry for these exceptions (immediately send to DLT)
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,     // bad data - no point retrying
                ClassCastException.class,
                com.fasterxml.jackson.core.JsonParseException.class  // deserialization failure
        );

        return errorHandler;
    }

    /**
     * Listener container factory with DLT error handler configured.
     */
    @Bean(name = "dltAwareListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
            dltAwareKafkaListenerContainerFactory(
                    DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}

// =========================================================
// 4. EMPLOYEE EVENT PRODUCER SERVICE
// =========================================================

/**
 * PRODUCER SERVICE:
 * ─────────────────────────────────────────────────────────────
 * KafkaTemplate.send() returns CompletableFuture<SendResult<K,V>>
 * - Non-blocking by default (fire and forget if you don't wait)
 * - Add callback via .whenComplete() for async success/failure handling
 * - .get() makes it blocking (wait for broker ack)
 *
 * Partition selection:
 * - With key: hash(key) % numPartitions (deterministic)
 * - Without key: round-robin across partitions
 * - Explicit partition: kafkaTemplate.send(topic, partition, key, value)
 *
 * Interview Q: How to guarantee message ordering?
 * Send related messages with same key → same partition → ordered.
 */
@Service
class EmployeeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventProducer.class);

    private final KafkaTemplate<String, EmployeeCreatedEvent> employeeKafkaTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeEventProducer(
            KafkaTemplate<String, EmployeeCreatedEvent> employeeKafkaTemplate,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.employeeKafkaTemplate = employeeKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------
    // Basic send - fire and forget (no callback)
    // -------------------------------------------------------

    /**
     * Simple fire-and-forget send.
     * Message is sent asynchronously. No guarantee it was received.
     * Use for non-critical events where loss is acceptable.
     */
    public void sendEmployeeCreatedEvent(EmployeeCreatedEvent event) {
        // Key = employeeId (ensures all events for same employee go to same partition)
        String key = String.valueOf(event.getEmployeeId());
        employeeKafkaTemplate.send(KafkaTopics.EMPLOYEE_CREATED, key, event);
        log.info("Sent EmployeeCreatedEvent for employeeId={}", event.getEmployeeId());
    }

    // -------------------------------------------------------
    // Send with CompletableFuture callback (async, non-blocking)
    // -------------------------------------------------------

    /**
     * Send with async callback using CompletableFuture.
     * whenComplete: called when send completes (success or failure).
     * Does NOT block the calling thread.
     *
     * Interview Q: What is the difference between thenApply and whenComplete?
     * thenApply: transform result on success only (does not execute on failure)
     * whenComplete: always called (success AND failure), does not transform result
     */
    public void sendEmployeeCreatedEventWithCallback(EmployeeCreatedEvent event) {
        String key = String.valueOf(event.getEmployeeId());

        CompletableFuture<SendResult<String, EmployeeCreatedEvent>> future =
                employeeKafkaTemplate.send(KafkaTopics.EMPLOYEE_CREATED, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // Success: message was acknowledged by broker
                log.info("Successfully sent EmployeeCreatedEvent: employeeId={}, " +
                         "topic={}, partition={}, offset={}",
                        event.getEmployeeId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                // Failure: broker did not acknowledge (network issue, leader election, etc.)
                log.error("Failed to send EmployeeCreatedEvent: employeeId={}, error={}",
                        event.getEmployeeId(), ex.getMessage(), ex);
                // In production: publish to internal failure queue, alert, retry manually
            }
        });
    }

    // -------------------------------------------------------
    // Blocking send - wait for broker acknowledgment
    // -------------------------------------------------------

    /**
     * Blocking send - waits until broker acknowledges.
     * Use when you need confirmation before proceeding (e.g., order processing).
     * WARNING: blocks calling thread. Avoid in high-throughput code.
     */
    public SendResult<String, EmployeeCreatedEvent> sendEmployeeCreatedEventSync(
            EmployeeCreatedEvent event) throws Exception {
        String key = String.valueOf(event.getEmployeeId());
        // .get() blocks until the future completes
        SendResult<String, EmployeeCreatedEvent> result =
                employeeKafkaTemplate.send(KafkaTopics.EMPLOYEE_CREATED, key, event).get();
        log.info("Sync send succeeded: partition={}, offset={}",
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
        return result;
    }

    // -------------------------------------------------------
    // Send with Kafka Headers
    // -------------------------------------------------------

    /**
     * Send message with custom Kafka headers.
     * Headers are metadata attached to the message (not part of the payload).
     * Use for: correlation IDs, event type, schema version, source service, auth tokens.
     *
     * Interview Q: What are Kafka record headers?
     * Key-value metadata attached to each Kafka record.
     * Consumers can inspect headers without deserializing the full payload.
     */
    public void sendEmployeeCreatedEventWithHeaders(EmployeeCreatedEvent event,
                                                     String correlationId) {
        String key = String.valueOf(event.getEmployeeId());

        org.springframework.messaging.Message<EmployeeCreatedEvent> message =
                org.springframework.messaging.support.MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, KafkaTopics.EMPLOYEE_CREATED)
                        .setHeader(KafkaHeaders.KEY, key)
                        // Custom headers
                        .setHeader("X-Correlation-ID", correlationId)
                        .setHeader("X-Event-Type", "EMPLOYEE_CREATED")
                        .setHeader("X-Event-Version", "1.0")
                        .setHeader("X-Source-Service", "employee-service")
                        .setHeader("X-Timestamp", LocalDateTime.now().toString())
                        .build();

        employeeKafkaTemplate.send(message);
        log.info("Sent EmployeeCreatedEvent with headers: correlationId={}", correlationId);
    }

    // -------------------------------------------------------
    // Send to specific partition
    // -------------------------------------------------------

    /**
     * Send to a specific partition explicitly.
     * Usually NOT needed - use key-based partitioning instead.
     * Useful when you need to co-locate related data on same partition.
     */
    public void sendToSpecificPartition(EmployeeCreatedEvent event, int partition) {
        String key = String.valueOf(event.getEmployeeId());
        // send(topic, partition, key, value)
        employeeKafkaTemplate.send(KafkaTopics.EMPLOYEE_CREATED, partition, key, event);
        log.info("Sent to partition={} for employeeId={}", partition, event.getEmployeeId());
    }

    // -------------------------------------------------------
    // Send JSON string (manual serialization)
    // -------------------------------------------------------

    /**
     * Manual JSON serialization and send as String.
     * Less elegant but gives full control over the JSON format.
     */
    public void sendEmployeeEventAsJsonString(EmployeeCreatedEvent event) {
        try {
            String key = String.valueOf(event.getEmployeeId());
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.EMPLOYEE_CREATED, key, jsonPayload);
            log.info("Sent JSON string event for employeeId={}", event.getEmployeeId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize EmployeeCreatedEvent: {}", e.getMessage());
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    // -------------------------------------------------------
    // Batch send
    // -------------------------------------------------------

    /**
     * Send multiple events - each is sent independently.
     * For true transactional batch, use executeInTransaction().
     */
    public void sendBatch(List<EmployeeCreatedEvent> events) {
        events.forEach(event -> sendEmployeeCreatedEventWithCallback(event));
        log.info("Queued {} events for async sending", events.size());
    }

    /**
     * Transactional send - all messages committed atomically.
     * Requires: transactional.id set in producer config.
     * Consumer must set isolation.level=read_committed.
     */
    public void sendTransactionally(List<EmployeeCreatedEvent> events) {
        employeeKafkaTemplate.executeInTransaction(operations -> {
            events.forEach(event -> {
                String key = String.valueOf(event.getEmployeeId());
                operations.send(KafkaTopics.EMPLOYEE_CREATED, key, event);
            });
            return true; // return value of Callable
        });
        log.info("Transactionally sent {} events", events.size());
    }
}

// =========================================================
// 5. EMPLOYEE EVENT CONSUMER SERVICE
// =========================================================

/**
 * CONSUMER SERVICE:
 * ─────────────────────────────────────────────────────────────
 * @KafkaListener marks a method as a message listener.
 * Spring creates a MessageListenerContainer that polls Kafka in a loop.
 *
 * Acknowledgment modes:
 * AUTO           - commit after listener returns without error (default)
 * MANUAL         - commit on next poll after acknowledge()
 * MANUAL_IMMEDIATE - commit immediately when acknowledge() is called
 * RECORD         - commit each record individually after processing
 * BATCH          - commit after batch of records processed
 * COUNT          - commit after N records
 * TIME           - commit every T milliseconds
 * COUNT_TIME     - commit after N records OR T milliseconds
 *
 * Interview Q: What is the difference between AUTO and MANUAL commit?
 * AUTO: Spring commits offset after method returns without throwing.
 *       Risk: if exception thrown after partial processing → reprocessing
 *       Risk: if exception thrown before processing → still committed in some modes
 * MANUAL: You explicitly call acknowledgment.acknowledge() after successful processing.
 *         Full control. Best for at-least-once with idempotent processing.
 */
@Component
class EmployeeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventConsumer.class);

    // -------------------------------------------------------
    // Basic @KafkaListener - auto-commit
    // -------------------------------------------------------

    /**
     * Simplest form - listens to single topic with auto-commit.
     * groupId here overrides the default group.id from application.properties.
     * Spring auto-deserializes the message payload from JSON.
     */
    @KafkaListener(
            topics      = KafkaTopics.EMPLOYEE_CREATED,
            groupId     = "employee-notification-group"
    )
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("Received EmployeeCreatedEvent: employeeId={}, name={} {}",
                event.getEmployeeId(), event.getFirstName(), event.getLastName());
        // Process the event (send notification, update read model, etc.)
        sendWelcomeEmail(event);
    }

    // -------------------------------------------------------
    // @KafkaListener with Acknowledgment (manual commit)
    // -------------------------------------------------------

    /**
     * Manual acknowledgment - commit offset only after successful processing.
     * Guarantees AT LEAST ONCE delivery (may reprocess on failure/restart).
     * containerFactory must be configured with AckMode.MANUAL or MANUAL_IMMEDIATE.
     *
     * Interview Q: When would you NOT acknowledge?
     * When processing fails and you want the message to be redelivered.
     * BUT: don't hold up the partition forever. Use retry/DLT instead.
     */
    @KafkaListener(
            topics           = KafkaTopics.EMPLOYEE_CREATED,
            groupId          = "employee-audit-group",
            containerFactory = "employeeEventListenerFactory"
    )
    public void handleEmployeeCreatedWithAck(
            @Payload EmployeeCreatedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.info("Consuming from topic={}, partition={}, offset={}: employeeId={}",
                topic, partition, offset, event.getEmployeeId());

        try {
            // Process message
            auditEmployeeCreation(event);

            // IMPORTANT: Only acknowledge AFTER successful processing
            // This ensures offset is not committed if processing fails
            acknowledgment.acknowledge();
            log.info("Acknowledged offset={} on partition={}", offset, partition);

        } catch (Exception ex) {
            log.error("Failed to process EmployeeCreatedEvent at offset={}: {}",
                    offset, ex.getMessage(), ex);
            // Do NOT acknowledge - message will be redelivered after session timeout
            // OR use nack() to specify a delay before redelivery (Spring Kafka 2.6+)
            acknowledgment.nack(java.time.Duration.ofSeconds(5)); // retry after 5s
        }
    }

    // -------------------------------------------------------
    // @KafkaListener with multiple topics
    // -------------------------------------------------------

    /**
     * Listen to multiple topics in a single listener.
     * The ConsumerRecord wrapper gives access to metadata.
     *
     * Interview Q: When to use ConsumerRecord vs @Payload?
     * @Payload: cleaner, just the message value
     * ConsumerRecord: full access to key, headers, partition, offset, timestamp
     */
    @KafkaListener(
            topics  = {KafkaTopics.EMPLOYEE_CREATED, KafkaTopics.EMPLOYEE_UPDATED},
            groupId = "employee-sync-group"
    )
    public void handleAllEmployeeEvents(ConsumerRecord<String, String> record) {
        log.info("Multi-topic listener: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(), record.partition(), record.offset());

        // Route based on topic
        switch (record.topic()) {
            case KafkaTopics.EMPLOYEE_CREATED:
                log.info("Processing creation for key={}", record.key());
                // handle created...
                break;
            case KafkaTopics.EMPLOYEE_UPDATED:
                log.info("Processing update for key={}", record.key());
                // handle updated...
                break;
            default:
                log.warn("Unhandled topic: {}", record.topic());
        }
    }

    // -------------------------------------------------------
    // Batch listener
    // -------------------------------------------------------

    /**
     * Batch consumer - receive a list of messages in one poll.
     * More efficient than one-at-a-time for high throughput.
     * Factory must have: factory.setBatchListener(true)
     */
    @KafkaListener(
            topics           = KafkaTopics.EMPLOYEE_EVENTS,
            groupId          = "employee-batch-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEmployeeEventsBatch(List<ConsumerRecord<String, String>> records) {
        log.info("Batch received: {} records", records.size());
        records.forEach(record -> {
            log.debug("Processing record: key={}, offset={}", record.key(), record.offset());
            // process each record
        });
    }

    // -------------------------------------------------------
    // @KafkaListener with custom headers
    // -------------------------------------------------------

    /**
     * Read custom headers from the Kafka message.
     * @Header binds specific header value to a method parameter.
     */
    @KafkaListener(
            topics  = KafkaTopics.EMPLOYEE_CREATED,
            groupId = "employee-tracing-group"
    )
    public void handleWithHeaders(
            @Payload EmployeeCreatedEvent event,
            @Header(value = "X-Correlation-ID", required = false) String correlationId,
            @Header(value = "X-Event-Version", required = false) String eventVersion,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        // Put correlationId in MDC for distributed tracing in logs
        if (correlationId != null) {
            org.slf4j.MDC.put("correlationId", correlationId);
        }

        log.info("Received v{} event: correlationId={}, employeeId={}, partition={}, offset={}",
                eventVersion, correlationId, event.getEmployeeId(), partition, offset);

        try {
            // process event
        } finally {
            org.slf4j.MDC.clear();
        }
    }

    // -------------------------------------------------------
    // Dead Letter Topic Consumer
    // -------------------------------------------------------

    /**
     * Consumes from the Dead Letter Topic.
     * Messages here failed all retry attempts on the main topic.
     * Options: inspect, fix, republish to main topic, alert ops team.
     *
     * DLT topic name: <original-topic>.DLT (Spring Kafka convention)
     */
    @KafkaListener(
            topics  = KafkaTopics.EMPLOYEE_CREATED_DLT,
            groupId = "employee-dlt-group"
    )
    public void handleDeadLetterMessages(
            ConsumerRecord<String, String> record,
            @Header(value = "kafka_dlt-exception-message", required = false) String exMessage,
            @Header(value = "kafka_dlt-original-topic", required = false) String originalTopic,
            @Header(value = "kafka_dlt-original-partition", required = false) Integer originalPartition,
            @Header(value = "kafka_dlt-original-offset", required = false) Long originalOffset) {

        log.error("DLT message received: originalTopic={}, partition={}, offset={}, " +
                  "failureReason={}, payload={}",
                originalTopic, originalPartition, originalOffset,
                exMessage, record.value());

        // In production: alert operations team, store in error database for manual review
        storeInErrorRepository(record, exMessage);
    }

    // -------------------------------------------------------
    // @RetryableTopic - automatic retry with separate retry topics
    // -------------------------------------------------------

    /**
     * @RetryableTopic: Spring Kafka non-blocking retry.
     * Creates retry topics: employee-created-retry-0, ...-retry-1, ...-retry-2
     * After all retries, sends to DLT automatically.
     *
     * Non-blocking: failed messages don't block the main topic partition.
     * They are moved to retry topics and retried with delay.
     *
     * Interview Q: Why non-blocking retry?
     * Blocking retry (retry in place) blocks the partition consumer.
     * Other messages in the partition cannot be processed during the retry delay.
     * Non-blocking retry moves the message to a retry topic - main topic continues.
     */
    @RetryableTopic(
            attempts        = "4",                        // 1 original + 3 retries
            backoff         = @Backoff(delay = 1000,
                                       multiplier = 2.0,
                                       maxDelay = 10000), // exponential: 1s, 2s, 4s
            autoCreateTopics = "true",
            dltTopicSuffix  = ".DLT"
    )
    @KafkaListener(
            topics  = KafkaTopics.NOTIFICATION_EVENTS,
            groupId = "notification-group"
    )
    public void handleNotificationEvent(String message) {
        log.info("Processing notification: {}", message);
        // If this throws, Spring Kafka retries automatically
        // After all retries: message goes to notification-events.DLT
        processNotification(message);
    }

    /**
     * @DltHandler: handles messages that reached the DLT from @RetryableTopic.
     * Must be in the same class as the @RetryableTopic listener.
     */
    @DltHandler
    public void handleDlt(String message,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("DLT handler: message={} from topic={}", message, topic);
        // alert/store in error DB
    }

    // -------------------------------------------------------
    // Helper methods (stubs)
    // -------------------------------------------------------

    private void sendWelcomeEmail(EmployeeCreatedEvent event) {
        log.info("Sending welcome email to: {}", event.getEmail());
    }

    private void auditEmployeeCreation(EmployeeCreatedEvent event) {
        log.info("Auditing employee creation: employeeId={}", event.getEmployeeId());
    }

    private void processNotification(String message) {
        log.info("Processing notification message: {}", message);
    }

    private void storeInErrorRepository(ConsumerRecord<String, String> record, String reason) {
        log.error("Storing failed record in error repository: key={}, reason={}", record.key(), reason);
    }
}

// =========================================================
// 6. MULTI-TYPE CONSUMER WITH @KafkaHandler
// =========================================================

/**
 * @KafkaHandler: handles different payload types within a @KafkaListener class.
 * Each @KafkaHandler method handles a different message type.
 * Spring Kafka dispatches to the correct handler based on the deserialized type.
 * Requires: type info in message headers (JsonSerializer.ADD_TYPE_INFO_HEADERS=true on producer)
 *
 * Interview Q: When to use @KafkaHandler vs separate @KafkaListener methods?
 * @KafkaHandler: when one topic carries multiple event types (polymorphic consumers)
 * Separate @KafkaListener: when different topics carry different event types (cleaner)
 */
@Component
@KafkaListener(
        id      = "employeeMultiTypeConsumer",
        topics  = KafkaTopics.EMPLOYEE_EVENTS,
        groupId = "employee-multi-type-group"
)
class EmployeeEventMultiTypeConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventMultiTypeConsumer.class);

    /**
     * Handles EmployeeCreatedEvent messages on the employee-events topic.
     */
    @KafkaHandler
    public void handleCreated(EmployeeCreatedEvent event) {
        log.info("@KafkaHandler - EmployeeCreated: employeeId={}", event.getEmployeeId());
        // handle created event
    }

    /**
     * Handles EmployeeUpdatedEvent messages on the same topic.
     */
    @KafkaHandler
    public void handleUpdated(EmployeeUpdatedEvent event) {
        log.info("@KafkaHandler - EmployeeUpdated: employeeId={}, fields={}",
                event.getEmployeeId(), event.getChangedFields());
        // handle updated event
    }

    /**
     * isDefault=true: catches all message types not handled by other @KafkaHandler methods.
     * Good safety net for unknown or future event types.
     */
    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object event) {
        log.warn("@KafkaHandler - Unknown event type: {}", event.getClass().getSimpleName());
    }
}

// =========================================================
// APPLICATION PROPERTIES REFERENCE (as comments)
// =========================================================

/**
 * ─────────────────────────────────────────────────────────────
 * application.properties - Kafka Producer / Consumer Config
 * ─────────────────────────────────────────────────────────────
 *
 * ### KAFKA CONNECTION ###
 * spring.kafka.bootstrap-servers=localhost:9092
 *
 * ### PRODUCER PROPERTIES ###
 * spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
 * spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
 * spring.kafka.producer.acks=all
 * spring.kafka.producer.retries=3
 * spring.kafka.producer.batch-size=16384
 * spring.kafka.producer.linger-ms=5
 * spring.kafka.producer.buffer-memory=33554432
 * spring.kafka.producer.compression-type=snappy
 * spring.kafka.producer.properties.enable.idempotence=true
 *
 * ### CONSUMER PROPERTIES ###
 * spring.kafka.consumer.group-id=employee-service-group
 * spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
 * spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
 * spring.kafka.consumer.auto-offset-reset=earliest
 * spring.kafka.consumer.enable-auto-commit=false
 * spring.kafka.consumer.max-poll-records=100
 * spring.kafka.consumer.properties.spring.json.trusted.packages=com.interview.master.springboot.kafka
 * spring.kafka.consumer.properties.isolation.level=read_committed
 *
 * ### LISTENER PROPERTIES ###
 * spring.kafka.listener.ack-mode=manual_immediate
 * spring.kafka.listener.concurrency=3
 * spring.kafka.listener.missing-topics-fatal=false
 *
 * ### ADMIN (auto-create topics) ###
 * spring.kafka.admin.auto-create=true
 *
 * ─────────────────────────────────────────────────────────────
 * application.properties - Kafka Topic Definitions (via @Bean TopicBuilder)
 * ─────────────────────────────────────────────────────────────
 *
 * # Topics are created via @Bean NewTopic in a @Configuration class:
 * # @Bean
 * # public NewTopic employeeCreatedTopic() {
 * #     return TopicBuilder.name("employee-created")
 * #         .partitions(6)
 * #         .replicas(3)
 * #         .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")  // 7 days
 * #         .build();
 * # }
 *
 * ─────────────────────────────────────────────────────────────
 * QUICK INTERVIEW RECAP - KAFKA
 * ─────────────────────────────────────────────────────────────
 *
 * Q: What is a consumer group?
 * A: Set of consumers that cooperatively consume a topic.
 *    Each partition consumed by exactly one consumer in the group.
 *    Multiple groups can independently consume the same topic.
 *
 * Q: What happens on consumer group rebalancing?
 * A: Triggered when consumer joins/leaves/crashes.
 *    All consumers stop consuming, partitions are redistributed.
 *    Rebalance can cause duplicate processing (at-least-once semantics).
 *    Minimize with: session.timeout.ms tuning, static group membership.
 *
 * Q: What is the difference between at-least-once and exactly-once?
 * A: At-least-once: commit offset after processing. On crash before commit, message reprocessed.
 *    Exactly-once: requires idempotent producer + transactional consumer + consumer idempotency.
 *
 * Q: How do you implement a dead letter topic?
 * A: Use DefaultErrorHandler with DeadLetterPublishingRecoverer in consumer config.
 *    OR use @RetryableTopic which auto-creates retry and DLT topics.
 *
 * Q: What is consumer lag?
 * A: Difference between latest offset (producer position) and committed offset (consumer position).
 *    High lag = consumer is falling behind. Monitor with kafka-consumer-groups.sh or Confluent Control Center.
 *
 * Q: What is log compaction?
 * A: Retention policy that keeps only the latest value per key.
 *    cleanup.policy=compact. Used for KTable materialized views.
 *    A tombstone message (null value) signals deletion of a key.
 */
class KafkaInterviewRecap {
    // This class serves as a recap container for the comments above.
    // See KafkaProducerConfig, KafkaConsumerConfig, EmployeeEventProducer, EmployeeEventConsumer
    // for concrete implementation examples.
}
