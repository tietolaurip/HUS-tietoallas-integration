package fi.tietoallas.integration.clinisoft.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:/opt/DataLake/config/kafka.properties")
public class KafkaPropertySource {
}
