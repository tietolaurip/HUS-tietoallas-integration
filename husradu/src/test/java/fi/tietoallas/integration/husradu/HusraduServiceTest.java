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

import com.google.gson.JsonParser;
import fi.tietoallas.integration.mq.MessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HusraduServiceTest {

    @Autowired
    private HusraduService husraduService;

    private MessageProducerStub messageProducer = new MessageProducerStub();

    @Before
    public void setUp() {
        husraduService.setMessageProducer(messageProducer);
    }

    @Test
    public void testReceive() throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(getClass().getResource("/tutkimus3900.json").toURI()));
        String message = new String(encoded, "UTF-8");
        husraduService.receive(message);

        // Check that data was written to the raw data queue
        new JsonParser().parse(messageProducer.latestValues.get("husradu-orig")).getAsJsonObject();
        assertTrue(messageProducer.latestKeys.get("husradu-orig").startsWith("husradu;3900;"));
    }

    public class MessageProducerStub implements MessageProducer {

        Map<String, String> latestKeys = new HashMap<>();
        Map<String, String> latestValues = new HashMap<>();

        @Override
        public void produce(String topic, String key, String value) {
            this.latestKeys.put(topic, key);
            this.latestValues.put(topic, value);
        }
    }
}
