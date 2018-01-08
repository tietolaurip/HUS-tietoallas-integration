package fi.tietoallas.integration.pseudonymization;

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

import fi.tietoallas.integration.pseudonymization.identity.IdentityManager;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An abstract base class for pseudonymizers.
 */
public abstract class AbstractPseudonymizer<T1, T2> implements Pseudonymizer<T1, T2> {

    /** A map from identifier to pseudonymization rules. */
    protected Map<String, List<PseudonymizerRule>> rules;

    /** An identity manager. */
    protected IdentityManager identityManager;

    /** Creates a new instance. */
    public AbstractPseudonymizer(IdentityManager identityManager, InputStream rules) {
        this.identityManager = identityManager;
        this.rules = PseudonymizerRule.parseMap(rules);
    }

    public IdentityManager getIdentityManager() {
        return identityManager;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }
}
