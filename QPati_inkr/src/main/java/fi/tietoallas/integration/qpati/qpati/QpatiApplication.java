package fi.tietoallas.integration.qpati.qpati;

/*-
 * #%L
 * qpati
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
import fi.tietoallas.integration.qpati.qpati.config.MetadataDbConfig;
import fi.tietoallas.integration.qpati.qpati.repository.MetadataRepository;
import fi.tietoallas.integration.qpati.qpati.service.FileWalker;
import fi.tietoallas.integration.qpati.qpati.service.SftpFileHandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackageClasses = { MetadataDbConfig.class, MetadataRepository.class, FileWalker.class,
		SftpFileHandler.class })
@EnableScheduling
public class QpatiApplication {

	public static final String INTEGRATION_NAME = "qpati";

	public static void main(String[] args) {
		SpringApplication.run(QpatiApplication.class, args);
	}

}
