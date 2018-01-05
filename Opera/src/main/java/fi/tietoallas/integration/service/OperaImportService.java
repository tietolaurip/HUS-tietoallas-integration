package fi.tietoallas.integration.service;

/*-
 * #%L
 * opera
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
import fi.tietoallas.integration.common.configuration.DbConfiguration;
import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.repository.StatusDatabaseRepository;
import fi.tietoallas.integration.common.services.LoadService;
import fi.tietoallas.integration.metadata.jpalib.domain.Column;
import fi.tietoallas.integration.metadata.jpalib.domain.Table;
import fi.tietoallas.integration.metadata.jpalib.repository.ColumnRepository;
import fi.tietoallas.integration.metadata.jpalib.repository.TableRepository;
import fi.tietoallas.integration.repository.OperaRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class OperaImportService  {

    public static final String INTEGRATION_NAME = "opera";
    @Autowired
    private StatusDatabaseRepository statusDatabaseRepository;

    private Environment environment;
    @Autowired
    private OperaRepository operaRepository;

    private Logger logger = LoggerFactory.getLogger(OperaImportService.class);
    @Autowired
    @Qualifier(DbConfiguration.HIVE_JDBC_TEMPLATE)
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ColumnRepository columnRepository;
    @Autowired
    private TableRepository tableRepository;
    private final List<String> proxyTables = Arrays.asList("vsRaison_BT_Changement_Cap",
            "vsRecInterventions_Associees",
            "vsRecVisite",
            "vsReferentiel_Organisation",
            "vsRequete",
            "vsRequete_hist_prog",
            "vsRessource_Requise",
            "vsRisque",
            "vsSite",
            "vsSpecialite_Ref",
            "vsStatut_Programme",
            "vsStringOpera",
            "vsSuivi_Ref",
            "vsTechnique_Operatoire_Ref",
            "vsTemps_Operatoires",
            "vsType_Anesthesie_Ref",
            "vsType_Visite_Ref",
            "vsUsager",
            "vsUser",
            "vsUtilisateur",
            "vsProtocoles",
            "hus",
            "HUSDWVIEW",
            "sysconstraints",
            "vsAllergie",
            "vsAnesthesie",
            "vsAppel",
            "vsCampus",
            "vsCDJ",
            "vsChariot",
            "vsChariot_verifie",
            "vsCodification_Actes",
            "vsCommentaire",
            "vsCondition_Prealable",
            "vsCustomProcedures",
            "vsCustomRequest",
            "vsDeioRequestList",
            "vsDelta_Requete",
            "vsEtats",
            "vsFiche_Urgence",
            "vsGrille_Infection",
            "vsHoraire_BlocsTemps",
            "vsHoraire_Ressource",
            "vsHoraire_Salles",
            "vsInfo_Tracabilite",
            "vsIntervenant_Ref",
            "vsIntervention_Ref",
            "vsInterventions_Sec",
            "vsListe_verification",
            "vsListe_verification_salle",
            "vsLocalisation_Ref",
            "vsLog_Transactions",
            "vsNiveau_Priorite_Ref",
            "vsNomenclature",
            "vsNotes_Particulieres",
            "vsOperaExecRechercheRefresh",
            "vsOperation",
            "vsParticipant_CUSTOM",
            "vsPatient_Non_Disponibilite",
            "vsPersonnel",
            "vsPersonnel_Heures",
            "vsPosition_fr_Ref",
            "vsPreferences_Base",
            "vsPrerequis_Operatoire",
            "vsProduit",
            "vsProduit_fournisseur",
            "vsProduit_localisation",
            "vsProduit_manufacturier",
            "vsProduit_verifie",
            "VsProduits_Consommes",
            "vsProfessionnels_associes",
            "vsProgramme",
            "vsProgramme_Verrouille");
    public OperaImportService(Environment environment) {
        this.environment = environment;

    }

    public void initialSetup(final String integration, boolean useViews) throws Exception {
        if (jdbcTemplate == null) {
            logger.error("hive jdbc template null");
        }
        String storageLocation = environment.getProperty("fi.datalake.storage.adl.location");
        //vsCustom_, CustomVaihto, CustomYesterDay, GEHC, vsNI_
        List<String> allDbViews = operaRepository.getAllDbViews().stream()
                .filter(s -> !StringUtils.containsIgnoreCase(s,"vsGEHC_") && !StringUtils.containsIgnoreCase(s,"vsCustom_")
                && !StringUtils.containsIgnoreCase(s,"CustomVaihto") && !StringUtils.containsIgnoreCase(s,"CustomYesterDay") &&!StringUtils.containsIgnoreCase(s,"GEHC") &&
                !StringUtils.containsIgnoreCase(s,"vsNI_") && !StringUtils.containsIgnoreCase(s,"testiview") && !StringUtils.containsIgnoreCase(s,"TWa_Test")
                && !StringUtils.containsIgnoreCase(s,"VIEW1"))
                .filter(s -> !proxyTables.contains(s))
                .collect(Collectors.toList());

        @SuppressWarnings("SqlNoDataSourceInspection") String sql = "CREATE DATABASE IF NOT EXISTS staging_opera";
        jdbcTemplate.execute(sql);
        @SuppressWarnings("SqlNoDataSourceInspection") String storageSql = "CREATE DATABASE IF NOT EXISTS varasto_opera_historia_log";
        jdbcTemplate.execute(storageSql);
        for (String table : allDbViews) {
            logger.info("====================table is been processes "+table);
            String columnQuery = operaRepository.getColumnOnlyNames(table);
            ParseTableInfo tableInfo = operaRepository.getPatientTableInfo(table, integration, columnQuery);
            Table jpaTable = new Table(INTEGRATION_NAME, convertTableName(table));
            jpaTable.setMetadataLastUpdated(new Date());
            jpaTable.setOrigTableName(table);
            tableRepository.saveAndFlush(jpaTable);
            for (LineInfo info : tableInfo.lines) {
                Column column = new Column(INTEGRATION_NAME, convertTableName(table), convertTableName(info.rowName));
                column.setOrigType(info.dataType);
                logger.info("===================writing original_column_name "+info.rowName+ " as value");
                column.setOrigColumnName(info.rowName);
                column.setMetadataLastUpdated(new Date());
                columnRepository.saveAndFlush(column);
            }

            logger.info("creating table " + table);

            statusDatabaseRepository.insertTableInfo(new ParseTableInfo.Builder()
                    .withColumnQuery(columnQuery)
                    .withIntegrationName(INTEGRATION_NAME)
                    .withTableName(table)
                    .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                    .withLines(new ArrayList<>())
                    .build());
            String statement = operaRepository.createStatement(table, INTEGRATION_NAME, true, columnQuery,storageLocation);
            jdbcTemplate.execute(statement);
            String storageStatement = operaRepository.createStatement(table, INTEGRATION_NAME, false, columnQuery,storageLocation);
            jdbcTemplate.execute(storageStatement);
            logger.info(table + " created");
        }
    }

}

