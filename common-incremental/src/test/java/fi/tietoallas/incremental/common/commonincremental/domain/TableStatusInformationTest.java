package fi.tietoallas.incremental.common.commonincremental.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(JUnit4.class)
public class TableStatusInformationTest {

    private static final Long CONST_INTERVAL=5L;
    private static final Timestamp CONST_LAST_ACCESSED_AT=Timestamp.valueOf(LocalDateTime.of(2000,1,1,1,1));
    private static final BigDecimal CONST_LAST_USED_VALUE=new BigDecimal(20000);

    @Test
    public void builderTest(){
        TableStatusInformation tableStatusInformation = new TableStatusInformation.Builder()
                .withColumnQuery("columnQuery")
                .withInterval(CONST_INTERVAL)
                .withKeyColumn("keyColumn")
                .withLassAccessAt(CONST_LAST_ACCESSED_AT)
                .withLastUsedValue(CONST_LAST_USED_VALUE)
                .withOriginalDatabase("originalDatabase")
                .withParameterType("parameterType")
                .withQuery("query")
                .withSearchColumn("searchColumn")
                .withTableName("tableName")
                .withTimeColumn("timeColumn")
                .build();
        assertThat(tableStatusInformation.columnQuery,is("columnQuery"));
        assertThat(tableStatusInformation.interval,is(CONST_INTERVAL));
        assertThat(tableStatusInformation.keyColumn,is("keyColumn"));
        assertThat(tableStatusInformation.lastAccessAt,is(CONST_LAST_ACCESSED_AT));
        assertThat(tableStatusInformation.lastUsedValue,is(CONST_LAST_USED_VALUE));
        assertThat(tableStatusInformation.originalDatabase,is("originalDatabase"));
        assertThat(tableStatusInformation.parameterType,is("parameterType"));
        assertThat(tableStatusInformation.query,is("query"));
        assertThat(tableStatusInformation.searchColumn,is("searchColumn"));
        assertThat(tableStatusInformation.tableName,is("tableName"));
        assertThat(tableStatusInformation.timeColumn,is("timeColumn"));
    }
}
