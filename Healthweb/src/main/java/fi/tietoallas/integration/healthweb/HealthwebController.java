package fi.tietoallas.integration.healthweb;

/*-
 * #%L
 * healthweb-integration
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

import fi.tietoallas.integration.exception.ParseException;
import fi.tietoallas.monitoring.TaggedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for receiving CDA R2 documents from Healthweb.
 */
@RestController
public class HealthwebController {

    private static TaggedLogger logger = new TaggedLogger(HealthwebController.class);

    @Autowired
    private HealthwebService messageService;

    @RequestMapping(method = RequestMethod.POST, path = "/kanta")
    public ResponseEntity<String> receiveMessage(@RequestBody String message){
        try {
            messageService.receive(message);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ParseException pe) {
            logger.error(pe.getMessage(), pe);
            return new ResponseEntity<>(pe.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            logger.error("Unable to process message.", e);
            // A more correct thing to do would be to return HttpStatus.INTERNAL_SERVER_ERROR but
            // the current version of BizTalk used by the source-system is unable to handle it.
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
