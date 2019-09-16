package org.pretty.es.core.common.es;

import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;

public interface ESIndexDao<T> {

    void rolloverIndex(RolloverRequest request) throws Exception;
}
