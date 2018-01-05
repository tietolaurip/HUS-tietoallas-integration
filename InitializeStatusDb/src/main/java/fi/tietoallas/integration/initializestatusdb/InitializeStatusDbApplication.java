package fi.tietoallas.integration.initializestatusdb;

/*-
 * #%L
 * initialize-status-db
 * %%
 * Copyright (C) 2017 - 2018 Pivotal Software, Inc.
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
import fi.tietoallas.integration.initializestatusdb.service.InitializeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class InitializeStatusDbApplication implements CommandLineRunner{

	@Autowired
	private InitializeService initializeService;
	@Autowired
    private Environment environment;
	public static void main(String[] args) {
		SpringApplication.run(InitializeStatusDbApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		String integration = environment.getProperty("fi.datalake.init.integration");
		String originalDatabase =environment.getProperty("fi.datalake.init.originaldbname");
		boolean updateColumnQuery = environment.getProperty("fi.datalake.init.useHiveColumns") != null;
		initializeService.initializeData(integration,originalDatabase,updateColumnQuery);

	}
}
