insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'husradu-orig', 0, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'husradu-orig' and partition = 0);

insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'husradu-orig', 1, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'husradu-orig' and partition = 1);

insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'husradu-orig', 2, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'husradu-orig' and partition = 2);

insert into kafka_offset_info with (rowlock) (topic, partition, offset, group_id)
  select 'husradu-pseudo', 0, 0, 'stream-to-hdfs'
  where not exists (select * from kafka_offset_info where topic = 'husradu-pseudo' and partition = 0);