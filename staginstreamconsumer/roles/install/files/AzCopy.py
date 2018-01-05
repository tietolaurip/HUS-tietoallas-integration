#!/usr/bin/python

from azure.datalake.store import core, lib, multithread
import os
import os.path
import sys
import time
import logging

# Import all client secrets from path, the caller must set the PYTHONPATH to include the file
from adlSecrets import *

LEV1=''
LEV2='   '
log=""
from datetime import datetime

# ***********************************************************************************
# * FUNCTIONS
# ***********************************************************************************
def setLogger( debugValue ):
  'Function sets logging options'
  global log
  log = logging.getLogger('AzCopy')
  ch = logging.StreamHandler()
  log.setLevel(logging.INFO)
  if ( debugValue == "-v"):
    log.setLevel(logging.DEBUG)
  # create formatter and add it to the handlers
  formatter = logging.Formatter('%(asctime)s [%(levelname)-6s][%(name)s/%(funcName)-14s]: %(message)s',datefmt="%Y/%m/%d %H:%M:%S")
  ch.setFormatter(formatter)
  # add the handler to the logger
  log.addHandler(ch)
  log.debug('%sLogging set to %s.', LEV1, log.getEffectiveLevel() )
  return
# *********************************************************************************** 
def readArgs():
  'Reads args and sets variables'
  nbr = 0
  source = ""
  target = ""
  debugValue = ""
  nbr = len(sys.argv) - 1
  if (nbr >= 2):
    source = sys.argv[1]
    target = sys.argv[2]
    if (nbr == 3):
      if (sys.argv[3] == "-v"):
        debugValue = "-v"
  else:
    print LEV1, "ERROR: Incorrect number of arguments: ", nbr
    print LEV2, "USE: AzCopy.py <source> <target> [<debug>]"
    sys.exit(1)
  return (source, target, debugValue)
# *********************************************************************************** 
def initializeAdls():
  'Initializes Azure DataLakeStore'
  log.debug("%sclientId: %s", LEV2, clientId)
  log.debug("%sclientSecret: %s", LEV2, clientSecret)
  log.debug("%stenantId: %s", LEV2, tenantId)
  log.debug("%sadlsAccountName: %s", LEV2, adlsAccountName)
  token = lib.auth(tenant_id = tenantId, client_secret = clientSecret, client_id  = clientId)
  adl = core.AzureDLFileSystem(token, store_name=adlsAccountName)
  return ( adl )
# ***********************************************************************************
def AzCopy( adl, source, target ):
  'Moving file from source-location to target-location'
  multithread.ADLUploader(adl, lpath=source, rpath=target, nthreads=64, overwrite=True, buffersize=4194304, blocksize=4194304)
  log.debug("%sSource: %s", LEV2, source)
  log.debug("%sTarget: %s", LEV2, target)
  
  return
# ***********************************************************************************  
def main():
  'Main()-function that moves files to storage(staging)'
  # 1. Read args
  (source, target, debugValue) = readArgs()
  
  # 2. LOG setting
  setLogger( debugValue )
  log.info("%sBEGIN: Move file from local directory to ADL", LEV1)
  startTime = time.time()
  
  # 3. Initialize dataLakeStore
  adl = initializeAdls()

  # 4. Copy file to Adls
  AzCopy( adl, source, target )
  
  endTime = time.time()

  log.info('%sEND: file (%s)  moved in %s seconds', LEV1, source, int(endTime - startTime))

# ***********************************************************************************
# RUN the program
# ***********************************************************************************
if __name__ == '__main__':
  main()
