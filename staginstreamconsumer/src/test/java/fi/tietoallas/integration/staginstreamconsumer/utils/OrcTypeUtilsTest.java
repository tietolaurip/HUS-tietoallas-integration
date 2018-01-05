package fi.tietoallas.integration.staginstreamconsumer.utils;


import fi.tietoallas.integration.staginstreamconsumer.util.OrcTypeUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static fi.tietoallas.integration.staginstreamconsumer.util.OrcTypeUtils.getOrgTypes;
import static fi.tietoallas.integration.staginstreamconsumer.util.OrcTypeUtils.resolveOrcValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class OrcTypeUtilsTest {

    public final String TEST_STRING= "struct<string_value:string,short_value:smallint,integer_value:int,long_value:bigint,double_value:double,float_value:float>";

    @Test
    public void testResolveTypesAsString(){
        List<String> orgTypes = getOrgTypes(TEST_STRING);
        assertThat(orgTypes,notNullValue());
        assertThat(orgTypes.size(),is(6));
        assertThat(orgTypes.get(0),is("string"));
        assertThat(orgTypes.get(1),is("smallint"));
        assertThat(orgTypes.get(2),is("int"));
        assertThat(orgTypes.get(3),is("bigint"));
        assertThat(orgTypes.get(4),is("double"));
        assertThat(orgTypes.get(5),is("float"));
    }
    @Test
    public void testResolveOrcTypes(){
        List<String> orgTypes = getOrgTypes(TEST_STRING);
        Object orcValue = resolveOrcValue("string_value", orgTypes.get(0));
        assertThat(orcValue,instanceOf(Text.class));
        orcValue = resolveOrcValue("1",orgTypes.get(1));
        assertThat(orcValue,instanceOf(DoubleWritable.class));
        orcValue = resolveOrcValue("2",orgTypes.get(2));
        assertThat(orcValue,instanceOf(DoubleWritable.class));
        orcValue = resolveOrcValue("3",orgTypes.get(3));
        assertThat(orcValue,instanceOf(DoubleWritable.class));

    }
}
