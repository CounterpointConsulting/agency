package com.c20g.labs.agency;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.c20g.labs.agency.agent.planner.PlannerAgent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.c20g.labs.agency.embeddings.EmbeddingService;
import com.c20g.labs.agency.milvus.MilvusService;
import com.c20g.labs.agency.skill.SkillLocator;
import com.c20g.labs.agency.util.ChatUtils;
import com.c20g.labs.agency.util.LogUtils;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
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

	@Autowired
	private MilvusService milvusService;

	@Autowired
	private EmbeddingService embeddingService;

	@Autowired
	private SkillLocator skillLocator;

	@Autowired
	private AgencyConfiguration agencyConfiguration;

	@Autowired
	private LogUtils logUtils;

	@Autowired
	private ChatUtils chatUtils;

	@Autowired
	private PlannerAgent plannerAgent;
	
	public static void main(String[] args) {
		SpringApplication.run(AgencyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		milvusService.loadCollection();
		milvusService.describeCollection();
		
		final Scanner stringScanner = new Scanner(System.in);

		File logFile = File.createTempFile(agencyConfiguration.getChatLogDirectory(), ".agency.log");
		PrintWriter writer = new PrintWriter(new FileWriter(logFile.getAbsolutePath()));
		LOGGER.info("Writing conversation to log file: " + logFile.getAbsolutePath());
		ConversationHistory conversation = new ConversationHistory();

		String nextMessage = null;

		System.out.println("Available agents: ");
		System.out.println("-----------------");

		System.out.println("1. Action Planner");
		System.out.println("2. Summarizer Agent");
		System.out.println("3. ChatGPT");
		System.out.println();

		System.out.print("Enter the number to run: ");

		String choice = stringScanner.nextLine();

		while(true) {
			if("1".equals(choice)) {
				plannerAgent.run("", conversation);
			}
			else if("2".equals(choice)) {
				System.out.println("Not implemented yet");
			}
			else if("3".equals(choice)) {

				conversation.getMessages().add(
						new ChatMessage(ChatMessageRole.SYSTEM.value(), 
						"You are a helpful AI agent"));

				
				while((nextMessage = chatUtils.getNextLine(stringScanner)) != null) {

					// quit on blank line
					if("".equals(nextMessage)) {
						stringScanner.close();
						openAiService.shutdownExecutor();
						System.exit(0);
					}

					// handle commands
					Set<String> commandSet = new HashSet<>();
					commandSet.add("\\p");
					if(commandSet.contains(nextMessage)) {
						// print conversation
						if("\\p".equals(nextMessage)) {
							String history = conversation.formattedHistory();
							LOGGER.debug("\nCONVERSATION HISTORY\n===\n" + history + "\n\n");
						}
					} 
					else {

						ChatMessage userInputMessage = new ChatMessage(ChatMessageRole.USER.value(), nextMessage);
						conversation.getMessages().add(userInputMessage);

						System.out.println("Sending request to OpenAI...");
						System.out.println();

						ChatCompletionRequest chatCompletionRequest = requestBuilder
							.messages(conversation.getMessages())
							.build();
							
						System.out.println();
						ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);
						Usage usage = chatCompletion.getUsage();
						LOGGER.debug("Used " + usage.getPromptTokens() + " tokens for prompt");
						LOGGER.debug("Used " + usage.getCompletionTokens() + " tokens for response");
						LOGGER.debug("Used " + usage.getTotalTokens() + " tokens total");
						
						String aiResponse = chatCompletion.getChoices().get(0).getMessage().getContent();
						System.out.println(aiResponse);
						ChatMessage aiResponseMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse);
						conversation.getMessages().add(aiResponseMessage);
						logUtils.logMessage(writer, aiResponseMessage);
						System.out.println();	
						
					}
					
				}
				stringScanner.close();
				openAiService.shutdownExecutor();
			}
			else {
				System.out.println("Invalid choice.  Please enter the number to run: ");
				choice = stringScanner.nextLine();
				continue;
			}
		}
		
	}

}
