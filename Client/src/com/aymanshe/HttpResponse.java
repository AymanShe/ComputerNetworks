package com.aymanshe;

public class HttpResponse {
    String status;
    String headers;
    String body;

    public HttpResponse() {
    }

    public HttpResponse(String status, String headers, String body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
