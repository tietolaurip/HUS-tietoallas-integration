package fi.tietoallas.integration.clinisoft;

import fi.tietoallas.integration.clinisoft.configuration.SourceDbConfiguration;
import fi.tietoallas.integration.clinisoft.repository.ClinisoftRepository;
import fi.tietoallas.integration.clinisoft.services.ClinisoftLoadService;
import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.common.utils.CommonConversionUtils;
import fi.tietoallas.integration.metadata.jpalib.configuration.JpaDbConfig;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
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

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;


@SpringBootApplication(scanBasePackageClasses ={JpaDbConfig.class,ClinisoftRepository.class, SourceDbConfiguration.class,ClinisoftLoadService.class, LoadService.class, DbConfiguration.class,StatusDatabaseRepository.class, TableRepository.class, ColumnRepository.class})
@EnableJpaRepositories("fi.tietoallas.integration.metadata.jpalib")
@EnableAutoConfiguration
public class ClinisoftIntegrationApplication implements CommandLineRunner {

    @Autowired
	private ClinisoftLoadService loadService;

    @Autowired
    private Environment environment;

	private Logger logger = LoggerFactory.getLogger(ClinisoftIntegrationApplication.class);

	public static final String HIVE_STOREGE_DB_NAME="varasto_clinisoft_historia_log";

	public static void main(String[] args) {
		SpringApplication.run(ClinisoftIntegrationApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
        String runType = environment.getProperty("fi.datalake.sourcedb.operation");
        Properties properties = new Properties();
        if(runType==null){
            return;
        }

         if (runType.equals("incremental_import")) {
            logger.info("starting incrementeal load");
            loadService.incrementalLoad(convertTableName(environment.getProperty("COMP_NAME")));
            logger.info("incremental load completed");
        } else if (runType.equals("initial_setup")) {
            logger.info("initial setup");
            loadService.initialSetup(convertTableName(environment.getProperty("COMP_NAME")),true);
            logger.info("initial setup complete");
        }
	 else {
            logger.error("Unknown run type");
        }
	}
}
