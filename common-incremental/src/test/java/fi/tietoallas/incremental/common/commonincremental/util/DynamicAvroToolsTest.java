package fi.tietoallas.incremental.common.commonincremental.util;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

import fi.tietoallas.incremental.common.commonincremental.util.DynamicAvroTools;
import org.apache.avro.Conversions.DecimalConversion;
import org.apache.avro.LogicalTypes;
import org.apache.avro.LogicalTypes.Decimal;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.ColumnRowMapper;
import fi.tietoallas.incremental.common.commonincremental.resultsetextrator.GenericRecordMapper;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
public class DynamicAvroToolsTest {

	@Test
	public void testToAvroSchema() throws IOException {
		List<String> names = asList("a", "b", "c", "d", "e", "f", "g", "h");
		List<String> types = asList("boolean", "int", "long", "float", "double", "bytes", "long", "int");

		Map<String, String> timestamp = new HashMap<>();
		timestamp.put(ColumnRowMapper.AVRO_LOGICAL_TYPE, "timestamp-millis");
		Map<String, String> date = new HashMap<>();
		date.put(ColumnRowMapper.AVRO_LOGICAL_TYPE, "date");
		Map<String, String> numeric = new HashMap<>();
		numeric.put(ColumnRowMapper.AVRO_LOGICAL_TYPE, "decimal");
		numeric.put("precision", "7");
		numeric.put("scale", "3");

		List<Map<String, String>> params = asList(null, null, null, null, null, numeric, timestamp, date);

		SchemaGenerationInfo info = new SchemaGenerationInfo("test", names, types, params);
		String intSchema = DynamicAvroTools.toIntegrationAvroSchema("integraition", info);
		String schema = DynamicAvroTools.toAvroSchema(info);
		Schema parsedIntSchema = new Schema.Parser().parse(intSchema);
		Schema parsedSchema = new Schema.Parser().parse(schema);

		System.out.println(parsedIntSchema);
		
		GenericRecordBuilder builder = new GenericRecordBuilder(parsedSchema);
		builder.set("a", Boolean.TRUE);
		builder.set("b", Integer.MAX_VALUE);
		builder.set("c", Long.MAX_VALUE);
		builder.set("d", Float.MAX_VALUE);
		builder.set("e", Double.MAX_VALUE);
		Decimal decimal = LogicalTypes.decimal(Integer.parseInt(numeric.get("precision")), Integer.parseInt(numeric.get("scale")));
		builder.set("f", new DecimalConversion().toBytes(new BigDecimal("1234.123"), null, decimal));
		builder.set("g", System.currentTimeMillis());
		builder.set("h", (int) (new Date(System.currentTimeMillis()).getTime() / GenericRecordMapper.MILLIS_IN_DAY));
		Record record = builder.build();

		System.out.println(record);
		
		GenericRecord message = new GenericData.Record(parsedIntSchema);
		message.put(DynamicAvroTools.ROWS_NAME, asList(record));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().directBinaryEncoder(bos, null);
		GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(message.getSchema());
		writer.write(message, encoder);
		byte[] data = bos.toByteArray();

		DatumReader<GenericRecord> reader = new SpecificDatumReader<>(parsedIntSchema);
		Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
		GenericRecord read = reader.read(null, decoder);
		GenericData.Array<GenericRecord> genericRecords = (GenericData.Array<GenericRecord>) read.get("rows");
		for (Iterator<GenericRecord> it = genericRecords.iterator(); it.hasNext();) {
			GenericRecord r = it.next();
			Schema s = r.getSchema();
			List<Schema.Field> fs = s.getFields();
			for (Schema.Field field : fs) {
				Object value = record.get(field.name());
				String logicalType = field.getProp("logicalType");
				if (logicalType != null) {
					if (value == null) {
						continue;
					} else if (logicalType.equals("timestamp-millis")) {
						System.out.println(new Timestamp(((Long) value)));
					} else if (logicalType.equals("date")) {
						System.out.println(new Date(((Integer) value) * GenericRecordMapper.MILLIS_IN_DAY));
					} else if (logicalType.equals("decimal")) {
						System.out.println(new DecimalConversion().fromBytes((ByteBuffer)value, null, decimal));
					}
				}
			}
		}
	}
	@Test
	public void testSplit() throws Exception{
		TietoallasKafkaProducer tietoallasKafkaProducer = new TietoallasKafkaProducer(new Properties());
		GenericRecord genericRecord = generateTestData(600000);
        List<ProducerRecord<String, byte[]>> producerRecords = tietoallasKafkaProducer.getProducerRecords(genericRecord, "test", "test");
        for (ProducerRecord<String,byte[]> producer : producerRecords){
			assertThat(producer.value().length<=20000020,is(true));
		}

        ProducerRecord<String,byte[]>  producerRecord = producerRecords.get(0);
        assertThat(producerRecord.value(),notNullValue());
        GenericData.Array<GenericRecord> rows = getRows(producerRecord.value());
        GenericRecord genericRecord1 = rows.get(0);
        assertThat(genericRecord1.get("a").toString(),is("a-value"));
        assertThat(genericRecord1.get("b").toString(),is("b-value"));
        assertThat(genericRecord1.get("c").toString(),is("c-value"));
        assertThat(genericRecord1.get("d").toString(),is("d-value"));

        GenericRecord notEven = generateTestData(799999);
        producerRecords = tietoallasKafkaProducer.getProducerRecords(notEven,"test","test");
        for (ProducerRecord<String,byte[]> producer : producerRecords){
            assertThat(producer.value().length<=20000020,is(true));
        }
    }
    private GenericRecord generateTestData(int numberOfRows){
        List<String> names = asList("a","b","c","d");
        List<String> types = asList("string","string","string","string");
        SchemaGenerationInfo schemaGenerationInfo = new SchemaGenerationInfo("testa",names,types, asList(null,null,null,null));
        Schema schema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema("testi",schemaGenerationInfo));
        GenericRecord genericRecord = new GenericData.Record(schema);
        List<Record> genericRecords = new ArrayList<>();
        Schema rowSchema = new Schema.Parser().parse(DynamicAvroTools.toAvroSchema(schemaGenerationInfo));
        for (int i=0; i<numberOfRows; i++){
            Record record = new Record(rowSchema);
            record.put("a","a-value");
            record.put("b","b-value");
            record.put("c","c-value");
            record.put("d","d-value");
            genericRecords.add(record);
        }
        genericRecord.put(DynamicAvroTools.ROWS_NAME,genericRecords);
        return genericRecord;
    }
    private GenericData.Array<GenericRecord> getRows(byte[] data) throws Exception{
        List<String> names = asList("a","b","c","d");
        List<String> types = asList("string","string","string","string");
        SchemaGenerationInfo schemaGenerationInfo = new SchemaGenerationInfo("testa",names,types, asList(null,null,null,null));
        Schema schema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema("testi",schemaGenerationInfo));
        DatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        GenericRecord read = reader.read(null, decoder);
        return  (GenericData.Array<GenericRecord>)read.get(DynamicAvroTools.ROWS_NAME);

    }


}
