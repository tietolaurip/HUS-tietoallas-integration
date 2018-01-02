/*-
 * #%L
 * ca-incremental-import
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

package fi.tietoallas.integration.ca.caincrementalimport;

import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.ca.caincrementalimport.configuration.CaDbConfig;
import fi.tietoallas.integration.ca.caincrementalimport.repository.CaRepository;
import fi.tietoallas.integration.ca.caincrementalimport.service.CaIncrementalLoadService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackageClasses = {CaIncrementalLoadService.class, CaRepository.class, CaDbConfig.class,  StatusDatabaseRepository.class, DbConfiguration.class})
@EnableScheduling
public class CaIncrementalImportApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaIncrementalImportApplication.class, args);
	}
}
