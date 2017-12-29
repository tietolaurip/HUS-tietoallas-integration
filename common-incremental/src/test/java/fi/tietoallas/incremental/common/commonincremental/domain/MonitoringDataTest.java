package fi.tietoallas.incremental.common.commonincremental.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(JUnit4.class)
public class MonitoringDataTest {

    private static int CONST_SEND_BYTES=5;

    @Test
    public void testConstructor(){
        MonitoringData monitoringData = new MonitoringData("integration","table",CONST_SEND_BYTES);
        assertThat(monitoringData.integration,is("integration"));
        assertThat(monitoringData.table,is("table"));
        assertThat(monitoringData.sendBytes,is(CONST_SEND_BYTES));

    }
}
