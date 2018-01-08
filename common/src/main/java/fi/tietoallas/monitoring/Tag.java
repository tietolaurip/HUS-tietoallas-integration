package fi.tietoallas.monitoring;

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
 * A tag used in logging.
 *
 * Tags make it easier to interpret and process an aggregated log stream.
 */
public enum Tag {

    /** A default tag to use when none of the others are appropriate. */
    DL_DEFAULT,

    /** Statistics */
    DL_STAT_INFO,

    /** An internal connection failure (e.g. HTTP call to pseudo-proxy failed) */
    DL_INTERNAL_CONNECTION_FAILURE,

    /** An external connection failure (e.g. source system database not responding) */
    DL_EXTERNAL_CONNECTION_FAILURE,

    /** Out of memory */
    DL_OUT_OF_MEMORY
}
