package fi.tietoallas.integration.clinisoft.services;

import fi.tietoallas.integration.clinisoft.ClinisoftIntegrationApplication;
import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ParseServiceImpl  {

    public static final String TABLE_CSV_TOP_LINE="data_table_name;orig_table_name;data_set_name;description;table_constraints;timestamp_column;deletion_marker_column;status;comment;source_document;metadata_last_updated;parameter_type;time_column";
    public static final String COLUMN_CSV_TOP_LINE = "data_column_name;orig_column_name;data_table_name;data_set_name;orig_type;description;code_system;sources;formation_rule;is_primary_key;foreign_key;format;unit_of_measure;pseudonymization_function;status;comment;source_document;metadata_last_updated";


    public List<ParseTableInfo> parseCSV(final String filename,final String integration) throws Exception {
        Path path = Paths.get(filename);
        Stream<String> lines = Files.lines(path);
        List<String> lineToParse = lines.sequential()
                .filter(line -> !line.contains("Centricity Critical Care — Reporting Manual") && !line.contains("General Electric Company."))
                .map(l -> l.trim())
                .collect(Collectors.toList());
        return getTableInformation(lineToParse,integration);
    }

    private List<ParseTableInfo> getTableInformation(final List<String> lineToParse,String integration) {
        List<ParseTableInfo> parseTableInfos = new ArrayList<>();
        List<String> tableNamesAndDescriptions = lineToParse.stream()
                .sequential()
                .filter(l -> (l.contains("PV_") || l.contains("DV_") || l.contains("SV_") || l.contains("view")) &&
                        !l.contains("Database view description:") && !l.contains("JOIN") && !l.contains("-") && !l.contains("Note:") && !l.contains("maximum table limit"))
                .filter(l -> !l.contains("Int") && !l.contains("Char") && !l.contains("Text") && !l.contains("DateTime") && !l.contains("Float"))
                .map(l -> l.trim())
                .collect(Collectors.toList());
        Map<String, List<String>> tableCommentMap = new HashMap<>();
        List<String> tableNames = new ArrayList<>();
        for (int i = 0; i < tableNamesAndDescriptions.size(); i++) {
            String line = tableNamesAndDescriptions.get(i);
            if (!line.contains(" ")) {
                String table = line;
                List<String> comments = new ArrayList<>();
                int a = i + 1;
                String commentLineCandidate = tableNamesAndDescriptions.get(a);
                while (commentLineCandidate.contains(" ")) {
                    comments.add(commentLineCandidate);
                    a++;
                    if (tableNamesAndDescriptions.size() == a) {
                        break;
                    }
                    commentLineCandidate = tableNamesAndDescriptions.get(a);
                }
                tableCommentMap.put(table, comments);
                tableNames.add(table);
            }

        }

        int start = 0;
        int end = 0;
        for (int t = 0; t < tableNames.size(); t++) {
            String table = tableNames.get(t);
            int tableLine = lineToParse.indexOf(table);
            for (int i = tableLine + 1; i < lineToParse.size(); i++) {

                if (!table.equals(lineToParse.get(i)) && t < tableNames.size() - 1 && lineToParse.get(i).equals(tableNames.get(t + 1))) {
                    end = i - 1;
                }
                if (t == tableNames.size() - 1) {
                    end = lineToParse.size() - 1;
                }

                if (lineToParse.get(i).contains("Column Name") && lineToParse.get(i).contains("Data Type") && lineToParse.get(i).contains("Description") && start == 0) {
                    start = i + 1;
                }
                if (start != 0 && end != 0) {
                    break;
                }
            }
            List<LineInfo> columns = null;
            columns = getColumns(lineToParse.subList(start, end));

            start = 0;
            end = 0;
            parseTableInfos.add(new ParseTableInfo.Builder()
                    .withLines(columns)
                    .withTableName(table)
                    .withTableType(ParseTableInfo.TableType.HISTORY_TABLE_LOOKUP)
                    .withDescription(StringUtils.join(tableCommentMap.get(table)," "))
                    .withIntegrationName(integration)
                    .build());
        }

        return parseTableInfos;
    }

    private List<LineInfo> getColumns(final List<String> lines) {
        return lines.stream()
                .filter(l -> l.contains("Int") || l.contains("Char") || l.contains("Text") || l.contains("DateTime") || l.contains("Float"))
                .map(l -> getColumnInfo(l))
                .collect(Collectors.toList());
    }

    public LineInfo getColumnInfo(String line) {

            int endOfColumnName = line.indexOf(" ");
            int startOfTypeName = notWhiteSpace(line.substring(endOfColumnName))+endOfColumnName;
            int endOfTypeName = line.indexOf(" ", startOfTypeName);

            return new LineInfo.Builder()
                    .withRowName(line.substring(0, endOfColumnName))
                    .withDataType(line.substring(startOfTypeName, endOfTypeName))
                    .withDescription(line.substring(endOfTypeName).trim())
                    .build();


    }
    private int notWhiteSpace(final String where){
        int endPoint = 0;
        while (StringUtils.isBlank(where.substring(endPoint,endPoint+1))){
            endPoint++;
        }
        return endPoint;
    }
}
