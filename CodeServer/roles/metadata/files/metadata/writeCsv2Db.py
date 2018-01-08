#!/usr/bin/python

#
# #%L
# CodeServer
# %%
# Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%

import pyodbc
import time
from datetime import datetime
import sys
import csv
import os

# ***********************************************************************************
# * General variables and defitions                                                 *
# ***********************************************************************************

driver = '{ODBC Driver 13 for SQL Server}'
server = ''
username = ""
password = ''
database = ''
DEBUG = 0
csvInDelimiter = '|'

# ***********************************************************************************
def debug(*args):
    global DEBUG
    if DEBUG:
        p = list(args) + ['']*(10-len(args))
        print "  ", p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9] 

# ***********************************************************************************
def readEnvVariables():
    'Reads needed env variables STATUSDB_xxx with fallback METADATA_JDBC/SQL_xxx'
    global server, database, username, password

    if (os.environ.has_key("STATUSDB_SERVER")):
        server = os.environ["STATUSDB_SERVER"]
        debug("STATUSDB_SERVER found:", server)
    elif (os.environ.has_key("METADATA_SQL_SERVER")):
        server = os.environ["METADATA_SQL_SERVER"]
        debug("METADATA_SQL_SERVER found:", server)
    else:
        raise Exception('STATUSDB_SERVER not set')

    if (os.environ.has_key("STATUSDB_NAME")):
        database = os.environ["STATUSDB_NAME"]
        debug("STATUSDB_NAME found:", database)
    elif (os.environ.has_key("METADATA_JDBC_NAME")):
        database = os.environ["METADATA_JDBC_NAME"]
        debug("METADATA_JDBC_NAME found:", database)
    else:
        raise Exception('STATUSDB_NAME not set')

    if (os.environ.has_key("STATUSDB_USER")):
        username = os.environ["STATUSDB_USER"]
        debug("STATUSDB_USER found:", username)
    elif (os.environ.has_key("METADATA_JDBC_USER")):
        username = os.environ["METADATA_JDBC_USER"]
        debug("METADATA_JDBC_USER found:", username)
    else:
        raise Exception('STATUSDB_USER not set')
 
    if (os.environ.has_key("STATUSDB_PWD")):
        password = os.environ["STATUSDB_PWD"]
        debug("STATUSDB_PWD found")
    elif (os.environ.has_key("METADATA_JDBC_PWD")):
        password = os.environ["METADATA_JDBC_PWD"]
        debug("METADATA_JDBC_PWD found")
    else:
        raise Exception('STATUSDB_PWD not set')

# ***********************************************************************************
def readArgs():
    'Reads command line args'

    global DEBUG
    if len(sys.argv) < 3 or len(sys.argv) > 4:
        print "     Usage: writeCsv2Db.py <table> <filename> [-v]"
        raise Exception("Incorrect number of arguments: %d", (len(sys.argv) - 1))

    table = sys.argv[1]
    filename = sys.argv[2]
    DEBUG = len(sys.argv) >  3 and sys.argv[3] == '-v'

    debug("DEBUG is ON")

    return (table, filename)
 
# ***********************************************************************************
def checkFile(filename):
    'Checks that the given file exists'
    if not os.path.exists(filename):
        raise Exception('File "%s" does not exist' % filename)

# ***********************************************************************************
def initializeDb():
    'Initialize connection to status database'

    global driver, server, database, username, password

    connectionString = ('Driver=' + driver
                        + ';Server=' + server
                        + ';Database=' + database
                        + ';uid=' + username
                        + ';pwd=' + password )
    debug("Connection string: " + ";".join(connectionString.split(';')[:-1]) + ";pwd=<deleted>")

    try:
        debug("Connecting to", server)
        connection = pyodbc.connect( connectionString )
    except pyodbc.Error as ex:
        sqlstate = ex.args[1]
        raise Exception("pyodbc ERROR: " + sqlstate)
    return connection

# ***********************************************************************************
def CloseDb(connection):
    'Close database collection'
    connection.close()

# ***********************************************************************************
def writeCsvToDb_insert(table, filename, connection):
    'Insert csv content into status database'
    print "   Filename: ", filename
    file = open(filename, "rb")
    reader = csv.reader(file, delimiter=csvInDelimiter)
    columns = next(reader) # read header
    query = "INSERT INTO " + table + " ({0}) VALUES ({1})"
    query = query.format(','.join(columns), ','.join('?' * len(columns))) 
    cursor = connection.cursor()

    print ("   query: " + query )
    print ("   Starting to read csv-file and storing data into db")

    for row in reader:
        cursor.execute(query, row)

    print ("   Finished storing data into db")

    cursor.commit()
    file.close()

# ***********************************************************************************
# * Main
# ***********************************************************************************

print "BEGIN: Main: writeCsv2Db.py:",datetime.now().strftime('%Y-%m-%d %H:%M:%S')

reload(sys)  
sys.setdefaultencoding('utf8')

startTime = time.time()

try:
    # Read command line arguments
    table, filename = readArgs()
    debug("filename: %s, table: %s" % (filename, table))

    #check that file exists
    checkFile(filename)

    # Read env variables
    readEnvVariables()

    # Initilize DB
    dbConnection = initializeDb()

    # Read source file and write PSN-function values into metadata DB
    writeCsvToDb_insert(table, filename, dbConnection)

    # close DB
    CloseDb(dbConnection)
except Exception as err:
    print "*** ERROR: ", err

# ***********************************************************************************
endTime = time.time()
print "END:   Main: writeCsv2Db.py:", datetime.now().strftime('%Y-%m-%d %H:%M:%S'), \
    " (", endTime - startTime, ")"
# ***********************************************************************************
 