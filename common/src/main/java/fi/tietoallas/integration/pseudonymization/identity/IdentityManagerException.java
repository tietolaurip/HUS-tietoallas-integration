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

/**
 * An exception indicating a failure in resolving the pseudo-identity.
 */
public class IdentityManagerException extends RuntimeException {

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param cause the root cause
     */
    public IdentityManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
