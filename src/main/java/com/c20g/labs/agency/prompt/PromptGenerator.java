package com.c20g.labs.agency.prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PromptGenerator {
	
	protected String template;
	protected List<String> inputs;
	
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}

	public List<String> getTemplateInputs() {
		Set<String> inputSet = new HashSet<>();
		Pattern pattern = Pattern.compile("\\{.+\\}");
		Matcher matcher = pattern.matcher(template);
		while(matcher.find()) {
			// System.out.println("Found match: " + matcher.group());
			inputSet.add(matcher.group());
		}
		List<String> result = new ArrayList<>();
		for(String s : inputSet) {
			result.add(s.substring(1, s.length()-1));
		}
		return result;
	}

	public abstract String generate();
	
}
