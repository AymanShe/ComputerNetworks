package com.aymanshe.server;

public class MissingHeaderException extends Exception{
    public MissingHeaderException(String message) {
        super(message);
    }
}
