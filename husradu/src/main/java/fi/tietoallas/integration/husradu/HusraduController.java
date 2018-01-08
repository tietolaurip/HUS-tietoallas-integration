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

import fi.tietoallas.integration.exception.ParseException;
import fi.tietoallas.monitoring.TaggedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HusraduController {

    private static TaggedLogger logger = new TaggedLogger(HusraduController.class, "husradu");

    @Autowired
    private HusraduService husraduService;

    @RequestMapping(method = RequestMethod.POST, path = "/receive")
    public ResponseEntity<String> receive(@RequestBody String value){
        try {
            husraduService.receive(value);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ParseException pe) {
            logger.error(pe.getMessage(), pe);
            return new ResponseEntity<>(pe.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            logger.error("Unable to process message.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
