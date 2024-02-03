package com.c20g.labs.agency.agent;

import com.c20g.labs.agency.chat.ConversationHistory;

public interface Agent {
    ConversationHistory run(String input, ConversationHistory parentConversation) throws Exception;
}
