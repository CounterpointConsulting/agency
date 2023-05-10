package com.c20g.labs.agency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgencyConfiguration {

    @Value("${agency.chat_log_dir}")
    private String chatLogDirectory;

    @Value("${agency.text_loader_chunk_size}")
    private Integer textLoaderChunkSize;

    @Value("${agency.chat_req_max_tokens}")
    private Integer chatRequestMaxTokens;

    public String getChatLogDirectory() {
        return chatLogDirectory;
    }    
    public void setChatLogDirectory(String chatLogDirectory) {
        this.chatLogDirectory = chatLogDirectory;
    }

    public Integer getTextLoaderChunkSize() {
        return textLoaderChunkSize;
    }
    public void setTextLoaderChunkSize(Integer chunkSize) {
        this.textLoaderChunkSize = chunkSize;
    }

    public Integer getChatRequestMaxTokens() {
        return chatRequestMaxTokens;
    }
    public void setChatRequestMaxTokens(Integer chatRequestMaxTokens) {
        this.chatRequestMaxTokens = chatRequestMaxTokens;
    }

}
