package fi.tietoallas.incremental.common.commonincremental.util;

import fi.tietoallas.incremental.common.commonincremental.domain.MonitoringData;
import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.byteThat;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TietoallasKafkaProducerTest {


    Producer<String,byte[]> kafkaProducer;
    TietoallasKafkaProducer tietoallasKafkaProducer;


    @Before
    public void setUp() throws Exception{
       kafkaProducer = new MockProducer<>(true,new StringSerializer(),new ByteArraySerializer());
        tietoallasKafkaProducer= new TietoallasKafkaProducer(kafkaProducer);
    }
    @Test
    public void testRun() throws Exception{
        SchemaGenerationInfo info = getSchemaGenerationInfo();
        Schema schema = new Schema.Parser().parse(DynamicAvroTools.toIntegrationAvroSchema("testi",info));
        List<MonitoringData> monitoringData = tietoallasKafkaProducer.run(schema, generateTestData(5, schema,info), "testa", "testi");
        assertThat(monitoringData,hasSize(1));

    }
    private GenericRecord generateTestData(int numberOfRows,Schema schema,SchemaGenerationInfo schemaGenerationInfo){
           GenericRecord genericRecord = new GenericData.Record(schema);
        List<GenericData.Record> genericRecords = new ArrayList<>();
        Schema rowSchema = new Schema.Parser().parse(DynamicAvroTools.toAvroSchema(schemaGenerationInfo));
        for (int i=0; i<numberOfRows; i++){
            GenericData.Record record = new GenericData.Record(rowSchema);
            record.put("a","a-value");
            record.put("b","b-value");
            record.put("c","c-value");
            record.put("d","d-value");
            genericRecords.add(record);
        }
        genericRecord.put(DynamicAvroTools.ROWS_NAME,genericRecords);
        return genericRecord;
    }
    private SchemaGenerationInfo getSchemaGenerationInfo(){
        List<String> names = asList("a","b","c","d");
        List<String> types = asList("string","string","string","string");
        return new SchemaGenerationInfo("testa",names,types, asList(null,null,null,null));

    }

}
