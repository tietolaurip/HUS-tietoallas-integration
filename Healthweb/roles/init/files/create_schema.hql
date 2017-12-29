create database if not exists staging_healthweb;
drop table if exists staging_healthweb.healthweb;
create external table staging_healthweb.healthweb(
    id string,
    data string,
    source string,
    import_timestamp timestamp
) stored as ORC location '/staging/healthweb/data';

create database if not exists varasto_healthweb_historia_log;
drop table if exists varasto_healthweb_historia_log.healthweb;
create external table varasto_healthweb_historia_log.healthweb(
    id string,
    data string,
    source string,
    import_timestamp timestamp
) stored as ORC location '/storage/healthweb/data';