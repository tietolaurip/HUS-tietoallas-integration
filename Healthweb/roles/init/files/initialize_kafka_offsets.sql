insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'healthweb-orig', 0, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'healthweb-orig');

insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'healthweb-pseudo', 0, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'healthweb-pseudo');