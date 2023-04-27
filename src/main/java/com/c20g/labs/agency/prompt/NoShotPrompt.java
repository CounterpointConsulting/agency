package com.c20g.labs.agency.prompt;

import java.util.HashMap;
import java.util.Map;

public class NoShotPrompt extends PromptGenerator {
	
	private Map<String, String> inputMappings = new HashMap<>();
	
	public NoShotPrompt(String template, Map<String, String> inputMappings) {
		this.template = template;
		this.inputMappings = inputMappings;
	}
	
	@Override
	public String generate() {
		for(String input : getTemplateInputs()) {
			template = template.replaceAll("\\{"+input+"\\}", inputMappings.get(input));
		}
		return template;
	}

}
