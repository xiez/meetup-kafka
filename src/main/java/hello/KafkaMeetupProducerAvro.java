package hello;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.joda.time.LocalTime;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;


public class KafkaMeetupProducerAvro {
    private final static String TOPIC = "test_topic";
    private final static String BOOTSTRAP_SERVERS = "10.6.5.112:19092";

    private static Producer<Object, Object> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaMeetupProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://10.6.5.112:8081");
        return new KafkaProducer<Object, Object>(props);
    }

    static void runProducer() throws Exception {
        Producer<Object, Object> producer = createProducer();

        String userSchema = "{\"type\":\"record\"," +
            "\"name\":\"meetuprecord\"," +
            "\"fields\":[{\"name\":\"rsvp\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);

        URL url = new URL("https://stream.meetup.com/2/rsvps/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed: Http error code: " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            int msgcount = 0;
            String output;

            while ((output = br.readLine()) != null) {
                GenericRecord avroRecord = new GenericData.Record(schema);
                avroRecord.put("rsvp", output);

                ProducerRecord<Object, Object> record = new ProducerRecord<>(TOPIC, msgcount++, avroRecord);
                RecordMetadata metadata = (RecordMetadata) producer.send(record).get();

                System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d) \n",
                                  new Object[] { record.key(), record.value(), Integer.valueOf(metadata.partition()),
                                                 Long.valueOf(metadata.offset()) }
                    );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
            producer.flush();
            producer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        runProducer();
    }
}
