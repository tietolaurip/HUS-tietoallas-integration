package fi.tietoallas.integration.clinisoft.domain;

import java.math.BigDecimal;

public class TableIdValuePair {
    public final String tableName;
    public final BigDecimal idValue;

    public TableIdValuePair(String tableName, BigDecimal idValue) {
        this.tableName = tableName;
        this.idValue = idValue;
    }
    public String getTableName(){
        return this.tableName;
    }
}
