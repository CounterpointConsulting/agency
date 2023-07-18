package com.c20g.labs.agency.skill.planner;

import com.c20g.labs.agency.skill.SkillRequest;

public class PlannerSkillRequest extends SkillRequest {
    private String requirements;

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}
