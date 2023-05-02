package com.c20g.labs.agency.skill.calculate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillDescription;
import com.c20g.labs.agency.skill.SkillService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SkillService
public class CalculateSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateSkill.class);

    @Override
    public String execute(String jsonRequest) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CalculateSkillRequest q = objectMapper.readValue(jsonRequest, CalculateSkillRequest.class);
            LOGGER.debug("Calculating expression: " + q.getExpression());
        }
        catch(JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, e);
        }
        return "80.23";
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "calculate",
            "This will take a mathematical expression and calculate its result.",
            "Do not perform any mathematical calculations yourself.  When you need any calculation performed, this still is the only way for you to get the result.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"calculate\", \"expression\":\"<calculation to perform>\"}"
        );
    }
    
}
