package com.c20g.labs.agency.skill.python;

import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillDescription;
import com.c20g.labs.agency.skill.SkillService;
import com.fasterxml.jackson.core.JsonProcessingException;

@SkillService
public class PythonSkill implements Skill {

    @Override
    public String execute(String jsonRequest) throws JsonProcessingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "python",
            "This will execute arbitrary Python code.",
            "Executes arbitrary logic or calculations.  When you need to use this skill, return as the result of that step the JSON formatted as {\"type\":\"python\", \"filename\":\"<name of the python file>\", \"content\" : \"<Python code to execute>\"}"
        );
    }
    
}
