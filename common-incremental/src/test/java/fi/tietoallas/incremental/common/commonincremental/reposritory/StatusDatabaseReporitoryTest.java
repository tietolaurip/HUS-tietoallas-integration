package fi.tietoallas.incremental.common.commonincremental.reposritory;

/*-
 * #%L
 * common-incremental
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

import com.zaxxer.hikari.HikariDataSource;
import fi.tietoallas.incremental.common.commonincremental.domain.TableStatusInformation;
import fi.tietoallas.incremental.common.commonincremental.repository.StatusDatabaseRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class StatusDatabaseReporitoryTest {

    private static StatusDatabaseRepository statusDatabase;

    private static DataSource dataSource;

    private static JdbcTemplate jdbcTemplate;
    @BeforeClass
    public static void setUp() throws Exception{
        dataSource = newH2Connection();
        jdbcTemplate = new JdbcTemplate(dataSource);
        statusDatabase = new StatusDatabaseRepository(jdbcTemplate);
        createTable();
    }
    @Test
    public void testSelectAll() {
        List<TableStatusInformation> allTables = statusDatabase.getAllTables("ca");
        assertThat(allTables,hasSize(3));
    }
    @Test
    public void testPairs(){
        //only full_table settigns are included
        List<TableStatusInformation> tablePairs = statusDatabase.getTablePairs("ca");
        assertThat(tablePairs,hasSize(1));
    }
    @Test
    public void testupdateTableInformation(){
        statusDatabase.updateTableInformation("ca","V_PatientCaseAge",new BigDecimal(10));
    }
    @Test
    public void testupdateTableTimeStamp()
    {
        statusDatabase.updateTableTimeStamp("ca","V_Location");
    }
    @Test
    public void testGetTimetableUpdateInfo(){
        List<TableStatusInformation> timetableUpdateInfo = statusDatabase.getTimetableUpdateInfo("ca");
        assertThat(timetableUpdateInfo,hasSize(1));
    }
    @Test
    public void testGetLastRunTimeForIntegration(){
        Timestamp lastRunTimeForIntegration = statusDatabase.getLastRunTimeForIntegration("ca");
        assertThat(lastRunTimeForIntegration.toLocalDateTime().getDayOfMonth(),is(LocalDateTime.now().getDayOfMonth()));
        assertThat(lastRunTimeForIntegration.toLocalDateTime().getMonthValue(),is(LocalDateTime.now().getMonthValue()));
        assertThat(lastRunTimeForIntegration.toLocalDateTime().getYear(),is(LocalDateTime.now().getYear()));
    }
    @Test
    public void testselectUsingXON(){
        List<TableStatusInformation> informations = statusDatabase.selectUsingXON("ca");
        assertThat(informations,hasSize(1));
    }

    private static void createTable(){
        jdbcTemplate.execute("CREATE TABLE integration_status(table_name VARCHAR(255) NOT NULL,key_column VARCHAR(255),parameter_type VARCHAR(255),time_column VARCHAR(255), last_used_value NUMERIC(21),last_run_at DATETIME,integration_name VARCHAR(80) NOT NULL,search_column VARCHAR(255),incremental_query   VARCHAR(255),lowerBound NVARCHAR(255),upperBound NVARCHAR(255),partitionColumn NVARCHAR(255),custom_sql NVARCHAR(1000),original_database   VARCHAR(255),column_query VARCHAR(MAX),numPartitions VARCHAR(5),orig_parameter_type VARCHAR(255),loadStart VARCHAR(255),loadStop VARCHAR(255),interval BIGINT,PRIMARY KEY (table_name, integration_name))");
        jdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_Location', NULL, 'TIME_COMPARISATION', 'Loc_UpdateTime', NULL, '2016-02-17 04:00:48.610', 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Loc_ID,Loc_UpdateTime,Loc_IDRoomGroup,Loc_RoomID,Loc_RoomMapcode,Loc_RoomIdent,Loc_RoomAbbreviation,Loc_RoomName,Loc_RoomType,Loc_IDDepartmentGroup,Loc_DepartmentID,Loc_DepartmentMapcode,Loc_DepartmentIdent,Loc_DepartmentAbbreviation,Loc_DepartmentName,Loc_DepTypeMapcode,Loc_DepTypeName,Loc_DepTypeAbb', NULL, 'TIME_COMPARISATION', NULL, NULL, NULL)");
        jdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_Route', 'Rou_DocID', 'FULL_TABLE', NULL, NULL, NULL, 'ca', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Rou_ID,Rou_DocID,Rou_Name,Rou_Description,Rou_Medication,Rou_Infusion,Rou_Insertion,Rou_MapCode,Rou_MapName,Rou_Abbreviation', NULL, 'FULL_TABLE', NULL, NULL, NULL)");
        jdbcTemplate.update("INSERT INTO integration_status (table_name, key_column, parameter_type, time_column, last_used_value, last_run_at, integration_name, search_column, incremental_query, lowerBound, upperBound, partitionColumn, custom_sql, original_database, column_query, numPartitions, orig_parameter_type, loadStart, loadStop, interval) VALUES ('V_PatientCaseAge', 'PatC_ID', 'HISTORY_TABLE_LOOKUP', null, 273, null, 'ca', null, null, null, null, null, null, null, 'PatC_ID,PatC_AgeInYears,PatC_AgeInMonths,PatC_AgeInDays', null, 'STATUSDB_TABLE_LOOKUP', null, null, null)");

    }

    private static DataSource newH2Connection() throws Exception {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("org.h2.Driver");
        hikariDataSource.setJdbcUrl("jdbc:h2:mem:test");
        return hikariDataSource;
    }
}