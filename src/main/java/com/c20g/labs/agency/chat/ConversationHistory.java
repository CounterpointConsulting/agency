package com.c20g.labs.agency.chat;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ConversationHistory {

	protected List<ChatMessage> allMessages = new ArrayList<>();

	public List<ChatMessage> getAllMessages() {
		return allMessages;
	}

	public void addMessage(ChatMessage message) {
		this.allMessages.add(message);
	}

	public void setAllMessages(List<ChatMessage> messages) {
		this.allMessages = messages;
	}

	public String formattedFullHistory() {
		StringBuilder sb = new StringBuilder();
		for(ChatMessage m : allMessages) {
			sb.append(m.getRole()).append(" > ").append(m.getContent()).append("\n\n");
		}
		return sb.toString();
	}
	
}
