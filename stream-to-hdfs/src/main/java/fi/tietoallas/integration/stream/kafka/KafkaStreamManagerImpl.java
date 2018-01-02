package fi.tietoallas.integration.stream.kafka;

/*-
 * #%L
 * stream-to-hdfs
 * %%
 * Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.ConsumerStrategy;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The default implementation for Kafka stream wrappers.
 */
public class KafkaStreamManagerImpl implements KafkaStreamManager {

    /** The logger */
    private static Logger logger = LoggerFactory.getLogger(KafkaStreamManagerImpl.class);

    /** The kafka broker */
    private String kafkaIp;

    /** The kafka group id */
    private String groupId;

    /** The offsets */
    private List<TopicPartitionOffset> offsets;

    /** The datasource for the metadata */
    private DataSource dataSource;

    /**
     * Creates a new instance.
     *
     * @param kafkaIp The kafka broker id
     * @param groupId The group id
     * @param dataSource The metadata datasource
     */
    public KafkaStreamManagerImpl(String kafkaIp, String groupId, DataSource dataSource) {
        this.kafkaIp = kafkaIp;
        this.groupId = groupId;
        this.dataSource = dataSource;
        this.offsets = queryTopicOffsets();
    }

    @Override
    public JavaDStream<SerializableRecord> createStream(
            JavaStreamingContext context,
            Function<ConsumerRecord<String, String>, SerializableRecord> mapper) {

        Map<String, Object> kafkaParameters = new HashMap<>();
        kafkaParameters.put("group.id", groupId);
        kafkaParameters.put("bootstrap.servers", kafkaIp);
        kafkaParameters.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParameters.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParameters.put("enable.auto.commit", false);
        kafkaParameters.put("auto.offset.reset", "earliest");
        kafkaParameters.put("compression.type", "gzip");
        kafkaParameters.put("max.partition.fetch.bytes", "20971520");

        Map<TopicPartition, Long> topicPartitionOffsets = offsets.stream().collect(Collectors.toMap(t -> t.getTopicPartition(), t -> t.offset));
        List<String> topics = offsets.stream().map(t -> t.topic).collect(Collectors.toList());
        System.out.println("Loading topics " + topicPartitionOffsets);

        ConsumerStrategy<String, String> consumerStrategy = ConsumerStrategies.Subscribe(topics, kafkaParameters, topicPartitionOffsets);
        return KafkaUtils.createDirectStream(context, LocationStrategies.PreferConsistent(), consumerStrategy)
                         .map(mapper);

    }

    @Override
    public void saveOffsets(String topic, int partition, long offset) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE kafka_offset_info SET offset=? WHERE topic=? AND partition=? AND group_id=?")) {

            statement.setLong(1, offset);
            statement.setString(2, topic);
            statement.setInt(3, partition);
            statement.setString(4, this.groupId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TopicPartitionOffset> queryTopicOffsets() {
        List<TopicPartitionOffset> offsets = new ArrayList<>();
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * from kafka_offset_info where group_id = ?")){

            statement.setString(1, this.groupId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                offsets.add(new TopicPartitionOffset(resultSet.getString("topic"), resultSet.getInt("partition"), resultSet.getLong("offset")));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("============= Found offsets for topics and partitions {}", offsets);
        return offsets;
    }
}
