package com.c20g.labs.agency.util;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.config.OpenAiConfiguration;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@Component
public class ChatUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatUtils.class);

    @Autowired
	private AgencyConfiguration agencyConfiguration;

    @Autowired
	private OpenAiService openAiService;

    @Autowired
    private OpenAiConfiguration openAiConfiguration;

    @Autowired
	private ChatCompletionRequestBuilder requestBuilder;

    public String getNextLine(Scanner stringScanner) {
		System.out.print("> ");
		String input = stringScanner.nextLine();
		return input;
	}

    public String getNextLine(Scanner stringScanner, String prompt) {
		System.out.print(prompt + " > ");
		String input = stringScanner.nextLine();
		return input;
	}

    public ChatMessage getNextChatResponse(ConversationHistory conversation) {

        Integer tokenCount = getTokenCount(
            Encodings.newDefaultEncodingRegistry(), 
            openAiConfiguration.chatModel(), 
            conversation);
        LOGGER.debug("JTokkit counted " + tokenCount + " tokens in the request");

        ChatCompletionRequest chatCompletionRequest = requestBuilder
                    .messages(conversation.getMessages())
                    .maxTokens(agencyConfiguration.getChatRequestMaxTokens())
                    .build();
        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);

        String aiResponse = chatCompletion.getChoices().get(0).getMessage().getContent();
        System.out.println("\n"+aiResponse+"\n");

        Usage usage = chatCompletion.getUsage();
        LOGGER.debug("Tokens: (" + usage.getPromptTokens() + " / " + usage.getCompletionTokens() + ")");
        return new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse);
    }

    private Integer getTokenCount(EncodingRegistry registry, String model, ConversationHistory conversation) {
        Encoding encoding = registry.getEncodingForModel(model).orElseThrow();
        int tokensPerMessage;
        if (model.startsWith("gpt-4")) {
            tokensPerMessage = 3;
        } else if (model.startsWith("gpt-3.5-turbo")) {
            tokensPerMessage = 4; // every message follows <|start|>{role/name}\n{content}<|end|>\n
        } else {
            throw new IllegalArgumentException("Unsupported model: " + model);
        }
        int sum = 0;
        for (final var message : conversation.getMessages()) {
            sum += tokensPerMessage;
            sum += encoding.countTokens(message.getContent());
            sum += encoding.countTokens(message.getRole());
        }

        sum += 3; // every reply is primed with <|start|>assistant<|message|>

        return sum;
    }
}
