create database if not exists staging_husradu;
drop table if exists staging_husradu.husradu;
create external table staging_husradu.husradu(
    id string,
    data string,
    source string,
    import_timestamp timestamp
) stored as ORC location '/staging/husradu/data';

create database if not exists varasto_husradu_historia_log;
drop table if exists varasto_husradu_historia_log.husradu;
create external table varasto_husradu_historia_log.husradu(
    id string,
    data string,
    source string,
    import_timestamp timestamp
) stored as ORC location '/storage/husradu/data';