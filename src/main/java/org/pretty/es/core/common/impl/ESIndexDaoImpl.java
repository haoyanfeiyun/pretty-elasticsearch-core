package org.pretty.es.core.common.impl;

import org.pretty.es.core.common.ESIndexDao;
import com.google.gson.Gson;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;
import org.elasticsearch.action.admin.indices.rollover.RolloverResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ESIndexDaoImpl<T> implements ESIndexDao<T> {
    private static final Logger logger = LogManager.getLogger(ESIndexDaoImpl.class);

    @Autowired
    private RestHighLevelClient client;

    @Override
    public void rolloverIndex(RolloverRequest request) throws Exception {
//        String alias = "test_order";
//        String newIndexName = "test_order_20190816_" + new Date().getTime();
//        RolloverRequest request = new RolloverRequest(alias, newIndexName);
//        request.addMaxIndexDocsCondition(5);
//        request.addMaxIndexAgeCondition(new TimeValue(1, TimeUnit.DAYS));
//        request.addMaxIndexSizeCondition(new ByteSizeValue(5, ByteSizeUnit.GB));
        Gson gson = new Gson();
        RolloverResponse response = client.indices().rollover(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            logger.info(request.getAlias() + " rollover Index successed! response:" + gson.toJson(response));
        } else {
            logger.error(request.getAlias() + " rollover Index failed! response:" + gson.toJson(response));
        }
    }
}
