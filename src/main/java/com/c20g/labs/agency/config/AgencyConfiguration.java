package com.c20g.labs.agency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class AgencyConfiguration {

    @Value("${agency.chat_log_dir}")
    private String chatLogDirectory;

    @Value("${agency.text_loader_chunk_size}")
    private Integer textLoaderChunkSize;

    @Value("${agency.chat_req_max_tokens}")
    private Integer chatRequestMaxTokens;

    @Value("${agency.chat_summary_retained_messages_count}")
    private Integer chatSummaryRetainedMessageCount;

}
