package com.aymanshe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class HttpClient {

    public void sendRequest(HttpRequest httpRequest) throws Exception {
        if (httpRequest.getMethod().equals("GET")) {
            getRequest(httpRequest);
        } else if (httpRequest.getMethod().equals("POST")) {
            postRequest(httpRequest);
        }else{
            System.out.println("Fix me baby one more time:"+ httpRequest.getMethod());
        }
    }

    private void getRequest(HttpRequest httpRequest) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(httpRequest.getAddress());

            Socket socket = new Socket(address, httpRequest.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());

            out.write(httpRequest.getMethod() + " " + httpRequest.getUrl() + " " + "HTTP/1.0\r\n\r\n");
            out.flush();

            while (in.hasNextLine()) {
                System.out.println(in.nextLine());
            }

            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    private void postRequest(HttpRequest httpRequest) {

    }
}
