package fi.tietoallas.integration.service;

import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import fi.tietoallas.integration.metadata.jpalib.domain.Table;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import fi.tietoallas.integration.repository.CareSuiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static fi.tietoallas.integration.common.utils.CommonConversionUtils.convertTableName;

@Service
public class CaresuiteLoadService extends LoadService{

    @Autowired
    private Environment environment;
    @Autowired
    private StatusDatabaseRepository statusRepository;
    @Autowired
    private CareSuiteRepository careSuiteRepository;
    @Autowired
    private ColumnRepository columnRepository;
    @Autowired
    private TableRepository tableRepository;

    private DbConfiguration dbConfiguration;

    private static Logger logger = LoggerFactory.getLogger(CaresuiteLoadService.class);

    private final List<String> usedTables = Arrays.asList("ADMISSIONAMBULATORYSTATUS",
            "ADMISSIONCHIEFCOMPLAINT",
            "ADMISSIONDIAGNOSIS",
            "ADMISSIONS",
            "ADMISSIONTYPES",
            "ADTSTATES",
            "AIRWAYCLASSES",
            "ALLERGIES",
            "ALLERGYTYPES",
            "AMBULATORYSTATUS",
            "ANALYSES",
            "ANALYSISTYPES",
            "APPLICATIONROLES",
            "APPLICATIONS",
            "APPLICATIONVERSION",
            "ASATYPES",
            "ASSESSMENTITEMS",
            "ATTENDINGTYPES",
            "BLOODGROUPS",
            "CATEGORIES",
            "CATEGORYTYPES",
            "CFGVALUES",
            "CHIEFCOMPLAINTS",
            "CLINICALPRIORITIES",
            "CLINICALROLEATTENDINGTYPE",
            "CLINICALROLES",
            "CNLCODES",
            "COMBINEDORDERS",
            "COMBINEDTASKS",
            "COMMONCHOICES",
            "COMMONCHOICETYPES",
            "COMPONENTS",
            "COMPONENTTYPES",
            "COUNTRIES",
            "CURRENTMEDFREQUENCIES",
            "CURRENTMEDICATION",
            "CURRENTMEDICATIONSTATUS",
            "DATABASEVERSION",
            "DEPARTMENTS",
            "DEPARTMENTTYPES",
            "DIAGFUNCTIONALTYPE",
            "DISCHARGES",
            "ENCOUNTERS",
            "ENVIRONMENTLOCATION",
            "ENVIRONMENTMEDICALPROCEDURE",
            "ENVIRONMENTS",
            "ENVIRONMENTTYPEGROUPS",
            "ENVIRONMENTTYPES",
            "ETHNICGROUPS",
            "EVENTCATEGORIES",
            "EVENTDATA",
            "EVENTPAIRS",
            "EVENTS",
            "EVENTTYPES",
            "FACILITIES",
            "FACILITYUSEAREA",
            "FAMILIES",
            "FAMILYBEHAVIOR",
            "FORMS",
            "FORMTYPES",
            "GROUPGROUP",
            "GROUPMAPPINGS",
            "GROUPS",
            "GROUPSTAFF",
            "H_DIAGNOSES",
            "H_DIAGNOSISTYPES",
            "H_MEDICALPROCEDURES",
            "H_MEDICALPROCEDURETYPES",
            "KINTYPES",
            "LABDATASTATUS",
            "LABRESULTAUDITED",
            "LABRESULTS",
            "LABSOURCES",
            "LABTESTS",
            "LATERALITIES",
            "LOCATIONS",
            "MARITALS",
            "MEDICATIONS",
            "MEDICATIONTYPEMEDICATION",
            "MEDICATIONTYPES",
            "MEDPROCFUNCTIONALTYPE",
            "MEMOS",
            "NEARESTKIN",
            "ORDERADDITIVE",
            "ORDERMODIFICATIONTYPES",
            "ORDERS",
            "ORDERSETS",
            "ORDERTASKSTATUS",
            "ORDERTYPES",
            "PARTCOMPONENT",
            "PARTS",
            "PATIENTALLERGY",
            "PATIENTALLERGYREACTION",
            "PATIENTINSURANCECOMPANY",
            "PATIENTPRECAUTION",
            "PATIENTS",
            "PCS_EVENTDATA",
            "PERIODS",
            "PICISDATA",
            "PRECAUTIONS",
            "PRECAUTIONTYPES",
            "REACTIONS",
            "REACTIONTYPES",
            "RELIGIONS",
            "ROOMS",
            "ROUTES",
            "ROUTETYPES",
            "RTDATA",
            "S_DIAGNOSES",
            "S_DIAGNOSISTYPES",
            "S_MEDICALPROCEDURES",
            "S_MEDICALPROCEDURETYPES",
            "SAVEDREPORTS",
            "SCHEDULES",
            "SCOREGROUPS",
            "SCOREITEMS",
            "SEXES",
            "SITES",
            "STAFF",
            "STAFFATTENDINGTYPE",
            "STAFFATTENDINGTYPEENVIRON",
            "STAFFTYPES",
            "STDADDITIVES",
            "STDCONDITIONS",
            "STDORDERADDITIVE",
            "STDORDERORDERSET",
            "STDORDERS",
            "TASKS",
            "TIMEZONES",
            "TREATMENTDATA",
            "TREATMENTFORMTYPE",
            "TREATMENTROUTETYPE",
            "TREATMENTS",
            "TREATMENTUNITTYPE",
            "UNITS",
            "UNITTIMES",
            "UNITTYPES");

