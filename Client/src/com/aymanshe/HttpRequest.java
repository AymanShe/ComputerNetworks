package com.aymanshe;

import java.util.Locale;

public class HttpRequest {
    private String address;
    private int port;
    private String method;
    private String url;

    public HttpRequest(String address, int port, String method, String url) {
        this.address = address;
        this.port = port;
        this.method = method;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
