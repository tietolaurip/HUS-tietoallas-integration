package fi.tietoallas.integration.staginstreamconsumer;

import java.io.Serializable;

public class PseudoInformation implements Serializable{
    public final String integration;
    public final String field;
    public final String pseudoFunction;
    public final String table;


    public PseudoInformation(String field, String pseudoFunction,String integration,String table) {
        this.field = field;
        this.pseudoFunction = pseudoFunction;
        this.integration=integration;
        this.table=table;
    }
    public String getField(){
        return field;
    }
}
