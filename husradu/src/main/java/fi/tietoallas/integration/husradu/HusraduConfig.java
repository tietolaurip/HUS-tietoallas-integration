package fi.tietoallas.integration.husradu;

/*-
 * #%L
 * husradu-integration
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

import fi.tietoallas.integration.mq.MessageProducer;
import fi.tietoallas.integration.mq.MessageProducerKafkaImpl;
import fi.tietoallas.monitoring.MetricService;
import fi.tietoallas.monitoring.MetricServicePrometheusImpl;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
public class HusraduConfig {

    @Autowired
    private Environment environment;

    public @Bean
    MessageProducer messageProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("kafka.bootstrap.servers"));
        properties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, environment.getProperty("kafka.max.request.size"));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        return new MessageProducerKafkaImpl(properties);
    }

    public @Bean MetricService metricService() {
        return new MetricServicePrometheusImpl(environment.getProperty("pushgateway.host"));
    }
}
