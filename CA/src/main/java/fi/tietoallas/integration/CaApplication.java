package fi.tietoallas.integration;

/*-
 * #%L
 * ca
 * %%
 * Copyright (C) 2017 - 2018 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
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
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.configuration.CADBConfig;
import fi.tietoallas.integration.common.repository.ColumnRepository;
import fi.tietoallas.integration.common.repository.TableRepository;
import fi.tietoallas.integration.repository.CaRepository;
import fi.tietoallas.integration.service.CALoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackageClasses ={CaRepository.class,CALoadService.class, ColumnRepository.class, CADBConfig.class,LoadService.class,  StatusDatabaseRepository.class,TableRepository.class, ColumnRepository.class})
@EnableJpaRepositories("fi.tietoallas.integration.common")
@EnableAutoConfiguration
public class CaApplication implements CommandLineRunner {

    @Autowired
    private CALoadService loadService;

	private Logger  logger = LoggerFactory.getLogger(CaApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CaApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
			logger.info("initial setup");
			loadService.initialSetup(true);

	}
}
