#!/usr/bin/env bash
#

function check_result {
  if [ $? -ne 0 ]
  then
    echo ""
    echo "mvn build failed, leaving with exit 1 from build.sh!"
    exit 1
  else
    echo "mvn build success!"
  fi
}

# if [ "$1" != "no-purge" ]
# then
#   echo "Note: Removing CommonJava(statudb) artifacts from .m2 directory for purging old dependencies out"
#   rm -rf ~/.m2/repository/fi/tietoallas/integration/statusdb
# fi

cd ../CommonJava/
mvn clean install -Dmaven.test.skip=true
check_result

cd ../Jpa-lib
mvn clean install -Dmaven.test.skip=true

cd ../Opera
mvn clean package -Dmaven.test.skip=true
check_result
