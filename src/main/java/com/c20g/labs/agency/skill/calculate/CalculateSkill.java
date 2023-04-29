package com.c20g.labs.agency.skill.calculate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c20g.labs.agency.skill.Skill;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CalculateSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateSkill.class);

    @Override
    public String execute(String jsonRequest) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CalculateSkillRequest q = objectMapper.readValue(jsonRequest, CalculateSkillRequest.class);
            LOGGER.debug("Calculating expression: " + q);
        }
        catch(JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, e);
        }
        return "80.23";
    }
    
}
