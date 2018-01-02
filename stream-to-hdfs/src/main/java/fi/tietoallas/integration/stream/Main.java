package fi.tietoallas.integration.stream;

/*-
 * #%L
 * stream-to-hdfs
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fi.tietoallas.integration.stream.kafka.KafkaStreamManager;
import fi.tietoallas.integration.stream.kafka.KafkaStreamManagerImpl;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import static org.apache.commons.lang3.StringUtils.trim;

public class Main {

    public static void main(String[] args) throws Exception {

        String statusDb = trim(args[0]);
        String statusDbUsername = trim(args[1]);
        String statusDbPassword = trim(args[2]);

        String kafkaIp = trim(args[3]);
        String groupId = trim(args[4]);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(statusDb);
        config.setUsername(statusDbUsername);
        config.setPassword(statusDbPassword);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        HikariDataSource ds = new HikariDataSource(config);

        KafkaStreamManager streamManager = new KafkaStreamManagerImpl(kafkaIp, groupId, ds);
        SparkSession session = SparkSession.builder().appName("stream-to-hdfs").getOrCreate();
        JavaSparkContext javaSparkContext = new JavaSparkContext(session.sparkContext());
        JavaStreamingContext streamingContext = new JavaStreamingContext(javaSparkContext, Durations.minutes(15));
        StreamConsumer streamConsumer = new StreamConsumer(streamingContext);

        String outputLocation = "adl://";
        streamConsumer.setupStreaming(streamManager, outputLocation);

        streamingContext.start();
        streamingContext.awaitTermination();
    }

}
