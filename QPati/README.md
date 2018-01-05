# QPati

QPati a DataLake integration component for integrating with QPati system.


## Import
This integration is based on QPati pushing CSV files over SFTP to the
data lake. SFTP server in turn pushes them to Azure Blob storage that forms
the QPati staging area.

## Preprocessing.
Preprocessing part of QPati integration forms Hive database varasto_qpati
using the files from staging.

