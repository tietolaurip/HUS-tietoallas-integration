package fi.tietoallas.incremental.common.commonincremental.monitoring;

import io.prometheus.client.Collector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(JUnit4.class)
public class IntegrationCollectionTest {

    @Test
    public void testAdd(){
        IntegrationCollection integrationCollection = new IntegrationCollection();
        assertThat(integrationCollection.collect(),hasSize(0));
        integrationCollection.add(
                new Collector.MetricFamilySamples("test", Collector.Type.GAUGE,"help", asList(new Collector.MetricFamilySamples.Sample("sample", asList("lables"), asList("labelValeus"),10.0))));
        assertThat(integrationCollection.collect(),hasSize(1));
    }
}
