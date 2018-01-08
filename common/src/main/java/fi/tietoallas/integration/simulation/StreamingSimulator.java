package fi.tietoallas.integration.simulation;

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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamingSimulator implements Simulator, Runnable {

    /** The HTTP client to use. */
    private HttpClient httpClient;

    /** The simulation config to use. */
    private SimulationConfig simulationConfig;

    /** The source files to use. */
    private List<String> sourceMessages;

    /** The target endpoint. */
    private String endpoint;

    public StreamingSimulator(SimulationConfig config, Path sourceFiles, String endpoint) {
        this.httpClient = HttpClients.createDefault();
        this.simulationConfig = config;
        this.endpoint = endpoint;

        this.sourceMessages = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceFiles)) {
            for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                byte[] encoded = Files.readAllBytes(path);
                sourceMessages.add(new String(encoded, "UTF-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void simulate() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < simulationConfig.getThreads(); i++) {
            Thread thread = new Thread(this);
            thread.start();
            threads.add(thread);
        }
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) { /* just ignore */}
        }
    }

    @Override
    public void run() {
        int currentIteration = 0;
        try {
            while (shouldContinue(currentIteration)) {
                if (simulationConfig.getDelay() > 0) {
                    Thread.sleep(simulationConfig.getDelay());
                }
                HttpPost post = new HttpPost(endpoint);
                post.setHeader("Content-Type", "application/xml");
                post.setHeader("charset", "utf-8");
                post.setEntity(new StringEntity(nextMessage(currentIteration), ContentType.APPLICATION_XML));
                httpClient.execute(post);
                currentIteration++;
            }
        } catch (Exception ie) { /* just exit */ }
    }

    private String nextMessage(int currentIteration) {
        if (simulationConfig.getDuration() == SimulationDuration.CONSUME_ALL) {
            return sourceMessages.get(currentIteration);
        } else {
            return sourceMessages.get((new Random()).nextInt(sourceMessages.size()));
        }
    }

    private boolean shouldContinue(int currentIteration) {
        SimulationDuration duration = simulationConfig.getDuration();
        if (duration == SimulationDuration.FIXED) {
            return currentIteration < simulationConfig.getIterations();
        } else if (duration == SimulationDuration.CONSUME_ALL) {
            return currentIteration < sourceMessages.size();
        } else {
            return true;
        }
    }
}
