package com.aymanshe;

import java.util.*;

public class HttpRequest {
    private String method;
    //    private boolean verbose = false;
    private Map<String, String> headers = new HashMap<>();
    private boolean isFile;
    //    private boolean inlineBody;
    private String body;
    //    private String bodyFilePath;
//    private String path;
//    private String address;
//    private int port = 80;
//    private boolean writeToFile = false;
    private String fileName;


    //region constructors
    public HttpRequest() {
    }
//
//    public HttpRequest(String address, int port, String method) {
//        this.address = address;
//        this.port = port;
//        this.method = method;
//    }
    //endregion

    // region setters and getters


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

    //    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public List<String> getHeader() {
//        return header;
//    }
//
//    public void setHeader(List<String> header) {
//        this.header = header;
//    }
//
//    public boolean isVerbose() {
//        return verbose;
//    }
//
//
//    public void setVerbose(boolean verbose) {
//        this.verbose = verbose;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    public int getPort() {
//        return port;
//    }
//
//    public void setPort(int port) {
//        this.port = port;
//    }
//
//    public String getMethod() {
//        return method.toUpperCase(Locale.ROOT);
//    }
//
    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    //
//    public String getBodyFilePath() {
//    	return bodyFilePath;
//    }
//
//    public void setBodyFilePath(String bodyFilePath) {
//    	this.bodyFilePath = bodyFilePath;
//    }
//
//    public boolean isWriteToFile() {
//        return writeToFile;
//    }
//
//    public void setWriteToFile(boolean writeToFile) {
//        this.writeToFile = writeToFile;
//    }
//
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
    //endregion
}
