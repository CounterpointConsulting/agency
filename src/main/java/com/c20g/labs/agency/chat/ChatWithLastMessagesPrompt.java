package com.c20g.labs.agency.chat;

import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public class ChatWithLastMessagesPrompt implements ChatPromptGenerator {
	
	private ConversationHistory conversation;
	private int historyCount;
	
	public ChatWithLastMessagesPrompt(ConversationHistory conversation, int historyCount) {
		this.conversation = conversation;
	}

	@Override
	public List<ChatMessage> generateToChatHistory() {
		List<ChatMessage> messages = new ArrayList<>();
		
		if(conversation.getMessages().size() < historyCount) {
			messages.addAll(conversation.getMessages());
		}
		else {
			messages.add(conversation.getMessages().get(0));
			messages.addAll(
					conversation.getMessages().subList(
							Math.max(conversation.getMessages().size() - historyCount, 0), 
							conversation.getMessages().size()));
		}
		
		return messages;
	}
	
}
