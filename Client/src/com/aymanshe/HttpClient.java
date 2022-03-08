package com.aymanshe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.BreakIterator;
import java.util.Scanner;

public class HttpClient {

    public HttpResponse sendRequest(HttpRequest request) throws Exception {
        if (request.getMethod().equals("GET")) {
            HttpResponse response = getRequest(request);
            if (response.getStatus().split(" ")[1].startsWith("30")){
                //adjust the url
                String newUrl = response.getHeaders().get("Location");
                if (newUrl == null){
                    return response;
                }
                request = CommandParser.processUrl(newUrl, request);
                //call get request again
                response = getRequest(request);
            }
            return response;
        } else if (request.getMethod().equals("POST")) {
            HttpResponse response = postRequest(request);
            if (response.getStatus().split(" ")[1].startsWith("30")){
                //adjust the url
                String newUrl = response.getHeaders().get("Location");
                if (newUrl == null){
                    return response;
                }
                request = CommandParser.processUrl(newUrl, request);
                //call get request again
                response = postRequest(request);
            }
            return response;
        }else{
            throw  new Exception("Something went wrong. Could not find correct method. HttpClient.sendRequest");
        }
    }

    private HttpResponse getRequest(HttpRequest request) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(request.getAddress());

            Socket socket = new Socket(address, request.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String command = request.getMethod() + " " + (request.getPath().isEmpty()? "/" : request.getPath()) + " " + "HTTP/1.0\r\n";

            if (!request.getHeader().isEmpty()){
                StringBuilder headers = new StringBuilder();
                for(String header : request.getHeader()) {
                    headers.append(header);
                    headers.append("\r\n");
                }
                command += headers;
            }
            command += "\r\n";

            out.write(command);
            out.flush();

            HttpResponse response = parseResponse(socket);

            out.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    private HttpResponse postRequest(HttpRequest request) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(request.getAddress());

            Socket socket = new Socket(address, request.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            StringBuilder headers = new StringBuilder();
            for(String header : request.getHeader()) {
            	headers.append(header);
            	headers.append("\r\n");
            }

            String command = request.getMethod() + " " + (request.getPath().isEmpty()? "/" : request.getPath()) + " " + "HTTP/1.0\r\n"
                    + headers
                    + "Content-Length: " + request.getBody().length() + "\r\n\r\n"
                    + request.getBody();
            out.write(command);
            out.flush();

            HttpResponse response = parseResponse(socket);

            out.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }
    
    private HttpResponse parseResponse(Socket socket) throws IOException {

        Scanner in = new Scanner(socket.getInputStream());
        
        HttpResponse response = new HttpResponse();
        //read status
        String line = in.nextLine();
        response.setStatus(line);
        //read headers
        String headers = "";
        while (in.hasNextLine()) {
            line = in.nextLine();
            if (!line.isEmpty()){
                String[] splitHeader = line.split(":");
                response.addHeader(splitHeader[0], splitHeader[1]);
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
