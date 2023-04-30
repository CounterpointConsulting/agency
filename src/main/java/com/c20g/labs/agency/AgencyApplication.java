package com.c20g.labs.agency;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.milvus.MilvusService;
import com.c20g.labs.agency.skill.SkillRequest;
import com.c20g.labs.agency.skill.calculate.CalculateSkill;
import com.c20g.labs.agency.skill.ticker.TickerSkill;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.embedding.EmbeddingRequest.EmbeddingRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@SpringBootApplication
public class AgencyApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgencyApplication.class);

	@Autowired
	private OpenAiService openAiService;

	@Autowired
	private ChatCompletionRequestBuilder requestBuilder;

	@Autowired
	private EmbeddingRequestBuilder embeddingRequestBuilder;

	@Autowired
	private MilvusService milvusService;

	@Autowired
	private CalculateSkill calculateSkill;

	@Autowired
	private TickerSkill tickerSkill;
	
	public static void main(String[] args) {
		SpringApplication.run(AgencyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		File logFile = File.createTempFile("/tmp", ".agency.log");
		PrintWriter writer = new PrintWriter(new FileWriter(logFile));
		LOGGER.info("Writing conversation to log file: " + logFile.getAbsolutePath());
		ConversationHistory conversation = new ConversationHistory();

		String systemMessage = "You are an AI assistant that helps users achieve a specified goal.";
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));

		String prelude = """
			You are an AI assistant that helps users achieve a specified goal.  To complete tasks, 
			you have a defined set of 'skills'.  The conversation below will provide a list of 
			skills you may make use of.  You use a skill by outputting JSON in the format specified 
			in the skill's description.

			I will also provide a list of notes called context_notes of things that you may find 
			useful, including revelant contextual information you should feel free to leverage 
			while determining your response.
				""";
		
		ChatMessage preludeMessage = new ChatMessage(ChatMessageRole.USER.value(), prelude);
		conversation.getMessages().add(preludeMessage);
		logMessage(writer, preludeMessage);
		
		ChatMessage ackPreludeMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackPreludeMessage);
		logMessage(writer, ackPreludeMessage);

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

			Each of these messages you send to me should be exactly 3 lines long.
			Do not put any content in your response after the end of the third line.  If you do, I will not
			be able to understand the request and our plan will fail.  I will send a message
			back with the correct value from using that skill.  Then carry on to the next step and repeat 
			the process.  Remember to always use the format above and we will only discuss one step at a time.

			Once you know the answer and do not need to use any further skills, send the following three lines:

			a. What I need to do: Nothing.  I know the answer.
			b. How to approach: Nothing to do.
			c. Final answer: {FINAL ANSWER}
				""";

		ChatMessage responseFormatMessage = new ChatMessage(ChatMessageRole.USER.value(), responseFormat);
		conversation.getMessages().add(responseFormatMessage);
		logMessage(writer, responseFormatMessage);

		ChatMessage ackFormatMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackFormatMessage);
		logMessage(writer, ackFormatMessage);

		String skillsDescription = """
			skills = [
				{ \"name\": \"calculate\", \"description\":\"This will take a mathematical expression and calculate its result.\", \"instructions\":\"Do not perform any mathematical calculations yourself.  When you need any calculation performed, this still is the only way for you to get the result.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"calculate\", \"expression\":\"<calculation to perform>\"}" },
				{ \"name\":\"ticker\", \"description\":\"This will take a stock symbol and date and return the open, high, low, and close value for the stock\", \"instructions\":\"Do not ever guess at the value of a stock.  Your ticker skill must be used when a stock price (or a calculation based on a stock price) is needed.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"ticker\", \"symbol\":\"<ticker symbol>\", \"date\":\"<yyyy-MM-dd>\"\"} and the result will give the open, high, low, and close prices for that stock on that day." }
			]
				""";

		ChatMessage skillsMessage = new ChatMessage(ChatMessageRole.USER.value(), skillsDescription);
		conversation.getMessages().add(skillsMessage);
		logMessage(writer, skillsMessage);

		ChatMessage ackSkillsMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackSkillsMessage);
		logMessage(writer, ackSkillsMessage);

		String contextNotes = """
			context_notes = [
			]
				""";
		
		ChatMessage contextNotesMessage = new ChatMessage(ChatMessageRole.USER.value(), contextNotes);
		conversation.getMessages().add(contextNotesMessage);
		logMessage(writer, contextNotesMessage);

		ChatMessage ackContextNotesMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand.");
		conversation.getMessages().add(ackContextNotesMessage);
		logMessage(writer, ackContextNotesMessage);

		String prompt = "What is the average of the opening stock price of AAPL and ABC on April 24, 2023?";
		ChatMessage userPromptMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
		conversation.getMessages().add(userPromptMessage);

		final Scanner stringScanner = new Scanner(System.in);

		while(true) {

			System.out.println("Sending request to OpenAI...");
			System.out.println();

			ChatCompletionRequest chatCompletionRequest = requestBuilder
				.messages(conversation.getMessages())
				.build();

			StringBuilder sb = new StringBuilder();
				
			System.out.println();
			openAiService.streamChatCompletion(chatCompletionRequest)
				.doOnError(Throwable::printStackTrace)
				.blockingForEach((x) -> { 
					if(x.getChoices().get(0).getMessage() != null && x.getChoices().get(0).getMessage().getContent() != null)
						sb.append(x.getChoices().get(0).getMessage().getContent()); 
				});
			
			String aiResponse = sb.toString();
			System.out.println(aiResponse);
			ChatMessage aiResponseMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse);
			conversation.getMessages().add(aiResponseMessage);
			logMessage(writer, aiResponseMessage);
			System.out.println();

			String nextMessage = null;
			try {
				boolean addedMessageFromSkill = parseResponseForSkillJSON(aiResponse, conversation);
				if(addedMessageFromSkill) {
					LOGGER.debug("The skill added a response automatically, proceeding...");
					continue;
				}
				else {
					LOGGER.debug("Manual intervention required.");
					System.out.print("> ");
					nextMessage = stringScanner.nextLine();
				}
			}
			catch(Exception badResponseFormatException) {
				ChatMessage exceptionMessage = new ChatMessage(ChatMessageRole.USER.value(), badResponseFormatException.getMessage());
				conversation.getMessages().add(exceptionMessage);
				logMessage(writer, exceptionMessage);
				LOGGER.debug("Caught a bad response format from OpenAI.  Castigation sent.");
				continue;
			}

			if("p".equals(nextMessage)) {
				String history = conversation.formattedHistory();
				LOGGER.debug("\nCONVERSATION HISTORY\n===\n" + history + "\n\n");
				System.out.print("> ");
				nextMessage = stringScanner.nextLine();
			}
			
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), nextMessage));
			System.out.println();

			if("".equals(nextMessage)) {
				break;
			}
		}

		stringScanner.close();
		openAiService.shutdownExecutor();
		
	}


	private boolean parseResponseForSkillJSON(String response, ConversationHistory conversation) throws Exception {
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
				skillLine = lines[i].substring(lines[i].indexOf("{"));
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
				if("ticker".equals(skillRequest.getType())) {
					try {
						String skillResult = tickerSkill.execute(req);
						LOGGER.debug("Get actual skill result: " + skillResult);
						skillResults.put(req, skillResult);
					}
					catch(Exception e) {
						LOGGER.error("Error executing skill: " + req, e);
						skillResults.put(req, "ERROR");
					}
				}
				else if("calculate".equals(skillRequest.getType())) {
					try {
						String skillResult = calculateSkill.execute(req);
						LOGGER.debug("Got actual skill result: " + skillResult);
						skillResults.put(req, skillResult);
					}
					catch(Exception e) {
						LOGGER.error("Error executing skill: " + req, e);
						skillResults.put(req, "ERROR");
					}
				}
				else {
					sendGenericRetryRequest = true;
				}
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

	private void logMessage(PrintWriter writer, ChatMessage msg) {
		writer.println(msg.getRole() + " > " + msg.getContent());
		writer.println("");
	}

}
