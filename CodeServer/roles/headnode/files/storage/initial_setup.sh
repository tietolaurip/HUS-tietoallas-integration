#!/bin/bash

fail() {
    echo "This is a script for initial setup of Hive for databases and"
    echo "tables for CodeServer storage and is intended to be run as part of"
    echo "CodeServer initial setup phase."
    echo "To use the normal configuration, source /opt/DataLake/CodeServer/config.sh"
    echo "If you know what you are doing, you may also redefine some of those"
    echo "variables to something else"
    echo
    echo "Failure reason: $1"
    exit 1
}

check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
check DATALAKE_LIB_DIR

check CODESERVER_TABLE_NAMES_FILE

"$DATALAKE_LIB_DIR"/storage/initial_setup.sh \
    CodeServer \
    $CODESERVER_STORAGE_URL \
    $CODESERVER_TABLE_NAMES_FILE
