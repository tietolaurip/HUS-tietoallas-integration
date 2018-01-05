package fi.tietoallas.integration.staginstreamconsumer.util;

import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrcTypeUtils {

    public static List<String> getOrgTypes(final String orgString) {
     return   Arrays.asList(orgString.substring(6, orgString.lastIndexOf(">")).split(",")).stream()
                .map(s -> s.split(":")[1])
                .collect(Collectors.toList());
    }
    public static Object resolveOrcValue(String part, String fieldType) {
        if (fieldType.equals("string")){
            return new Text(part);
        }
        if (fieldType.equals("int") || fieldType.equals("double") || fieldType.equals("bigint") || fieldType.equals("smallint")){
            return new DoubleWritable(Double.valueOf(part));
        }
        if (fieldType.equals("float")){
            return new FloatWritable(Float.valueOf(fieldType));
        }
        if (fieldType.equals("timestamp")){
            return new TimestampWritable(Timestamp.valueOf(part));
        }
        return new Text(part);
    }
}
