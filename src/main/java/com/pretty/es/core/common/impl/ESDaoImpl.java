package com.pretty.es.core.common.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import com.pretty.es.core.common.ESDao;
import com.pretty.es.core.util.CUDResponse;
import com.pretty.es.core.util.ESConstant;
import com.pretty.es.core.util.MetaData;
import com.pretty.es.core.util.Tools;
import com.pretty.es.core.vo.ESPage;
import com.pretty.es.core.vo.ESParamVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ESDaoImpl<T> implements ESDao<T> {

    private Logger logger = Logger.getLogger(ESDaoImpl.class);

    private RestHighLevelClient client;

    @Override
    public boolean save(T t) throws Exception {
        return this.save(t, DocWriteRequest.OpType.INDEX);
    }

    @Override
    public CUDResponse save(List<T> list) throws Exception {
        return this.save(list, DocWriteRequest.OpType.INDEX);
    }

    @Override
    public boolean save(T t, DocWriteRequest.OpType opType) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(t.getClass());
        String id = Tools.getESId(t);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        IndexRequest request;

        if (StringUtils.isEmpty(id)) {
            request = new IndexRequest(indexName, indexType);
        } else {
            request = new IndexRequest(indexType, indexType, id);
        }

        Map map = JSONObject.parseObject(JSON.toJSONString(t), Map.class);
        request.source(map);
        request.opType(opType);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }

    @Override
    public CUDResponse save(List<T> list, DocWriteRequest.OpType opType) throws Exception {
        if (list == null || list.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(list.get(0).getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        BulkRequest bulkRequest = null;

        for (T t : list) {
            String id = Tools.getESId(t);
            Map map = JSONObject.parseObject(JSON.toJSONString(t), Map.class);

            IndexRequest request = new IndexRequest(indexName, indexType, id)
                    .source(map)
                    .opType(opType);

            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }

    @Override
    public boolean delete(String id, Class<T> clazz) throws Exception {
        if (id.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        DeleteRequest request = new DeleteRequest(indexName, indexType, id);

        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }

    @Override
    public CUDResponse delete(List<String> ids, Class<T> clazz) throws Exception {
        if (ids == null && ids.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        BulkRequest bulkRequest = new BulkRequest();

        for (String id : ids) {
            DeleteRequest request = new DeleteRequest(indexName, indexType, id);
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }

    @Override
    public CUDResponse deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);//6.5就把type给屏蔽掉了
        request.setQuery(queryBuilder);
        request.setConflicts("proceed");//发生冲突继续执行

        BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
        return dealBulkByScrollResponse(response);
    }

    @Override
    public boolean update(T t) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(t.getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();
        String id = Tools.getESId(t);

        if (id == null) {
            throw new Exception("ESId can not be null!");
        }

        Map map = JSONObject.parseObject(JSON.toJSONString(t), Map.class);
        UpdateRequest request = new UpdateRequest(indexName, indexType, id).doc(map);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

        return dealResponse(response);
    }

    @Override
    public CUDResponse update(List<T> list) throws Exception {
        if (list == null && list.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(list.get(0).getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();


        BulkRequest bulkRequest = new BulkRequest();

        for (T t : list) {
            String id = Tools.getESId(t);
            if (StringUtils.isEmpty(id)) {
                throw new Exception("ESId can not be null!");
            }
            Map map = JSON.parseObject(JSON.toJSONString(t), Map.class);
            UpdateRequest request = new UpdateRequest(indexName, indexType, id).doc(map);
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return dealBulkResponse(bulkResponse);
    }

    @Override
    public boolean updateCover(T t) throws Exception {
        return save(t);
    }

    @Override
    public CUDResponse updateCover(List<T> list) throws Exception {
        return save(list);
    }

    @Override
    public CUDResponse updateByQuery(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception {
        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        UpdateByQueryRequest request = new UpdateByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        request.setScript(script);
        request.setConflicts("proceed");//发生冲突继续执行

        BulkByScrollResponse response = client.updateByQuery(request, RequestOptions.DEFAULT);

        return dealBulkByScrollResponse(response);
    }

    @Override
    public SearchResponse search(SearchRequest request) throws Exception {
        return client.search(request, RequestOptions.DEFAULT);
    }

    @Override
    public T search(String id, Class<T> clazz) throws Exception {
        if (id == null && id.isEmpty()) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();

        GetRequest request = new GetRequest(indexName, indexType, id);
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        if (response.isExists()) {
            String resultString = response.getSourceAsString();
            return JSON.parseObject(resultString, clazz);
        }

        return null;
    }

    @Override
    public List<T> search(List<String> ids, Class<T> clazz) throws Exception {
        List<T> resultList = new ArrayList<>();

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();

        MultiGetRequest request = new MultiGetRequest();

        for (String id : ids) {
            if (id == null && id.isEmpty()) {
                throw new Exception("es parameter is null!");
            }
            request.add(new MultiGetRequest.Item(indexName, indexType, id));
        }

        MultiGetResponse responses = client.mget(request, RequestOptions.DEFAULT);

        for (MultiGetItemResponse itemResponse : responses) {
            if (itemResponse.getResponse().isExists()) {
                String resultString = itemResponse.getResponse().getSourceAsString();
                resultList.add(JSON.parseObject(resultString, clazz));
            }
        }
        return resultList;
    }

    @Override
    public List<T> search(T t) throws Exception {
        return search(t, null);
    }

    @Override
    public List<T> search(T t, ESPage page) throws Exception {
        if (t == null) {
            throw new Exception("es parameter is null!");
        }

        List<T> resultList = new ArrayList<>();
        MetaData metaData = Tools.getMetaData(t.getClass());
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
        request.types(indexType);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(t), Map.class);
        for (String key : map.keySet()) {
            boolQueryBuilder.must(new TermQueryBuilder(key, map.get(key)));
        }

        // 处理分页信息
        if (page != null) {
            page.countFromAndSize();
            sourceBuilder.size(page.getSize());
            sourceBuilder.from(page.getFrom());
            if (page.getSize() + page.getFrom() > ESConstant.esCoreSizeParam.PAGINATION_SIZE) {
                throw new Exception("分页深度超过一万条，拒绝请求！");
            }
        } else {
            sourceBuilder.size(ESConstant.esCoreSizeParam.PAGINATION_SIZE);//默认最大返回10000条，超过的话用searchScroll()方法游标查询全部的
        }

        request.source(sourceBuilder.query(boolQueryBuilder));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits()) {
            String resultString = hit.getSourceAsString();
            resultList.add((T) JSON.parseObject(resultString, t.getClass()));
        }

        // 处理分页结果
        if (page != null) {
            int totalNum = (int) response.getHits().getTotalHits();
            int totalPage = totalNum / page.getSize() + 1;
            page.setTotalNumber(totalNum);
            page.setTotalPage(totalPage);
        }

        return resultList;
    }

    @Override
    public SearchResponse search(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception {
        return search(sourceBuilder, clazz, null);
    }

    @Override
    public SearchResponse search(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception {
        if (sourceBuilder == null) {
            throw new Exception("es parameter is null!");
        }

        MetaData metaData = Tools.getMetaData(clazz);
        String indexName = metaData.getIndexName();
        String indexType = metaData.getIndexType();

        SearchRequest request = new SearchRequest(indexName);
        request.types(indexType);

        // 处理分页信息
        if (page != null) {
            page.countFromAndSize();
            sourceBuilder.size(page.getSize());
            sourceBuilder.from(page.getFrom());
            if (page.getSize() + page.getFrom() > ESConstant.esCoreSizeParam.PAGINATION_SIZE) {
                throw new Exception("分页深度超过一万条，拒绝请求！");
            }
        } else {
            sourceBuilder.size(ESConstant.esCoreSizeParam.PAGINATION_SIZE);//默认最大返回10000条，超过的话用searchScroll()方法游标查询全部的
        }

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 处理分页结果
        if (page != null) {
            int totalNum = (int) response.getHits().getTotalHits();
            int totalPage = totalNum / page.getSize() + 1;
            page.setTotalNumber(totalNum);
            page.setTotalPage(totalPage);
        }

        return response;
    }

    @Override
    public SearchResponse searchByScroll(RestHighLevelClient client, ESParamVo vo, SearchSourceBuilder sourceBuilder, String scrollId) throws Exception {
        if (sourceBuilder == null && org.apache.commons.lang3.StringUtils.isEmpty(scrollId)) {
            throw new Exception("The es parameter must have an not empty one!");
        }

        SearchResponse response;
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
        int size = sourceBuilder.size();//一次查多少
        // 第一次查询用sourceBuilder查
        if (org.apache.commons.lang3.StringUtils.isEmpty(scrollId)) {
            SearchRequest request = new SearchRequest();
            request.scroll(scroll);
            request.source(sourceBuilder);

            response = client.search(request, RequestOptions.DEFAULT);
        } else {
            // 后面查询用scrollId查
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        }

        // 所有数据都查询完毕的时候，清除游标
        if (response.getHits().totalHits == 0) {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            if (clearScrollResponse.isSucceeded()) {
                logger.info("es scroll clear successed!");
            } else {
                logger.error("es scroll clear failed!");
            }
        }

        return response;
    }

    @Override
    public SearchResponse searchByScrollSlice(RestHighLevelClient client, ESParamVo vo, SearchSourceBuilder sourceBuilder, String scrollId) throws Exception {
        if (sourceBuilder == null && org.apache.commons.lang3.StringUtils.isEmpty(scrollId)) {
            throw new Exception("The es parameter must have an not empty one!");
        }

        SearchResponse response;
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
        int size = sourceBuilder.size();//一次查多少
        // 第一次查询用sourceBuilder查
        if (org.apache.commons.lang3.StringUtils.isEmpty(scrollId)) {
            SearchRequest request = new SearchRequest();
            request.scroll(scroll);
            request.source(sourceBuilder);

            response = client.search(request, RequestOptions.DEFAULT);
        } else {
            // 后面查询用scrollId查
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        }

        // 所有数据都查询完毕的时候，清除游标
        if (response.getHits().totalHits == 0) {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            if (clearScrollResponse.isSucceeded()) {
                logger.info("es scroll clear successed!");
            } else {
                logger.error("es scroll clear failed!");
            }
        }

        return response;
    }


    /**
     * 处理返回结果
     *
     * @param response
     * @return
     */
    private boolean dealResponse(DocWriteResponse response) {
        boolean result = true;
        if (response.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("DOCUMENT CREATE SUCCESS!");
        } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("DOCUMENT UPDATE SUCCESS!");
        } else if (response.getResult() == DocWriteResponse.Result.DELETED) {
            logger.info("DOCUMENT DELETE SUCCESS!");
        } else {
            result = false;
            logger.info("DOCUMENT OPERATION FAILED! ---" + response.getId());
        }

        return result;
    }

    /**
     * 处理批量返回结果
     *
     * @return
     */
    private CUDResponse dealBulkResponse(BulkResponse bulkResponse) {
        List<String> succeedIdList = new ArrayList<>();
        List<String> failedIdList = new ArrayList<>();

        if (bulkResponse != null) {
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                String id = bulkItemResponse.getId();
                if (bulkItemResponse.isFailed()) {
                    failedIdList.add(id);
                } else {
                    succeedIdList.add(id);
                }
            }
        }
        if (!failedIdList.isEmpty()) {
            logger.error("BULK DOCUMENT OPERATION FAILED! ---" + failedIdList);
        }

        return new CUDResponse(succeedIdList.size(), failedIdList.size(), succeedIdList, failedIdList);
    }

    private CUDResponse dealBulkByScrollResponse(BulkByScrollResponse bulkByScrollResponse) {
        long successed = 0;
        long failed = 0;
        List<String> failedIdList = new ArrayList<>();

        if (bulkByScrollResponse != null) {
            for (BulkItemResponse.Failure failure : bulkByScrollResponse.getBulkFailures()) {
                failedIdList.add(failure.getId());
            }
            successed = bulkByScrollResponse.getTotal() - bulkByScrollResponse.getVersionConflicts();//用总数-版本冲突数
            failed = failedIdList.size();
        }

        return new CUDResponse(successed, failed, null, failedIdList);
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }
}
