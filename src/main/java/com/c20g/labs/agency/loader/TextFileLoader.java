package com.c20g.labs.agency.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.c20g.labs.agency.config.AgencyConfiguration;

@Component
public class TextFileLoader implements Loader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextFileLoader.class);

    @Autowired
    private AgencyConfiguration agencyConfiguration;

    @Override
    public Document loadDocument(String ref) throws Exception {
        LOGGER.debug("Loading text document from: " + ref);
        Document d = new Document();
        File input = new File(ref);
        Scanner scanner = new Scanner(input);
        
        StringBuilder sb = new StringBuilder();
        while(scanner.hasNextLine()) {
            sb.append(scanner.nextLine()).append(" ");
        }
        scanner.close();

        LOGGER.debug("Got original text: " + sb.toString());
        d.setOriginalText(sb.toString());
        
        
        StringBuilder outputBuilder = new StringBuilder();
        String[] splitText = d.getOriginalText().split("\\n");
        List<String> chunks = chunkText(outputBuilder, splitText, agencyConfiguration.getTextLoaderChunkSize());
        d.setChunks(chunks);
        
        return d;
    }

    private List<String> chunkText(StringBuilder builder, String[] parts, Integer chunkSize) {

        List<String> chunksOut = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            if(parts[i].length() > chunkSize) {
                // needs to be split up and re-run
                List<String> insideSplits = chunkText(builder, parts[i].split("."), chunkSize);
                chunksOut.addAll(insideSplits);
            }
            else {
                if(builder.length() + parts[i].length() < chunkSize) {
                    builder.append(parts[i]);
                }
                else {
                    // this is messy, but close enough
                    chunksOut.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
        }
        
        return chunksOut;
    }
    
}
