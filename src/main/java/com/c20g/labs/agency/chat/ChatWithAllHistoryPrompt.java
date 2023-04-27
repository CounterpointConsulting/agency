package com.c20g.labs.agency.chat;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ChatWithAllHistoryPrompt implements ChatPromptGenerator {
	
	private ConversationHistory conversation;
	
	public ChatWithAllHistoryPrompt(ConversationHistory conversation) {
		this.conversation = conversation;
	}

	@Override
	public List<ChatMessage> generateToChatHistory() {
		return conversation.getMessages();
	}
}
