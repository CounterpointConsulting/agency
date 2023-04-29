package com.c20g.labs.agency;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.c20g.labs.agency.chat.ConversationHistory;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

@SpringBootApplication
public class AgencyApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgencyApplication.class);

	@Autowired
	private OpenAiService openAiService;

	@Autowired
	private ChatCompletionRequestBuilder requestBuilder;
	
	public static void main(String[] args) {
		SpringApplication.run(AgencyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
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
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), prelude));
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand."));

		String responseFormat = """
			Your response should take the following format:


			Plan to {WHATEVER THE FINAL GOAL IS}

			Complete task list: 
			{STEP 1}
			{STEP 2}
			{STEP n}

			Only print the complete task list once, and for each step, just print a short descriptive name.

			Then for each step, we will carry out a conversation to work through the answer. 
			For the current step, send the following three lines: 

			a. What I need to do: {STEP}
			b. How to approach: {WHICH SKILL TO USE AND WHY}
			c. Interim step: {JSON to use a skill}

			Each of these messages you send to me should be exactly 3 lines long.
			Do not put any content in your response after the end of the third line.  If you do, I will not
			be able to understand the request and our plan will fail.  I will send a message
			back with the correct value from using that skill.  Then carry on to the next step and repeat 
			the process.  Remember to always use the format above and we will only discuss one step at a time.
				""";

		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), responseFormat));
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand."));

		String skillsDescription = """
			skills = [
				{ 'name': 'calculate', 'description':'This will take a mathematical expression and calculate its result.', 'instructions':'Do not perform any mathematical calculations yourself.  When you need any calculation performed, this still is the only way for you to get the result.  When you need to use this skill, return as the result of that step the JSON formatted as {'type':'calculate', 'expression":'<calculation to perform>'}" },
				{ 'name':'ticker', 'description':'This will take a stock symbol and date and return the open, high, low, and close value for the stock', 'instructions':'Do not ever guess at the value of a stock.  Your ticker skill must be used when a stock price (or a calculation based on a stock price) is needed.  When you need to use this skill, return as the result of that step the JSON formatted as {'type':'ticker', 'symbol':'<ticker symbol>', 'date':'<yyyy-MM-dd>''} and the result will give the open, high, low, and close prices for that stock on that day." }
			]
				""";

		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), skillsDescription));
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand."));

		String contextNotes = """
			context_notes = [
			]
				""";
		
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), contextNotes));
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), "I understand."));

		String prompt = "What is the average of the opening stock price of AAPL and ABC on April 24, 2023?";
		conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

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
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse));
			System.out.println();

			try {
				boolean addedMessageFromSkill = parseResponseForSkillJSON(aiResponse, conversation);
				if(addedMessageFromSkill)
					continue;
			}
			catch(Exception badResponseFormatException) {
				conversation.getMessages().add(
					new ChatMessage(ChatMessageRole.USER.value(), badResponseFormatException.getMessage()));
				LOGGER.debug("Caught a bad response format from OpenAI.  Castigation sent.");
				continue;
			}

			System.out.print("> ");
			String nextMessage = stringScanner.nextLine();

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
		String[] lines = response.split("\n");

		// if(lines.length > 3) {
		// 	throw new Exception("""
		// 		Please resend and only print the 3 lines specified:

		// 		a. What I need to do: {STEP}
		// 		b. How to approach: {WHICH SKILL TO USE AND WHY}
		// 		c. Interim step: {JSON to use a skill}
		// 	""");
		// }

		String skillLine = null;
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].startsWith("Interim step: {")) {
				skillLine = lines[i].substring(14);
				break;
			}
		}
		LOGGER.debug("Skill to execute: " + skillLine);

		if(skillLine == null) {
			return false; // nothing to do
		}
		if("{'type':'ticker', 'symbol':'AAPL', 'date':'2023-04-24'}".equals(skillLine)) {
			String tickerSkillTxt = "{\"Open\":145.23, \"High\":146.02, \"Low\":145.23, \"Close\":145.88}";
			LOGGER.debug("Sending skill result > " + tickerSkillTxt);
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), tickerSkillTxt));
		}
		if("{'type':'ticker', 'symbol':'ABC', 'date':'2023-04-24'}".equals(skillLine)) {
			String tickerSkillTxt = "{\"Open\":15.23, \"High\":16.02, \"Low\":15.23, \"Close\":15.88}";
			LOGGER.debug("Sending skill result > " + tickerSkillTxt);
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), tickerSkillTxt));
		}
		if("{'type':'calculate', 'expression':'(145.23 + 15.23) / 2'}".equals(skillLine)) {
			String calcSkillText = "80.23";
			LOGGER.debug("Sending skill result > " + calcSkillText);
			conversation.getMessages().add(new ChatMessage(ChatMessageRole.USER.value(), calcSkillText));
		}
		return true;
	}

}
