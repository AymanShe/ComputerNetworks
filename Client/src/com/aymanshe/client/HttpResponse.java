package com.aymanshe.client;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

    public static HttpResponse parseResponse(Scanner in) throws IOException {
        HttpResponse response = new HttpResponse();
        //read status
        String line = in.nextLine();
        response.setStatus(line);
        //read headers
        String headers = "";
        while (in.hasNextLine()) {
            line = in.nextLine();
            if (!line.isEmpty()){
                int endOfKey = line.indexOf(":");
                String[] splitHeader = line.split(":");
                response.addHeader(line.substring(0,endOfKey), line.substring(endOfKey+1));
            }else{
                break;
            }
        }

        //read body
        StringBuilder body = new StringBuilder();
        while (in.hasNextLine()){
            line = in.nextLine();
            body.append(line);
            body.append("\r\n");
        }
        response.setBody(body.toString());

        in.close();
        return response;
    }
}
