#!/usr/bin/env bash


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
#   echo Note: Running mvn dependency:purge-local-repository...
#   mvn dependency:purge-local-repository -DreResolve=false -DactTransitively=false
# fi

cd ../CommonJava/
mvn clean install -Dmaven.test.skip=true
check_result

cd ../Jpa-lib
mvn clean install -Dmaven.test.skip=true
check_result

cd ../Clinisoft
mvn clean package -Dmaven.test.skip=true
check_result

