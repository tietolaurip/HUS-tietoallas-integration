package fi.tietoallas.integration.husradu;

/*-
 * #%L
 * husradu-integration
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.tietoallas.integration.exception.ParseException;
import fi.tietoallas.integration.mq.MessageProducer;
import fi.tietoallas.monitoring.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * A service for receiving HUS RADU documents and passing them to the integration specific message queue.
 */
@Service
public class HusraduService {

    /** The name of the integration. */
    private final static String INTEGRATION_NAME = "husradu";

    /** The key separator. */
    private final static String KEY_SEPARATOR = ";";

    /** The message producer. */
    private MessageProducer messageProducer;

    /** The metric service */
    private MetricService metricService;

    @Autowired
    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Autowired
    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * Receives a HUS RADU JSON object.
     *
     * @param value the HUS RADU JSON object as a string
     */
    public void receive(final String value) throws Exception {

        JsonObject object;
        try {
            object = new JsonParser().parse(value).getAsJsonObject();
        } catch (Exception e) {
            throw new ParseException("Unable to parse HUS RADU object: " + e.getMessage(), e);
        }

        Instant timestamp = Instant.now();
        String key = INTEGRATION_NAME + KEY_SEPARATOR +
                     object.getAsJsonObject("Tutkimus").get("InstanceId").getAsString() + KEY_SEPARATOR +
                     timestamp.toString();

        // Write the original message to the "raw data" topic
        messageProducer.produce(INTEGRATION_NAME + "-orig", key, value);
        metricService.reportSendBytes(INTEGRATION_NAME, value.getBytes().length);
    }

}
