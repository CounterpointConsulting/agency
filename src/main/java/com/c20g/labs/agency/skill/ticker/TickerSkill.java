package com.c20g.labs.agency.skill.ticker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillDescription;
import com.c20g.labs.agency.skill.SkillService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@SkillService
public class TickerSkill implements Skill {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickerSkill.class);

    @Override
    public String execute(String jsonRequest) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TickerSkillRequest q = objectMapper.readValue(jsonRequest, TickerSkillRequest.class);
            LOGGER.debug("Getting ticker values for: " + q.getSymbol() + " on " + q.getDate());

            // this is just two data points I'm using for testing, stock APIs are 'spensive

            if("AAPL".equals(q.getSymbol()) && "2023-04-24".equals(q.getDate())) {
				String tickerSkillTxt = "{\"Open\":145.23, \"High\":146.02, \"Low\":145.23, \"Close\":145.88}";
				LOGGER.debug("Skill result > " + tickerSkillTxt);
				return tickerSkillTxt;
			}
			else if("ABC".equals(q.getSymbol()) && "2023-04-24".equals(q.getDate())) {
				String tickerSkillTxt = "{\"Open\":15.23, \"High\":16.02, \"Low\":15.23, \"Close\":15.88}";
				LOGGER.debug("Skill result > " + tickerSkillTxt);
				return tickerSkillTxt;
			}
            else {
                throw new Exception(
                    "Ticker skill couldn't find a result for " + q.getSymbol() + " / " + q.getDate());
            }
        }
        catch(JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: " + jsonRequest, e);
        }
        return null;
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "ticker", 
            "This will take a stock symbol and date and return the open, high, low, and close value for the stock.", 
            "Do not ever guess at the value of a stock.  Your ticker skill must be used when a stock price (or a calculation based on a stock price) is needed.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"ticker\", \"symbol\":\"<ticker symbol>\", \"date\":\"<yyyy-MM-dd>\"}"
        );
    }
    
}
