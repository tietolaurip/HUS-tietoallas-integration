package fi.tietoallas.integration.staginstreamconsumer.util;

import org.apache.avro.Schema;

import java.io.Serializable;

public class MetaInformation implements Serializable{

    public String integration;
    public String table;
    public String schemaAsString;

    public MetaInformation(String integration, String table, String schema) {
        this.integration = integration;
        this.table = table;
        this.schemaAsString = schema;
    }
    public static MetaInformation create(final String integration,final String table,final String schemaAsString){
        return new MetaInformation(integration,table,schemaAsString);
    }
}
