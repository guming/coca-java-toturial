package org.coca.kafka.demo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaProducerDemo {
    private static final String TOPIC = "benchmark-string";
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.9:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //Optimizing throughput and latency
        //linger.ms=500
        //batch.size=16384
        //buffer.memory=33554432
        //compression.type=gzip
        //Adjusting message delivery waiting time
//        delivery.timeout.ms=120000
//        partitioner.class=my-custom-partitioner
        //Ordered delivery & Reliability
        //enable.idempotence=true
        //max.in.flight.requests.per.connection=5
        //acks=all
        //retries=2147483647
        //另一种保证有序，in.flight=1 idempotence=false

        //data durability
        //replica.lag.time.max.ms =10000
        //min.insync.replicas < replicas nums
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);
        long time = System.nanoTime();
        System.out.println(producer.send(new ProducerRecord<>(TOPIC,
                "{\"a\":" + time +
                        ",\"b\":" + time + "}")).get());
        producer.close();
    }
}
