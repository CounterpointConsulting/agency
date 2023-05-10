package com.c20g.labs.agency.skill.summarizer;

import com.c20g.labs.agency.skill.SkillRequest;

public class SummarizerSkillRequest extends SkillRequest {
    private String inputText;

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
    
}
