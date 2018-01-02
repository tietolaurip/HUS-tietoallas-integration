#!/bin/bash

echo "Installing $(basename $PWD) integration to the Data Lake"

#"This may reconfigure and activate services and have other effects"
#    echo "on all parts of the data lake, like the Hadoop cluster and this"
#    echo "manager."
#    echo "Usage: activate.sh"

ansible-playbook -i ../config/hosts activate.yml

