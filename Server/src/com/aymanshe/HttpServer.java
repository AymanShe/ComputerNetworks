package com.aymanshe;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.BiFunction;

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
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //now the server is waiting for requests
            HttpRequest request = parseRequest(in);
            HttpResponse2 response = processRequest(request);
            ok(socket, response);
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
    }

    private HttpResponse2 processRequest(HttpRequest request) throws IOException {
        HttpResponse2 response = new HttpResponse2();
        if (request.isGet()) {
            if (request.isFile()) {
                String fileContent = getFileContent(request.getFileName());
                response.setBody(fileContent);
            } else {
                var fileNamesList = getDirectoryFiles();
                //TODO check if files is empty
                var fileNamesString = getDirectoryFilesAsString(fileNamesList);
                response.setBody(fileNamesString);
            }
        } else {
            //write to file
            File file = new File(request.getFileName());
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(request.getBody());
            myWriter.close();
        }
        return response;
    }

    private void ok(Socket socket, HttpResponse2 httpResponse2) throws IOException {
        httpResponse2.setStatus("HTTP/1.0 200 Success");

        String response = buildResponse(httpResponse2);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        out.flush();
        out.close();
    }

    private String buildResponse(HttpResponse2 response) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(response.getStatus()).append("\r\n");
        if (!response.getHeaders().isEmpty()) {
            for (var header : response.getHeaders().entrySet()) {
                stringBuilder.append(header.getKey()).append(":").append(header.getValue()).append("\r\n");
            }
        }
        stringBuilder.append("\r\n");
        if (!response.getBody().isEmpty()){
            stringBuilder.append(response.getBody()).append("\r\n");
        }
        return stringBuilder.toString();
    }

    private List<String> getDirectoryFiles() {
        File folder = new File(path);
        File[] files = folder.listFiles();
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    private String getDirectoryFilesAsString(List<String> filenames) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String fileName : filenames) {
            stringBuilder.append(fileName).append("\r\n");
        }
        return stringBuilder.toString();
    }

    private String getFileContent(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        StringBuilder data = new StringBuilder();
        while (scanner.hasNextLine()) {
            data.append(scanner.nextLine()).append("\r\n");
        }
        data.delete(data.length() - 2, data.length());
        scanner.close();
        return data.toString();
    }

    private HttpRequest parseRequest(BufferedReader in) throws IOException {
        //TODO validate input
        HttpRequest request = new HttpRequest();


        //read the first line
        String line = in.readLine();
        String requestLine = line;
        String[] requestLineArguments = requestLine.split(" ");

        boolean isGet = requestLineArguments[0].toLowerCase(Locale.ROOT).equals("get");
        String path = requestLineArguments[1];
        if (isGet) {
            request.setMethod("get");
            //check if directory or file
            if (path.equals("/")) {
                request.setFile(false);
            } else {
                request.setFile(true);
                String targetFileName = path.substring(1);
                request.setFileName(targetFileName);
            }
        } else {
            request.setMethod("post");
            String targetFileName = path.substring(1);
            request.setFileName(targetFileName);
            //read any further headers if any
            line = in.readLine();
            while (!line.equals("")) {
                int endOfKey = line.indexOf(":");
                request.addHeadersPair(line.substring(0, endOfKey), line.substring(endOfKey + 1));
                line = in.readLine();
            }

            //read body
            int contentLength = Integer.parseInt(request.getHeaders("Content-Length").trim());
            char[] bodyArray = new char[contentLength];
            in.read(bodyArray, 0, contentLength);
            //TODO put a timeout if contentLength is longer that actual body in request
            String body = new String(bodyArray);
            request.setBody(body);
        }
        return request;
    }
}
