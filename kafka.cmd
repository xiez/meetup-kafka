export ZK_SERVER=10.6.5.112
export KAFKA_SERVER=10.6.5.112

--

docker run --rm debezium/kafka:1.4 bash -c "bin/kafka-topics.sh  --list --zookeeper ${ZK_SERVER}:2181"
docker run --rm debezium/kafka:1.4 bin/kafka-topics.sh  --list --zookeeper ${ZK_SERVER}:2181
docker run --rm confluentinc/cp-kafka:5.5.0 /usr/bin/kafka-topics  --list --zookeeper ${ZK_SERVER}:2181

docker run --rm debezium/kafka:1.4 bash -c "bin/kafka-topics.sh  --create --zookeeper ${ZK_SERVER}:2181 --partitions 1 --replication-factor 1 --topic test-console-topic"
docker run --rm debezium/kafka:1.4 bin/kafka-topics.sh  --create --zookeeper ${ZK_SERVER}:2181 --partitions 1 --replication-factor 1 --topic test-console-topic
docker run --rm confluentinc/cp-kafka:5.5.0 /usr/bin/kafka-topics --create --zookeeper ${ZK_SERVER}:2181 --partitions 1 --replication-factor 1 --topic test-console-topic


docker run --rm debezium/kafka:1.4 bash -c "bin/kafka-topics.sh  --describe --zookeeper ${ZK_SERVER}:2181  --topic test-console-topic"
docker run --rm debezium/kafka:1.4 bin/kafka-topics.sh  --describe --zookeeper ${ZK_SERVER}:2181  --topic test-console-topic
docker run --rm confluentinc/cp-kafka:5.5.0 /usr/bin/kafka-topics  --describe --zookeeper ${ZK_SERVER}:2181  --topic test-console-topic

--

docker run -ti --rm debezium/kafka:1.4 bin/kafka-console-producer.sh --bootstrap-server ${KAFKA_SERVER}:9092 --topic test-console-topic
docker run -ti --rm confluentinc/cp-kafka:5.5.0 /usr/bin/kafka-console-producer --bootstrap-server ${KAFKA_SERVER}:19092 --topic test-console-topic

docker run --rm debezium/kafka:1.4 ./bin/kafka-console-consumer.sh --bootstrap-server ${KAFKA_SERVER}:9092 --topic test-console-topic --from-beginning
docker run --rm confluentinc/cp-kafka:5.5.0 /usr/bin/kafka-console-consumer --bootstrap-server ${KAFKA_SERVER}:19092 --topic test-console-topic --from-beginning


--

docker network create rmoff_kafka

docker run --network=rmoff_kafka --rm --detach --name zookeeper -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:5.5.0

docker run --network=rmoff_kafka --rm --detach --name kafka \
           -p 9092:9092 \
           -e KAFKA_BROKER_ID=1 \
           -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
           -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 \
           -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
           confluentinc/cp-kafka:5.5.0

docker run --network=rmoff_kafka --rm --detach --name kafka \
           -p 19092:19092 \
           -p 19093:19093 \
           -e KAFKA_BROKER_ID=1 \
           -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
           -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,CONNECTIONS_FROM_HOST://10.6.5.112:19092,FOO://10.6.5.112:19093 \
           -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONNECTIONS_FROM_HOST:PLAINTEXT,FOO:PLAINTEXT \
           -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
           confluentinc/cp-kafka:5.5.0



docker run --network=rmoff_kafka --rm --detach --name kafka \
           -p 9092:9092 \
           -e KAFKA_BROKER_ID=1 \
           -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
           -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 \
           -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
           confluentinc/cp-kafka:5.5.0

docker run --network=rmoff_kafka --rm --detach --name schema_reg \
           -p 8081:8081 \
           -e SCHEMA_REGISTRY_HOST_NAME=schema_registry \
           -e SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL=zookeeper:2181 \
           -e SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_ORIGIN=* \
           -e SCHEMA_REGISTRY_ACCESS_CONTROL_ALLOW_METHODS=GET,POST,PUT,OPTIONS \
           confluentinc/cp-schema-registry:3.3.0


