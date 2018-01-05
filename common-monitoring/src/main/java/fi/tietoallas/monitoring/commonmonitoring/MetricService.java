package fi.tietoallas.monitoring.commonmonitoring;

/*-
 * #%L
 * common-monitoring
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
public interface MetricService {

        /**
         * Report the number of send bytes for the specified component (e.g. integration).
         *
         * @param component the component
         * @param value the number of send bytes
         */
        void reportSendBytes(final String component, long value);

        /**
         * Report the number of send bytes for the specified part of a component (e.g. a specific table of an integration).
         *
         * @param component the component
         * @param part the part
         * @param value the number of send bytes
         */
        void reportSendBytes(final String component, final String part, long value);

        /**
         * Report successful completion of a task (e.g. streaming integration processed an event).
         *
         * @param component the component
         */
        void reportSuccess(final String component);

        /**
         * Report successfult completion of a part of a task.
         *
         * @param component the component
         * @param part the part
         */
        void reportSuccess(final String component, final String part);
}
