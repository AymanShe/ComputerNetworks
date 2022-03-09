package com.aymanshe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            //now the server is waiting for requests
            if (in.hasNextLine()) {
                HttpRequest request = parseRequest(in);
                HttpResponse2 response = processRequest(request);
                //sendResponse
                ok(socket, response);
            }
        } catch (IOException e) {
            System.out.println("Echo error " + e);
        }
    }

    private HttpResponse2 processRequest(HttpRequest request) throws FileNotFoundException {
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
            //TODO POST
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
        } else {
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\r\n");
        stringBuilder.append(response.getBody()).append("\r\n");
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
        data.delete(data.length()-2,data.length());
        scanner.close();
        return data.toString();
    }


    private HttpRequest parseRequest(Scanner in) {
        //TODO validate input
        HttpRequest request = new HttpRequest();

        //read the first line
        String line = in.nextLine();
        String requestLine = line;
        String[] requestLineArguments = requestLine.split(" ");
        //check if get or post
        boolean isGet = requestLineArguments[0].toLowerCase(Locale.ROOT).equals("get");
        if (isGet) {
            request.setMethod("get");
            //check if directory or file
            String path = requestLineArguments[1];
            if (path.equals("/")) {
                request.setFile(false);
            } else {
                request.setFile(true);
                String targetFileName = path.substring(1);
                request.setFileName(targetFileName);
            }
        } else {
            //read any further headers if any
            line = in.nextLine();
            while (!line.equals("")) {
                request.addHeaderPair(line);
                line = in.nextLine();
            }
        }
        return request;
    }
}
