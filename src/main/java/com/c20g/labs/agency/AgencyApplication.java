package com.c20g.labs.agency;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.c20g.labs.agency.agent.planner.PlannerAgent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

@SpringBootApplication
public class AgencyApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgencyApplication.class);

	@Autowired
	private PlannerAgent topLevelPlanner;
	
	public static void main(String[] args) {
		SpringApplication.run(AgencyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		final Scanner stringScanner = new Scanner(System.in);
		ConversationHistory topLevelPlannerConversation = new ConversationHistory();

		System.out.println("Welcome to AgencyGPT!  Please do some stuff.");

		System.out.print("AgencyGPT > ");
		String nextMessage = null;
		while((nextMessage = stringScanner.nextLine()) != null) {

			if("exit".equals(nextMessage.toLowerCase())) {
				break;
			}

			ChatMessage userInputMessage = new ChatMessage(ChatMessageRole.USER.value(), nextMessage);
			topLevelPlannerConversation.addMessage(userInputMessage);
			topLevelPlanner.run(nextMessage, topLevelPlannerConversation);

			System.out.print("AgencyGPT > ");
		} 
		stringScanner.close();
	}

}
