package com.c20g.labs.agency.skill;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


@Service
public class SkillLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillLocator.class);
    
    @Autowired
    private ApplicationContext applicationContext;

    private Map<String,Object> beans;

    // gets a single Skill
    public Skill locate(String name) throws Exception {
        return locate(Arrays.asList(name)).get(name);
    }

    // gets a subset of all the registered SkillServices, useful when you
    // want to save prompt tokens and don't need all the skills at the
    // agent's disposal
    public Map<String, Skill> locate(List<String> skills) throws Exception {

        if(beans == null) {
            beans = applicationContext.getBeansWithAnnotation(SkillService.class);
            if(beans == null || beans.size() == 0) {
                throw new Exception("Not able to locate skill classes");
            }
        }

        Map<String, Skill> returnVal = new HashMap<>();
        Skill currentSkill = null;
        for(String skillType : skills) {
            for(String skillBeanKey : beans.keySet()) {
                currentSkill = (Skill)beans.get(skillBeanKey);
                SkillDescription description = currentSkill.describe();
                if(description.getName().equals(skillType)) {
                    LOGGER.debug("Found skill '" + skillType + "', adding to return map");
                    returnVal.put(skillType, currentSkill);
                    break;
                }  
            }
            if(currentSkill == null) {
                throw new Exception("Unknown skill '" + skillType + "'");    
            }
        }
        StringBuffer logMessage = new StringBuffer();
        logMessage.append("Returning " + returnVal.keySet().size() + " skills");
        return returnVal;
    }



}
