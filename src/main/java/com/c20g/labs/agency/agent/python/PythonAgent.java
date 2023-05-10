package com.c20g.labs.agency.agent.python;

import org.springframework.stereotype.Service;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;

@Service
public class PythonAgent implements Agent {

    @Override
    public String run(String input, ConversationHistory parentConversation) throws Exception {
        
        return null;
    }
    
}
