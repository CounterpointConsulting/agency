package com.c20g.labs.agency.skill.summarizer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.prompt.NoShotPrompt;
import com.c20g.labs.agency.prompt.PromptGenerator;
import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillDescription;
import com.c20g.labs.agency.skill.SkillService;
import com.c20g.labs.agency.util.ChatUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

@SkillService
public class SummarizerSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizerSkill.class);

    @Autowired
	private AgencyConfiguration agencyConfiguration;

    @Autowired
    private ChatUtils chatUtils;

    @Override
    public String execute(String jsonRequest) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SummarizerSkillRequest q = objectMapper.readValue(jsonRequest, SummarizerSkillRequest.class);
        LOGGER.debug("Summarizing text: " + q.getInputText());

        ConversationHistory conversation = new ConversationHistory();

        ChatMessage systemMessage = new ChatMessage(
            ChatMessageRole.SYSTEM.value(), 
            """
                You are a helpful AI assistant that summarizes pieces of text into shorter ones.
                The user will provide text and you will reply with the summary.  Do not include
                any explanatory text in your response, only the summary itself.
                    """);    
        conversation.addMessage(systemMessage);

        String reqTemplate = """
            Text to summarize:
            {text}
                """;
        PromptGenerator p = new NoShotPrompt(reqTemplate);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("text", q.getInputText());

        ChatMessage inputMessage = new ChatMessage(ChatMessageRole.USER.value(), p.generate(inputs));
        conversation.addMessage(inputMessage);

        ChatMessage aiResponseMsg = chatUtils.getNextChatResponse(conversation);
        String aiResponse = aiResponseMsg.getContent();

        return aiResponse;
    }

    @Override
    public SkillDescription describe() throws Exception {
        String instructions = """
            When you need to shorten a piece of text, send a response with valid the JSON formatted as: 
            ```json
            {
                \"type\":\"summarize\", 
                \"inputText\":\"<text to shorten>\"
            }
            ```
                """;
        return new SkillDescription(
            "summarize",
            "This will take a piece of text and return a summary.",
            instructions
        );
    } 
}
