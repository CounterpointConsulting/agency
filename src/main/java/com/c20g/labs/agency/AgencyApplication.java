package com.c20g.labs.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.c20g.labs.agency.chat.ChatWithLastMessagesPrompt;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.prompt.Example;
import com.c20g.labs.agency.prompt.MultiShotPrompt;
import com.c20g.labs.agency.prompt.NoShotPrompt;
import com.c20g.labs.agency.chat.ChatPromptGenerator;
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
		
		Map<String, String> mappings = new HashMap<>();
		mappings.put("text", "Counterpoint is at the forefront of the Business Digital Transformation and AI Revolution.  Guided by Bill, Kevin, and Steve, Counterpoint has won multiple BPTW awards and has a great set of customers.");
		
		String template = """
				Your job is to extract key words from provided text.  For example:

				{examples}

				Now you do it, following the examples given.

				Query: {text}
				Response:""";

		List<Example> examples = new ArrayList<>();
		examples.add(new Example(
			"Stripe provides APIs that web developers can use to integrate payment processing into their websites and mobile applications.", 
			"Stripe, payment processing, APIs, web developers, websites, mobile applications"));
		examples.add(new Example(
			"OpenAI has trained cutting-edge language models that are very good at understanding and generating text. Our API provides access to these models and can be used to solve virtually any task that involves processing language.", 
			"OpenAI, language models, text processing, API"));
		
		MultiShotPrompt p = new MultiShotPrompt(template, examples);
		
		CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
				.temperature(0.0)
                .prompt(p.generate(mappings))
                .echo(true)
                .user("wjm")
                .n(1)
                .maxTokens(200)
                .build();
        service.createCompletion(completionRequest).getChoices().forEach((choice) -> {
        	System.out.println(choice.getText());
        });
		
//		ConversationHistory conversation = new ConversationHistory();
//		ChatPromptGenerator prompt = new ChatWithLastMessagesPrompt(conversation, 10);
//		
//		final Scanner stringScanner = new Scanner(System.in);
//		System.out.println("What is my role?");
//		final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), stringScanner.nextLine());
//		conversation.getMessages().add(systemMessage);
//		
//		while(true) {
//			System.out.println("\n\nWhat would you like me to do?");
//			String userInput = stringScanner.nextLine();
//			final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
//			
//			if("".equals(userInput)) {
//				break;
//			}
//			
//			conversation.getMessages().add(userMessage);
//	        
//	        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
//	                .builder()
//	                .model("gpt-3.5-turbo")
//	                .messages(conversation.getMessages())
//	                .n(1)
//	                .logitBias(new HashMap<>())
//	                .build();
//
//	        StringBuilder sb = new StringBuilder();
//	        
//	        service.streamChatCompletion(chatCompletionRequest)
//	                .doOnError(Throwable::printStackTrace)
//	                .blockingForEach((x) -> { 
//	                	if(x.getChoices().size() > 0 && x.getChoices().get(0).getMessage().getContent() != null)
//	                		sb.append(x.getChoices().get(0).getMessage().getContent()); 
//	                });
//	        
//	        System.out.println("Response: " + sb.toString());
//	        
//	        final ChatMessage aiMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), sb.toString());
//	        conversation.getMessages().add(aiMessage);
//	        
//		}
//		stringScanner.close();
		
		
		// service.shutdownExecutor();
		
	}

}
