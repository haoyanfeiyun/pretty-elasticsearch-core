# pretty-elasticsearch-core

elasticsearch的HighRestClient使用工具包

一、使用说明

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

1、单集群配置和使用
1.1、添加配置
```
#ES配置
es.server.host=stock-preempt-es.beta1.fn:9200
```
1.2、使用
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
2、多集群配置和使用
2.1、添加配置
```
#ES配置
es.server.host=stock-preempt-es.beta1.fn:9200
#ES配置-fresh集群
fresh.es.server.host=10.202.252.3:9200
```
2.2、加入ESConfig类		
```java
@Configuration
@ComponentScan(basePackages = "com")
public class ESConfig {
    @Value("${fresh.es.server.host}")
    private String freshHost;
    @Value("${fresh.es.xpark.enable:false}")
    private boolean xparkEnable;
    @Value("${fresh.es.xpark.username:noXpark}")
    private String username;
    @Value("${fresh.es.xpark.password:noXpark}")
    private String password;
    @Scope("singleton")
    @Bean(name = "freshClient", destroyMethod = "close")
    public RestHighLevelClient clientInstanceFresh() {
        RestHighLevelClient client;
        try {
            HttpHost[] httpHosts = getHttpHosts(freshHost);
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
            e.printStackTrace();
            return null;
        }
        return client;
    }
    @Bean(name = "esDaoFresh")
    public ESDaoImpl freshESDaoInstance() {
        ESDaoImpl esDao = new ESDaoImpl();
        esDao.setClient(clientInstanceFresh());
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
}
```
2.3、使用
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
3、X-Park配置
	如果ES使用了X-Park，只需加入配置信息即可，具体修改如下：

3.1、添加配置
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
*添加x-park对使用不受影响
*如果多个集群都使用了xpark，则需要分别添加对应的配置信息,如上图

六、注意事项
1、1.0.2版本的fresh-perform-es-core工具包对1.0.1是不兼容的，可以看成两个完全不一样的工具包，请勿同时引入

七、参考来源
	1、Elasticsearch-EsClientRHL
		这是个人开发的一个ES使用工具包，适用于spring和spring boot项目，看源码是借鉴了Spring Data Elasticsearch，但是不支持xpark和多集群模式，聚合和全文检索功能较多，文档也非常详细，但是局限性较大
		参考链接：https://gitee.com/zxporz/ESClientRHL
	2、Spring Data Elasticsearch
		这是spring官方提供spring项目集成ElasticSearch的工具包。这次优化的重点基本是参考这个实现的，但是该工具包更新较慢，如果公司要升级ES，而该工具包没有对应ES版本的话，只能干瞪眼
		只有node client和transport client两种客户端的使用，最新的3.2.0以上版本支持rest client，使用方法和本文档使用方法一样。 
		参考链接：https://github.com/spring-projects/spring-data-elasticsearch


