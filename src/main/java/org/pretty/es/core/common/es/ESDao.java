package org.pretty.es.core.common.es;

import org.pretty.es.core.util.CUDResponse;
import org.pretty.es.core.vo.es.ESPage;
import org.pretty.es.core.vo.es.ESParamVo;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;

public interface ESDao<T> {

    /*--------------------------------------CREATE-----------------------------------------*/

    /**
     * 新增
     *
     * @param t
     * @throws Exception
     */
    boolean save(T t) throws Exception;

    CUDResponse save(List<T> list) throws Exception;

    /**
     * opType指定操作类型
     * 当opType=CREATE时，如果出现ID重复，则抛出异常
     * 当opType=UPDATE时，如果出现ID不存在，则抛出异常
     *
     * @param t
     * @param opType
     * @return
     * @throws Exception
     */
    boolean save(T t, DocWriteRequest.OpType opType) throws Exception;

    CUDResponse save(List<T> list, DocWriteRequest.OpType opType) throws Exception;


    /*--------------------------------------DELETE-----------------------------------------*/
    boolean delete(String id, Class<T> clazz) throws Exception;

    CUDResponse delete(List<String> ids, Class<T> clazz) throws Exception;

    CUDResponse deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    /*--------------------------------------UPDATE-----------------------------------------*/
    boolean update(T t) throws Exception;

    CUDResponse update(List<T> list) throws Exception;

    boolean updateCover(T t) throws Exception;

    CUDResponse updateCover(List<T> list) throws Exception;

    CUDResponse updateByQuery(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception;


    /*--------------------------------------RETRIEVE-----------------------------------------*/
    SearchResponse search(SearchRequest request) throws Exception;

    T search(String id, Class<T> clazz) throws Exception;

    List<T> search(List<String> ids, Class<T> clazz) throws Exception;

    List<T> search(T t) throws Exception;

    List<T> search(T t, ESPage page) throws Exception;

    SearchResponse search(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception;

    SearchResponse search(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception;

    /**
     * 用游标查询数据，适用于大数据量导出
     * 第一次用SearchSourceBuilder来查，返回数据和游标，后面只用游标查，查完为止，示例代码如下：
     * SearchResponse searchResponse = searchByScroll(client, vo, sourceBuilder, null);
     * SearchHit[] searchHits = searchResponse.getHits().getHits();
     * String scrollId = searchResponse.getScrollId();
     * <p>
     * while(searchHits != null && searchHits.length>0){
     * searchResponse = searchByScroll(client, vo, null, scrollId);
     * searchHits = searchResponse.getHits().getHits();
     * scrollId = searchResponse.getScrollId();
     * //在这里把数据处理掉，比如往数据库里面写
     * }
     *
     * @param client
     * @param vo
     * @param sourceBuilder
     * @param scrollId
     * @return
     * @throws Exception
     */
    public SearchResponse searchByScroll(RestHighLevelClient client, ESParamVo vo, SearchSourceBuilder sourceBuilder, String scrollId) throws Exception;

    public SearchResponse searchByScrollSlice(RestHighLevelClient client, ESParamVo vo, SearchSourceBuilder sourceBuilder, String scrollId) throws Exception;
}
