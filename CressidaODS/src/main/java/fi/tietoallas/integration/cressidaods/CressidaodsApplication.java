package fi.tietoallas.integration.cressidaods;

/*-
 * #%L
 * cressidaods
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
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.cressidaods.config.CressidaOdsConfig;
import fi.tietoallas.integration.cressidaods.service.CressidaOdsImportService;
import fi.tietoallas.integration.metadata.jpalib.configuration.JpaDbConfig;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackageClasses ={JpaDbConfig.class,CressidaOdsConfig.class,DbConfiguration.class,CressidaOdsImportService.class,CressidaodsApplication.class,LoadService.class,TableRepository.class, ColumnRepository.class  })
@EnableJpaRepositories("fi.tietoallas.integration.metadata.jpalib")
@EnableAutoConfiguration
public class CressidaodsApplication implements CommandLineRunner {

    @Autowired
    private CressidaOdsImportService service;

	public static final String INTEGRATION_NAME="cressidaods";

	public static void main(String[] args) {
		SpringApplication.run(CressidaodsApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		service.initialSetup(INTEGRATION_NAME,true);
	}
}
