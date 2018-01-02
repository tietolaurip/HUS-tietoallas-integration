package fi.tietoallas.integration.clinisoft.services;

import fi.tietoallas.integration.clinisoft.ClinisoftIntegrationApplication;
import fi.tietoallas.integration.clinisoft.repository.ClinisoftRepository;
import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.domain.SchemaGenerationInfo;
import fi.tietoallas.integration.common.domain.TableStatusInformation;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.common.utils.CommonConversionUtils;
import fi.tietoallas.integration.common.utils.TietoallasKafkaProducer;
import fi.tietoallas.integration.metadata.jpalib.configuration.JpaDbConfig;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import fi.tietoallas.integration.metadata.jpalib.domain.Table;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static fi.tietoallas.integration.clinisoft.ClinisoftIntegrationApplication.HIVE_STOREGE_DB_NAME;
import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;
import static fi.tietoallas.integration.common.utils.DynamicAvroTools.ROWS_NAME;
import static fi.tietoallas.integration.common.utils.DynamicAvroTools.toIntegrationAvroSchema;
import static java.util.stream.Collectors.toList;

@Service
public class ClinisoftLoadService extends LoadService {

    @Autowired
    private StatusDatabaseRepository statusDatabaseRepository;
    @Autowired
    private ClinisoftRepository clinisoftRepository;

    @Autowired
    private DbConfiguration dbConfiguration;
    @Autowired
    private TableRepository tableRepository;
    @Autowired
    private ColumnRepository columnRepository;

    private Environment environment;

    private JpaDbConfig jpaDbConfig;
    private static Logger logger = LoggerFactory.getLogger(ClinisoftLoadService.class);

    public ClinisoftLoadService(Environment environment) {
        super(environment);
        this.environment = environment;
        jpaDbConfig = new JpaDbConfig(environment);
    }

    private final List<String> proxyTables = Arrays.asList("P_CareNotesDC",
            "P_DischSummaryDC",
            "P_DischargeDataDC",
            "P_GeneralDataDC",
            "P_LabAnswerDC",
            "P_LabResDC",
            "P_ObservRecDC",
            "P_LabResDupl",
            "SV_CareUnitRoomsBedsDC",
            "SV_CareUnitStaffDC",
            "SV_DepartmentsDC",
            "SV_PatGroupCareUnitsDC",
            "SV_PatGroupPharmaProgsDC",
            "SV_PatGroupsDC",
            "SV_PharmaGroupPharmasDC",
            "SV_PharmaGroupsDC",
            "SV_PharmaOrdersDC",
            "SV_PharmaOrdersPreparationsDC",
            "SV_PharmaProductsDC",
            "SV_PharmaProgOrdersDC",
            "SV_PharmaProgsDC",
            "SV_StaffPrivilegesDC",
            "labresduplat",
            "pharmaordersdupli",
            "tables_count",
            "S_BedRoomWardViewDC",
            "S_PatientGroupDC",
            "D_MaterialStatsDC",
            "D_PatCostsDC",
            "D_PatDgsDC",
            "D_PatScoresDC",
            "D_PatStatsDC",
            "VersionHistoryDC",
            "D_BedPlaceStatsDC",
            "D_RemoteSQL",
            "varmuusseqvars",
            "CCCSysLogins",
            "CCCUserRoles",
            "CCCUsers");

    @Override
    public void initialSetup(String integration, boolean usesViews) throws Exception {

        logger.info("Starting initial setup for " + integration);

        JdbcTemplate jdbcTemplate = dbConfiguration.hiveJdbcTemplate(dbConfiguration.hiveDatasource());
        if (jdbcTemplate == null) {

            logger.error("clinisoft repository null");
            throw new RuntimeException();
        }
        String sql = "CREATE DATABASE IF NOT EXISTS staging_" + integration;
        jdbcTemplate.execute(sql);
        String storageSql = "CREATE DATABASE IF NOT EXISTS varasto_" + integration + "_historia_log";
        jdbcTemplate.execute(storageSql);

        List<String> allTables = clinisoftRepository.getAllTables();
        logger.info("alltables size " + allTables.size());
        List<String> validTables = allTables.stream()
                .filter(s -> !proxyTables.contains(s))
                .filter(s -> !StringUtils.containsIgnoreCase(s, "DC"))
                .filter(s -> !StringUtils.contains(s, "bak_"))
                .filter(s -> !StringUtils.startsWith(s, "test"))
                .collect(toList());
        logger.info("valid tables size " + validTables.size());
        logger.info("BEGIN: Processing tables...");
        logger.info("   Processing tables for Patient(default)-database...");
        for (String table : validTables) {
            ParseTableInfo tableInfo = clinisoftRepository.getPatientTableInfo(table, integration);
            Table metadata = new Table(integration, table);
            metadata.setMetadataLastUpdated(new Date());
            metadata.setName(convertTableName(table));
            metadata.setOrigTableName(table);
            tableRepository.saveAndFlush(metadata);
            statusDatabaseRepository.insertTableInfo(tableInfo);
            logger.info("      Processing table: " + table);
            List<Column> columnInfos = clinisoftRepository.getColumnInfo(table, "patientId", integration);
            saveColumnInfos(columnInfos);
            String stagingStatement = clinisoftRepository.createStatement(table, integration, true, tableInfo.columnQuery);
            jdbcTemplate.execute(stagingStatement);
            String storageStatement = clinisoftRepository.createStatement(table, integration, false, tableInfo.columnQuery);
            jdbcTemplate.execute(storageStatement);
        }
        clinisoftRepository.changeDatabase("System");
        logger.info("   Processing tables for System-database...");
        allTables = clinisoftRepository.getAllTables();
        validTables = allTables.stream()
                .filter(s -> !proxyTables.contains(s))
                .filter(s -> !StringUtils.containsIgnoreCase(s, "DC"))
                .filter(s -> !StringUtils.contains(s, "bak_"))
                .filter(s -> !StringUtils.startsWith(s, "test"))
                .collect(toList());
        initialSetupForDepartmentOrSystemTables(integration, validTables, jdbcTemplate, true);
        clinisoftRepository.changeDatabase("Department");
        logger.info("   Processing tables for Department-database...");
        List<String> tables = clinisoftRepository.getAllTables();
        validTables = tables.stream()
                .filter(s -> !proxyTables.contains(s))
                .filter(s -> !StringUtils.containsIgnoreCase(s, "DC"))
                .filter(s -> !StringUtils.contains(s, "bak_"))
                .filter(s -> !StringUtils.startsWith(s, "test"))
                .collect(toList());
        initialSetupForDepartmentOrSystemTables(integration, validTables, jdbcTemplate, false);
        logger.info("END: Processing tables.");

    }

