/*-
 * #%L
 * common-incremental
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


package fi.tietoallas.incremental.common.commonincremental.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for Kafka Producers with features to split bigger messages to smaller parts
 * and generate kafka message header which included Avro schema
 *
 * @author Antti Kalliokoski
 * @author Tuukka Arola
 */
public class TietoallasKafkaProducer {

    public static final String METADATA_SEPARATOR = ";";

    private final Properties properties;

    private Logger logger = LoggerFactory.getLogger(TietoallasKafkaProducer.class);

    private EncoderFactory encoderFactory;

    private Producer<String, byte[]> messageProducer;

    public static final long MAX_MESSAGE_SIZE = 20000000L;
    public TietoallasKafkaProducer(final Properties properties) {
        this.properties = properties;
        this.encoderFactory = EncoderFactory.get();
    }
    //for testing
    public TietoallasKafkaProducer(final Producer<String,byte[]> messageProducer){
        this.messageProducer=messageProducer;
        this.properties=null;
        this.encoderFactory = EncoderFactory.get();
    }

    /**
     * Method sends one or more depending size of newData parameter to kafka witch is configured at constructor
     * @param schema Avro schema for message
     * @param newData new Date incuded in on one list element inside GenericRecoed
     * @param table normalized name of table
     * @param integration name of integration
     * @return One or more MonitorData objects which has information about sended messages
     * @throws Exception
     */
    public List<MonitoringData> run(Schema schema, GenericRecord newData, final String table, final String integration) throws Exception {
        List<MonitoringData> monitoringDatas = new ArrayList<>();
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);
        String title = integration + "." + table + METADATA_SEPARATOR + schema.toString() + METADATA_SEPARATOR + nowUTC.toString();
        if (this.properties != null) {
            //in unit tests properties are null
            messageProducer = new KafkaProducer<>(properties);
        }
        List<ProducerRecord<String, byte[]>> producerRecords = getProducerRecords(newData, integration, title);
        for (ProducerRecord<String, byte[]> producerRecord : producerRecords) {
            RecordMetadata recordMetadata = messageProducer.send(producerRecord).get();
            logger.info("Kafka: topic {}, partition {} at offset  {} with data length of {}", recordMetadata.topic(), recordMetadata.partition(),
                    recordMetadata.offset(), producerRecord.value().length);
            monitoringDatas.add(new MonitoringData(integration,table,producerRecord.value().length));
        }
        messageProducer.close();
        return monitoringDatas;
    }

    private byte[] serializeAvroData(GenericRecord record) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Encoder encoder = encoderFactory.directBinaryEncoder(bos, null);
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());
        writer.write(record, encoder);
        return bos.toByteArray();
    }

    public List<ProducerRecord<String, byte[]>> getProducerRecords(final GenericRecord originalData, final String integration, final String title)
            throws Exception {
        long fullSize = serializeAvroData(originalData).length;
        List<GenericRecord> genericRecords = (List<GenericRecord>) originalData.get("rows");
        List<ProducerRecord<String, byte[]>> producerRecords = new ArrayList<>();
        if (fullSize < MAX_MESSAGE_SIZE) {
            producerRecords.add(new ProducerRecord<>(integration, title, serializeAvroData(originalData)));
        } else {
            long sizeOfOneRow = fullSize / (long) genericRecords.size();
            int numberOfMessages = (int) Math.ceil((double) fullSize / (double) MAX_MESSAGE_SIZE);
            int numberOfLinesPerMessage = 0;
            while (true){
                if (MAX_MESSAGE_SIZE>numberOfLinesPerMessage*sizeOfOneRow){
                    numberOfLinesPerMessage++;
                }
                else {
                    break;
                }
            }
            for (int i = 0; i < numberOfMessages; i++) {
                    int start = i * numberOfLinesPerMessage;
                    int end = numberOfLinesPerMessage + start;
                    GenericRecord data = originalData;
                    if (end < genericRecords.size()) {
                        data.put("rows", genericRecords.subList(start, end));
                    } else {
                        data.put("rows", genericRecords.subList(start, genericRecords.size()));
                    }
                    producerRecords.add(new ProducerRecord<>(integration, title, serializeAvroData(data)));

            }

        }
        return producerRecords;
    }
}
