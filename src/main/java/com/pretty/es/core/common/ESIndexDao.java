package com.pretty.es.core.common;

import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;

public interface ESIndexDao<T> {

    void rolloverIndex(RolloverRequest request) throws Exception;
}
