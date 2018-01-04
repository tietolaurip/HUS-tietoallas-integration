#!/bin/bash

fail() {
    echo "This is the $OPERATION process for $COMP integration covering"
    echo "setup of HDFS directories and Hive tables for staging phase."
    echo "Intended to be run as part of CA component installation."
    echo "   Run at: headnode"
    echo "   Usage: $(basename $0)"
    echo "Failure reason: $1"
    echo
    echo "To get env variables fixed, source them manually:"
    echo "   source  ../../config/config.sh"
    echo "   source ../../StatusDb/config/config.sh"
    echo "   source ../../Metadata/config/config.sh"
    echo "   source ../config/config.sh"
    exit 1
}
check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }

#######################################################################
# 0. Setting variables and checking parameters
#######################################################################
# Setting up log collection
source ../../config/config.sh # base config
source ../config/config.sh    # component config
check DATALAKE_LOG_DIR
check COMPONENT_NAME
check COMPONENT_DIR
check CA_STAGING_URL
check CA_STORAGE_URL

START=$(date -u +"%Y-%m-%d_%H%M%S")
VM=$(hostname)
SW=$(basename $0 .sh)
LOG=$DATALAKE_LOG_DIR/$COMPONENT_NAME/$SW.${START}.log
exec > $LOG
exec 2>&1

SECONDS=0
#######################################################################
DATE=$(date -u +"%Y/%m/%d %H:%M:%S")
MESSAGE="BEGIN: Cleanup of $COMPONENT_NAME hive database and hdfs"
echo "$DATE [INFO ] $COMP/$VM/$SW: $MESSAGE"
$DATALAKE_LIB_DIR/uninstall/remove_component_data_and_databases_from_hadoop.sh \
    $COMPONENT_NAME \
    $CA_STAGING_URL \
    $CA_STORAGE_URL

#######################################################################
COLLAPSED_TIME=`date -d@${SECONDS} -u +%H:%M:%S`
MESSAGE="END: Done in $COLLAPSED_TIME"
DATE=$(date -u +"%Y/%m/%d %H:%M:%S")
echo "$DATE [INFO] $COMP/$VM/$SW: $MESSAGE"
#######################################################################
