package com.c20g.labs.agency.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFileLoader implements Loader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextFileLoader.class);

    @Override
    public Document loadDocument(String ref) throws Exception {
        LOGGER.debug("Loading text document from: " + ref);
        Document d = new Document();
        File input = new File(ref);
        Scanner scanner = new Scanner(input);
        
        StringBuilder sb = new StringBuilder();
        while(scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
        }
        scanner.close();

        LOGGER.debug("Got original text: " + sb.toString());
        d.setOriginalText(sb.toString());
        
        List<String> chunks = new ArrayList<>();
        String[] splitText = d.getOriginalText().split("\\.");
        for(int i = 0; i < splitText.length; i++) {
            chunks.add(splitText[i]);
        }
        d.setChunks(chunks);
        
        return d;
    }
    
}
