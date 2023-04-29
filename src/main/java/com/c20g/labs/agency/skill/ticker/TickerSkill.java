package com.c20g.labs.agency.skill.ticker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.c20g.labs.agency.skill.Skill;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TickerSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickerSkill.class);

    @Override
    public String execute(String jsonRequest) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TickerSkillRequest q = objectMapper.readValue(jsonRequest, TickerSkillRequest.class);
            LOGGER.debug("Getting ticker values for: " + q.getSymbol() + " on " + q.getDate());

            if("{\"type\":\"ticker\", \"symbol\":\"AAPL\", \"date\":\"2023-04-24\"}".equals(jsonRequest)) {
				String tickerSkillTxt = "{\"Open\":145.23, \"High\":146.02, \"Low\":145.23, \"Close\":145.88}";
				LOGGER.debug("Skill result > " + tickerSkillTxt);
				return tickerSkillTxt;
			}
			else if("{\"type\":\"ticker\", \"symbol\":\"ABC\", \"date\":\"2023-04-24\"}".equals(jsonRequest)) {
				String tickerSkillTxt = "{\"Open\":15.23, \"High\":16.02, \"Low\":15.23, \"Close\":15.88}";
				LOGGER.debug("Skill result > " + tickerSkillTxt);
				return tickerSkillTxt;
			}
        }
        catch(JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, e);
        }
        return null;
    }
    
}
