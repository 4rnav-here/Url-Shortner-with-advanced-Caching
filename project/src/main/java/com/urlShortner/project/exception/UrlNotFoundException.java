package com.urlShortner.project.exception;

public class UrlNotFoundException
        extends RuntimeException {

    public UrlNotFoundException(String message) {
        super(message);
    }
}