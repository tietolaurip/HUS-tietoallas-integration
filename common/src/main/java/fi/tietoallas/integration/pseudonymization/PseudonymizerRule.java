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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.*;


/**
 * A pseudonymization rule.
 */
public class PseudonymizerRule {

    /** The type of object this rule applies to */
    private String type;

    /** An extra condition that must match for the rule to get applied. */
    private List<String> conditions;

    /** A list of expressions to hash (e.g. field names or XPath expressions) */
    private List<String> hashedFields;

    /** A list of expressions to clear (e.g. field names or XPath expressions) */
    private List<String> clearedFields;

    /** A list of expressions to remove (e.g. field names or XPath expressions) */
    private List<String> removedFields;

    /**
     * Parses the rules from the given input stream.
     *
     * @param inputStream An input stream
     * @return a list of rules
     */
    public static List<PseudonymizerRule> parse(InputStream inputStream) {
        List<PseudonymizerRule> rules = new ArrayList<>();
        try {
            byte[] encoded = IOUtils.toByteArray(inputStream);
            JsonObject root = (JsonObject) new JsonParser().parse(new String(encoded, "UTF-8"));
            root.get("rules").getAsJsonArray().forEach(e -> {
                JsonObject json = e.getAsJsonObject();
                PseudonymizerRule rule = new PseudonymizerRule();
                rule.type = json.get("type").getAsString();
                if (json.has("condition")) {
                    List<String> condition = new LinkedList<>();
                    json.get("condition").getAsJsonArray().forEach(i -> condition.add(i.getAsString()));
                    rule.conditions = condition;
                }
                if (json.has("hash")) {
                    List<String> hash = new LinkedList<>();
                    json.get("hash").getAsJsonArray().forEach(i -> hash.add(i.getAsString()));
                    rule.hashedFields = hash;
                }
                if (json.has("null")) {
                    List<String> clear = new LinkedList<>();
                    json.get("null").getAsJsonArray().forEach(i -> clear.add(i.getAsString()));
                    rule.clearedFields = clear;
                }
                if (json.has("remove")) {
                    List<String> remove = new LinkedList<>();
                    json.get("remove").getAsJsonArray().forEach(i -> remove.add(i.getAsString()));
                    rule.removedFields = remove;
                }
                rules.add(rule);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rules;
    }

    /**
     * Parses the rules from the given input stream.
     *
     * @param inputStream An input stream
     * @return a map from type to the corresponding pseudonymization rule
     */
    public static Map<String, List<PseudonymizerRule>> parseMap(InputStream inputStream) {
        List<PseudonymizerRule> rules = parse(inputStream);
        Map<String, List<PseudonymizerRule>> result = new HashMap<>();
        for (PseudonymizerRule rule : rules) {
            if (result.containsKey(rule.getType())) {
                result.get(rule.getType()).add(rule);
            } else {
                List<PseudonymizerRule> list = new ArrayList<>();
                list.add(rule);
                result.put(rule.getType(), list);
            }
        }
        return result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public boolean hasConditions() {
        return conditions != null && !conditions.isEmpty();
    }

    public List<String> getHashedFields() {
        return hashedFields;
    }

    public void setHashedFields(List<String> hashedFields) {
        this.hashedFields = hashedFields;
    }

    public List<String> getClearedFields() {
        return clearedFields;
    }

    public void setClearedFields(List<String> clearedFields) {
        this.clearedFields = clearedFields;
    }

    public List<String> getRemovedFields() {
        return removedFields;
    }

    public void setRemovedFields(List<String> removedFields) {
        this.removedFields = removedFields;
    }
}
