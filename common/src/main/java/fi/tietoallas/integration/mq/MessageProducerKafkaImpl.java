package fi.tietoallas.integration.mq;

/*-
 * #%L
 * common
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

import fi.tietoallas.monitoring.Tag;
import fi.tietoallas.monitoring.TaggedLogger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

/**
 * A kafka based message producer.
 */
public class MessageProducerKafkaImpl implements MessageProducer {

    /** The logger */
    private static TaggedLogger logger = new TaggedLogger(MessageProducerKafkaImpl.class);

    /** The Kafka producer */
    private KafkaProducer<String, String> producer;

    /**
     * Creates a new instance.
     *
     * @param properties the properties for the kafka producer
     */
    public MessageProducerKafkaImpl(Properties properties) {
        producer = new KafkaProducer<>(properties);
    }

    @Override
    public void produce(String queue, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(queue, key, value);
        try {
            RecordMetadata metadata = producer.send(record).get();
            logger.info("kafka topic " + metadata.topic());
            logger.info("kafka partition " + metadata.partition());
            logger.info("kafka offset " + metadata.offset());
        } catch (Exception e) {
            logger.error(Tag.DL_INTERNAL_CONNECTION_FAILURE, "Failed to produce kafka message.", e);
            throw new RuntimeException(e);
        }
    }

}
