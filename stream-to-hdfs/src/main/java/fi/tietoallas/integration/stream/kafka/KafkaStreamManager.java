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


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;


/**
 * A kafka stream manager.
 */
public interface KafkaStreamManager {

    /**
     * Creates the kafka stream.
     *
     * @param context the context
     * @param mapper the mapping from (non-serializable) consumer records to serializable records
     * @return the stream
     */
    JavaDStream<SerializableRecord> createStream(
            JavaStreamingContext context,
            Function<ConsumerRecord<String, String>, SerializableRecord> mapper);

    /**
     * Saves the offsets.
     */
    void saveOffsets(String topic, int partition, long offset);

}
