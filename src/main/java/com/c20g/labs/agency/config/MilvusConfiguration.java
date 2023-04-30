package com.c20g.labs.agency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;

@Configuration
public class MilvusConfiguration {

    @Value("${milvus.host}")
    private String host;
    
    @Value("${milvus.port}")
    private Integer port;

    @Value("${milvus.username}")
    private String username;

    @Value("${milvus.password}")
    private String password;

    @Value("${milvus.index}")
    private String index;

    @Value("${milvus.collection}")
    private String collection;

    @Value("${milvus.dimensions}")
    private Integer dimensions;

    @Value("${milvus.timeout.ms}")
    private Long timeout;

    @Value("${milvus.shards}")
    private Integer shards;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        MilvusServiceClient milvusClient = null;
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withAuthorization(username, password)
                .build();
        milvusClient = new MilvusServiceClient(connectParam);
        return milvusClient;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getShards() {
        return shards;
    }

    public void setShards(Integer shards) {
        this.shards = shards;
    }

}
