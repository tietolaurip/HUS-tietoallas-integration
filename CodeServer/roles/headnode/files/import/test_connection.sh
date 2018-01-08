#!/bin/bash

fail() {
    echo "This is a script for testing connection to the configured CodeServer"
    echo "usage: $(basename $0)"
    echo
    echo "Failure reason: $1"
    exit 1
}

check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }
check CODESERVER_URL

curl --header "Content-Type: text/xml;charset=UTF-8" ${CODESERVER_URL}?wsdl
