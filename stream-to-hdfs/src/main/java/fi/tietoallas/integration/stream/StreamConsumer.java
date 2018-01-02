package fi.tietoallas.integration.stream;

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

import fi.tietoallas.integration.stream.kafka.KafkaStreamManager;
import fi.tietoallas.integration.stream.kafka.SerializableRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A consumer for Kafka streams produced by document-based integrations (i.e JSON or XML). The implementation writes the
 * documents to ORC files as such. Note that to avoid the "small files problem" an additional compaction phase is needed
 * afterwards. The consumer assumes that the message keys follow the following structure:
 *
 * integration;object-id;import-timestamp
 *
 * For example:
 *
 * husradu;4061;2017-12-15 09:55:17
 */
public class StreamConsumer {

    /**
     * The key separator to use.
     */
    private static final String KEY_SEPARATOR = ";";

    /**
     * The logger
     */
    private static Logger logger = LoggerFactory.getLogger(StreamConsumer.class);

    /**
     * The streaming context
     */
    private static JavaStreamingContext streamingContext;

    /**
     * Creates a new instance.
     *
     * @param javaStreamingContext the streaming context
     */
    public StreamConsumer(JavaStreamingContext javaStreamingContext) {
        this.streamingContext = javaStreamingContext;
    }

    /**
     * Sets up the stream processor.
     *
     * @param streamManager the stream manager
     * @param outputLocation the output location (e.g. 'adl:///')
     */
    public void setupStreaming(KafkaStreamManager streamManager, String outputLocation) {
        try {

            JavaDStream<SerializableRecord> directStream = streamManager.createStream(streamingContext, mapper);
            directStream.foreachRDD((JavaRDD<SerializableRecord> rdd) -> {
                if (!rdd.isEmpty()) {

                    JavaSparkContext context = new JavaSparkContext(rdd.context());
                    SQLContext sqlContext = SQLContext.getOrCreate(context.sc());
                    StructType schema = createSchema();

                    Map<TopicPartition, List<SerializableRecord>> groupedRecords =
                            rdd.collect().stream().collect(Collectors.groupingBy(r -> new TopicPartition(r.getTopic(), r.getPartition())));

                    groupedRecords.forEach((topicPartition, records) -> {

                        // Write to disk
                        List<Row> rows = records.stream().map(r -> r.getRow()).collect(Collectors.toList());
                        Dataset<Row> dataset = sqlContext.createDataFrame(context.parallelize(rows, 1), schema);
                        String location = outputLocation + resolveLocation(topicPartition.topic());
                        dataset.write().mode(SaveMode.Append).orc(location);

                        // Commit current offset to status-db
                        long offset = records.stream().map(r -> r.getOffset()).max(Long::compare).get();
                        streamManager.saveOffsets(topicPartition.topic(), topicPartition.partition(), offset + 1);
                    });
                }
            });

        } catch (Exception e) {
            logger.error("An error occurred while processing the stream", e);
            throw new RuntimeException();
        }

    }

    /**
     * Creates the schema for the ORC files.
     *
     * @return the schema
     */
    public static StructType createSchema() {
        List<StructField> structFields = new ArrayList<>();
        structFields.add(new StructField("id", DataTypes.StringType, false, Metadata.empty()));
        structFields.add(new StructField("data", DataTypes.StringType, false, Metadata.empty()));
        structFields.add(new StructField("source", DataTypes.StringType, false, Metadata.empty()));
        structFields.add(new StructField("timestamp", DataTypes.TimestampType, false, Metadata.empty()));
        return new StructType(structFields.toArray(new StructField[structFields.size()]));
    }

    /**
     * Resolves the location from the topic name (e.g. husradu-orig, healthweb-pseudo).
     *
     * @param topic the topic name
     * @return the location
     */
    static String resolveLocation(String topic) {
        if (topic.endsWith("-orig")) {
            String integration = topic.substring(0, topic.indexOf("-orig"));
            return "/staging/" + integration + "/data";
        } else if (topic.endsWith("-pseudo")) {
            String integration = topic.substring(0, topic.indexOf("-pseudo"));
            return "/storage/" + integration + "/data";
        } else {
            throw new IllegalStateException("Unsupported topic name: " + topic);
        }
    }

    /**
     * Mapper from Kafka's unserializable ConsumerRecords to our own SerializableRecord.
     */
    static Function<ConsumerRecord<String, String>, SerializableRecord> mapper = consumerRecord -> {
        System.out.println("Receiving record for topic " + consumerRecord.topic() +
                ", partition " + consumerRecord.partition() +
                ", offset " + consumerRecord.offset());

        String source = consumerRecord.topic() + "-" + consumerRecord.partition() + "-" + consumerRecord.offset();
        String key = consumerRecord.key();
        String[] tokens = key.split(KEY_SEPARATOR);
        if (tokens.length != 3) {
            throw new IllegalStateException("Unsupported key: " + key);
        }


        Timestamp timestamp = null;
        try {
            // First try the ISO 8601 instant format..
            Instant instant = Instant.parse(tokens[2]);
            timestamp = Timestamp.from(instant);
        } catch (DateTimeParseException e) {
            // ..and then the ISO 8601 local date-time format.
            timestamp = Timestamp.valueOf(
                    LocalDateTime.from(
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(tokens[2])));
        }


        return new SerializableRecord(
                RowFactory.create(tokens[1], consumerRecord.value(), source, timestamp),
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset());
    };

}
