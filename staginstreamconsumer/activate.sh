#!/bin/bash
echo "Installing $(basename $PWD) integration to the Data Lake"

../common/runplaybook.sh -c ../common/config/config_maindatalake.yml -p install.yml

