package com.c20g.labs.agency.agent.summarizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.skill.summarizer.SummarizerSkill;
import com.c20g.labs.agency.skill.summarizer.SummarizerSkillRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

@Service
public class SummarizerAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizerAgent.class);

    @Autowired
    private SummarizerSkill summarizerSkill;

    @Override
    public ConversationHistory run(String input, ConversationHistory parentConversation) throws Exception {
        // this gets returned, the "parentConversation" passed in is thrown away
        ConversationHistory conversation = new ConversationHistory();

        // normally this would come from a json request, but this makes it callable programmatically
        // alternatively, you could actually pass a string of json to "execute" but that's ugly
        // the fun part is it's immediately getting turned back into a Request by the skill...
        SummarizerSkillRequest req = new SummarizerSkillRequest();
        req.setType("summarize");
        req.setInputText(input);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String reqString = objectMapper.writeValueAsString(req);

        String summary = summarizerSkill.execute(reqString);
        conversation.addMessage(new ChatMessage(ChatMessageRole.ASSISTANT.value(), summary));
        
        return conversation;
    }
    
}
