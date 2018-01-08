#!/bin/bash

fail() {
    echo "This is a script for initial setup of HDFS directories and Hive"
    echo "tables for CodeServer staging and is intended to be run as part of"
    echo "CodeServer component installation"
    echo
    echo "Failure reason: $1"
    exit 1
}

check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
check HIVE_JDBC_CONNECTION_STRING
check CODESERVER_STAGING_URL
check CODESERVER_CODESETS_FILE

# To allow running beeline in background
# https://issues.apache.org/jira/browse/HIVE-6758
export HADOOP_CLIENT_OPTS="-Djline.terminal=jline.UnsupportedTerminal"

while IFS='' read -r CODESET_NAME_AND_ID; do

    CODESET=$(echo $CODESET_NAME_AND_ID | cut -d\; -f1)
    HIVE_CODESET=$(echo $CODESET | tr '[:upper:]' '[:lower:]' | tr '-' '_' | tr ' ' '_')

    echo "Starting table creation for codeset $CODESET"
    hadoop fs -mkdir -p $CODESERVER_STAGING_URL/${HIVE_CODESET}_raw \
        $CODESERVER_STAGING_URL/${HIVE_CODESET}_ids \
        $CODESERVER_STAGING_URL/${HIVE_CODESET}_attributes \
        $CODESERVER_STAGING_URL/${HIVE_CODESET}_references

    hadoop fs -mkdir -p \
        $CODESERVER_STORAGE_URL/${HIVE_CODESET}_ids \
        $CODESERVER_STORAGE_URL/${HIVE_CODESET}_attributes \
        $CODESERVER_STORAGE_URL/${HIVE_CODESET}_references

    beeline -u "$HIVE_JDBC_CONNECTION_STRING" \
        -f $(dirname $0)/setup_codeset_tables.hql \
        --hivevar staging_url=$CODESERVER_STAGING_URL \
        --hivevar codeset=$HIVE_CODESET

done < $CODESERVER_CODESETS_FILE
