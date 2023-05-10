package com.c20g.labs.agency.loader;

public interface Loader {
    // "ref" here is some external ID
    Document loadDocument(String ref) throws Exception;
}
