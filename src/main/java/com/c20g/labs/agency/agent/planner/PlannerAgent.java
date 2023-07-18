package com.c20g.labs.agency.agent.planner;

import java.util.ArrayList;
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
import com.c20g.labs.agency.util.ChatUtils;
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
	private OpenAiService openAiService;

    @Autowired
    private ChatCompletionRequestBuilder requestBuilder;


    @Override
    public ConversationHistory run(String input, ConversationHistory parentConversation) throws Exception {
        
		ConversationHistory conversation = new ConversationHistory();

		String preludeString = """
			You are an AI agent designed to interact with human users and respond to arbitrary requests 
			or conversation.  You are the leader of a team of special agents that provide you with an 
			array of services.  Your task is primarily to develop a plan to respond to the user's requests.  
			Think through the problem step-by-step and generate a plan -- each step should be carried out by 
			one agent.  If your plan requires a step that none of your agents can complete, recommend and 
			describe in detail a new type of agent or operation that would be able to solve the step.

			Your team of agents includes:

			Name: Alice_Internet
			Description: Can perform network and web operations
			Operations: google_search, wikipedia_search, retrieve_url

			Name: Bob_Filesystem
			Description: Can perform filesystem operations, like saving and deleting files or retrieving file content
			Operations: write_file, read_file, delete_file, open_file_with_executable

			Name: Charlie_Programmer
			Description: Can perform tasks generally done by human software developer, which can often be used to solve general problems when combined
			Operations: write_python_script, execute_python_script

			Name: Diana_LLM
			Description: Can interact with GPT models like GPT-3.5 or GPT-4, for general conversation or problem solving
			Operations: send_message, send_message_with_history

			Do not provide any additional text or commentary other than the plan.  Do not answer anything by yourself without consulting your team of agents.  Here's a few example interaction:

			=== START EXAMPLE 1 ===
			user> Should I bring an umbrella with me today when I go outside?
			assistant> 
			Step 1: Use InternetBot to perform "google_search" to find weather near you
			Step 2: Use LLMBot to perform "send_message" and ask whether the current weather retrieved from "google_search" requires an umbrella.
			Step 3: Respond to the user with the response from LLMBot.
			=== EXAMPLE END ===

			=== START EXAMPLE 2 ===
			user> Is opening a Subway franchise in New York City a good idea for a business that will be profitable in 5 years?
			assistant> 
			Step 1: Use InternetBot to perform "google_search" for "What is the cost of opening a Subway franchise?"
			Step 2: If the answer is not directly in the search results from Step 1, use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results.
			Step 3: Use InternetBot to perform "google_search" for "How many square feet are in the average Subway location?"
			Step 4: If the answer isn't directly in the search results from Step 3, use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results.
			Step 5: Use InternetBot to perform "google_search" for "How expensive is a retail location in lower Manhatten for {{number of square feet}} square feet?", using the number of square feet returned from Step 3 or Step 4.
			Step 6: If the answer isn't directly in the search results from Step 5, use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results.
			Step 7: Use InternetBot to perform "google_search" for "What the average revenue of a Subway location in lower Manhatten?"
			Step 8: If the answer isn't directly in the search results from Step 7, use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results.
			Step 9: Use LLMBot to perform "send_message", sending the results of the previous steps in the message, and asking LLMBot whether, given the numbers retrieved whether a Subway franchise in New York City will be profitable in 10 years.
			Step 10: Respond to the user with the response from LLMBot.
			=== EXAMPLE END ===

			=== START EXAMPLE 3 ===
			user> Create a CSV file in the directory "/tmp/" called "state_facts.csv".  The CSV file should have three columns and be comma-separated.  The columns should be: 

			{{state_name}}, {{state_abbreviation}}, {{state_population}}

			There should be one row for each state in the USA.
			assistant> 
			Step 1: Use InternetBot to perform "google_search" for "What are the 50 states in the USA?".  That should contain a page that gives the states and their abbreviations.
			Step 2: Use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results from Step 1 until you find one that contains the states and abbreviations.
			Step 3: Use InternetBot to perform "google_search" for "US state populations".  That should contain a page that gives the population per state.
			Step 4: Use InternetBot to perform "retrieve_url" to retrieve likely pages from the search results from Step 3 until you find one that contains the state populations.
			Step 5: Use LLMBot to perform "send_message", sending the results of the previous steps in the message, and asking LLMBot to assemble the data into a CSV format sorted alphabetically by state abbreviation
			Step 6: Use FilesystemBot to perform "write_file", sending the results of Step 5 in the message.
			=== EXAMPLE END ===

				""";

		conversation.addMessage(new ChatMessage(ChatMessageRole.SYSTEM.value(), preludeString));
		conversation.addMessage(new ChatMessage(ChatMessageRole.USER.value(), input));

		ChatCompletionRequest chatCompletionRequest = requestBuilder
			.messages(conversation.getAllMessages())
			.build();
			
		ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);
		Usage usage = chatCompletion.getUsage();
		LOGGER.debug("Used " + usage.getPromptTokens() + " tokens for prompt");
		LOGGER.debug("Used " + usage.getCompletionTokens() + " tokens for response");
		LOGGER.debug("Used " + usage.getTotalTokens() + " tokens total");
		
		String aiResponse = chatCompletion.getChoices().get(0).getMessage().getContent();
		LOGGER.debug("Planner Agent Response > " + aiResponse);

		ChatMessage aiResponseMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), aiResponse);
		conversation.addMessage(aiResponseMessage);

		return conversation;

	}
    
}
