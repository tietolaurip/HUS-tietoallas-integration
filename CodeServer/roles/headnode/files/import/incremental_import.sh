#!/bin/bash

fail() {
    echo "This is a running full import phase of CodeServer"
    echo "After this the new files are in HDFS and Hive's tables have"
    echo "tables pointing at them"
    echo "usage: $(basename $0) <Date>"
    echo
    echo "Failure reason: $1"
    exit 1
}

check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
check HIVE_JDBC_CONNECTION_STRING
check CODESERVER_URL
check CODESERVER_STAGING_URL
check CODESERVER_CODESETS_FILE
if [ $# -lt 1 ]; then fail "Too few parameters"; fi

DATE=$1

# To allow running beeline in background
# https://issues.apache.org/jira/browse/HIVE-6758
export HADOOP_CLIENT_OPTS="-Djline.terminal=jline.UnsupportedTerminal"

TERMITEM_XPATH='//{urn:codeapi:Codeservice}termItemEntry'
ATTRIBUTE_XPATH='.//{urn:codeapi:Codeservice}termItemEntry/{urn:codeapi:Codeservice}attribute'
REFERENCE_XPATH='.//{urn:codeapi:Codeservice}termItemEntry/{urn:codeapi:Codeservice}referencedCode'

set_location() {
    TABLE=$1
    CSV_PATH=$2
    beeline -u "$HIVE_JDBC_CONNECTION_STRING" \
        -e "ALTER TABLE $TABLE SET LOCATION '$CSV_PATH'"
}

copy_to_storage() {
    STAGING_CSV_FILE=$1
    STORAGE_CSV_PATH=$2
    echo "$(date): Copying table $STAGING_CSV_FILE to storage"
    hadoop fs -mkdir -p $STORAGE_CSV_PATH
    hadoop fs -cp $STAGING_CSV_FILE $STORAGE_CSV_PATH
}

while IFS='' read -r CODESET_NAME_AND_ID; do

    CODESET=$(echo $CODESET_NAME_AND_ID | cut -d\; -f1)
    TMPID=$(echo $CODESET_NAME_AND_ID | cut -d\; -f2)
    CODESET_ID=${TMPID:-$CODESET}
    HIVE_CODESET=$(echo $CODESET | tr '[:upper:]' '[:lower:]' | tr '-' '_' | tr ' ' '_')

    echo "$(date): Starting import for codeset $CODESET"

    # Fetch raw codeset XML to HDFS staging area
    echo "$(date): Fetch raw XML"
    REQUEST=$(sed -e "s/__REPLACE_WITH_CODE_SYSTEM_ID__/$CODESET_ID/" $(dirname $0)/request_template.xml \
        | tr -d '\n')
    RAW_FILE="$CODESERVER_STAGING_URL/${HIVE_CODESET}_raw/${DATE}.xml"
    $(dirname $0)/get_from_soap_to_hdfs.sh "$CODESERVER_URL" "$REQUEST" "$RAW_FILE"
    if [ $? -ne 0 ]; then
        echo "ERROR: getting new raw response for $CODESET failed"
        echo "Stopping processing for $CODESET and continuing to next one"
        continue
    fi

    # Extract ids from the raw xml and store it to staging and copy storage area
    echo "$(date): Create ${HIVE_CODESET}_ids.csv"
    CSV_PATH="$CODESERVER_STAGING_URL/${HIVE_CODESET}_ids/${DATE}"
    CSV_FILE="${CSV_PATH}/${HIVE_CODESET}_ids.csv"
    STORAGE_CSV_PATH="$CODESERVER_STORAGE_URL/${HIVE_CODESET}_ids/${DATE}"
    hadoop fs -cat "$RAW_FILE" | \
    python xml_to_csv.py "$DATE" "$TERMITEM_XPATH" "./@id" \
        | hadoop fs -put - "$CSV_FILE"
    set_location staging_codeserver.${HIVE_CODESET}_ids $CSV_PATH
    copy_to_storage $CSV_FILE $STORAGE_CSV_PATH

    # Extract attributes from the raw xml and store it to staging and copy storage area
    echo "$(date): Create ${HIVE_CODESET}_attributes.csv"
    CSV_PATH="$CODESERVER_STAGING_URL/${HIVE_CODESET}_attributes/${DATE}"
    CSV_FILE="${CSV_PATH}/${HIVE_CODESET}_attributes.csv"
    STORAGE_CSV_PATH="$CODESERVER_STORAGE_URL/${HIVE_CODESET}_attributes/${DATE}"
    hadoop fs -cat "$RAW_FILE" \
        | python xml_to_csv.py "$DATE" "$ATTRIBUTE_XPATH" "./../@id" "./@language" "./@type" "./text()" \
        | hadoop fs -put - "$CSV_FILE"
    set_location staging_codeserver.${HIVE_CODESET}_attributes $CSV_PATH
    copy_to_storage $CSV_FILE $STORAGE_CSV_PATH

    # Extract references from the raw xml and store it to staging and copy storage area
    echo "$(date): Create ${HIVE_CODESET}_references.csv"
    CSV_PATH="$CODESERVER_STAGING_URL/${HIVE_CODESET}_references/${DATE}"
    CSV_FILE="${CSV_PATH}/${HIVE_CODESET}_references.csv"
    STORAGE_CSV_PATH="$CODESERVER_STORAGE_URL/${HIVE_CODESET}_references/${DATE}"
    hadoop fs -cat "$RAW_FILE" \
        | python xml_to_csv.py "$DATE" "$REFERENCE_XPATH" "./../@id" "./@referenceId" "./@beginDate" "./@code" "./@codeSystem" "./@codeSystemVersion" "./@expirationDate" \
        | hadoop fs -put - "$CSV_FILE"
    set_location staging_codeserver.${HIVE_CODESET}_references $CSV_PATH
    copy_to_storage $CSV_FILE $STORAGE_CSV_PATH

    echo "$(date): Import for codeset $CODESET complete"

done < $CODESERVER_CODESETS_FILE
