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
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A kafka stream manager for producing a constant stream (for testing purposes).
 */
public class KafkaStreamManagerQueueImpl implements KafkaStreamManager {

    List<ConsumerRecord<String, String>> data;

    public KafkaStreamManagerQueueImpl(String topic, List<Tuple2<String, String>> data) {
        this.data = IntStream.range(0, data.size())
                         .mapToObj(i -> new ConsumerRecord<>(topic, 0, i, data.get(i)._1, data.get(i)._2))
                         .collect(Collectors.toList());
    }

    @Override
    public JavaDStream<SerializableRecord> createStream(
            JavaStreamingContext context,
            Function<ConsumerRecord<String, String>, SerializableRecord> mapper) {

        List<SerializableRecord> serializableData = this.data.stream().map(i -> {
            SerializableRecord record = null;
            try {
                record = mapper.call(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return record;
        }).collect(Collectors.toList());

        JavaRDD<SerializableRecord> rdd = context.sparkContext().parallelize(serializableData);
        LinkedBlockingDeque<JavaRDD<SerializableRecord>> queue = new LinkedBlockingDeque<>();
        queue.add(rdd);
        return context.queueStream(queue);
    }

    @Override
    public void saveOffsets(String topic, int partition, long offset) {
        /* no op */
    }


}
