package com.pretty.es.core.config;

import com.pretty.es.core.common.impl.ESDaoImpl;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "com")
public class DefaultESConfig {

    private static final Logger LOGGER = LogManager.getLogger(DefaultESConfig.class);

    @Value("${default.es.server.host:127.0.0.1:9200}")
    private String host;

    @Value("${default.es.xpark.enable:false}")
    private boolean xparkEnable;

    @Value("${default.es.xpark.username:noXpark}")
    private String username;

    @Value("${default.es.xpark.password:noXpark}")
    private String password;

    @Primary
    @Scope("singleton")
    @Bean(destroyMethod = "close")
    public RestHighLevelClient clientInstance() {
        return this.getClient();
    }

    @Bean
    @Primary
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