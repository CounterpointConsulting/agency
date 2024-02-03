package com.c20g.labs.agency.agent.planner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.c20g.labs.agency.agent.Agent;
import com.c20g.labs.agency.chat.ConversationHistory;
import com.c20g.labs.agency.config.AgencyConfiguration;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder;
import com.theokanning.openai.service.OpenAiService;

public class PlanStepValidatorAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanStepValidatorAgent.class);

    @Autowired
    private AgencyConfiguration agencyConfiguration;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private ChatCompletionRequestBuilder requestBuilder;
    
    @Override
    public ConversationHistory run(String input, ConversationHistory parentConversation) throws Exception {
        
        ConversationHistory conversation = new ConversationHistory();
        
        String preludeString = """
            You are an AI agent designed to review the steps of a plan and decide whether each as been
            appropriate assigned to another capable agent.  Each agent has a set of operations.  You will
            be asked to determine whether it is likely that the suggested agent and operation will
            fulfill the purpose of that step.

            Is it likely that a software agent called {agent_name} would be able to use an operation 
            called {operation_name} to fulfill the goal "{purpose}"?  Answer in the following JSON
            format (only populate the "reason" field if the operation is not appropriate):
            {
                "operation_is_appropriate" : [true or false],
                "reason" : "[REASON THIS OPERATION IS NOT LIKELY USEFUL FOR THE PURPOSE]"
            }
                """;
        
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
    
}
