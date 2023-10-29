package com.c20g.labs.agency.agent.planner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
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
			or conversation.  You have at your disposal a set of agents that provide you with an 
			array of services.  Your task is primarily to develop a plan to respond to the user's requests.  
			Think step-by-step and generate a plan -- each step should be carried out by 
			one agent.  If your plan requires a step that none of your agents can complete, recommend and 
			describe in detail a new type of agent or operation that would be able to solve the step.

			Your team of agents includes:

			Name: InternetBot
			Description: Can perform network and web operations
			Operations: google_search, wikipedia_search, retrieve_url

			Name: FilesystemBot
			Description: Can perform filesystem operations, like saving and deleting files or retrieving file content
			Operations: write_file, read_file, delete_file, open_file_with_executable

			Name: ProgrammerBot
			Description: Can perform tasks generally done by human software developer, which can often be used to solve general problems when combined
			Operations: write_python_script, execute_python_script

			Name: LLMBot
			Description: Can interact with GPT models like GPT-3.5 or GPT-4, for general conversation or problem solving
			Operations: send_message, send_message_with_history

			You should return a response in JSON format, which will describe the plan and a list of "steps".  The response should be in the following format:
			{
				"created_plan_successfully" : [true/false],
				"steps" : [
					{
						"step_number" : [STEP NUMBER],
						"agent" : "[AGENT_NAME]",
						"operation" : "[OPERATION]",
						"purpose" : "[OBJECTIVE IN INVOKING THIS OPERATION]"
					}
				]
			}

			Do not provide any additional text or commentary other than the plan.  Do not answer anything by yourself without consulting your team of agents.  Here's a few example interactions:

			=== START EXAMPLE 1 ===
			user> Should I bring an umbrella with me today when I go outside?
			assistant> 
			{
				"created_plan_successfully" : true,
				"steps" : [
					{
						"step_number" : 1,
						"agent" : "InternetBot",
						"operation" : "google_search",
						"purpose" : "I will use this operation to find weather near you"
					},
					{
						"step_number" : 2,
						"agent" : "LLMBot",
						"operation" : "send_message",
						"purpose" : "I will use this operation to ask whether the current weather retrieved from Step 1 requires an umbrella"
					},
					{
						"step_number" : 3,
						"agent" : null,
						"operation" : null,
						"purpose" : "I will return the response from Step 2 to the user"
					}
				]
			}
			=== EXAMPLE END ===

			=== START EXAMPLE 2 ===
			user> create a new project on my local filesystem at /home/bill/Scratch/test123 that contains the source code located at https://github.com/CounterpointConsulting/agency
			assistant> 
			{
				"created_plan_successfully" : false,
				"failure_reason" : "I do not have an agent capable of cloning a git repository"
			}
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
