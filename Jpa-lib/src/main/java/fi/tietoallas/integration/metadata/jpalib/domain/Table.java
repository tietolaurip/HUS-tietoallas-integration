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
import javax.persistence.Column;
import javax.persistence.*;
import java.util.Date;


@Entity
@javax.persistence.Table(name="data_table")
public class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    protected long id;

    @Column(name = "data_table_name")
    protected String name;
    @Column(name = "orig_table_name")
    protected String origTableName;
    @Column(name = "data_set_name")
    protected String dataSetName;
    protected String description;
    @Column(name = "table_constraints")
    protected String tableConstraints;
    @Column(name = "timestamp_column")
    protected String timestampColumn;

    private Table(String name, String origTableName, String dataSetName, String description, String tableConstraints, String deletionMarkerColumn, String status, String comment, String sourceDocument, Date metadataLastUpdated, String timestampColumn) {
        this.name = name;
        this.origTableName = origTableName;
        this.dataSetName = dataSetName;
        this.description = description;
        this.tableConstraints = tableConstraints;
        this.deletionMarkerColumn = deletionMarkerColumn;
        this.status = status;
        this.comment = comment;
        this.sourceDocument = sourceDocument;
        this.metadataLastUpdated = metadataLastUpdated;
        this.timestampColumn=timestampColumn;
    }
    @Column(name = "deletion_marker_column")
    protected String deletionMarkerColumn;
    protected String status;
    protected String comment;
    @Column(name = "source_document")
    protected String sourceDocument;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "metadata_last_updated")
    protected Date metadataLastUpdated;

    protected Table() {}


    public Table(String dataSetName, String name) {
        this.name = name;
        this.dataSetName = dataSetName;
    }

    public String getStagingName() {
        return "staging_"+dataSetName+"."+name;
    }

    public String getSnapshotName() {
        return "varasto_"+dataSetName+"_historia_snap."+name;
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

    public String getOrigTableName() {
        return origTableName;
    }

    public void setOrigTableName(String origTableName) {
        this.origTableName = origTableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTableConstraints() {
        return tableConstraints;
    }

    public void setTableConstraints(String table_constraints) {
        this.tableConstraints = table_constraints;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDeletionMarkerColumn() {
        return deletionMarkerColumn;
    }

    public void setDeletionMarkerColumn(String deletionMarkerColumn) {
        this.deletionMarkerColumn = deletionMarkerColumn;
    }

    public Date getMetadataLastUpdated() {
        return metadataLastUpdated;
    }

    public void setMetadataLastUpdated(Date metadataLastUpdated) {
        this.metadataLastUpdated = metadataLastUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(String sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public String getTimestampColumn(){
        return this.timestampColumn;
    }
    public void setTimestampColumn(String timestampColumn){
        this.timestampColumn=timestampColumn;
    }

    public static class Builder {
        private String name;
        private String origTableName;
        private String dataSetName;
        private String description;
        private String tableConstraints;
        private String deletionMarkerColumn;
        private String status;
        private String comment;
        private String sourceDocument;
        private Date metadataLastUpdated;
        private String timestampColumn;
        public Builder(){

        }
        public Builder withName(final String name) {
            this.name = name;
            return this;
        }
        public Builder withOrigTableName(final String origTableName){
            this.origTableName=origTableName;
            return this;
        }
        public Builder withDataSetName(final String dataSetName){
            this.dataSetName=dataSetName;
            return this;
        }
        public Builder withDescription(final String description){
            this.description=description;
            return this;
        }
        public Builder withTableConstraints(final String tableConstraints){
            this.tableConstraints=tableConstraints;
            return this;
        }
        public Builder withDeletionMakerColumn(final String deletionMarkerColumn){
            this.deletionMarkerColumn=deletionMarkerColumn;
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
        public Builder withMetadataLastUpdated(final Date metadataLastUpdated){
            this.metadataLastUpdated=metadataLastUpdated;
            return this;
        }
        public Builder withTimestampColumn(final String timestampColumn){
            this.timestampColumn=timestampColumn;
            return this;
        }
        public Table build(){
            return new Table(name,origTableName,dataSetName,description,tableConstraints,deletionMarkerColumn,status,comment,sourceDocument,metadataLastUpdated,timestampColumn);
        }
    }

}
