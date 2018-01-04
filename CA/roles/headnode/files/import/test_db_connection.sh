#!/bin/bash

fail() {
    echo "This is a script for testing database connection to the configured"
    echo "CA instance"
    echo "Usage: $(basename $0)"
    echo
    echo "Failure reason: $1"
    exit 1
}


check() { eval "VAL=\$$1"; if [ -z $VAL ]; then fail "$1 not defined"; fi; }

check CA_JCEKS_URL
check CA_JDBC_CONNECTION
check CA_JDBC_USER_NAME
check CA_JDBC_PASSWORD_ALIAS

#sqoop eval \
#	 -Dorg.apache.sqoop.credentials.loader.class=org.apache.sqoop.util.password.CredentialProviderPasswordLoader \
#	 -Dhadoop.security.credential.provider.path="$CA_JCEKS_URL" \
#	 --driver "com.microsoft.sqlserver.jdbc.SQLServerDriver" \
#         --connect "$CA_JDBC_CONNECTION" \
#	 --username "$CA_JDBC_USER_NAME" \
#	 --password-alias "$CA_JDBC_PASSWORD_ALIAS" \
#         --query "SELECT name FROM sysobjects WHERE xtype = 'U'"

sqoop eval \
	 --driver "com.microsoft.sqlserver.jdbc.SQLServerDriver" \
         --connect "$CA_JDBC_CONNECTION" \
	 --username "$CA_JDBC_USER_NAME" \
	 --password "$CA_JDBC_PWD" \
         --query "SELECT name FROM sysobjects WHERE xtype = 'U'"
