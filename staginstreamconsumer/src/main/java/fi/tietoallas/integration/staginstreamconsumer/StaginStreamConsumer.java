package fi.tietoallas.integration.staginstreamconsumer;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avro.Conversions.DecimalConversion;
import org.apache.avro.LogicalTypes;
import org.apache.avro.LogicalTypes.Decimal;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import fi.tietoallas.integration.Pseudonymization;
import fi.tietoallas.integration.domain.SsnPseudonymizationRequest;
import scala.Tuple2;
import scala.Tuple6;

public class StaginStreamConsumer {

	private static final long MILLIS_IN_DAY = 1000L * 60 * 60 * 24;

	private static JavaSparkContext javaSparkContext;
	private static JavaStreamingContext ssc;
	private static HikariDataSource dataSource;

	private static final String METADATA_SEPARATOR = ";";

	private static Logger logger = LoggerFactory.getLogger(StaginStreamConsumer.class);

	public StaginStreamConsumer() {
		SparkSession session = SparkSession.builder().appName("JavaKafkaStreamConsumer").config("spark.streaming.kafka.maxRatePerPartition", "2").getOrCreate();
		javaSparkContext = new JavaSparkContext(session.sparkContext());
		ssc = new JavaStreamingContext(javaSparkContext, Durations.seconds(60));
		dataSource = new HikariDataSource();
	}

	public void startStreamer(Map<String, String> pseudoInformations, String pseudoUrl, String groupId, String kafkaIp,
			Collection<TopicPartitionOffset> topicOffsets, String username, String jdbcUrl, String password) {
		try {
		        System.out.println(LocalDateTime.now().toString() + " [INFO ] BEGIN: kConsumer...");
			
			dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			dataSource.setJdbcUrl(jdbcUrl);
			dataSource.setUsername(username);
			dataSource.setPassword(password);

			Map<String, Object> kafkaParameters = new HashMap<>();
			kafkaParameters.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			kafkaParameters.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaIp);
			kafkaParameters.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
			kafkaParameters.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
			kafkaParameters.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
			kafkaParameters.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			kafkaParameters.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "20971520");

		        System.out.println(LocalDateTime.now().toString() + " [INFO ] === Parameters set.");

			Map<TopicPartition, Long> topicPartitionOffsets = topicOffsets.stream()
					.collect(Collectors.toMap(t -> t.getTopicPartition(), t -> t.offset));
			List<String> topics = topicOffsets.stream().map(t -> t.topic).collect(Collectors.toList());

		        System.out.println(LocalDateTime.now().toString() + " [INFO ] === Loading topics: " + topicPartitionOffsets);

