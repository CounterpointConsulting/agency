package com.c20g.labs.agency.chat;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public interface ChatPromptGenerator {
	
	List<ChatMessage> generateToChatHistory();

}
