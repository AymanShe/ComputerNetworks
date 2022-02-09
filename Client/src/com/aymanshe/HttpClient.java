package com.aymanshe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class HttpClient {

    public String sendRequest(HttpRequest httpRequest) throws Exception {
        if (httpRequest.getMethod().equals("GET")) {
            return getRequest(httpRequest);
        } else if (httpRequest.getMethod().equals("POST")) {
            return postRequest(httpRequest);
        }else{
            System.out.println("Fix me baby one more time:"+ httpRequest.getMethod());
            //todo handle return
            return null;
        }
    }

    private String getRequest(HttpRequest httpRequest) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(httpRequest.getAddress());

            Socket socket = new Socket(address, httpRequest.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());

            String command = httpRequest.getMethod() + " " + httpRequest.getPath() + " " + "HTTP/1.0\r\n\r\n";
            out.write(command);
            out.flush();

            StringBuilder stringBuilder = new StringBuilder();
            while (in.hasNextLine()) {
                stringBuilder.append(in.nextLine());
                stringBuilder.append("\r\n");
            }
            String response = stringBuilder.toString();

            out.close();
            in.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    private String postRequest(HttpRequest httpRequest) {
        return "";
    }
}
