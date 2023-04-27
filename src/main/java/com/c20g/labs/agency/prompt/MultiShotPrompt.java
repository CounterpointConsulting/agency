package com.c20g.labs.agency.prompt;

import java.util.List;
import java.util.Map;

import org.slf4j.*;


public class MultiShotPrompt extends PromptGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiShotPrompt.class);
	
	private List<Example> examples;
	
	public MultiShotPrompt(String template, List<Example> examples) {
		this.template = template;
		this.examples = examples;
	}

	@Override
	public String generate(Map<String, String> inputMappings) {

		StringBuilder sbExamples = new StringBuilder();
		for(Example e : examples) {
			String examples = e.toString();
			sbExamples.append(examples).append("\n\n");
		}
		String templateWithExamples = template.replaceAll("\\{examples\\}", sbExamples.toString());
		
		List<String> inputList = getTemplateInputs();
		for(String input : inputList) {
			templateWithExamples = templateWithExamples.replaceAll("\\{"+input+"\\}", inputMappings.get(input));
		}

		return templateWithExamples;
	}

}
