#!/bin/bash

echo "Installing $(basename $PWD) integration to the Data Lake"

function check_result {
  if [ $? -ne 0 ]
  then
    echo ""
    echo "Installation failed, leaving with exit 1 from activate.sh!"
    exit 1
  else
    echo "Installation successful!"
  fi
}

#"This may reconfigure and activate services and have other effects"
#    echo "on all parts of the data lake, like the Hadoop cluster and this"
#    echo "manager."
#    echo "Usage: activate.sh"
DEBUG=$1
params="--extra-vars \"\""
ansible_debug=""

if [ "$DEBUG" == "-v" ]
then
    params="--extra-vars \"debug_flag='-v'}\"" # Set debug for scripts 
fi

if [ "$DEBUG" == "-vvvv" ]
then
    
    ansible_debug="-vvvv" # Set debug for ansible
fi

if [ "$DEBUG" == "-va" ]
then
    params="--extra-vars \"debug_flag='-v'}\""
    ansible_debug="-vvvv"
fi

ansible-playbook -i ../config/hosts activate.yml $params $ansible_debug
#../common/runplaybook.sh -c ../common/config/config_integration.yml -p activate.yml
check_result

