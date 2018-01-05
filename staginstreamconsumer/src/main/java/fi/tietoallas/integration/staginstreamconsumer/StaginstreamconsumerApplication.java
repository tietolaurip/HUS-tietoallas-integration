
package fi.tietoallas.integration.staginstreamconsumer;

import static org.apache.commons.lang3.StringUtils.trim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class StaginstreamconsumerApplication {

	private static Logger LOG = LoggerFactory.getLogger(StaginstreamconsumerApplication.class);

	public static void main(String[] args) {
		try {
			String metadataDbUrl = trim(args[0]);
			String metadataUsername = trim(args[1]);
			String metadataPassword = trim(args[2]);
			String pseudoUrl = trim(args[3]);
			String kafkaIp = trim(args[4]);
			String groupId = trim(args[5]);

			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(metadataDbUrl);
			config.setUsername(metadataUsername);
			config.setPassword(metadataPassword);
			config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			HikariDataSource ds = new HikariDataSource(config);

			Map<String, String> pseudoinfo = getPseudoinfo(ds.getConnection());
			List<TopicPartitionOffset> topics = getTopicOffsets(ds.getConnection(), groupId);
			LOG.info("============= Found offsets for topics and partitions {}", topics);

			StaginStreamConsumer staginStreamConsumer = new StaginStreamConsumer();
			staginStreamConsumer.startStreamer(pseudoinfo, pseudoUrl, groupId, kafkaIp, topics, metadataUsername, metadataDbUrl, metadataPassword);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static List<TopicPartitionOffset> getTopicOffsets(Connection connection, String consumerGroup) {
		List<TopicPartitionOffset> offsets = new ArrayList<>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * from kafka_offset_info where group_id = ?");
			statement.setString(1, consumerGroup);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				offsets.add(new TopicPartitionOffset(resultSet.getString("topic"), resultSet.getInt("partition"), resultSet.getLong("offset")));
			}
			resultSet.close();
			statement.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return offsets;
	}

	private static Map<String, String> getPseudoinfo(Connection connection) {
		Map<String, String> pseudoInformations = new HashMap<>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM data_column");
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String key = resultSet.getString("data_set_name") + "." + resultSet.getString("data_table_name") + "."
						+ resultSet.getString("orig_column_name");
				pseudoInformations.put(key.toLowerCase(), resultSet.getString("pseudonymization_function"));
			}
			resultSet.close();
			statement.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return pseudoInformations;
	}

}
