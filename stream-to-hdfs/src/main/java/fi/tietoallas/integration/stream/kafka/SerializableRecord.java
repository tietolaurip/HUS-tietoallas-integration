package fi.tietoallas.integration.stream.kafka;

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


import org.apache.spark.sql.Row;
import scala.Tuple4;
import scala.Tuple6;

import java.io.Serializable;
import java.util.List;

/**
 * Kafka's ConsumerRecord is not serializable and this can cause some headaches with Spark. To avoid these issues, our
 * streaming related interfaces use this domain specific record instead.
 */
public class SerializableRecord implements Serializable {

    Tuple4<Row, String, Integer, Long> data;

    public SerializableRecord(Row row, String topic, Integer partition, Long offset) {
        this.data = new Tuple4<>(row, topic, partition, offset);
    }

    public Row getRow() {
        return this.data._1();
    }

    public String getTopic() {
        return this.data._2();
    }

    public Integer getPartition() {
        return this.data._3();
    }

    public Long getOffset() {
        return this.data._4();
    }

}
