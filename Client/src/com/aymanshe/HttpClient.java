package com.aymanshe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class HttpClient {

    public HttpResponse sendRequest(HttpRequest request) throws Exception {
        if (request.getMethod().equals("GET")) {
            return getRequest(request);
        } else if (request.getMethod().equals("POST")) {
            return postRequest(request);
        }else{
            System.out.println("Fix me baby one more time:"+ request.getMethod());
            //todo handle return
            return null;
        }
    }

    private HttpResponse getRequest(HttpRequest request) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(request.getAddress());

            Socket socket = new Socket(address, request.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());

            String command = request.getMethod() + " " + request.getPath() + " " + "HTTP/1.0\r\n\r\n";
            out.write(command);
            out.flush();

            HttpResponse response = new HttpResponse();
            //read status
            String line = in.nextLine();
            response.setStatus(line);
            //read headers
            String headers = "";
            while (in.hasNextLine()) {
                line = in.nextLine();
                if (!line.isEmpty()){
                    headers += line;
                    headers += "\r\n";
                }else{
                    break;
                }
            }
            response.setHeaders(headers);

            //read body
            String body = "";
            while (in.hasNextLine()){
                line = in.nextLine();
                body +=line;
                body += "\r\n";
            }
            response.setBody(body);


            out.close();
            in.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    private HttpResponse postRequest(HttpRequest httpRequest) {
        return null;
    }
}
