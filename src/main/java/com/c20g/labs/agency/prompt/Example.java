package com.c20g.labs.agency.prompt;

public class Example {

	private String query;
	private String response;
	
	public Example(String query, String response) {
		this.query = query;
		this.response = response;
	}
	
	public String toString() {
		return "Query: " + query + "\nResponse: " + response;
	}
	
}
