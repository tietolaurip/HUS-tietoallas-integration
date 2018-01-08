#!/bin/bash

fail() {
    echo "This is a script for storing raw SOAP responses to HDFS"
    echo "The <Request> parameter is passed tu curl as such so you can"
    echo "include the request inline or point to a file"
    echo "usage: $(basename $0) <URL> <Request> <HADOOP_FILE_URL>"
    echo
    echo "Failure reason: $1"
    exit 1
}


check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
if [ $# -lt 3 ]; then fail "Too few parameters"; fi

URL=$1
REQUEST_BODY=$2
HADOOP_FILE_URL=$3

curl -sS --header "Content-Type: text/xml;charset=UTF-8" -d "$REQUEST_BODY" "$URL" \
    | hadoop fs -put - $HADOOP_FILE_URL
