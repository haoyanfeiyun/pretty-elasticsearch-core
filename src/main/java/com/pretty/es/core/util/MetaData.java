package com.pretty.es.core.util;

public class MetaData {

    private String indexName;

    private String indexType;

    public MetaData(String indexName, String indexType) {
        this.indexName = indexName;
        this.indexType = indexType;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }
}
