package com.c20g.labs.agency.chat;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ConversationHistory {

	private List<ChatMessage> messages = new ArrayList<>();

	public List<ChatMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessage> messages) {
		this.messages = messages;
	}
	
}
