package com.c20g.labs.agency.skill.calculate;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

@SkillService
public class CalculateSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateSkill.class);

    @Autowired
	private AgencyConfiguration agencyConfiguration;

    @Autowired
    private ChatUtils chatUtils;

    @Override
    public String execute(String jsonRequest) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CalculateSkillRequest q = objectMapper.readValue(jsonRequest, CalculateSkillRequest.class);
            LOGGER.debug("Calculating expression: " + q.getExpression());

            try {
                String reqTemplate = """
                    The user needs to solve the following math problem:

                    {expression}

                    Write the Python code to perform the calculation.  You should
                    return only executable Python code, with no explanation.
                    
                    Begin your output with #!/usr/bin/python3
                    
                    The calculation should be in a function called calculate, and
                    the program should call 'calculate' as it's final line.  The
                    calculate function should not return any value, and it should
                    print exactly one line, which should be the final answer to
                    the request.
                        """;
                    
                ConversationHistory conversation = new ConversationHistory();

                Map<String, String> exprInputMap = new HashMap<>();
                exprInputMap.put("expression", q.getExpression());

                ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful AI agent that helps users calculate numerical results.");
                conversation.getAllMessages().add(systemMessage);

                PromptGenerator p = new NoShotPrompt(reqTemplate);
                ChatMessage userPrompt = new ChatMessage(ChatMessageRole.USER.value(), p.generate(exprInputMap));
                conversation.getAllMessages().add(userPrompt);

                ChatMessage aiResponseMsg = chatUtils.getNextChatResponse(conversation);
                System.out.println("\n" + aiResponseMsg.getContent() + "\n");
                conversation.getAllMessages().add(aiResponseMsg);

                File tmpPython = File.createTempFile(agencyConfiguration.getChatLogDirectory(), ".agency.calculate.py");
                LOGGER.debug("Writing python code to " + tmpPython.getAbsolutePath());
                PrintWriter writer = new PrintWriter(tmpPython);
                writer.println(aiResponseMsg.getContent());
                writer.close();
                tmpPython.delete();
                
                LOGGER.debug("Executing python...");
                Process proc = new ProcessBuilder("python3", tmpPython.getAbsolutePath()).start();
                InputStream is = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                br.close();
                isr.close();
                is.close();
                String response = responseBuilder.toString();
                LOGGER.debug("Got output from python: " + response);

                return response;
            }
            catch(Exception e) {
                LOGGER.error("Error getting and executing python code from mathematical expression: " + q.getExpression(), e);
                throw e;
            }
        }
        catch(JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, e);
        }

        return "FAIL";
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "calculate",
            "This will take a mathematical expression and calculate its result.",
            "Do not perform any mathematical calculations yourself.  When you need any calculation performed, this still is the only way for you to get the result.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"calculate\", \"expression\":\"<calculation to perform>\"}"
        );
    }
    
}
