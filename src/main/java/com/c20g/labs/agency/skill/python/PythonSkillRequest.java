package com.c20g.labs.agency.skill.python;

import com.c20g.labs.agency.skill.SkillRequest;

public class PythonSkillRequest extends SkillRequest {
    private String filename;
    private String content;

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
}
