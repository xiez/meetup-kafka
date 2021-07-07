package hello;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.joda.time.LocalTime;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class KafkaMeetupConsoleConsumer {
    private final static String TOPIC = "test_topic";
    private final static String BOOTSTRAP_SERVERS = "10.6.5.112:19092";

    private static Consumer<Integer, GenericRecord> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaMeetupConsumerGroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "http://10.6.5.112:8081");

        final Consumer<Integer, GenericRecord> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }


    static void runConsumer() throws Exception {
        final Consumer<Integer, GenericRecord> consumer = createConsumer();
        try {
            while (true) {
                final ConsumerRecords<Integer, GenericRecord> consumerRecords = consumer.poll(Long.MAX_VALUE);
                consumerRecords.forEach(record -> {
                        GenericRecord r = record.value();
                        String json = r.get("rsvp").toString();

                        System.out.printf("Consumer record:(%d, %s, %d, %d)\n", record.key(), json,
                                          record.partition(), record.offset());
                    });
            }
        } finally {
            consumer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        runConsumer();
    }
}
