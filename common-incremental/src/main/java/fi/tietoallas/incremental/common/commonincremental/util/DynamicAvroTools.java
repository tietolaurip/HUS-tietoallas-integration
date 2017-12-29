/*-
 * #%L
 * common-incremental
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

package fi.tietoallas.incremental.common.commonincremental.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.tietoallas.incremental.common.commonincremental.domain.SchemaGenerationInfo;

/**
 * Class with methods creating Avro schema from SchemaGenertaion info
 *
 * @author Antti Kalliokoski
 * @author Tuukka Arola
 */
public class DynamicAvroTools {

	public static final String ROWS_NAME = "rows";

	private static Logger logger = LoggerFactory.getLogger(DynamicAvroTools.class);

    /**
     *
     * @param integration Name of integration
     * @param info schema information to generate record record
     * @return Schema as one string
     */
	public static String toIntegrationAvroSchema(String integration, SchemaGenerationInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"type\":\"record\",");
		buffer.append("\"name\":\"" + integration + "\",");
		buffer.append("\"namespace\" : \"fi.tietoallas.integration\",");
		buffer.append("\"fields\":[{");
		buffer.append("\"name\":\"");
		buffer.append(ROWS_NAME);
		buffer.append("\",\"type\":{\"type\":\"array\",\"items\":");
		buffer.append(toAvroSchema(info));
		buffer.append("}}]}");
		logger.debug("====== Avro schema: "+ buffer.toString());
		return buffer.toString();
	}

    /**
     *
     * @param info information of rows to generate schema for them
     * @return schema for all to rows
     */

	public static String toAvroSchema(SchemaGenerationInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{\"type\":\"record\", \"name\":\"");
		buffer.append(CommonConversionUtils.convertTableName(info.table));
		buffer.append("\", \"namespace\":\"fi.tietoallas.integration\", \"fields\":[");
		for (int i = 0; i < info.names.size(); i++) {
			buffer.append("{\"name\":\"");
			buffer.append(CommonConversionUtils.convertTableName(info.names.get(i)));
			buffer.append("\",\"type\":[\"null\",\"");
			buffer.append(info.types.get(i));
			buffer.append("\"]");
			if (info.params.get(i) != null) {
				for (Map.Entry<String, String> param : info.params.get(i).entrySet()) {
					buffer.append(",\"");
					buffer.append(param.getKey());
					buffer.append("\":\"");
					buffer.append(param.getValue());
					buffer.append("\"");
				}
			}
			buffer.append(",\"default\":null}");
			if (info.names.size() > i + 1) {
				buffer.append(",");
			}
		}

		buffer.append("]}");
		return buffer.toString();
	}
}
