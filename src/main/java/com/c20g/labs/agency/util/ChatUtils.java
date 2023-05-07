package com.c20g.labs.agency.util;

import java.util.Scanner;

import org.springframework.stereotype.Component;

@Component
public class ChatUtils {
    public String getNextLine(Scanner stringScanner) {
		System.out.print("> ");
		String input = stringScanner.nextLine();
		return input;
	}
}
