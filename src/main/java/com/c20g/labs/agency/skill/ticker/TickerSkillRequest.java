package com.c20g.labs.agency.skill.ticker;

import com.c20g.labs.agency.skill.SkillRequest;

public class TickerSkillRequest extends SkillRequest {
    private String symbol;
    private String date;

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    
}
