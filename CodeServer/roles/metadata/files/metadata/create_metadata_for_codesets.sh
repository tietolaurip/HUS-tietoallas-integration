#!/bin/bash

fail() {
    echo "This is a script for populating CodeServer metadata to a metadata"
    echo "database. This removes any existing metadata for CodeServer."
    echo "Usage: $(basename $0) <codesets file>"
    echo
    echo "Failure reason: $1"
    exit 1
}

check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
check STATUSDB_SERVER
check STATUSDB_NAME
check STATUSDB_USER
check STATUSDB_PWD

if [ $# -lt 1 ]; then fail "Too few parameters"; fi

CODESETS_FILE=$1

## column_metadata.csv

DATE_NOW=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COLUMN_METADATA_TEMPLATE_FILE=$(dirname $0)/column_metadata_template.csv
ALL_CODESETS_COLUMN_METADATA_CSV_FILE=/tmp/codeserver_column_metadata_${DATE_NOW}.csv
TABLE_METADATA_TEMPLATE_FILE=$(dirname $0)/table_metadata_template.csv
ALL_CODESETS_TABLE_METADATA_CSV_FILE=/tmp/codeserver_table_metadata_${DATE_NOW}.csv

#write header row in the template to column_metadata.csv
head -1 $COLUMN_METADATA_TEMPLATE_FILE > $ALL_CODESETS_COLUMN_METADATA_CSV_FILE
head -1 $TABLE_METADATA_TEMPLATE_FILE > $ALL_CODESETS_TABLE_METADATA_CSV_FILE

while IFS='' read -r CODESET; do
    HIVE_CODESET=$(echo $CODESET | tr '[:upper:]' '[:lower:]' | tr '-' '_' | tr ' ' '_')

    cat $COLUMN_METADATA_TEMPLATE_FILE \
        | tail -n +2 \
        | sed -e "s/__CODESERVER_METADATA_IDS_TABLE__/${HIVE_CODESET}_ids/" \
        | sed -e "s/__CODESERVER_METADATA_ATTRIBUTES_TABLE__/${HIVE_CODESET}_attributes/" \
        | sed -e "s/__CODESERVER_METADATA_REFERENCES_TABLE__/${HIVE_CODESET}_references/" \
        | sed -e "s/__DATE_NOW__/${DATE_NOW}/" \
        | cat >> $ALL_CODESETS_COLUMN_METADATA_CSV_FILE

    cat $TABLE_METADATA_TEMPLATE_FILE \
        | tail -n +2 \
        | sed -e "s/__CODESERVER_METADATA_IDS_TABLE__/${HIVE_CODESET}_ids/" \
        | sed -e "s/__CODESERVER_METADATA_ATTRIBUTES_TABLE__/${HIVE_CODESET}_attributes/" \
        | sed -e "s/__CODESERVER_METADATA_REFERENCES_TABLE__/${HIVE_CODESET}_references/" \
        | sed -e "s/__DATE_NOW__/${DATE_NOW}/" \
        | cat >> $ALL_CODESETS_TABLE_METADATA_CSV_FILE

done < $CODESETS_FILE

# Delete old data
sqlcmd -S $STATUSDB_SERVER -U $STATUSDB_USER -P $STATUSDB_PWD -N -d $STATUSDB_NAME -Q \
       "DELETE FROM data_table WHERE data_set_name = 'CodeServer'"
sqlcmd -S $STATUSDB_SERVER -U $STATUSDB_USER -P $STATUSDB_PWD -N -d $STATUSDB_NAME -Q \
       "DELETE FROM data_column WHERE data_set_name = 'CodeServer'"

#write metadata to statusdb
python $(dirname 0)/writeCsv2Db.py data_column $ALL_CODESETS_COLUMN_METADATA_CSV_FILE -v
python $(dirname 0)/writeCsv2Db.py data_table $ALL_CODESETS_TABLE_METADATA_CSV_FILE -v

# remove the temp files
rm $ALL_CODESETS_COLUMN_METADATA_CSV_FILE
rm $ALL_CODESETS_TABLE_METADATA_CSV_FILE
