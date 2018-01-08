#!/bin/bash
fail() {
    echo "This is the main orchestration process for CodeServer integration"
    echo "This is normally triggered from cron, but is usable for special"
    echo "uses directly. Unlike most scripts, this one internally sources the"
    echo "configuration from standard places. Similarly all output is directed"
    echo "to standard logs"
    echo "   Usage: $(basename $0)"
    echo
    echo "Failure reason: $1"
    exit 1
}

DATE=$(date -u "+%Y-%m-%d")
START=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
LOG=/var/log/DataLake/CodeServer/$(basename $0).${START}.log
echo "Log is created to $LOG"

exec > $LOG
exec 2>&1

echo "Starting CodeServer $0 on $START for $DATE"

COMP=CodeServer
INSTALL_DIR=/opt/DataLake/$COMP

source $INSTALL_DIR/config/config.sh

echo "Starting incremental import on $(date)"
cd $INSTALL_DIR/import/
./incremental_import.sh $DATE
echo "Incremental import complete on $(date)"

END=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

echo "Finished CodeServer $0 on $END for $DATE"

exit 0
