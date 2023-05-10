package com.c20g.labs.agency.agent.summarizer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.prompt.NoShotPrompt;
import com.c20g.labs.agency.prompt.PromptGenerator;
import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillLocator;
import com.c20g.labs.agency.skill.summarizer.SummarizerSkill;
import com.c20g.labs.agency.util.ChatUtils;
import com.c20g.labs.agency.util.LogUtils;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@Service
public class SummarizerAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizerAgent.class);

    @Autowired
	private OpenAiService openAiService;
    
    @Autowired
    private AgencyConfiguration agencyConfiguration;

    @Autowired
    private LogUtils logUtils;

    @Autowired
    private ChatUtils chatUtils;

    @Autowired
    private SkillLocator skillLocator;

    @Autowired
    private ChatCompletionRequestBuilder requestBuilder;

    @Override
    public String run(String input, ConversationHistory parentConversation) throws Exception {

        File logFile = File.createTempFile(agencyConfiguration.getChatLogDirectory(), ".planner.agency.log");
		PrintWriter writer = new PrintWriter(new FileWriter(logFile.getAbsolutePath()));
		LOGGER.info("Writing conversation to log file: " + logFile.getAbsolutePath());
        ConversationHistory conversation = new ConversationHistory();

        Scanner stringScanner = new Scanner(System.in);

        while(true) {
            Skill summarizerSkill = (skillLocator.locate(Arrays.asList("summarize"))).get("summarize");
            // String userPrompt = chatUtils.getNextLine(stringScanner, "Provide text to summarize or enter to quit");
            StringBuilder sb = new StringBuilder();
            String userPrompt = getLongInput(stringScanner);
            
            if("".equals(userPrompt)) {
                break;
            }
            String summary = summarizerSkill.execute("{\"type\":\"summarize\", \"inputText\":\"" + userPrompt + "\"}");
        }

        return "COMPLETE";
    }

    private String getLongInput(Scanner stringScanner) {
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
    
}
