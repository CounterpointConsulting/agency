package com.c20g.labs.agency.util;

import java.io.PrintWriter;

import org.springframework.stereotype.Component;

import com.theokanning.openai.completion.chat.ChatMessage;

@Component
public class LogUtils {
    public void logMessage(PrintWriter writer, ChatMessage msg) {
		writer.println(msg.getRole() + " > " + msg.getContent());
		writer.println("");
		writer.flush();
	}
}
