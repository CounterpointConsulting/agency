package com.c20g.labs.agency.util;

import java.util.Scanner;

import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.c20g.labs.agency.agent.summarizer.SummarizerAgent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.config.OpenAiConfiguration;
import com.c20g.labs.agency.skill.SkillLocator;
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

    @Autowired
    private SummarizerAgent summarizerAgent;

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
                    .messages(conversation.getAllMessages())
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
        for (final var message : conversation.getAllMessages()) {
            sum += tokensPerMessage;
            sum += encoding.countTokens(message.getContent());
            sum += encoding.countTokens(message.getRole());
        }

        sum += 3; // every reply is primed with <|start|>assistant<|message|>

        return sum;
    }


    public static String getLongInput(Scanner stringScanner) {
        System.out.println("Enter text to summarize.  To process, enter \\z on the final line.");
        StringBuilder sb = new StringBuilder();
        while(true) {
            String nextLine = stringScanner.nextLine();

            if("\\z".equals(nextLine)) {
                return sb.toString();
            }
            else {
                sb.append(JSONValue.escape(nextLine));
            }
        }
    }


    public ConversationHistory summarizeConversation(ConversationHistory conversation) throws Exception {
        ConversationHistory historyAgentConversation = 
            summarizerAgent.run(conversation.formattedFullHistory(), null);

        ConversationHistory summarized = new ConversationHistory();

        // copy the system message if there is one
        if(conversation.getAllMessages().get(0).getRole().equals(ChatMessageRole.SYSTEM.value())) {
            summarized.addMessage(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                                conversation.getAllMessages().get(0).getContent()));
        }
        
        summarized.addMessage(
            new ChatMessage(ChatMessageRole.USER.value(), 
                "Here is a summary of our conversation so far:\n\n" + 
                    historyAgentConversation.getAllMessages().get(
                        historyAgentConversation.getAllMessages().size()-1)));
        
        StringBuilder recentMessagesSB = new StringBuilder("Here are our most recent messages: \n\n");
        
        if(conversation.getAllMessages().size() > 10) {
            for(ChatMessage m : conversation.getAllMessages().subList(Math.max(conversation.getAllMessages().size() - 3, 0), conversation.getAllMessages().size())) {
                recentMessagesSB.append(m.getRole()).append(" > " + m.getContent()).append("\n");
            }

            summarized.addMessage(
                new ChatMessage(ChatMessageRole.USER.value(), 
                    recentMessagesSB.toString()));
        }

        return summarized;
    }
}
