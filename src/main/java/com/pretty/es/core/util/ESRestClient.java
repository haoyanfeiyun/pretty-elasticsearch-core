package com.pretty.es.core.util;

import org.apache.http.HttpHost;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *懒汉式获取单例RestHighLevelClient
 * @author lihaohao
 *
 */

@Component
public class ESRestClient {
	private static Logger logger = LogManager.getLogger(ESRestClient.class);

	@Value("${default.es.server.host:127.0.0.1:9200}")
	private void setHost(String host){
		this.host = host;
	}

	private static String host;

	private volatile RestHighLevelClient client = null;

	public RestHighLevelClient getClient() {
		try {
			if (client == null || isClose(client)) {
				synchronized (RestHighLevelClient.class) {
					if (client == null || isClose(client)) {
						HttpHost[] httpHosts = getHttpHosts(host);
						client = new RestHighLevelClient(RestClient.builder(httpHosts));
					}
				}
			}
		}catch (Exception e){
			logger.error("getClient failed", e);
		}

		return client;
	}

	private static HttpHost[] getHttpHosts(String host) {
		String[] hosts = host.split(",");
		HttpHost[] httpHosts = new HttpHost[hosts.length];
		for (int i = 0; i < httpHosts.length; i++) {
			String h = hosts[i];
			httpHosts[i] = new HttpHost(h.split(":")[0]
					, Integer.parseInt(h.split(":")[1]), "http");
		}
		return httpHosts;
	}

	private static boolean isClose(RestHighLevelClient client) throws Exception{
		boolean isClose = false;
		try {
			client.info(RequestOptions.DEFAULT);
		} catch (IllegalStateException e) {
			isClose = true;
			logger.error("client has been close, please check code!", e);
		}

		return isClose;
	}
}
