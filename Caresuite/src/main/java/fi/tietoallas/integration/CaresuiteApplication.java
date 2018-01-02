package fi.tietoallas.integration;

import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.configuration.MultibleSqlServerConfiguration;
import fi.tietoallas.integration.metadata.jpalib.configuration.JpaDbConfig;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import fi.tietoallas.integration.repository.CareSuiteRepository;
import fi.tietoallas.integration.service.CaresuiteLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;
@SpringBootApplication(scanBasePackageClasses ={JpaDbConfig.class,CareSuiteRepository.class,CaresuiteLoadService.class, ColumnRepository.class, MultibleSqlServerConfiguration.class,LoadService.class,  DbConfiguration.class,StatusDatabaseRepository.class,TableRepository.class, ColumnRepository.class})
@EnableJpaRepositories("fi.tietoallas.integration.metadata.jpalib")
@EnableAutoConfiguration
public class CaresuiteApplication implements CommandLineRunner {

    @Autowired
    private CaresuiteLoadService caresuiteLoadService;
    @Autowired
    private Environment environment;




    private static Logger logger = LoggerFactory.getLogger(CaresuiteApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CaresuiteApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


        String runType = environment.getProperty("fi.datalake.sourcedb.operation");
        
        if (runType==null || runType.equals("incremental_import")) {
            Properties props = new Properties();
            props.put("bootstrap.servers", environment.getProperty("bootstrap.servers"));
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
            logger.info("starting incrementeal load");;
            logger.info("incremental load completed");
        } else if (runType.equals("initial_setup")) {
            logger.info("initial setup");
            caresuiteLoadService.initialSetup(environment.getProperty("COMP_NAME").toLowerCase(),false);
            logger.info("initial setup complete");

        } else {
            logger.error("Unknown run type");
        }

    }
}
