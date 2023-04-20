package com.c20g.labs.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

@SpringBootApplication
public class AgencyApplication implements CommandLineRunner {

	@Value("${openai.apikey}")
	private String openaiApiKey;
	
	public static void main(String[] args) {
		SpringApplication.run(AgencyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		OpenAiService service = new OpenAiService(openaiApiKey);
		final List<ChatMessage> messages = new ArrayList<>();
		
		Scanner stringScanner = new Scanner(System.in);
		System.out.println("What is my role?");
		final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), stringScanner.nextLine());
		messages.add(systemMessage);
		
		while(true) {
			System.out.println("\n\nWhat would you like me to do?");
			String userInput = stringScanner.nextLine();
			final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
			
			if("".equals(userInput)) {
				break;
			}
			
			messages.add(userMessage);
	        
	        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
	                .builder()
	                .model("gpt-3.5-turbo")
	                .messages(messages)
	                .n(1)
	                .logitBias(new HashMap<>())
	                .build();

	        StringBuilder sb = new StringBuilder();
	        
	        // System.out.println("\n\nSending: " + chatCompletionRequest.toString() + " \n\n");
	        
	        service.streamChatCompletion(chatCompletionRequest)
	                .doOnError(Throwable::printStackTrace)
	                .blockingForEach((x) -> { 
	                	if(x.getChoices().size() > 0 && x.getChoices().get(0).getMessage().getContent() != null)
	                		sb.append(x.getChoices().get(0).getMessage().getContent()); 
	                });
	        
	        System.out.println("Response: " + sb.toString());
	        
	        final ChatMessage aiMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), sb.toString());
	        messages.add(aiMessage);
	        
		}
		
		service.shutdownExecutor();
		
	}

}
