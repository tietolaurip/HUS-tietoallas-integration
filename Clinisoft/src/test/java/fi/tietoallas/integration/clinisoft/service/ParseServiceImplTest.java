package fi.tietoallas.integration.clinisoft.service;


import fi.tietoallas.integration.clinisoft.services.ParseServiceImpl;
import fi.tietoallas.integration.common.domain.LineInfo;
import fi.tietoallas.integration.common.domain.ParseTableInfo;
import fi.tietoallas.integration.common.utils.CommonConversionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class ParseServiceImplTest {

    ParseServiceImpl parseService = new ParseServiceImpl();


    @Test
    public void testParseColumn(){
        LineInfo columnInfo = parseService.getColumnInfo("VariableDescription       VarChar(255) Description");
        assertThat(columnInfo.rowName,is("VariableDescription"));
        assertThat(columnInfo.dataType,is("VarChar(255)"));
        assertThat(columnInfo.description,is("Description"));
    }

    @Test
    public void testParse() throws Exception {
        List<ParseTableInfo> tableInfos = parseService.parseCSV("/home/antti/newsource/integrations/Clinisoft/src/main/resources/ccc.txt","clinisoft");
        assertThat(tableInfos, notNullValue());
        assertThat(tableInfos, hasSize(59));
        FileWriter tableFileWriter = new FileWriter("table_metadata.csv");
        FileWriter columnFileWriter = new FileWriter("column_metadata.csv");
        tableFileWriter.write(ParseServiceImpl.TABLE_CSV_TOP_LINE);
        tableFileWriter.append("\n");
        columnFileWriter.write(ParseServiceImpl.COLUMN_CSV_TOP_LINE);
        columnFileWriter.append("\n");
        for (ParseTableInfo info : tableInfos) {
            tableFileWriter.append(CommonConversionUtils.generateTableCSVLine(info));
            tableFileWriter.append("\n");
            for (LineInfo lineInfo : info.lines) {
                columnFileWriter.append(CommonConversionUtils.generateColumnCSVLine(lineInfo, info.tableName, "clinisoft"));
                columnFileWriter.append("\n");
            }
        }
        tableFileWriter.close();
        tableFileWriter.close();


    }
}


