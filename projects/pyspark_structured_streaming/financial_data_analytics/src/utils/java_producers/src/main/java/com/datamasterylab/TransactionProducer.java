package com.datamasterylab;

import com.datamasterylab.dto.Transaction;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionProducer {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProducer.class);
    private static final String TOPIC = "financial_transactions";
    private static final String BOOTSTRAP_SERVERS = "localhost:29092,localhost:39092,localhost:49092";
    private static final int NUM_THREADS = 20;
    private static final int NUM_PARTITIONS = 20;
    private static final short REPLICATION_FACTOR = 3;
    private static final int BATCH_SIZE = 100;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AtomicLong totalRecordsSent = new AtomicLong(0);

    public static void main(String[] args) {
        createTopicIfNotExists();
        Properties props = createProducerConfig();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        startMetricsReporter();

        // Start producer threads
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> producerTask(props));
        }

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down producer...");
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Shutdown interrupted", e);
            }
            logger.info("Producer stopped. Total records sent: {}", totalRecordsSent.get());
        }));
    }

    private static Properties createProducerConfig() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Performance optimizations
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 131072); // 128KB
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864); // 64MB
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // Additional configurations for throughput
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        return props;
    }

    private static void producerTask(Properties props) {
        long threadRecordsSent = 0;
        long startTime = System.currentTimeMillis();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            while (!Thread.currentThread().isInterrupted()) {
                List<ProducerRecord<String, String>> batch = new ArrayList<>(BATCH_SIZE);

                // Create batch of records
                for (int i = 0; i < BATCH_SIZE; i++) {
                    Transaction transaction = Transaction.randomTransaction();
                    try {
                        String transactionJson = objectMapper.writeValueAsString(transaction);
                        batch.add(new ProducerRecord<>(TOPIC,
                            transaction.getTransactionId(),
                            transactionJson));
                    } catch (Exception e) {
                        logger.error("Error serializing transaction", e);
                    }
                }

                // Send batch
                for (ProducerRecord<String, String> record : batch) {
                    producer.send(record, (metadata, exception) -> {
                        if (exception != null) {
                            logger.error("Failed to send record: {}", exception.getMessage());
                        }
                    });
                }

                producer.flush();
                threadRecordsSent += BATCH_SIZE;
                totalRecordsSent.addAndGet(BATCH_SIZE);

                // Calculate thread-specific throughput every 5 seconds
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                if (elapsedTime >= 5000) {
                    double throughput = threadRecordsSent / (elapsedTime / 1000.0);
                    logger.info("Thread throughput: {} records/sec", String.format("%.2f", throughput));
                    threadRecordsSent = 0;
                    startTime = currentTime;
                }
            }
        } catch (Exception e) {
            logger.error("Producer thread error", e);
        }
    }

    private static void startMetricsReporter() {
        Thread metricsThread = new Thread(() -> {
            long lastTotalRecords = 0;
            long lastTime = System.currentTimeMillis();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    long currentTime = System.currentTimeMillis();
                    long currentTotalRecords = totalRecordsSent.get();
                    long recordsDelta = currentTotalRecords - lastTotalRecords;
                    double elapsed = (currentTime - lastTime) / 1000.0;
                    double throughput = recordsDelta / elapsed;

                    logger.info("Overall throughput: {} records/sec, Total records: {}",
                        String.format("%.2f", throughput),
                        currentTotalRecords);

                    lastTotalRecords = currentTotalRecords;
                    lastTime = currentTime;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        metricsThread.setDaemon(true);
        metricsThread.start();
    }

    private static void createTopicIfNotExists() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(props)) {
            boolean topicExists = adminClient.listTopics().names().get().contains(TOPIC);

            if (!topicExists) {
                logger.info("Creating topic '{}'", TOPIC);
                NewTopic newTopic = new NewTopic(TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR);

                // Optional: Add topic configurations
                Map<String, String> configs = new HashMap<>();
                configs.put("retention.ms", String.valueOf(TimeUnit.HOURS.toMillis(24)));
                configs.put("segment.bytes", String.valueOf(1073741824)); // 1GB
                newTopic.configs(configs);

                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                logger.info("Topic '{}' created successfully", TOPIC);
            } else {
                logger.info("Topic '{}' already exists", TOPIC);
            }
        } catch (Exception e) {
            logger.error("Error creating topic", e);
            System.exit(1);
        }
    }
}