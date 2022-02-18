package com.aymanshe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HttpRequest {
    private String method;
    private boolean verbose = false;
    private List<String> header = new ArrayList<>();
    private boolean inlineBody;
    private String body;
    private String bodyFilePath;
    private String path;
    private String address;
    private int port = 80;


    //region constructors
    public HttpRequest() {
    }

    public HttpRequest(String address, int port, String method) {
        this.address = address;
        this.port = port;
        this.method = method;
    }
    //endregion

    // region setters and getters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setInlineBody(boolean inlineBody) {
        this.inlineBody = inlineBody;
    }

    public boolean isInlineBody() {
        return inlineBody;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMethod() {
        return method.toUpperCase(Locale.ROOT);
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getBody() {
    	return body;
    }
    
    public void setBody(String body) {
    	this.body = body;
    }
    
    public String getBodyFilePath() {
    	return bodyFilePath;
    }
    
    public void setBodyFilePath(String bodyFilePath) {
    	this.bodyFilePath = bodyFilePath;
    }
    //endregion

    public  void  addHeaderPair(String fullHeader){
        header.add(fullHeader);
    }
}
