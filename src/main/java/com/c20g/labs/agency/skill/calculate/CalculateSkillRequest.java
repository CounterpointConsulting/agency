package com.c20g.labs.agency.skill.calculate;

import com.c20g.labs.agency.skill.SkillRequest;

public class CalculateSkillRequest extends SkillRequest {
    
    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
    
}
