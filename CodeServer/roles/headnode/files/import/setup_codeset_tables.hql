CREATE DATABASE IF NOT EXISTS staging_codeserver;

USE staging_codeserver;

CREATE EXTERNAL TABLE IF NOT EXISTS staging_codeserver.${codeset}_ids (
 id STRING COMMENT 'CodeServer TermItemEntry ID',
 allas__pvm_partitio STRING COMMENT 'Import date'
)
COMMENT '{codeset} identifiers from CodeServer'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\'
STORED AS TEXTFILE
LOCATION '${staging_url}/${codeset}_ids';

CREATE EXTERNAL TABLE IF NOT EXISTS staging_codeserver.${codeset}_attributes (
 id STRING COMMENT 'CodeServer TermItemEntry ID',
 language STRING COMMENT 'Language code of the Attribute',
 type STRING COMMENT 'Attribute name',
 value STRING COMMENT 'Attribute value',
 allas__pvm_partitio STRING COMMENT 'Import date'
)
COMMENT '${codeset} attributes from CodeServer'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\'
STORED AS TEXTFILE
LOCATION '${staging_url}/${codeset}_attributes';

CREATE EXTERNAL TABLE IF NOT EXISTS staging_codeserver.${codeset}_references (
 id STRING COMMENT 'CodeServer TermItemEntry ID',
 referenceId STRING COMMENT 'ID of the reference',
 beginDate STRING COMMENT 'Begin date of the validity of the reference',
 code STRING COMMENT 'The code to be referred in the Code System',
 codeSystem STRING COMMENT 'Name of the referred Code System',
 codeSystemVersion STRING COMMENT 'Attribute value',
 expirationDate STRING COMMENT 'Attribute value',
 allas__pvm_partitio STRING COMMENT 'Import date'
)
COMMENT '${codeset} references from CodeServer'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' ESCAPED BY '\\'
STORED AS TEXTFILE
LOCATION '${staging_url}/${codeset}_references';
