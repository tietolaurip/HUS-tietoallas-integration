package fi.tietoallas.integration.staginstreamconsumer;

import org.apache.kafka.common.TopicPartition;

class TopicPartitionOffset {

	String topic;
	int partition;
	long offset;

	public TopicPartitionOffset(String topic, int partition, long offset) {
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
	}

	TopicPartition getTopicPartition() {
		return new TopicPartition(topic, partition);
	}

	@Override
	public String toString() {
		return "TopicPartitionOffset [topic=" + topic + ", partition=" + partition + ", offset=" + offset + "]";
	}

}