    @Autowired
    public CaresuiteLoadService(Environment environment) {

        super(environment);
        this.environment=environment;
        dbConfiguration = new DbConfiguration(environment);
        statusRepository = new StatusDatabaseRepository(dbConfiguration.jdbcTemplate());
       }

    @Override
    public void initialSetup(final String integration,boolean useViews) throws Exception{
        String storageLocation = environment.getProperty("fi.datalake.storage.adl.location");
        String hiveStrorage = "varasto_"+ integration+"_historia_log";
        JdbcTemplate jdbcTemplate = dbConfiguration.hiveJdbcTemplate(dbConfiguration.hiveDatasource());
        String sql = "CREATE DATABASE IF NOT EXISTS " +environment.getProperty("fi.datalake.targetdb.name");
        jdbcTemplate.execute(sql);
        String storageSql = "CREATE DATABASE IF NOT EXISTS  "+hiveStrorage;
        jdbcTemplate.execute(storageSql);
        List<String> allTables = careSuiteRepository.getAllTables().stream()
                .filter(s -> usedTables.contains(s))
                .collect(Collectors.toList());
        for (String table : allTables){
            logger.info("creating table "+table);
            String columnQuery = careSuiteRepository.getQueryColumns(table);
            ParseTableInfo parseTableInfo = new ParseTableInfo.Builder()
                    .withTableName(table)
                    .withIntegrationName(integration)
                    .withColumnQuery(columnQuery)
                    .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                    .withLines(new ArrayList<>())
                    .build();
            if (parseTableInfo != null) {
                statusRepository.insertTableInfo(parseTableInfo);
            }
            Table jpaTable = new Table(convertTableName(table),integration);
            jpaTable.setOrigTableName(table);
            jpaTable.setMetadataLastUpdated(new Date());
            jpaTable.setDataSetName(integration);
            jpaTable.setName(convertTableName(table));
            tableRepository.saveAndFlush(jpaTable);
            List<Column> columns = careSuiteRepository.getColumnInfos(table,integration,careSuiteRepository.getPrimaryKeys(table));
            logger.info("================size of column info"+columns.size());
            for(Column column : columns){
                columnRepository.saveAndFlush(column);
            }
            String statement = careSuiteRepository.createStatement(table, integration,true,columnQuery,storageLocation);
            jdbcTemplate.execute(statement);
            String storageStatement = careSuiteRepository.createStatement(table,integration,false,columnQuery,storageLocation);
            jdbcTemplate.execute(storageStatement);
        }
    }

}
