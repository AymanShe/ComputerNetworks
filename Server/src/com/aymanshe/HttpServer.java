package com.aymanshe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HttpServer {

    int port;
    String path;

    public HttpServer(int port, String path) {
        this.port = port;
        this.path = path;
    }

    public void run() throws IOException {
        try (ServerSocket server = new ServerSocket(port, 0, InetAddress.getLoopbackAddress())) {
            System.out.println("Server started listening on port :" + port);

            while (true) {
                Socket client = server.accept();
                listenAndRespond(client);
            }
        }
    }

    private void listenAndRespond(Socket socket) {
        try (Socket client = socket) {
            Scanner in = new Scanner(client.getInputStream());
            StringBuilder response = new StringBuilder();
            HttpRequest request = new HttpRequest();
            //now the server is waiting for requests
            if (in.hasNextLine()){
                ok(socket);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Echo error "+ e);
        }
    }

    void ok(Socket socket) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        String status = "HTTP/1.0 200 We Got your request";
        String header = "someheaderkey:someheadervalue";
        String body =
                """
                               -=[ teapot ]=-
                                   _...._
                                 .'  _ _ `.
                                | ."` ^ `". _,
                                \\_;`"---"`|//
                                  |       ;/
                                  \\_     _/
                                    `""\"`
                        """;
        String stringBuilder = status + "\r\n" +
                header + "\r\n" +
                "\r\n" +
                body + "\r\n";

        out.write(stringBuilder);
        out.flush();
    }
}
