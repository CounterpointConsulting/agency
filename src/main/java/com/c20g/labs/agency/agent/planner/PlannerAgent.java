package com.c20g.labs.agency.agent.planner;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillLocator;
import com.c20g.labs.agency.skill.SkillRequest;
import com.c20g.labs.agency.util.ChatUtils;
import com.c20g.labs.agency.util.LogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;


@Service
public class PlannerAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerAgent.class);

    @Autowired
    private AgencyConfiguration agencyConfiguration;

    @Autowired
    private LogUtils logUtils;

    @Autowired
    private ChatUtils chatUtils;

    @Autowired
    private SkillLocator skillLocator;

    @Autowired
	private OpenAiService openAiService;

    @Autowired
    private ChatCompletionRequestBuilder requestBuilder;

    @Override
    public String run(String input, ConversationHistory parentConversation) throws Exception {
        
        File logFile = File.createTempFile(agencyConfiguration.getChatLogDirectory(), ".planner.agency.log");
		PrintWriter writer = new PrintWriter(new FileWriter(logFile.getAbsolutePath()));
		LOGGER.info("Writing conversation to log file: " + logFile.getAbsolutePath());
		ConversationHistory conversation = new ConversationHistory();

        Scanner stringScanner = new Scanner(System.in);

		String systemMessage = "You are an AI assistant that helps users achieve a specified goal.";
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));

		String prelude = """
			You are an AI assistant that helps users achieve a specified goal.  To complete tasks, 
			you have a defined set of 'skills'.  The conversation below will provide a list of 
			skills you may make use of.  You use a skill by outputting JSON in the format specified 
			in the skill's description.
				""";
		
		ChatMessage preludeMessage = new ChatMessage(ChatMessageRole.USER.value(), prelude);
		conversation.getMessages().add(preludeMessage);
		logUtils.logMessage(writer, preludeMessage);
		
		ChatMessage ackPreludeMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackPreludeMessage);
		logUtils.logMessage(writer, ackPreludeMessage);

		String responseFormat = """
			Your response should take the following format:


			Plan to {WHATEVER THE FINAL GOAL IS}

			Complete task list: 
			{STEP 1}
			{STEP 2}
			{STEP n}

			Only print the complete task list once, and for each step, just print a short descriptive name.
			Do not specify in the steps which skills you will use to solve it, just print the high-level
			steps.

			Then for each step, we will carry out a conversation to work through the answer. 
			We will only do one step at a time.
			For the current step, send the following three lines: 

			a. What I need to do: {STEP}
			b. How to approach: {WHICH SKILL TO USE AND WHY}
			c. Interim step: {JSON to use a skill}

			Each of these messages you send to me should be exactly 3 lines long.  In particular, the 
			\"Interim step:\" JSON must be on one line (do not pretty-print the JSON).
			Do not put any content in your response after the end of the third line.  If you do, I will not
			be able to understand the request and our plan will fail.  I will send a message
			back with the correct value from using that skill.  Then carry on to the next step and repeat 
			the process.  Remember to always use the format above and we will only discuss one step at a time.

			Once you know the answer and do not need to use any further skills, send the following three lines:

			a. What I need to do: Nothing.  I know the answer.
			b. How to approach: Nothing to do.
			c. Final answer: {FINAL ANSWER}

			Every message you send to me should be exactly 3 lines long.
				""";

		ChatMessage responseFormatMessage = new ChatMessage(ChatMessageRole.USER.value(), responseFormat);
		conversation.getMessages().add(responseFormatMessage);
		logUtils.logMessage(writer, responseFormatMessage);

		ChatMessage ackFormatMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackFormatMessage);
		logUtils.logMessage(writer, ackFormatMessage);

		Map<String, Skill> skills = skillLocator.locate(Arrays.asList("ticker", "calculate", "python"));
		
		StringBuilder skillsSB = new StringBuilder("Skills").append("\n\n");
		for(String k : skills.keySet()) {
			Skill s = skills.get(k);
			skillsSB.append("Name: " + s.describe().getName()).append("\n");
			skillsSB.append("Description: " + s.describe().getDescription()).append("\n");
			skillsSB.append("Instructions: " + s.describe().getInstructions()).append("\n\n");
		}

		String skillsDescription = """
			Skills

			{skills}
				""";
		
		skillsDescription = skillsDescription.replaceAll("\\{skills\\}", skillsSB.toString());

		ChatMessage skillsMessage = new ChatMessage(ChatMessageRole.USER.value(), skillsDescription);
		conversation.getMessages().add(skillsMessage);
		logUtils.logMessage(writer, skillsMessage);

		ChatMessage ackSkillsMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackSkillsMessage);
		logUtils.logMessage(writer, ackSkillsMessage);

		System.out.println("Here's an example prompt: What is the average of the opening stock price of AAPL and ABC on April 24, 2023?");
		String prompt = chatUtils.getNextLine(stringScanner);
		ChatMessage userPromptMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
		conversation.getMessages().add(userPromptMessage);

		while(true) {

			System.out.println("Sending request to OpenAI...");
			System.out.println();

			ChatCompletionRequest chatCompletionRequest = requestBuilder
				.messages(conversation.getMessages())
				.build();
				
			System.out.println();
			ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);
			Usage usage = chatCompletion.getUsage();
            LOGGER.debug("Tokens: (" + usage.getPromptTokens() + " / " + usage.getCompletionTokens() + ")");
			
			String aiResponse = chatCompletion.getChoices().get(0).getMessage().getContent();
			System.out.println(aiResponse);
			ChatMessage aiResponseMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse);
			conversation.getMessages().add(aiResponseMessage);
			logUtils.logMessage(writer, aiResponseMessage);
			System.out.println();

			try {
				boolean addedMessageFromSkill = parseResponseForSkillJSON(aiResponse, conversation, skills);
				if(addedMessageFromSkill) {
					LOGGER.debug("The skill added a response automatically, proceeding...");
					continue;
				}
				else {
					LOGGER.debug("Manual intervention required.");
					String userInput = chatUtils.getNextLine(stringScanner);
                    ChatMessage exceptionMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
                    conversation.getMessages().add(exceptionMessage);
                    continue;
				}
			}
			catch(Exception badResponseFormatException) {
				ChatMessage exceptionMessage = new ChatMessage(ChatMessageRole.USER.value(), badResponseFormatException.getMessage());
				conversation.getMessages().add(exceptionMessage);
				logUtils.logMessage(writer, exceptionMessage);
				LOGGER.debug("Caught a bad response format from OpenAI.  Castigation sent.");
				continue;
			}
        }
    }

    private boolean parseResponseForSkillJSON(String response, ConversationHistory conversation, Map<String, Skill> skills) throws Exception {
		LOGGER.debug("Analyzing the response for skill JSON");
		
		String[] lines = response.split("\n");

		boolean sendGenericRetryRequest = false;

		// this is a little too restrictive.  it's hard to get chatgpt to 
		// respond without additional commentary, so it'll generally have more than 3 lines
		// that said, it usually DOES work correctly the second time.  just not...  you know.

		/*
		// if(lines.length > 3) {
		// 	throw new Exception(
		// 		Please resend and only print the 3 lines specified:

		// 		a. What I need to do: {STEP}
		// 		b. How to approach: {WHICH SKILL TO USE AND WHY}
		// 		c. Interim step: {JSON to use a skill}
		// 	);
		}
		*/

		// yes, there are better ways to do this.  be quiet.
		List<String> skillRequests = new ArrayList<>();
		String skillLine = null;
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].contains("Interim step: {")) {
				skillLine = lines[i].trim().substring(lines[i].indexOf("{"));
				skillRequests.add(skillLine);
				break;
			}
		}

		if(skillLine == null) {
			// try again and just see if we can find any json that looks like a skill request
			for(int i = 0; i < lines.length; i++) {
				if(lines[i].contains("{\"type\":\"") && lines[i].endsWith("}")) {
					skillLine = lines[i].substring(lines[i].indexOf("{"));
					skillRequests.add(skillLine);
					break;
				}
			}	
		}

		Map<String, String> skillResults = new HashMap<>();

		for(String req : skillRequests) {
			LOGGER.debug("Skill to execute: " + req);

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				SkillRequest skillRequest = objectMapper.readValue(req, SkillRequest.class);
				String skillResult = skills.get(skillRequest.getType()).execute(req);
				LOGGER.debug("Get actual skill result: " + skillResult);
				skillResults.put(req, skillResult);				
			}
			catch(Exception e) {
				sendGenericRetryRequest = true;
			}
		}

		if(sendGenericRetryRequest) {

			// Another version that might work:
			// Please resend and only print the 3 lines specified: a. What I need to do: {STEP}, b. How to approach: {WHICH 
			// SKILL TO USE AND WHY}, c. Interim step: {JSON to use a skill}
			//
			String errMsg = """
				I was unable to process your response because you did not follow the format 
				specified in the instructions.  Please limit your response to exactly 3 lines
				and follow the format:
				
				a. What I need to do: {STEP}
				b. How to approach: {WHICH SKILL TO USE AND WHY}
				c. Interim step: {JSON to use a skill}
					""";
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), errMsg));
			return true;
		}

		if(skillRequests.size() == 0) {
			return false;
		}

		StringBuilder skillResponseSB = new StringBuilder();
		skillResponseSB.append("You requested to use " + skillRequests.size() + " skills.  Here are the results:");
		skillResponseSB.append("\n\n");
		for(String req : skillRequests) {
			skillResponseSB.append("The request " + req + " resulted in " + skillResults.get(req));
			skillResponseSB.append("\n");
		}
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), skillResponseSB.toString()));

		return true;
	}
    
}
