package com.c20g.labs.agency.config;

import java.util.HashMap;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingRequest.EmbeddingRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@Configuration
public class OpenAiConfiguration {

    // private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiConfiguration.class);
    
    @Value("${openai.apikey:#{null}}")
    private String openAIApiKey;

    @Value("${openai.chat.model}")
    private String openAIChatModel;

    @Value("${openai.chat.model.temp}")
    private Double openAIChatModelTemp;

    @Value("${openai.embedding.model}")
    private String openAIEmbeddingsModel;

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
        builder.model(openAIChatModel);
        builder.temperature(openAIChatModelTemp);
        builder.n(1);
		builder.logitBias(new HashMap<>());
        return builder;
    }

    @Bean
    public EmbeddingRequestBuilder embeddingRequestBuilder() {
        EmbeddingRequestBuilder builder = EmbeddingRequest.builder();
        builder.model(openAIEmbeddingsModel);
        return builder;
    }

}
