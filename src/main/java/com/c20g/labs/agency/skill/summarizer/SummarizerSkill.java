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
import com.c20g.labs.agency.util.LogUtils;
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

    @Autowired
    private LogUtils logUtils;

    @Override
    public String execute(String jsonRequest) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SummarizerSkillRequest q = objectMapper.readValue(jsonRequest, SummarizerSkillRequest.class);
        LOGGER.debug("Summarizing text: " + q.getInputText());

        File logFile = File.createTempFile(agencyConfiguration.getChatLogDirectory(), ".summarizer.agency.log");
		PrintWriter writer = new PrintWriter(new FileWriter(logFile.getAbsolutePath()));
		LOGGER.info("Writing conversation to log file: " + logFile.getAbsolutePath());
        ConversationHistory conversation = new ConversationHistory();

        String systemMessage = "You are an AI assistant that helps users summarize text";
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));


        String reqTemplate = """
            You are a helpful AI assistant that summarizes longer pieces of text into shorter ones.
            The user will provide text and you will reply with the summary.  Do not include
            any explanatory text in your response, only the summary itself.

            Text to summarize:
            {text}
                """;
        
        PromptGenerator p = new NoShotPrompt(reqTemplate);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("text", q.getInputText());
        ChatMessage inputMessage = new ChatMessage(ChatMessageRole.USER.value(), p.generate(inputs));
        conversation.getMessages().add(inputMessage);
        logUtils.logMessage(writer, inputMessage);

        ChatMessage aiResponseMsg = chatUtils.getNextChatResponse(conversation);
        String aiResponse = aiResponseMsg.getContent();
        System.out.println(aiResponse);
        logUtils.logMessage(writer, aiResponseMsg);

        return aiResponse;
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "summarize",
            "This will take a piece of text and return a summary.",
            "When you need to shorten a piece of text, return as the result of that step the JSON formatted as {\"type\":\"summarize\", \"inputText\":\"<text to shorten>\"}"
        );
    } 
}
