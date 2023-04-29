package com.c20g.labs.agency;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@Configuration
public class OpenAiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiConfiguration.class);
    
    @Value("${openai.apikey:#{null}}")
    private String openAIApiKey;

    @Value("${openai.model}")
    private String openAIModel;

    @Value("${openai.model.temp}")
    private Double openAIModelTemp;

    @Bean
    public OpenAiService openAiService() throws Exception {
        if(openAIApiKey == null || "".equals(openAIApiKey.trim())) {
            openAIApiKey = System.getenv("OPENAI_API_KEY");
        }

        if(openAIApiKey == null || openAIApiKey.trim().length() == 0) {
            throw new Exception("OpenAI API key not set.");
        }

        return new OpenAiService(openAIApiKey);
    }

    @Bean
    public ChatCompletionRequestBuilder chatCompletionRequestBuilder() {
        ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.model(openAIModel);
        builder.temperature(openAIModelTemp);
        builder.n(1);
		builder.logitBias(new HashMap<>());
        return builder;
    }

}
