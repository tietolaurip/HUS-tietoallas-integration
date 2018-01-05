package fi.tietoallas.integration.staginstreamconsumer;

import org.apache.avro.Schema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Assert;
import org.junit.Test;

public class StaginStreamConsumerTest {

	@Test
	public void testConvertToStructType() {
		String schemaString = "{\"type\":\"record\",\"name\":\"test\",\"namespace\":\"fi.tietoallas.integration\","
				+ "\"fields\":["
				+ "{\"name\":\"a\",\"type\":[\"null\",\"int\"],\"default\":null},"
				+ "{\"name\":\"b\",\"type\":[\"null\",\"long\"],\"default\":null},"
				+ "{\"name\":\"c\",\"type\":[\"null\",\"float\"],\"default\":null},"
				+ "{\"name\":\"d\",\"type\":[\"null\",\"double\"],\"default\":null},"
				+ "{\"name\":\"e\",\"type\":[\"null\",\"boolean\"],\"default\":null},"
				+ "{\"name\":\"f\",\"type\":[\"null\",\"string\"],\"default\":null},"
				+ "{\"name\":\"g\",\"type\":[\"null\",\"long\"],\"logicalType\":\"timestamp-millis\",\"default\":null},"
				+ "{\"name\":\"h\",\"type\":[\"null\",\"int\"],\"logicalType\":\"date\",\"default\":null}]}";
		Schema schema = new Schema.Parser().parse(schemaString);
		StructType structType = StaginStreamConsumer.convertToStructType(schema);
		StructField[] fields = structType.fields();
		
		Assert.assertSame(DataTypes.IntegerType, fields[0].dataType());
		Assert.assertSame(DataTypes.LongType, fields[1].dataType());
		Assert.assertSame(DataTypes.FloatType, fields[2].dataType());
		Assert.assertSame(DataTypes.DoubleType, fields[3].dataType());
		Assert.assertSame(DataTypes.BooleanType, fields[4].dataType());
		Assert.assertSame(DataTypes.StringType, fields[5].dataType());
		Assert.assertSame(DataTypes.TimestampType, fields[6].dataType());
		Assert.assertSame(DataTypes.DateType, fields[7].dataType());
	}

}
