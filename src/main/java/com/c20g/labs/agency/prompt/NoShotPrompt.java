package com.c20g.labs.agency.prompt;

import java.util.Map;

public class NoShotPrompt extends PromptGenerator {
	
	public NoShotPrompt(String template) {
		this.template = template;
	}
	
	@Override
	public String generate(Map<String, String> inputMappings) {
		String finalPrompt = template;
		for(String input : getTemplateInputs()) {
			finalPrompt = finalPrompt.replaceAll("\\{"+input+"\\}", inputMappings.get(input));
		}
		return finalPrompt;
	}

}
