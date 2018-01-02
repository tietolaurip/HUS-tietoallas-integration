package fi.tietoallas.integration.incremental.cressidaods;

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
import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.incremental.cressidaods.config.SourceDbConfig;
import fi.tietoallas.integration.incremental.cressidaods.repository.CressidaOdsRepository;
import fi.tietoallas.integration.incremental.cressidaods.service.CressidaOdsIncrementaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackageClasses = {SourceDbConfig.class,CressidaOdsRepository.class,CressidaOdsIncrementaService.class, StatusDatabaseRepository.class, DbConfiguration.class})
@EnableScheduling
public class CressidaodsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CressidaodsApplication.class, args);
	}
}
