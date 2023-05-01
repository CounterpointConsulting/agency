package com.c20g.labs.agency.loader;

import java.util.List;

public class Document {
    private String originalText;
    private List<String> chunks;

    public String getOriginalText() {
        return originalText;
    }
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public List<String> getChunks() {
        return chunks;
    }
    public void setChunks(List<String> chunks) {
        this.chunks = chunks;
    }

}
