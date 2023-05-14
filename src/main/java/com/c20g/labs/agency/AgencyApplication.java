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
import com.c20g.labs.agency.agent.summarizer.SummarizerAgent;
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
	private AgencyConfiguration agencyConfiguration;

	@Autowired
	private LogUtils logUtils;

	@Autowired
	private ChatUtils chatUtils;

	@Autowired
	private PlannerAgent plannerAgent;

	@Autowired
	private SummarizerAgent summarizerAgent;
	
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

		conversation.getMessages().add(new ChatMessage(
			ChatMessageRole.SYSTEM.value(), 
			"You are a helpful general-purpose AI.  Respond to the user's prompts in a helpful manner."));

		System.out.println("PlannerGPT > ");
		String nextMessage = null;
		while((nextMessage = stringScanner.nextLine()) != null) {

			if("".equals(nextMessage)) {
				System.exit(0);
			}

			

		} 
		
	}

}
