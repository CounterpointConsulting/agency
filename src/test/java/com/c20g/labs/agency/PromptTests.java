package com.c20g.labs.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

import com.c20g.labs.agency.prompt.Example;
import com.c20g.labs.agency.prompt.MultiShotPrompt;
import com.c20g.labs.agency.prompt.NoShotPrompt;

@SpringBootTest
public class PromptTests {
	
	@Test
	void buildNoShotTemplate() {
		String template = "what do you think about {topic}?";
		NoShotPrompt p = new NoShotPrompt(template);
		
		Map<String, String> mappings = new HashMap<>();
		mappings.put("topic", "love");
		
		String gen = p.generate(mappings);
		Assertions.assertEquals("what do you think about love?", gen);
	}
	
	@Test
	void buildMultiShotTemplate() {
		String template = """
				Your job is to extract key words from provided text.  For example:

				{examples}

				Now you do it, following the examples given.

				Query: {text}
				Response:""";
		
		List<Example> examples = new ArrayList<>();
		examples.add(new Example(
			"Stripe provides APIs that web developers can use to integrate payment processing into their websites and mobile applications.", 
			"Stripe, payment processing, APIs, web developers, websites, mobile applications"));
		examples.add(new Example(
			"OpenAI has trained cutting-edge language models that are very good at understanding and generating text. Our API provides access to these models and can be used to solve virtually any task that involves processing language.", 
			"OpenAI, language models, text processing, API"));
		
		MultiShotPrompt p = new MultiShotPrompt(template, examples);

		// note that stringifying the examples also adds two newlines to the tail of the examples
		String expected = """
				Your job is to extract key words from provided text.  For example:

				Query: Stripe provides APIs that web developers can use to integrate payment processing into their websites and mobile applications.
				Response: Stripe, payment processing, APIs, web developers, websites, mobile applications

				Query: OpenAI has trained cutting-edge language models that are very good at understanding and generating text. Our API provides access to these models and can be used to solve virtually any task that involves processing language.
				Response: OpenAI, language models, text processing, API



				Now you do it, following the examples given.

				Query: Counterpoint is at the forefront of the Business Digital Transformation and AI Revolution.  Guided by Bill, Kevin, and Steve, Counterpoint has won multiple BPTW awards and has a great set of customers.
				Response:""";

		Map<String, String> inputMappings = new HashMap<>();
		inputMappings.put("text", "Counterpoint is at the forefront of the Business Digital Transformation and AI Revolution.  Guided by Bill, Kevin, and Steve, Counterpoint has won multiple BPTW awards and has a great set of customers.");

		String gen = p.generate(inputMappings);
		Assertions.assertEquals(expected, gen);
	}

}
