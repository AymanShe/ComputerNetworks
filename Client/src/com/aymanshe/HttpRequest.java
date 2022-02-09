package com.aymanshe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpRequest {
    private String address;
    private String path;
    private int port = 80;
    private String method;
    private boolean verbose = false;
    private Map<String,String> header = new HashMap<>();


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

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public boolean isVerbose() {
        return verbose;
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
    //endregion

    public  void  addHeaderPair(String key,String value){
        header.put(key,value);
    }
}
