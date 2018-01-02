package fi.tietoallas.integration.load.service;

/*-
 * #%L
 * copytool
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
import fi.tietoallas.integration.load.domain.TableLoadInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Main class which is run as Spark job
 *
 * @author xxkallia
 */

public class CopyToHDFS {

    static SparkSession session;
    static SQLContext sqlContext;
    static JavaSparkContext context;
    private static Logger LOG = LoggerFactory.getLogger(CopyToHDFS.class);


    public CopyToHDFS() {
        session = SparkSession.builder()
                .appName("CopyToHDFS")
                .enableHiveSupport()
                .getOrCreate();
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(timeZone);
        sqlContext = session.sqlContext();
        context = new JavaSparkContext(session.sparkContext());

    }

    public void copyTable(final TableLoadInfo table,  final String integration, final
    String driver, String jdbcUrl, String userName, String password,  String currentDate,
                          final List<String> columnNames, final String path) {
        System.out.println(LocalDateTime.now().toString() + " [INFO ]       copyTable(" + integration + "): " + table.tableName);
        Map<String, String> options = new HashMap<>();
        options.put("driver", driver);
        options.put("url", jdbcUrl);
        options.put("fetchsize","10000");
        if (table.customSql != null) {
            options.put("dbtable", table.customSql);
        } else {
            options.put("dbtable", table.tableName);
        }
        options.put("user", userName);
        options.put("password", password);
        if (StringUtils.isNotEmpty(table.lowerBound) && StringUtils.isNotEmpty(table.upperBound) && StringUtils.isNotEmpty(table.partitionColumn)) {
            options.put("lowerBound", table.lowerBound);
            options.put("upperBound", table.upperBound);
            options.put("partitionColumn", table.partitionColumn);
            options.put("numPartitions", table.numPartitions);
        }
        Dataset<Row> dataset = null;
        dataset = sqlContext.read().format("jdbc").options(options).load();
        dataset =  dataset.select(columnNames.get(0),columnNames.subList(1,columnNames.size()).toArray(new String[0]));
        List<String> fieldNames = Arrays.stream(dataset.schema().fieldNames()).map(s -> convertTableName(s))
                .collect(toList());

        List<String> validated = getNormalizedFields(fieldNames);
        dataset= renameDataset(dataset, validated);
        dataset = dataset.withColumn("allas__pvm_partitio", functions.lit(currentDate));
        String fullPath =null;

        if (path == null) {
            fullPath="adl:///staging/" + integration + "/" + convertTableName(table.tableName);
        } else {
            fullPath="adl:///staging/" + integration + "/" +path +"/"+ convertTableName(table.tableName);
        }
        System.out.println(LocalDateTime.now().toString() + " [INFO ]       Start loading to adl: " + fullPath);
        dataset.write().format("orc").option("compression", "zlib").mode(SaveMode.Append).orc(fullPath);
        System.out.println(LocalDateTime.now().toString() + " [INFO ]       Done loading to adl: " +fullPath);

    }


    private static Dataset<Row> renameDataset(Dataset<Row> dataset, List<String> reservedFileldNames) {
        Dataset<Row> df = dataset;
        for (String name : reservedFileldNames) {
            df = df.withColumnRenamed(name, convertTableName(name));
        }
        return df;
    }

    private final List<String> getNormalizedFields(List<String> fieldNames) {
        List<String> reservedWords = Arrays.asList("primary", "section");
        return fieldNames.stream()
                .map(s -> reservedWords.stream().anyMatch(x -> x.equalsIgnoreCase(s)) ? ("ref_"+s) :s)
                .collect(toList());
    }


    public static String convertTableName(final String originalTableName) {
        if (StringUtils.isNotBlank(originalTableName)) {
            return originalTableName.
                    replace(" ", "_").
                    replace("-", "_").
                    replace("Ä", "a").
                    replace("ä", "a").
                    replace("Å", "a").
                    replace("å", "a").
                    replace("Ö", "o").
                    replace("ö", "o").toLowerCase();
        }
        return null;
    }

}
