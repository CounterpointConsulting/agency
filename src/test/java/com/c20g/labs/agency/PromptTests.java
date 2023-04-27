package com.c20g.labs.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

import com.c20g.labs.agency.prompt.NoShotPrompt;

@SpringBootTest
public class PromptTests {
	
	@Test
	void buildNoShotTemplate() {
		Map<String, String> mappings = new HashMap<>();
		mappings.put("topic", "love");
		
		NoShotPrompt p = new NoShotPrompt("what do you think about {topic}?", mappings);
		
		String gen = p.generate();
		Assertions.assertEquals("what do you think about love?", gen);
	}

}
