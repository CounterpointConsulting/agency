package com.c20g.labs.agency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgencyConfiguration {

    @Value("${agency.chat_log_dir}")
    private String chatLogDirectory;

    public String getChatLogDirectory() {
        return chatLogDirectory;
    }    
    public void setChatLogDirectory(String chatLogDirectory) {
        this.chatLogDirectory = chatLogDirectory;
    }

}
