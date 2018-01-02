#!/bin/bash

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

echo "Installing $(basename $PWD) integration to the Data Lake"

#"This may reconfigure and activate services and have other effects"
#    echo "on all parts of the data lake, like the Hadoop cluster and this"
#    echo "manager."
#    echo "Usage: activate.sh"

ansible-playbook -i ../config/hosts activate.yml

#ansible-playbook -i ${HOME_DIR}/azure_inv.sh --private-key ${CONFIG_DIR}/ssh_keys/id_rsa --extra-vars "@${DL_CONFIG_FILE}" $PLAYBOOK -u $admin_name

#ansible-playbook -i ~/repos/main/ansible/azure_inv.sh activate.yml
check_result

