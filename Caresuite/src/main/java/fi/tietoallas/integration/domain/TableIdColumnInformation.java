package fi.tietoallas.integration.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TableIdColumnInformation {

    public final String table;
    public final BigDecimal id;
    public final String columnName;
    public final Timestamp created;

    public TableIdColumnInformation(String table, BigDecimal id, String columnName, Timestamp created) {
        this.table = table;
        this.id = id;
        this.columnName=columnName;
        this.created=created;
    }
    public BigDecimal getId(){
        return id;
    }

    public String getTable(){
        return table;
    }
}
