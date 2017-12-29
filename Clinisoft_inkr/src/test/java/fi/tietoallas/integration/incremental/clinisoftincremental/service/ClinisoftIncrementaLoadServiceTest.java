package fi.tietoallas.integration.incremental.clinisoftincremental.service;

/*-
 * #%L
 * clinisoft-incremental
 * %%
 * Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import fi.tietoallas.incremental.common.commonincremental.config.DbConfiguration;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import fi.tietoallas.incremental.common.commonincremental.util.TietoallasKafkaProducer;
import fi.tietoallas.integration.incremental.clinisoftincremental.configuration.ClinisoftDbConfig;
import fi.tietoallas.integration.incremental.clinisoftincremental.repository.ClinisoftRepository;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ClinisoftIncrementaLoadServiceTest {

    private JdbcTemplate statusJdbcTemplate;
    private JdbcTemplate clinisoftJdbcTemplate;
    private ClinisoftDbConfig clinisoftDbConfig;
    private DbConfiguration dbConfiguration;
    @Mock
    private Environment environment;

    private ClinisoftRepository clinisoftRepository;
    private StatusDatabaseRepository statusDatabaseRepository;
    MockProducer<String,byte[]> producer;
    @Mock
    private TietoallasKafkaProducer tietoallasKafkaProducer;

    @Before
    public void setUp(){
        when(environment.getProperty(eq("fi.datalake.sourcedb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.sourcedb.url"))).thenReturn("jdbc:h2:mem:test");
        when(environment.getProperty(eq("fi.datalake.statusdb.url"))).thenReturn("jdbc:h2:mem:test");
        when(environment.getProperty(eq("fi.datalake.statusdb.driver"))).thenReturn("org.h2.Driver");
        when(environment.getProperty(eq("fi.datalake.comp.instancename"))).thenReturn("clinisoft");
        clinisoftDbConfig = new ClinisoftDbConfig(environment);
        clinisoftJdbcTemplate = clinisoftDbConfig.jdbcTemplate();
        clinisoftRepository= new ClinisoftRepository(clinisoftJdbcTemplate);
        dbConfiguration = new DbConfiguration(environment);
        statusJdbcTemplate = dbConfiguration.jdbcTemplate();
        statusDatabaseRepository = new StatusDatabaseRepository(statusJdbcTemplate);
        producer = new MockProducer<>(true, new StringSerializer(),new ByteArraySerializer());
        setupTestStatusData();

    }

    @Test
    public void testPatient() throws Exception{
        clinisoftJdbcTemplate.execute("drop table if EXISTS P_Allergy");
        clinisoftJdbcTemplate.execute("drop table if EXISTS P_GENERALDATA");
        clinisoftJdbcTemplate.execute("drop table IF EXISTS P_DischargeData");
        clinisoftJdbcTemplate.execute("create table P_DischargeData (patientId int, DischargeTime DATETIME)");
        clinisoftJdbcTemplate.execute("create table p_generaldata (patientId int, status int)");
        clinisoftJdbcTemplate.update("insert into p_generaldata (patientId,status) values(3,8)");
        clinisoftJdbcTemplate.update("insert into p_generaldata (patientId,status) values(4,8)");
        clinisoftJdbcTemplate.update("insert into P_DischargeData (patientId,DischargeTime) values(3,'2030-01-01 10:10:10')");
        clinisoftJdbcTemplate.update("insert into P_DischargeData (patientId,DischargeTime) values(4,'2031-01-01 10:10:10')");
        clinisoftJdbcTemplate.execute("CREATE TABLE P_Allergy (AllergenID INT, AllergenName VARCHAR(40), AllergyID INT, CheckedBy VARCHAR(12), EnteredBy VARCHAR(12), EnterTime DATETIME, IdentificationDate DATETIME, Notes VARCHAR(255), PatientID INT, SeverityID INT, TypeID INT)");
        clinisoftJdbcTemplate.update("INSERT INTO P_Allergy (AllergenID, AllergenName, AllergyID, CheckedBy, EnteredBy, EnterTime, IdentificationDate, Notes, PatientID, SeverityID, TypeID) VALUES (-1, NULL, 2, 'someone', 'other', '2007-03-24 11:29:40', NULL, NULL, 3, NULL, NULL)");
       clinisoftJdbcTemplate.update("INSERT INTO P_Allergy (AllergenID, AllergenName, AllergyID, CheckedBy, EnteredBy, EnterTime, IdentificationDate, Notes, PatientID, SeverityID, TypeID) VALUES (-1, NULL, 2, 'someone', 'other', '2007-03-24 11:29:40', NULL, NULL,4, NULL, NULL)");
        ClinisoftIncrementalLoadService clinisoftIncrementalLoadService = new ClinisoftIncrementalLoadService(environment,statusDatabaseRepository);
        clinisoftIncrementalLoadService.clinisoftRepository=clinisoftRepository;
        clinisoftIncrementalLoadService.producer=new TietoallasKafkaProducer(producer);
        List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> beforeUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Patient", allTables);
        clinisoftIncrementalLoadService.processIncrementalInPatientDb(beforeUpdate,"clinisoft");
        assertThat(producer.history(),hasSize(1));
        allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> afterUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Patient",allTables);
        for (TableStatusInformation tableStatusInformation : beforeUpdate){
            assertThat(tableStatusInformation.lastUsedValue,not(findSameUsedValue(afterUpdate,tableStatusInformation.tableName)));
            assertThat(tableStatusInformation.lastAccessAt,not(findSameUsedTimeStamp(afterUpdate,tableStatusInformation.tableName)));
        }
    }
    @Test
    public void testSystem() throws Exception{
        clinisoftJdbcTemplate.execute("drop table if EXISTS S_AllergenRef");
        clinisoftJdbcTemplate.execute("CREATE TABLE S_AllergenRef (AllergenID INT, ArchStatus INT, ArchTime DATETIME, CompID INT, Description VARCHAR(255), ExtCode VARCHAR(40), Name VARCHAR(40), TypeID INT)");
        clinisoftJdbcTemplate.update("INSERT INTO S_AllergenRef (AllergenID, ArchStatus, ArchTime, CompID, Description, ExtCode, Name, TypeID) VALUES (-1, 2, '2990-01-01 00:00:00', NULL, ' ', NULL, 'No allergies', -1)");
        ClinisoftIncrementalLoadService clinisoftIncrementalLoadService = new ClinisoftIncrementalLoadService(environment,statusDatabaseRepository);
        clinisoftIncrementalLoadService.producer=new TietoallasKafkaProducer(producer);
        clinisoftIncrementalLoadService.clinisoftRepository=clinisoftRepository;
        List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> beforeUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("System", allTables);
        clinisoftIncrementalLoadService.processIncrementsInSystemDb(beforeUpdate,"clinisoft");
        allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> afterUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("System", allTables);
        for (TableStatusInformation tableStatusInformation : beforeUpdate){
            assertThat(tableStatusInformation.lastAccessAt,not(findSameUsedTimeStamp(afterUpdate,tableStatusInformation.tableName)));
        }
        assertThat(producer.history(),hasSize(1));
    }
    @Test
    public void testDepartment() throws Exception {
        clinisoftJdbcTemplate.execute("drop table IF EXISTS D_PatScores");
        clinisoftJdbcTemplate.execute("CREATE TABLE D_PatScores (ArchStatus INT , ArchTime DATETIME, Datetime DATETIME, PatientID INT, Type INT, Value FLOAT)");
        clinisoftJdbcTemplate.execute("INSERT INTO D_PatScores (ArchStatus, ArchTime, Datetime, PatientID, Type, Value) VALUES (2, '2004-09-24 13:45:04', '2001-07-26 13:20:00', 86, 2, 10)");
        ClinisoftIncrementalLoadService clinisoftIncrementalLoadService = new ClinisoftIncrementalLoadService(environment,statusDatabaseRepository);
        clinisoftIncrementalLoadService.producer=new TietoallasKafkaProducer(producer);
        clinisoftIncrementalLoadService.clinisoftRepository=clinisoftRepository;
        List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> beforeUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Department",allTables);
        clinisoftIncrementalLoadService.processIncrementsInDeparmentDb(beforeUpdate,"clinisoft");
        assertThat(producer.history(),hasSize(1));
        allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> afterUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Department",allTables);
        for (TableStatusInformation tableStatusInformation : beforeUpdate){
            assertThat(tableStatusInformation.lastAccessAt,not(findSameUsedTimeStamp(afterUpdate,tableStatusInformation.tableName)));
        }
    }
    @Test(expected = RuntimeException.class)
    public void testKafkaError() throws Exception{
        when(tietoallasKafkaProducer.run(Mockito.any(Schema.class), Mockito.any(GenericRecord.class),Mockito.anyString(),Mockito.anyString())).thenThrow(new RuntimeException());
        clinisoftJdbcTemplate.execute("drop table if EXISTS S_AllergenRef");
        clinisoftJdbcTemplate.execute("CREATE TABLE S_AllergenRef (AllergenID INT, ArchStatus INT, ArchTime DATETIME, CompID INT, Description VARCHAR(255), ExtCode VARCHAR(40), Name VARCHAR(40), TypeID INT)");
        clinisoftJdbcTemplate.update("INSERT INTO S_AllergenRef (AllergenID, ArchStatus, ArchTime, CompID, Description, ExtCode, Name, TypeID) VALUES (-1, 2, '2990-01-01 00:00:00', NULL, ' ', NULL, 'No allergies', -1)");
        ClinisoftIncrementalLoadService clinisoftIncrementalLoadService = new ClinisoftIncrementalLoadService(environment,statusDatabaseRepository);
        clinisoftIncrementalLoadService.producer=tietoallasKafkaProducer;
        clinisoftIncrementalLoadService.clinisoftRepository=clinisoftRepository;
        List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> beforeUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Department", allTables);
        clinisoftIncrementalLoadService.processIncrementsInDeparmentDb(beforeUpdate,"clinisoft");
        allTables = statusDatabaseRepository.getAllTables("clinisoft");
        List<TableStatusInformation> afterUpdate = clinisoftIncrementalLoadService.getTablesFromDataBase("Department", allTables);
        for (TableStatusInformation tableStatusInformation : beforeUpdate){
            assertThat(tableStatusInformation.lastUsedValue,is(findSameUsedValue(afterUpdate,tableStatusInformation.tableName)));
        }
    }

    private BigDecimal findSameUsedValue(List<TableStatusInformation> afterUpdate, String tableName) {
       return afterUpdate.stream()
                .filter(a -> a.tableName.equals(tableName))
                .findFirst()
                .get()
                .lastUsedValue;
    }

    private Timestamp findSameUsedTimeStamp(List<TableStatusInformation> afterUpdate, String tableName) {
        return afterUpdate.stream()
                .filter(a -> a.tableName.equals(tableName))
                .findFirst()
                .get()
                .lastAccessAt;
    }

    private void setupTestStatusData() {
        statusJdbcTemplate.execute("DROP TABLE IF EXISTS integration_status");
        statusJdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('D_PatScores', NULL, 'TIME_COMPARISATION', 'ArchTime', NULL, '1917-12-20 04:17:11.533', 'clinisoft', NULL, NULL, NULL, NULL, NULL, NULL, 'Department', 'ArchStatus,ArchTime,Datetime,PatientID,Type,Value', NULL, 'FULL_TABLE', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('P_Allergy', 'patientId', 'HISTORY_TABLE_LOOKUP', NULL, 1, '2017-12-05 01:15:16.837', 'clinisoft', NULL, NULL, NULL, NULL, NULL, NULL, 'Patient', 'AllergenID,AllergenName,AllergyID,CheckedBy,EnteredBy,EnterTime,IdentificationDate,Notes,PatientID,SeverityID,TypeID', NULL, 'HISTORY_TABLE_LOOKUP', NULL, NULL, NULL)");
        statusJdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('S_AllergenRef', NULL, 'FULL_TABLE', NULL, NULL, '2017-12-20 09:37:18.860', 'clinisoft', NULL, NULL, NULL, NULL, NULL, NULL, 'System', 'AllergenID,ArchStatus,ArchTime,CompID,Description,ExtCode,Name,TypeID', NULL, 'FULL_TABLE', NULL, NULL, NULL)");
    }
}
