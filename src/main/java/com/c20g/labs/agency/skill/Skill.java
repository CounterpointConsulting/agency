package com.c20g.labs.agency.skill;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Skill {
    String execute(String jsonRequest) throws JsonProcessingException;
}
