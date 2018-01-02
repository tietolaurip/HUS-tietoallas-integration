package fi.tietoallas.integration.load;

/*-
 * #%L
 * copytool
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class LoadToolApplicationTest {

    private static final String TEST_JDBC_URL_WITHOUT_DB="jdbc:sybase:Tds:127.0.0.1:5000?EnableBatchWorkaround=true;ENABLE_BULK_LOAD=true;ENABLE_SSL=true&SSL_TRUST_ALL_CERTS=true";
    private static final String TEST_JDBC_URL_WITH_TEST_DB="jdbc:sybase:Tds:127.0.0.1:5000/test?EnableBatchWorkaround=true;ENABLE_BULK_LOAD=true;ENABLE_SSL=true&SSL_TRUST_ALL_CERTS=true";

    @Test
    public void testJDBCURLParsing(){
        String jdbcurl = LoadToolApplication.addDatabaseToJDBCURL(TEST_JDBC_URL_WITHOUT_DB, "test");
        assertThat(jdbcurl,is(TEST_JDBC_URL_WITH_TEST_DB));
    }
}
