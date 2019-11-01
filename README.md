# pretty-elasticsearch-core

pretty-elasticsearch-core是基于elasticsearch 6.5.3版本的HighRestClient使用工具包，可整合到spring 4.0以上的版本中

## 一、使用说明

注1：以下使用都需要引入pretty-elasticsearch-core包，如下：
```
<dependency>
    <groupId>com.feiniu</groupId>
    <artifactId>pretty-elasticsearch-core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

注2：以下使用都需要给索引对应的实体类添加注解，标记索引名、类型和id，如下：
```java
@ESMetaData(indexName = "stock_preempt_fulfillsuborder", indexType = "_doc")
public class StockPreemptFulfillsuborderOld{
    @ESId
    private String ESId;// ESId
}
```

### 1、单集群配置和使用
#### 1.1、添加配置
```
#ES配置
es.server.host=stock-preempt-es.beta1:9200
```
#### 1.2、使用
```java
@Autowired
private ESDao<StockPreemptFulfillsuborderOld> ESDaoOld;
@Test
public void configOutTest() throws Exception {
    StockPreemptFulfillsuborderOld stock = new StockPreemptFulfillsuborderOld();
    stock.setStoreNo("1001");
    List<StockPreemptFulfillsuborderOld> resultList = ESDaoOld.search(stock);
}
```
### 2、多集群配置和使用

每加入一个新的集群，都需要新加配置和ESConfig类，方便管理，ESConfig类主要修改的是配置引入名、Bean的名字即可

#### 2.1、添加配置
```
#ES配置
es.server.host=stock-preempt-es.beta1:9200
#ES配置-fresh集群
fresh.es.server.host=10.202.252.3:9200
```
#### 2.2、加入ESConfig类		
```java
@Configuration
@ComponentScan(basePackages = "com")
public class ESConfig {

    private static final Logger LOGGER = LogManager.getLogger(ESConfig.class);

    @Value("${fresh.es.server.host}")
    private String host;

    @Value("${fresh.es.xpark.enable:false}")
    private boolean xparkEnable;

    @Value("${fresh.es.xpark.username:noXpark}")
    private String username;

    @Value("${fresh.es.xpark.password:noXpark}")
    private String password;

    @Scope("singleton")
    @Bean(name = "freshClient", destroyMethod = "close")
    public RestHighLevelClient clientInstance() {
        return this.getClient();
    }

    @Bean(name = "esDaoFresh")
    public ESDaoImpl esDaoInstance() {
        ESDaoImpl esDao = new ESDaoImpl();
        esDao.setClient(getClient());
        return esDao;
    }

    private HttpHost[] getHttpHosts(String host) {
        String[] hosts = host.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < httpHosts.length; i++) {
            String h = hosts[i];
            httpHosts[i] = new HttpHost(h.split(":")[0]
                    , Integer.parseInt(h.split(":")[1]), "http");
        }
        return httpHosts;
    }

    private RestHighLevelClient getClient() {
        RestHighLevelClient client;
        try {
            HttpHost[] httpHosts = getHttpHosts(host);

            if (xparkEnable) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(this.username, this.password));
                client = new RestHighLevelClient(
                        RestClient.builder(httpHosts)
                                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

                                    @Override
                                    public HttpAsyncClientBuilder customizeHttpClient(
                                            HttpAsyncClientBuilder httpAsyncClientBuilder) {
                                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                    }
                                }));
            } else {
                client = new RestHighLevelClient(RestClient.builder(httpHosts));
            }
        } catch (Exception e) {
            LOGGER.error("getClient Failed!", e);
            return null;
        }
        return client;
    }
}
```
#### 2.3、使用

这里需要加上@Qualifier注解，标记注入哪个bean
```java
@Autowired
@Qualifier("esDaoFresh")
private ESDao<StockPreemptFulfillsuborderOld> ESDaoOldFresh;

@Test
public void configOutTest() throws Exception {
    StockPreemptFulfillsuborderOld stock = new StockPreemptFulfillsuborderOld();
    stock.setStoreNo("1001");
    List<StockPreemptFulfillsuborderOld> resultListFresh = ESDaoOldFresh.search(stock);
}
```
### 3、X-Park配置

如果ES使用了X-Park，只需加入配置信息即可，具体修改如下：

#### 3.1、添加配置
```
#ES x-park配置
es.xpark.enable=true
es.xpark.username=elastic
es.xpark.password=elastic

#ES fresh集群x-park配置
fresh.es.xpark.enable=true
fresh.es.xpark.username=elastic
fresh.es.xpark.password=elastic
```
*添加x-park对使用不受影响，如果多个集群都使用了xpark，则需要分别添加对应的配置信息,如上

## 二、方法说明

具体使用以com.pretty.es.core.common.ESDao为准，接口方法及说明如下：
```java
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
```

## 三、参考来源
	
### 1、Elasticsearch-EsClientRHL
这是个人开发的一个ES使用工具包，适用于spring和spring boot项目，看源码是借鉴了Spring Data Elasticsearch，但是不支持xpark和多集群模式，聚合和全文检索功能较多，文档也非常详细，但是局限性较大

参考链接： https://gitee.com/zxporz/ESClientRHL
	
### 2、Spring Data Elasticsearch
这是spring官方提供spring项目集成ElasticSearch的工具包。这次优化的重点基本是参考这个实现的，但是该工具包更新较慢，如果公司要升级ES，而该工具包没有对应ES版本的话，只能干瞪眼
只有node client和transport client两种客户端的使用，最新的3.2.0以上版本支持rest client，使用方法和本文档使用方法一样。 

参考链接： https://github.com/spring-projects/spring-data-elasticsearch


