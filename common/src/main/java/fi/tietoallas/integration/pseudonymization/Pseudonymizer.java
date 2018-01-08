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

/**
 * A common interface for pseudonymizers.
 *
 * @param <T1> the type of the source object
 * @param <T2> the type of the result object
 */
public interface Pseudonymizer<T1, T2> {

    /**
     * Pseudonymizes the given source object.
     *
     * Note that the implementations are allowed to mutate the source object.
     *
     * @param source the source object to pseudonymize
     * @return a pseudonymized version of the source object
     */
    T2 pseudonymize(T1 source);
}
