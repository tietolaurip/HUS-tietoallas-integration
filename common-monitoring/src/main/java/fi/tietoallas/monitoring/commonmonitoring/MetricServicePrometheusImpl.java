package fi.tietoallas.monitoring.commonmonitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Prometheus PushGateway backed metric service.
 *
 * The implementation uses an executor service to push the metrics to guard the monitored process from
 * the latency caused by the internal HTTP call. A call to PushGateway::pushAdd will block for 10s if the
 * push gateway is down and tens of milliseconds it's up.
 */

public class MetricServicePrometheusImpl implements MetricService{
    /** The logger */
    private static TaggedLogger logger = new TaggedLogger(MetricServicePrometheusImpl.class);

    /** The Prometheus push-gateway to use */
    private ThreadLocal<PushGateway> gateway;

    /** The executor service */
    private ExecutorService executor;

    public MetricServicePrometheusImpl(final String gatewayAddress) {
        this.gateway = ThreadLocal.withInitial(() -> new PushGateway(gatewayAddress));
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void reportSendBytes(String component, long value) {
        executor.submit(() -> {
            CollectorRegistry registry = new CollectorRegistry();
            Gauge event = Gauge.build()
                    .name("number_of_send_bytes")
                    .help("Number of processed bytes")
                    .labelNames("instance")
                    .register(registry);
            event.labels(component).set((double) value);
            reportMetric(registry, component);
        });
    }

    @Override
    public void reportSendBytes(String component, String part, long value) {
        executor.submit(() -> {
            CollectorRegistry registry = new CollectorRegistry();
            Gauge event = Gauge.build()
                    .name("number_of_send_bytes")
                    .help("Number of processed bytes")
                    .labelNames("instance", "table")
                    .register(registry);
            event.labels(component, part).set((double) value);
            reportMetric(registry, component);
        });
    }

    @Override
    public void reportSuccess(String component) {
        executor.submit(() -> {
            CollectorRegistry registry = new CollectorRegistry();
            Gauge event = Gauge.build()
                    .name("last_success")
                    .help("Last successful completion of a process.")
                    .labelNames("instance")
                    .register(registry);
            event.labels(component).setToCurrentTime();
            reportMetric(registry, component);
        });
    }

    @Override
    public void reportSuccess(String component, String part) {
        executor.submit(() -> {
            CollectorRegistry registry = new CollectorRegistry();
            Gauge event = Gauge.build()
                    .name("last_success")
                    .help("Last successful completion of a process.")
                    .labelNames("instance", "table")
                    .register(registry);
            event.labels(component, part).setToCurrentTime();
            reportMetric(registry, component);
        });
    }

    /**
     * Reports the specified metrics to Prometheus PushGateway.
     *
     * @param registry the registry
     * @param job the job name
     */
    private void reportMetric(CollectorRegistry registry, String job) {
        try {
            gateway.get().pushAdd(registry, job);
        } catch (IOException e) {
            logger.error("Failed to push metrics to Prometheus PushGateway.");
        }
    }
}
