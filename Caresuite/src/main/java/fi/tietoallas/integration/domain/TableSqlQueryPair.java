package fi.tietoallas.integration.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TableSqlQueryPair {

    public final String tableName;
    public final String sql;
    public final BigDecimal dboid;
    public final String columnName;
    public final Timestamp lastUpdatedAt;

    public TableSqlQueryPair(String tableName, String columnName, String sql, BigDecimal dboid,
                             Timestamp lastUpdatedAt) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.sql = sql;
        this.dboid=dboid;
        this.lastUpdatedAt=lastUpdatedAt;
    }
    public static class Builder{
        private String tableName;
        private String sql;
        private BigDecimal dboid;
        private String columnName;
        private Timestamp lastUpdatedAt;

        public Builder(){

        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withColumnName(final String columnName){
            this.columnName=columnName;
            return this;
        }
        public Builder withSql(final String sql){
            this.sql=sql;
            return this;
        }
        public Builder withDbIOD(final BigDecimal dboid){
            this.dboid=dboid;
            return this;
        }
        public Builder withLastRunAt(final Timestamp lastUpdatedAt){
            this.lastUpdatedAt=lastUpdatedAt;
            return this;
        }
        public TableSqlQueryPair build(){
            return new TableSqlQueryPair(tableName,columnName,sql,dboid,lastUpdatedAt);
        }
    }
}
