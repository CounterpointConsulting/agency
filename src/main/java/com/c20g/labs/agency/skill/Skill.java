package com.c20g.labs.agency.skill;

public interface Skill {
    String execute(String jsonRequest) throws Exception;
    SkillDescription describe() throws Exception;
}
