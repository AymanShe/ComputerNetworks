package com.aymanshe.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class HttpClient {

    public HttpResponse sendRequest(HttpRequest request) throws Exception {

        HttpResponse response = sendTcp(request);
        if (response.getStatus().split(" ")[1].startsWith("30")){
            //adjust the url
            String newUrl = response.getHeaders().get("Location");
            if (newUrl == null){
                return response;
            }
            request = CommandParser.processUrl(newUrl, request);
            //call get request again
            response = sendTcp(request);
        }
        return response;
    }

    private HttpResponse sendTcp(HttpRequest request) throws Exception {
        try {
            InetAddress address = InetAddress.getByName(request.getAddress());

            Socket socket = new Socket(address, request.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String command = request.getPayload();
            out.write(command);
            out.flush();

            Scanner in = new Scanner(socket.getInputStream());
            HttpResponse response = HttpResponse.parseResponse(in);

            out.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }
}
