package org.coca.kafka.demo.producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class KafkaConsumerDemo {
    public static int consume(String brokers, String groupId, String topicName) {
        // Create a consumer
        KafkaConsumer<String, String> consumer;
        // Configure the consumer
        Properties properties = new Properties();
        // Point it to the brokers
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        // Set the consumer group (all consumers must belong to a group).
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // Set how to serialize key/value pairs
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        // When a group is first created, it has no offset stored to start reading from. This tells it to start
        // with the earliest record in the stream.
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        //Improving throughput
        properties.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,"500");
        properties.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,"16384");
        //Lowering latency
        properties.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG,"52428800");
        properties.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,"1048576");

        //Avoiding data loss or duplication when committing offsets
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        //2.commit sync

        //Configuring a reliable data pipeline
        //producer:
        // Idempotence =true
        //transactional.id=UNIQUE-ID
        //transaction.timeout.ms=900000
        properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"read_committed");
        //Recovering from failure within a consumer group
        properties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"10000");
        properties.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,"3000");

        // specify the protocol for Domain Joined clusters
//        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");

        consumer = new KafkaConsumer<>(properties);

        // Subscribe to the 'test' topic
        consumer.subscribe(Arrays.asList(topicName));

        // Loop until ctrl + c
        int count = 0;
        while(true) {
            // Poll for records
            ConsumerRecords<String, String> records = consumer.poll(200);
            // Did we get any?
            if (records.count() == 0) {
                // timeout/nothing to read
            } else {
                // Yes, loop over records
                for(ConsumerRecord<String, String> record: records) {
                    // Display record and count
                    count += 1;
                    System.out.println( count + ": " + record.value());
                }
                consumer.commitSync();
            }
        }
    }
    public static void main(String[] args) {
        consume("192.168.1.9:9092", "1001", "benchmark-string");
    }
}
