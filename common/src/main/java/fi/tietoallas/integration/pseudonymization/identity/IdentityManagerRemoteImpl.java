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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An identity manager that connects to a remote identity management service.
 */
public class IdentityManagerRemoteImpl implements IdentityManager {

    /** The HTTP client to use. */
    private HttpClient httpClient;

    /** The remote endpoint to connect to. */
    private String remoteEndpoint;

    /** The scope to use. */
    private String scope;

    public IdentityManagerRemoteImpl(String remoteEndpoint, String scope) {
        this(HttpClients.createDefault(), remoteEndpoint, scope);
    }

    public IdentityManagerRemoteImpl(HttpClient httpClient, String remoteEndpoint, String scope) {
        this.httpClient = httpClient;
        this.remoteEndpoint = remoteEndpoint;
        this.scope = scope;
    }

    @Override
    public String pseudonymize(String identity) {
        return pseudonymize(Arrays.asList(identity)).entrySet().iterator().next().getValue();
    }

    @Override
    public Map<String, String> pseudonymize(List<String> identities) {
        try {
            if (identities == null || identities.isEmpty()){
                throw new RuntimeException("Unable to pseudonymize an empty identity list.");
            }

            HttpPost httpPost = new HttpPost(remoteEndpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("charset", "utf-8");

            JsonObject request = new JsonObject();
            JsonArray identityArray = new JsonArray();
            identities.forEach(i -> identityArray.add(new JsonPrimitive(i)));
            request.add("personalId", identityArray);
            request.addProperty("scope", scope);

            StringEntity stringEntity = new StringEntity(request.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpClient.execute(httpPost);
            String handleResponse = new BasicResponseHandler().handleResponse(response);
            return new Gson().fromJson(handleResponse, Map.class);

        } catch (Exception e) {
            throw new IdentityManagerException("Failed to pseudonymize the given identity list.", e);
        }
    }
}
