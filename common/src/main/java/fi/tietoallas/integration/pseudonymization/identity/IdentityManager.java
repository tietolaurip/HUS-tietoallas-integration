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

import java.util.List;
import java.util.Map;

/**
 * A common interface for identity manager implementations.
 */
public interface IdentityManager {

    /**
     * Pseudonymizes the given identity (e.g. SSN).
     *
     * @param identity the identity to pseudonymize
     * @return a pseudonymized identity
     */
    String pseudonymize(String identity);

    /**
     * Pseudonymizes the given identities (e.g. SSNs)
     *
     * @param identities the list of identities to pseudonymize
     * @return a map from identities their pseudonymized counterparts
     */
    Map<String, String> pseudonymize(List<String> identities);
}
