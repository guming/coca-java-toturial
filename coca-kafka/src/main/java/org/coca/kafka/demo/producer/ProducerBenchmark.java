package org.coca.kafka.demo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 2)
@Warmup(iterations = 1)
public class ProducerBenchmark {

    private static final String TOPIC = "benchmark-string";

    private KafkaProducer<String, String> producer;

    @Param({"192.168.1.9:9092;true"})
    public String parameters;

    @Setup
    public void setup() {
        String[] parts = parameters.split(";");
        String server = parts[0];
//        boolean clientEncryption = Boolean.parseBoolean(parts[1]);
//        System.setProperty(JaasUtils.JAVA_LOGIN_CONFIG_PARAM, "src/main/resources/jaas.conf");
        Properties properties = new Properties();
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,1024*16*32);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 10240 * 1024 * 32);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        properties.put("security.protocol", SecurityProtocol.SASL_PLAINTEXT.toString());
//        properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
//        if (clientEncryption) {
//            properties.put(AesStructuredMessageSerializerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                    properties.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
//            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                    AesStructuredMessageSerializer.class);
//            properties.put(AesStructuredMessageSerializerConfig.GRANULAR_DATA_ACCESSOR_CONFIG,
//                    JsonDataAccessor.class);
//            properties.put(AesStructuredMessageSerializerConfig.PUBLIC_KEY_CONFIG,
//                    "src/main/resources/P521.pem");
//            properties.put(AesStructuredMessageSerializerConfig.FIELDS_LIST_CONFIG,
//                    Collections.singletonList("a"));
//        }
        producer = new KafkaProducer<>(properties);
    }

    @TearDown
    public void close() {
        producer.close();
    }

//    @Benchmark
//    public void testProducer() {
//        long time = System.nanoTime();
//        System.out.println("cool");
//        System.out.println("cool");
//        producer.send(new ProducerRecord<>(TOPIC,
//                "{\"a\":" + time +
//                        ",\"b\":" + time + "}"));
//    }

    @Benchmark
    public void testProducerSync() throws ExecutionException, InterruptedException {
        long time = System.nanoTime();
        producer.send(new ProducerRecord<>(TOPIC,
                "{\"a\":" + time +
                        ",\"b\":" + time +
                        ",\"c\":就啊师傅觉得师傅讲的是恐惧的反馈孙俊峰时代俊峰会觉得是时间快点发货快就收到回复健康会收到尽快发货快结束倒海翻江可视电话开发环境看电视剧开发活动空间划分就开始对黄金矿工和艰苦奋斗和进口法国红酒烤豆腐收到回复的手机发的第三方" +",\"" +
                        ",\"d\":就啊师傅觉得师傅讲的是恐惧的反馈孙俊峰时代俊峰会觉得是收到回复的手机发的第三方" +   ",\"e\":就啊师傅觉得师傅讲的是恐惧的反馈孙俊峰时代俊峰会觉得是收到回复的手机发的第三方"+"}"),(recordMetadata, exception) -> {
            if (exception != null) {
                System.out.println("Record written to offset " +
                        recordMetadata.offset() + " timestamp " +
                        recordMetadata.timestamp());
            }
//            System.out.println(recordMetadata.);
        });
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(ProducerBenchmark.class.getCanonicalName())
                .forks(1)
                .build();

        new Runner(options).run();
    }
}
