package com.pretty.es.core.vo;

import org.elasticsearch.search.sort.SortOrder;

public class Sort {
    private SortOrder direction;
    private String property;

    public Sort() {
    }

    public Sort(SortOrder direction, String property) {
        this.direction = direction;
        this.property = property;
    }

    public SortOrder getDirection() {
        return direction;
    }

    public void setDirection(SortOrder direction) {
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
