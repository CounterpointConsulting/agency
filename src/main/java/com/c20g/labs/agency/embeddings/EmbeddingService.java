package com.c20g.labs.agency.embeddings;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.embedding.EmbeddingRequest.EmbeddingRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@Service
public class EmbeddingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private EmbeddingRequestBuilder embeddingRequestBuilder;
    
    public List<Embedding> getEmbeddings(List<String> input) {
        LOGGER.debug("Getting embedding for " + input.size() + " inputs");
        EmbeddingRequest request = embeddingRequestBuilder
            .input(input)
            .build();
        EmbeddingResult result = openAiService.createEmbeddings(request);
        return result.getData();
    }
}
