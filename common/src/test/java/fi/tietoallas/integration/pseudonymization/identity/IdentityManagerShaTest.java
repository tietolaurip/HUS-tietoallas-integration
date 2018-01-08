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

import org.junit.Test;

import static org.junit.Assert.*;

public class IdentityManagerShaTest {

    @Test
    public void testIdentityManager() throws Exception {
        IdentityManager identityManager = new IdentityManagerShaImpl();
        assertEquals("sZgHs8VMrRbLnnaxA2bfA0WaOgZ9s6zQO8BfbbQAOJ4=", identityManager.pseudonymize("021197-967C"));
    }
}
