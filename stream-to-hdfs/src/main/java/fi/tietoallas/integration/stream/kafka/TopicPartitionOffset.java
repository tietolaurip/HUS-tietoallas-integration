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


import org.apache.kafka.common.TopicPartition;

public class TopicPartitionOffset {

    String topic;
    int partition;
    long offset;

    public TopicPartitionOffset(String topic, int partition, long offset) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
    }

    TopicPartition getTopicPartition() {
        return new TopicPartition(topic, partition);
    }

    @Override
    public String toString() {
        return "TopicPartitionOffset [topic=" + topic + ", partition=" + partition + ", offset=" + offset + "]";
    }

}
