package fi.tietoallas.integration.metadata.jpalib.domain;

/*-
 * #%L
 * jpa-lib
 * %%
 * Copyright (C) 2017 - 2018 Tieto Finland Oyj
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
import javax.persistence.*;
import java.util.Date;
import javax.persistence.Table;

@Entity
@Table(name="data_column")
public class Column {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @javax.persistence.Column(name = "id", updatable = false, nullable = false)
    protected long id;

    @javax.persistence.Column(name = "data_column_name")
    protected String name;
    @javax.persistence.Column(name = "orig_column_name")
    protected String origColumnName;
    @javax.persistence.Column(name = "data_table_name")
    protected String tableName;
    @javax.persistence.Column(name = "data_set_name")
    protected String dataSetName;
    @javax.persistence.Column(name = "orig_type")
    protected String origType;
    @javax.persistence.Column(name = "hive_type")
    protected String hiveType;
    protected String description;
    @javax.persistence.Column(name ="code_system")
    protected String codeSystem;
    protected String sources;
    @javax.persistence.Column(name = "formation_rule")
    protected String formationRule;
    @javax.persistence.Column(name = "is_primary_key")
    protected Boolean isPrimaryKey;
    @javax.persistence.Column(name ="foreign_key")
    protected String foreignKey;
    protected String format;
    @javax.persistence.Column(name = "unit_of_measure")
    protected String unitOfMeasure;
    @javax.persistence.Column(name = "pseudonymization_function")
    protected String pseudonymizationFunction;
    protected String status;
    protected String comment;
    @javax.persistence.Column(name = "source_document")
    protected String sourceDocument;

    @javax.persistence.Column(name = "metadata_last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date metadataLastUpdated;

    private Column(String name, String origColumnName, String tableName, String dataSetName, String origType, String description, String codeSystem, String sources, String formationRule, Boolean isPrimaryKey, String foreignKey, String format, String unitOfMeasure, String pseudonymizationFunction, String status, String comment, String sourceDocument, Date metadataLastUpdated, String hiveType) {
        this.name = name;
        this.origColumnName = origColumnName;
        this.tableName = tableName;
        this.dataSetName = dataSetName;
        this.origType = origType;
        this.hiveType = hiveType;
        this.description = description;
        this.codeSystem = codeSystem;
        this.sources = sources;
        this.formationRule = formationRule;
        this.isPrimaryKey = isPrimaryKey;
        this.foreignKey = foreignKey;
        this.format = format;
        this.unitOfMeasure = unitOfMeasure;
        this.pseudonymizationFunction = pseudonymizationFunction;
        this.status = status;
        this.comment = comment;
        this.sourceDocument = sourceDocument;
        this.metadataLastUpdated = metadataLastUpdated;
    }

    protected Column() {}

    public Column(String datasetName, String tableName, String name) {
        this.name = name;
        this.tableName = tableName;
        this.dataSetName = datasetName;
    }
    public Column (Column column){
        this.codeSystem=column.codeSystem;
        this.comment=column.comment;
        this.dataSetName=column.dataSetName;
        this.description=column.description;
        this.foreignKey=column.foreignKey;
        this.format=column.format;
        this.formationRule=column.formationRule;
        this.hiveType=column.hiveType;
        this.id=column.id;
        this.isPrimaryKey=column.isPrimaryKey;
        this.metadataLastUpdated=column.metadataLastUpdated;
        this.name=column.name;
        this.origColumnName=column.origColumnName;
        this.origType=column.origType;
        this.pseudonymizationFunction=column.pseudonymizationFunction;
        this.sourceDocument=column.sourceDocument;
        this.sources=column.sources;
        this.status=column.status;
        this.tableName=column.tableName;
        this.unitOfMeasure=column.unitOfMeasure;
    }
    public Column(String datasetName, String tableName, String name, String origType, String description) {
        this.name = name;
        this.tableName = tableName;
        this.dataSetName = datasetName;
        this.origType=origType;
        this.description=description;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigColumnName() {
        return origColumnName;
    }

    public void setOrigColumnName(String origColumnName) {
        this.origColumnName = origColumnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public String getOrigType() {
        return origType;
    }

    public void setOrigType(String origType) {
        this.origType = origType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getFormationRule() {
        return formationRule;
    }

    public void setFormationRule(String formationRule) {
        this.formationRule = formationRule;
    }

    public Boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getPseudonymizationFunction() {
        return pseudonymizationFunction;
    }

    public void setPseudonymizationFunction(String pseudonymizationFunction) {
        this.pseudonymizationFunction = pseudonymizationFunction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(String sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public Date getMetadataLastUpdated() {
        return metadataLastUpdated;
    }

    public void setMetadataLastUpdated(Date metadataLastUpdated) {
        this.metadataLastUpdated = metadataLastUpdated;
    }
    public static class Builder {
        private String name;
        private String origColumnName;
        private String tableName;
        private String dataSetName;
        private String origType;
        private String hiveType;
        private String description;
        private String codeSystem;
        private String sources;
        private String formationRule;
        private Boolean isPrimaryKey;
        private String foreignKey;
        private String format;
        private String unitOfMeasure;
        private String pseudonymizationFunction;
        private String status;
        private String comment;
        private String sourceDocument;
        private Date metadataLastUpdated;
        public Builder(){

        }
        public Builder withName(final String name){
            this.name=name;
            return this;
        }
        public Builder withOrigColumnName(final String origColumnName){
            this.origColumnName=origColumnName;
            return this;
        }
        public Builder withTableName(final String tableName){
            this.tableName=tableName;
            return this;
        }
        public Builder withDataSetName(final String dataSetName){
            this.dataSetName=dataSetName;
            return this;
        }
        public Builder withOrigType(final String origType){
            this.origType=origType;
            return this;
        }
        public Builder withDescription(final String description){
            this.description=description;
            return this;
        }
        public Builder withCodeSystem(final String codeSystem){
            this.codeSystem=codeSystem;
            return this;
        }
        public Builder withSources(final String sources){
            this.sources=sources;
            return this;
        }
        public Builder withFormationRule(final String formationRule){
            this.formationRule=formationRule;
            return this;
        }
        public Builder withIsPrimaryKey(final Boolean isPrimaryKey){
            this.isPrimaryKey=isPrimaryKey;
            return this;
        }
        public Builder withForeignKey(final String foreignKey){
            this.foreignKey=foreignKey;
            return this;
        }
        public Builder withFormat(final String format){
            this.format=format;
            return this;
        }
        public Builder withuUnitOfMeasure(final String uUnitOfMeasure){
            this.unitOfMeasure=uUnitOfMeasure;
            return this;
        }
        public Builder withPseudonymizationFunction(final String pseudonymizationFunction){
            this.pseudonymizationFunction=pseudonymizationFunction;
            return this;
        }
        public Builder withStatus(final String status){
            this.status=status;
            return this;
        }
        public Builder withComment(final String comment){
            this.comment=comment;
            return this;
        }
        public Builder withSourceDocument(final String sourceDocument){
            this.sourceDocument=sourceDocument;
            return this;
        }
        public Builder withHiveType(final String hiveType){
            this.hiveType=hiveType;
            return this;
        }
        public Builder withMetadataLastUpdated(final Date metadataLastUpdated){
            this.metadataLastUpdated=metadataLastUpdated;
            return this;
        }
        public Column build(){
            return new Column(name,origColumnName,tableName,dataSetName,origType,description,codeSystem,sources,formationRule,isPrimaryKey,foreignKey,format,unitOfMeasure,pseudonymizationFunction,status,comment,sourceDocument,metadataLastUpdated, hiveType);
        }

    }

}