			JavaInputDStream<ConsumerRecord<String, byte[]>> directStream = KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferConsistent(),
					ConsumerStrategies.<String, byte[]>Subscribe(topics, kafkaParameters, topicPartitionOffsets));

			// Unwrap the incoming AVRO data to DataFrame compatible objects. This block is ran on EXECUTORS.
			JavaDStream<Tuple6<List<Row>, String, String, String, Integer, Long>> eventssDStream = directStream.map(consumerRecord -> {
				System.out.println(LocalDateTime.now().toString() + " [INFO ] === Receiving record for topic " + consumerRecord.topic() + ", partition " + consumerRecord.partition() + ", offset "
						+ consumerRecord.offset());

				String kafkaKey = consumerRecord.key();
				String[] keyComps = StringUtils.split(kafkaKey, METADATA_SEPARATOR);
				String schemaString = keyComps[1];
				GenericRecord genericRecord = generateRecord(schemaString, consumerRecord.value());

				GenericData.Array<GenericRecord> genericRecords = (GenericData.Array<GenericRecord>) genericRecord.get("rows");
				Iterator<GenericRecord> recordIterator = genericRecords.iterator();
				List<Row> rows = new ArrayList<>();

				String schema = null;

				while (recordIterator.hasNext()) {
					GenericRecord record = recordIterator.next();
					if (schema == null) {
						schema = record.getSchema().toString();
					}
					rows.add(generateRow(record));
				}

				return new Tuple6<List<Row>, String, String, String, Integer, Long>(rows, schema, kafkaKey, consumerRecord.topic(),
						consumerRecord.partition(), consumerRecord.offset());
			});

			eventssDStream.foreachRDD(eventsRDD -> {
				if (!eventsRDD.isEmpty()) {
					JavaSparkContext context = new JavaSparkContext(eventsRDD.context());
					SQLContext sqlContext = SQLContext.getOrCreate(context.sc());

					// Cache the RDD so that we do not accidentally reload the whole pipeline
					JavaRDD<Tuple6<List<Row>, String, String, String, Integer, Long>> events = eventsRDD;//.cache();

					// Find out the events we have (there is never a huge amount of them), a collect minimum info to the driver.
					// This block is ran on EXECUTORS.
					List<Tuple2<String, Long>> eventsOrderedCollect = events.map(t -> new Tuple2<String, Long>(t._3(), t._6())).collect();
					List<Tuple2<String, Long>> eventsOrdered = new ArrayList<>();
					eventsOrdered.addAll(eventsOrderedCollect);
					// Need to sort the table for ascending offset order not to mess up the offset tracking
					Collections.sort(eventsOrdered, (t1, t2) -> t1._2().compareTo(t1._2()));

					System.out.println(LocalDateTime.now().toString() + " [INFO ] === Received " + eventsOrdered.size() + " events.");
					
					// Do the write and process sequentially for each event to retain the offset order. This is essential for our only one
					// time processing.
					eventsOrdered.forEach(event -> {
						// Filter out all but the current event and collect that to the driver to avoid driver memory issues.
						// This is inefficient as we need to cycle all data via the driver and needs to be fixed later.
						// FIXME: Data collection to driver is inefficient and slows down the pipeline.
						// This block is ran on EXECUTORS.
						List<Tuple6<List<Row>, String, String, String, Integer, Long>> tableDatas = events.filter(t -> {
							return t._3().equals(event._1()) && t._6().equals(event._2());
						}).collect();
												
						for (Tuple6<List<Row>, String, String, String, Integer, Long> tableData : tableDatas) {
							List<Row> rows = tableData._1();
							String schemaAsString = tableData._2();
							String metaInfo = tableData._3();
							String topic = tableData._4();
							Integer partition = tableData._5();
							Long offset = tableData._6();
							
							System.out.println(LocalDateTime.now().toString() + " [INFO ] === Processing topic " + topic + ", partition " + partition + ", offset " + offset);

							String[] metadata = StringUtils.split(metaInfo, METADATA_SEPARATOR);
							String[] targetMeta = StringUtils.split(metadata[0].toLowerCase(), ".");
							String integration = targetMeta[0];
							String tableName = targetMeta[1];
							String timestamp = metadata[2];
							Schema schema = new Schema.Parser().parse(schemaAsString);
							StructType structType = convertToStructType(schema);

							String stagingLoc = "adl:///staging/" + integration + "/" + convertTableName(tableName) + "/incr";
							
							// Create dataset and parallelize it to 1 partition as the amount of data is small and we do not need 
							// heavy distribution.
							Dataset<Row> dataset = sqlContext.createDataFrame(context.parallelize(rows, 1), structType);
							dataset = dataset.withColumn("allas__pvm_partitio", functions.lit(timestamp));

							String pseudoFuncKeyPrefix = integration + "." + tableName;
							// Get the fields to hash fields
							List<Column> hashFields = pseudoInformations.entrySet().stream().filter(p -> p.getKey().startsWith(pseudoFuncKeyPrefix))
									.filter(p -> p.getValue().equalsIgnoreCase("HASH")).map(p -> new Column(p.getKey().split("\\.")[2])).collect(toList());
							Set<String> ssns = new HashSet<>();
							for (Column colToHash : hashFields) {
								// Fire up a distributed processing to find out the pseudonymizad rows and
								// collect them to the driver
								// NOTE: This is a source of a potential OutOfMemoryError (or similar).
								// This block is ran on EXECUTORS.
								List<Row> values = dataset.select(colToHash).distinct().collectAsList();
								values.stream().forEach(r -> ssns.add(r.getString(0)));
							}

							Map<String, String> pseudoSsns = new HashMap<>();
							List<String> ssnsList = new ArrayList<>();
							ssnsList.addAll(ssns);
							if (!ssnsList.isEmpty()) {
								System.out.println(LocalDateTime.now().toString() + " [INFO ] === Calling pseudonymization service with " + ssnsList.size() + " ssns.");
								pseudoSsns = Pseudonymization
										.getPseudonymizatedPersonId(new SsnPseudonymizationRequest(ssnsList, "datalake", pseudoUrl));
							}
							
							// Broadcast the collected values and the pseudoSsns to the executors
							Broadcast<Map<String, String>> pseudoSsnsBroadcast = context.broadcast(pseudoSsns);
							
							// Call the pseudonymization set up. The actual pseudonymization is done during write later.
							Dataset<Row> pseudoDataset = pseudonymize(sqlContext, pseudoFuncKeyPrefix, dataset, pseudoInformations, pseudoSsnsBroadcast);
							
							String storageLoc = "adl:///storage/" + integration + "/" + convertTableName(tableName) + "/incr";
							
							System.out.println(LocalDateTime.now().toString() + " [INFO ] === Writing storage for topic " + topic + ", partition " + partition + ", offset " + offset
									+ ". Total num of rows " + rows.size() + ", location " + storageLoc);

							// Write the pseyudonymized storate data. This block is ran on EXECUTORS.
							pseudoDataset.write().format("orc").option("compression", "zlib").mode(SaveMode.Append).orc(storageLoc);
							
							System.out.println(LocalDateTime.now().toString() + " [INFO ] === Writing staging for topic " + topic + ", partition " + partition + ", offset " + offset
									+ ". Total num of rows " + rows.size() + ", location " + stagingLoc);

							// Write staging data after storage, this is to prevent incomplete write on pseudonymization failure. 
							// This block is ran on EXECUTORS.
							dataset.write().format("orc").option("compression", "zlib").mode(SaveMode.Append).orc(stagingLoc);

							pseudoSsnsBroadcast.destroy();
							try {
								saveOffset(topic, partition, offset + 1, groupId);
							} catch (SQLException e) {
								throw new RuntimeException(e);
							}
						}
					});
					
//					events.unpersist();
				}
			});

			System.out.println("Starting!");
			System.out.println(LocalDateTime.now().toString() + " [INFO ] === Starting... ");

			ssc.start();
			ssc.awaitTermination();
			System.out.println(LocalDateTime.now().toString() + " [INFO ] === Stopped. ");

		} catch (Exception e) {
			logger.error("virhe", e);
			throw new RuntimeException();
		}

	}

	private static Row generateRow(GenericRecord record) {
		Schema schema = record.getSchema();
		List<Schema.Field> fields = schema.getFields();
		Object[] values = new Object[fields.size()];
		int i = 0;
		for (Schema.Field field : fields) {
			Object value = record.get(field.name());
			String logicalType = field.getProp("logicalType");
			if (value instanceof Utf8) {
				values[i] = ((Utf8) value).toString();
			} else if (logicalType != null) {
				if (value == null) {
					values[i] = null;
				} else if (logicalType.equals("timestamp-millis")) {
					values[i] = new Timestamp(((Long) value));
				} else if (logicalType.equals("date")) {
					values[i] = new Date(((Integer) value) * MILLIS_IN_DAY);
				} else if (logicalType.equals("decimal")) {
					Decimal decimal = LogicalTypes.decimal(Integer.parseInt(field.getProp("precision")), Integer.parseInt(field.getProp("scale")));
					values[i] = new DecimalConversion().fromBytes((ByteBuffer) value, null, decimal);
				} else {
					throw new RuntimeException("Unsupported Avro logicalType = " + logicalType);
				}
			} else {
				values[i] = value;
			}
			i++;
		}
		return RowFactory.create(values);

	}

	private static void saveOffset(String topic, int partition, long offset, String groupId) throws SQLException {
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = connection
				.prepareStatement("UPDATE kafka_offset_info SET offset=? WHERE topic=? AND partition=? AND group_id=?");
		statement.setLong(1, offset);
		statement.setString(2, topic);
		statement.setInt(3, partition);
		statement.setString(4, groupId);
		statement.executeUpdate();
		statement.close();
		connection.close();
	}

	private static Dataset<Row> pseudonymize(SQLContext sqlContext, String pseudoFuncKeyPrefix, final Dataset<Row> originalDf, final Map<String, String> pseudoInformations,
			final Broadcast<Map<String, String>> pseudoSsns) {
		List<String> fieldNames = Arrays.asList(originalDf.schema().fieldNames());

		Dataset<Row> df = originalDf;
		
		sqlContext.udf().register("hash", val -> pseudoSsns.value().get(val), DataTypes.StringType);

		for (String name : fieldNames) {
			String fullName = pseudoFuncKeyPrefix + "." + name;
			String processingFunc = pseudoInformations.get(fullName);
			if (processingFunc != null) {
				if (processingFunc.equals("NULL")) {
					System.out.println("Nulling column " + name);
					StructType schema = df.schema();
					df = df.withColumn(name, createNullColumn(schema.fields()[schema.fieldIndex(name)].dataType()));
				} else if (processingFunc.equals("HASH")) {
					df = df.withColumn(name, functions.callUDF("hash", functions.col(name)));
					System.out.println("Hashing column " + name);
				}
			} else {
				System.out.println("No pseudonymization information found for column " + name);
			}
		}

		return df;
	}

	private static Column createNullColumn(DataType dataType) {
		if (dataType.acceptsType(DataTypes.NullType)) {
			return functions.lit(null);
		}
		if (StringUtils.containsIgnoreCase(dataType.typeName(), "decimal")) {
			return functions.lit(new BigDecimal(-1));
		}
		if (dataType.acceptsType(DataTypes.LongType)) {
			return functions.lit(Long.valueOf("-1"));
		}
		if (dataType.acceptsType(DataTypes.IntegerType)) {
			return functions.lit(Integer.valueOf("-1"));
		}
		if (dataType.acceptsType(DataTypes.ShortType)) {
			return functions.lit(Short.valueOf("-1"));
		}
		if (dataType.acceptsType(DataTypes.FloatType)) {
			return functions.lit(Float.valueOf("-1"));
		}
		if (dataType.acceptsType(DataTypes.DoubleType)) {
			return functions.lit(Double.valueOf("-1"));
		}

		if (dataType.acceptsType(DataTypes.DateType)) {
			return functions.lit(Date.valueOf("0101-01-01"));
		}
		if (dataType.acceptsType(DataTypes.TimestampType)) {
			return functions.lit(Timestamp.valueOf("0101-01-01 01:01:01"));
		}
		if (dataType.acceptsType(DataTypes.BooleanType)) {
			return functions.lit(Boolean.FALSE);
		}
		if (dataType.acceptsType(DataTypes.BinaryType)) {
			return functions.lit(null);
		}

		return functions.lit("");
	}

	public static StructType convertToStructType(Schema schema) {
		List<StructField> structTypes = new ArrayList<>();
		for (Schema.Field field : schema.getFields()) {
			structTypes.add(convertField(field.name(), field.schema(), field.getProp("logicalType"), field.getJsonProps()));
		}
		return new StructType(structTypes.toArray(new StructField[structTypes.size()]));
	}

	private static StructField convertField(String name, Schema field, String logicalType, Map<String, JsonNode> props) {
		if (logicalType != null) {
			if (logicalType.equals("timestamp-millis")) {
				return new StructField(name, DataTypes.TimestampType, true, Metadata.empty());
			} else if (logicalType.equals("date")) {
				return new StructField(name, DataTypes.DateType, true, Metadata.empty());
			} else if (logicalType.equals("decimal")) {
				int precision = Integer.parseInt(props.get("precision").asText());
				int scale = Integer.parseInt(props.get("scale").asText());
				return new StructField(name, DataTypes.createDecimalType(precision, scale), true, Metadata.empty());
			}
		}

		switch (field.getType()) {
		case INT:
			return new StructField(name, DataTypes.IntegerType, true, Metadata.empty());
		case LONG:
			return new StructField(name, DataTypes.LongType, true, Metadata.empty());
		case FLOAT:
			return new StructField(name, DataTypes.FloatType, true, Metadata.empty());
		case DOUBLE:
			return new StructField(name, DataTypes.DoubleType, true, Metadata.empty());
		case BOOLEAN:
			return new StructField(name, DataTypes.BooleanType, true, Metadata.empty());
		case BYTES:
			return new StructField(name, DataTypes.BinaryType, true, Metadata.empty());
		case UNION:
			List<Schema> types = field.getTypes();
			for (Schema s : types) {
				if (s.getType() != Schema.Type.NULL) {
					return convertField(name, s, logicalType, props);
				}
			}
			// Fall-through to default on purpose!
		default:
			return new StructField(name, DataTypes.StringType, true, Metadata.empty());
		}
	}

	private static String convertTableName(final String originalTableName) {
		if (StringUtils.isNotBlank(originalTableName)) {
			if (originalTableName.equalsIgnoreCase("@type")) {
				return "type";
			}
			return originalTableName.replace(" ", "_").replace("-", "_").replace("Ä", "a").replace("ä", "a").replace("Å", "a").replace("å", "a")
					.replace("Ö", "o").replace("ö", "o").toLowerCase();
		}
		return null;
	}

	private static GenericRecord generateRecord(String schemaString, byte[] data) throws IOException {
		Schema schema = new Schema.Parser().parse(schemaString);
		DatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
		Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
		return reader.read(null, decoder);
	}

	public static final List<Column> getPseudo(Collection<PseudoInformation> pseudoInformations, String integration, String table) {
		return pseudoInformations.stream()
				.filter(p -> p != null && p.pseudoFunction != null && p.integration != null && p.table != null
						&& p.pseudoFunction.equalsIgnoreCase("HASH") && p.integration.equalsIgnoreCase(integration)
						&& p.table.equalsIgnoreCase(table))
				.map(p -> new Column(p.field)).collect(toList());
	}

}
