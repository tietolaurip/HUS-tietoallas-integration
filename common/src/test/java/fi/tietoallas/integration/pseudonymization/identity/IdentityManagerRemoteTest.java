package fi.tietoallas.integration.pseudonymization.identity;

/*-
 * #%L
 * common
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

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IdentityManagerRemoteTest {

    @Test
    public void testIdentityManager() throws Exception {

        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        StringEntity entity = new StringEntity("{\"021197-967C\": \"foobar\"}", ContentType.APPLICATION_JSON);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        IdentityManager identityManager =
                new IdentityManagerRemoteImpl(
                        httpClient,
                        "http://foobar.hustietoallastesti.fi",
                        "test");

        assertEquals("foobar", identityManager.pseudonymize("021197-967C"));
    }

}
