package fi.tietoallas.integration;

/*-
 * #%L
 * opera
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
import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.configuration.SourceDbConfiguration;
import fi.tietoallas.integration.metadata.jpalib.configuration.JpaDbConfig;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import fi.tietoallas.integration.repository.OperaRepository;
import fi.tietoallas.integration.service.OperaImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication(scanBasePackageClasses ={JpaDbConfig.class,OperaRepository.class,OperaImportService.class, ColumnRepository.class, SourceDbConfiguration.class,LoadService.class,  DbConfiguration.class,StatusDatabaseRepository.class,TableRepository.class, ColumnRepository.class})
@EnableJpaRepositories("fi.tietoallas.integration.metadata.jpalib")
@EnableAutoConfiguration
public class OperaApplication implements CommandLineRunner{


	@Autowired
	private OperaImportService loadService;

	private Logger logger = LoggerFactory.getLogger(OperaApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(OperaApplication.class, args);
	}

	@Override
	public void run(String... strings){
		try {
			logger.info("=======================Starting========================");

				loadService.initialSetup(OperaImportService.INTEGRATION_NAME, true);
				logger.info("initial setup complete");


		} catch (Exception e){
			logger.error("error in opera integration",e);
		}
	}
}
