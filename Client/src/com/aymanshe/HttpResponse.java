package com.aymanshe;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    String status;
    Map<String, String> headers;
    String body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public  void addHeader(String key, String value){
        headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
