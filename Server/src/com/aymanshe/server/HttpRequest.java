package com.aymanshe.server;

import java.util.*;

public class HttpRequest {
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private boolean isFile;
    private String body;
    private String Directory;
    private String fileName;


    public HttpRequest() {
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isGet() {
        return method.toLowerCase(Locale.ROOT).equals("get");
    }

    public boolean isPost() {
        return method.toLowerCase(Locale.ROOT).equals("post");
    }

    public String getDirectory() {
        return Directory;
    }

    public void setDirectory(String Directory) {
        this.Directory = Directory;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addHeadersPair(String key, String value) {
        headers.put(key, value);
    }

    public String getHeaders(String key) {
        return headers.get(key);
    }
}