    private void saveColumnInfos(final List<Column> columnInfos) {
        if (columnInfos != null) {
            logger.info("column info size " + columnInfos.size());
            columnRepository.save(columnInfos);
            columnRepository.flush();
        } else {
            logger.error("column info null");
        }

    }

    private void initialSetupForDepartmentOrSystemTables(String integration, List<String> tables, JdbcTemplate jdbcTemplate, boolean isFullTable) {
        for (String table : tables) {
            logger.info("      Processing table: " + table);
            ParseTableInfo tableInfo;
            String primaryKeyColumn = null;
            if (isFullTable) {
                tableInfo = clinisoftRepository.getSystemTableInfo(table, integration);
            } else {
                tableInfo = clinisoftRepository.getDepartmentTableInfo(table, integration);
                primaryKeyColumn = "ArchTime";
            }
            Table metadata = new Table.Builder()
                    .withMetadataLastUpdated(new Date())
                    .withDataSetName(integration)
                    .withName(convertTableName(table))
                    .withOrigTableName(table)
                    .build();
            tableRepository.saveAndFlush(metadata);
            statusDatabaseRepository.insertTableInfo(tableInfo);
            List<Column> columnInfos = clinisoftRepository.getColumnInfo(table, primaryKeyColumn, integration);
            saveColumnInfos(columnInfos);
            String stagingStatement = clinisoftRepository.createStatement(table, integration, true, tableInfo.columnQuery);
            jdbcTemplate.execute(stagingStatement);
            String storageStatement = clinisoftRepository.createStatement(table, integration, false, tableInfo.columnQuery);
            jdbcTemplate.execute(storageStatement);
        }

    }

    public void initialLoad(final String integrationName) throws Exception {
        throw new Exception("not in use at this integration");

    }

    public void incrementalLoad(String integration) throws Exception {
        Properties kafkaProperties = CommonConversionUtils.getKafkaProperties(environment);
        TietoallasKafkaProducer producer = new TietoallasKafkaProducer(kafkaProperties);
        List<TableStatusInformation> allTables = statusDatabaseRepository.getAllTables(integration);
        List<TableStatusInformation> patientTables = allTables.stream()
                .filter(t -> t.originalDatabase.equalsIgnoreCase("Patient"))
                .collect(toList());

        List<String> patientIds = clinisoftRepository.findPatientIds(patientTables.get(0))
                .stream()
                .map(String::valueOf)
                .collect(toList());
        for (TableStatusInformation table : patientTables) {
            String sql = generateSql(table, patientIds, "patientId", table.columnQuery);
            List<SchemaGenerationInfo> info = clinisoftRepository.getColumnNames(table.tableName);
            if (info != null && !info.isEmpty()) {
                Schema recordSchema = new Schema.Parser().parse(toIntegrationAvroSchema(integration, info.get(0)));
                List<GenericRecord> rows = clinisoftRepository.getNewData(sql, table.tableName);

                if (rows != null && !rows.isEmpty()) {
                    logger.info("rows size " + rows.size());

                    sendToKafka(producer, rows, recordSchema.getTypes().get(1), recordSchema, table.tableName, integration);

                }
            }
        }
        List<TableStatusInformation> departmentTables = allTables.stream()
                .filter(s -> s.originalDatabase.equalsIgnoreCase("Department"))
                .collect(toList());
        List<String> archiveIds = clinisoftRepository.findArchiveIds(departmentTables.get(0).lastAccessAt);


    }


    private String generateSql(final TableStatusInformation tableStatusInformation, List<String> ids, String keyColumn, String columns) {
        return "select " + columns + " from " + tableStatusInformation.tableName + "where " + keyColumn + " in (" + StringUtils.join(ids, ",") + ")";
    }

    private void sendToKafka(TietoallasKafkaProducer tietoallasKafkaProducer, List<GenericRecord> rows,
                             Schema messageSchema, Schema fullSchema, String tableName, String integration) throws Exception{
        GenericRecord message = new GenericData.Record(messageSchema);
        message.put(ROWS_NAME, rows);
        List<GenericRecord> genericRecords = (ArrayList<GenericRecord>) message.get(ROWS_NAME);
        for (GenericRecord record : genericRecords) {
            Schema schema = record.getSchema();
            for (Schema.Field field : schema.getFields()) {
                logger.info("===============field " + field.name() + " has value " + String.valueOf(record.get(field.name())));
            }
        }
        String separationChar = "\u00ad";
        logger.info("topic " + integration);
        tietoallasKafkaProducer.run(messageSchema, message, integration + "." + tableName + separationChar + messageSchema.toString(), integration + "-kafka");
        statusDatabaseRepository.updateTableTimeStamp(integration, tableName);
    }


}
