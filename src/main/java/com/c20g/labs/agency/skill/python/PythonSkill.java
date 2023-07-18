package com.c20g.labs.agency.skill.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

@SkillService
public class PythonSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonSkill.class);

    @Autowired
	private AgencyConfiguration agencyConfiguration;

    @Autowired
    private ChatUtils chatUtils;

    @Override
    public String execute(String jsonRequest) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PythonSkillRequest q = objectMapper.readValue(jsonRequest, PythonSkillRequest.class);
            LOGGER.debug("Executing python for : " + q.getDescription());

            try {

                String reqTemplate = """
                    The user needs Python code that fits the following description:

                    {description}

                    Write the Python code to perform the calculation.  You should
                    return only executable Python code, with no explanation.
                    
                    Begin your output with '#!/usr/bin/python3'

                    Your code to solve the problem should be in a function called
                    'execute', and the program should call 'execute' as its final line.
                    The execute function should not return any value, and it
                    should only print one line with the final result to answer
                    the request.
                        """;

                ConversationHistory conversation = new ConversationHistory();

                Map<String, String> inputMap = new HashMap<>();
                inputMap.put("description", q.getDescription());

                ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), """
                    You are a helpful AI agent that solves problems by writing Python
                    code for the user to execute.
                        """);

                conversation.addMessage(systemMessage);

                PromptGenerator p = new NoShotPrompt(reqTemplate);
                ChatMessage userPrompt = new ChatMessage(ChatMessageRole.USER.value(), p.generate(inputMap));
                conversation.addMessage(userPrompt);

                ChatMessage aiResponseMsg = chatUtils.getNextChatResponse(conversation);
                System.out.println("\n" + aiResponseMsg.getContent() + "\n");
                conversation.addMessage(aiResponseMsg);

                return aiResponseMsg.getContent();
            }
            catch(Exception e) {
                LOGGER.error("Error getting python code for description: " + q.getDescription(), e);
                throw e;
            }
        }
        catch(JsonParseException jpe) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, jpe);
        }

        return "FAIL";
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "python",
            "This will execute arbitrary Python code.",
            "Executes arbitrary logic or calculations.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"python\", \"description\" : \"<Description of the Python code to generate>\"}"
        );
    }
    
}
