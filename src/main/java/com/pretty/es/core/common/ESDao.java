package com.pretty.es.core.common;


import com.pretty.es.core.util.CUDResponse;
import com.pretty.es.core.vo.Condition;
import com.pretty.es.core.vo.ESPage;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.SourceSimpleFragmentsBuilder;

import java.util.List;

public interface ESDao<T> {

    /*--------------------------------------CREATE-----------------------------------------*/

    /**
     * 新增
     * ESID重复则覆盖，相当于mysql的insert ...on duplicate key update
     *
     * @param t
     * @throws Exception
     */
    boolean add(T t) throws Exception;

    /**
     * 批量新增
     * ESID重复则覆盖
     *
     * @param list
     * @return
     * @throws Exception
     */
    CUDResponse add(List<T> list) throws Exception;

    /**
     * 新增
     * ESID重复则不插入，抛出异常，相当于mysql insert语句，
     * 区别于1.0，这里没有专属异常抛出，需要自己判断异常类型
     * 如果没有ESId则正常插入
     *
     * @param t
     * @return
     * @throws Exception
     */
    boolean addNoRepeat(T t) throws Exception;

    /**
     * 批量新增
     * ESID重复则不插入，不会抛出异常，失败的id list会返回
     * 如果没有ESId则正常插入
     *
     * @param list
     * @return
     * @throws Exception
     */
    CUDResponse addNoRepeat(List<T> list) throws Exception;


    /*--------------------------------------DELETE-----------------------------------------*/

    /**
     * 根据id删除
     *
     * @param id
     * @param clazz
     * @return
     * @throws Exception
     */
    boolean delete(String id, Class<T> clazz) throws Exception;

    /**
     * 根据id列表删除，返回删除情况
     *
     * @param ids
     * @param clazz
     * @return
     * @throws Exception
     */
    CUDResponse delete(List<String> ids, Class<T> clazz) throws Exception;

    /**
     * 根据查询删除数据
     * 删除超过10w数据量的时候，会出现性能问题和超时问题，请使用deleteByQueryBigData方法
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    CUDResponse deleteByQuery(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    /**
     * 根据查询删除数据(超过10w数据量)
     * 大数据量删除需要使用切片提高性能
     * 如果是同步执行，会出现socket超时情况，所以改成了异步执行，不会返回执行结果
     *
     * @param queryBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    void deleteByQueryBigData(QueryBuilder queryBuilder, Class<T> clazz) throws Exception;

    /*--------------------------------------UPDATE-----------------------------------------*/

    /**
     * 更新数据，没有id会抛异常
     *
     * @param t
     * @return
     * @throws Exception
     */
    boolean update(T t) throws Exception;

    /**
     * 批量更新数据，没有id抛异常，返回更新详情
     *
     * @param list
     * @return
     * @throws Exception
     */
    CUDResponse update(List<T> list) throws Exception;

    /**
     * 覆盖更新
     * 没有ESId直接插入
     *
     * @param t
     * @return
     * @throws Exception
     */
    boolean updateCover(T t) throws Exception;

    /**
     * 批量覆盖更新
     * 没有ESId直接插入
     *
     * @return
     * @throws Exception
     */
    CUDResponse updateCover(List<T> list) throws Exception;

    /**
     * 根据脚本查询更新
     * 更新超过10w数据量的时候，会出现性能问题和超时问题，请使用updateByQueryBigData方法
     *
     * @param queryBuilder
     * @param script
     * @param clazz
     * @return
     * @throws Exception
     */
    CUDResponse updateByQuery(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception;

    /**
     * 根据脚本查询更新(大数据量更新)
     * 做了切片优化，异步且没有返回
     *
     * @param queryBuilder
     * @param script
     * @param clazz
     * @return
     * @throws Exception
     */
    void updateByQueryBigData(QueryBuilder queryBuilder, Script script, Class<T> clazz) throws Exception;


    /*--------------------------------------RETRIEVE-----------------------------------------*/

    /**
     * 查询数据，直接把SearchRequest传进来
     *
     * @param request
     * @return
     * @throws Exception
     */
    SearchResponse search(SearchRequest request) throws Exception;

    /**
     * 根据id查询数据
     *
     * @param id
     * @param clazz
     * @return
     * @throws Exception
     */
    T search(String id, Class<T> clazz) throws Exception;

    /**
     * 根据id列表查询数据
     *
     * @param ids
     * @param clazz
     * @return
     * @throws Exception
     */
    List<T> search(List<String> ids, Class<T> clazz) throws Exception;

    /**
     * 根据对象查询
     * 最多返回一万条，超过一万条性能消耗太大，请使用searchByScroll方法
     *
     * @param t
     * @return
     * @throws Exception
     */
    List<T> search(T t) throws Exception;

    /**
     * 根据对象查询---分页
     * 分页深度超过一万条，会直接抛出异常，请使用searchByScroll方法
     *
     * @param t
     * @return
     * @throws Exception
     */
    List<T> search(T t, ESPage page) throws Exception;

    /**
     *  根据条件查询---分页+排序
     *  分页深度超过一万条，会直接抛出异常，请使用searchByScroll方法
     * @param sourceBuilder
     * @param clazz
     * @param page
     * @return
     * @throws Exception
     */
    List<T> search(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception;

    /**
     * 聚合查询
     * 聚合查询默认是不返回文档的
     * 聚合默认返回条数默认也是10，记得合理设置
     *
     * @param sourceBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz) throws Exception;

    /**
     * 聚合查询---分页
     * 分页数据对返回文档有效，聚合分页需要自行在sourceBuilder内定义
     *
     * @param sourceBuilder
     * @param clazz
     * @param page
     * @return
     * @throws Exception
     */
    SearchResponse searchAggregation(SearchSourceBuilder sourceBuilder, Class<T> clazz, ESPage page) throws Exception;

    /**
     * 用游标查询数据，适用于大数据量导出
     * <p>
     * 第一次用SearchSourceBuilder来查，返回数据和游标，后面只用游标查，查完为止，实例代码如下：
     * String scrollId = null;
     * SearchHit[] searchHits = null;
     * SearchSourceBuilder builder = new SearchSourceBuilder().query(new TermQueryBuilder("storeNo", "1001"));
     * builder.size(2);
     * <p>
     * while (scrollId == null || (searchHits != null && searchHits.length > 0)) {
     * SearchResponse searchResponse = esDao.searchByScroll(builder, StockPreemptFulfillsuborderOld.class, scrollId);
     * searchHits = searchResponse.getHits().getHits();
     * scrollId = searchResponse.getScrollId();
     * //在这里把数据处理掉，比如往数据库里面写
     * for (SearchHit searchHit : searchHits) {
     * System.out.println(searchHit.getSourceAsString());
     * }
     * }
     *
     * @param sourceBuilder
     * @param scrollId
     * @return
     * @throws Exception
     */
    public SearchResponse searchByScroll(SearchSourceBuilder sourceBuilder, Class<T> clazz, String scrollId) throws Exception;

}
