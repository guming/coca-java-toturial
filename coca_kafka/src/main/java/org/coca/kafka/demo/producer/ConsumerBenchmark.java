package org.coca.kafka.demo.producer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)

public class ConsumerBenchmark {
    public static final String TOPIC = "benchmark-string";

    private KafkaConsumer<String, String> consumer;

    @Param({"192.168.1.9:9092;false"})
    public String parameters;

    private long count;

    @Setup
    public void setup() {
        String[] parts = parameters.split(";");
        String server = parts[0];
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, server + "-" + UUID.randomUUID().toString());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
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
        consumer.subscribe(Collections.singleton(TOPIC));
    }

    @TearDown
    public void close() {
        consumer.close();
        System.out.println("Count = " + count);
    }
    @Benchmark
    public void testConsumer(Blackhole blackhole) {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, String> record : records) {
            blackhole.consume(record.value());
        }
        consumer.commitSync();
        count += records.count();
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(ConsumerBenchmark.class.getCanonicalName())
                .forks(1)
                .build();

        new Runner(options).run();
    }

}
