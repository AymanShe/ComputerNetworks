package com.aymanshe.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HttpServer {

    int port;
    String path;
    boolean verbose;
    boolean attachment;

    public HttpServer(int port, String path, boolean verbose, boolean attachment) {
        this.port = port;
        this.path = path;
        this.verbose = verbose;
        this.attachment = attachment;
    }

    public void run() throws IOException {
        try (ServerSocket server = new ServerSocket(port, 0, InetAddress.getLoopbackAddress())) {
            log("Server started listening on port :" + port);

            while (true) {
                log("waiting for new requests\n");
                Socket client = server.accept();
                log("Incoming request");
                listenAndRespond(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenAndRespond(Socket socket) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //now the server is waiting for requests
            HttpRequest request = parseRequest(in);
            HttpResponse response = processRequest(request);
            ok(socket, response);
        } catch (FileNotFoundException e) {
            notFound(socket);
        } catch (IllegalAccessException e) {
            illegalAccess(socket);
        } catch (IOException e) {
            internal(socket);
        } catch (MissingHeaderException e) {
            badRequest(socket, e.getMessage());
        } catch (CommandParseException e) {
            badRequest(socket, e.getMessage());
        } finally {
            log("Closing connection");
            socket.close();
        }
    }

    private void internal(Socket socket) throws IOException {
        log("Internal exception");
        log("Response status code 500");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 500 Unexpected Error. Try Again");

        String response = buildResponse(httpResponse);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        log("Sending Response");
        out.flush();
        out.close();
    }

    private void badRequest(Socket socket, String message) throws IOException {
        log("Bad request");
        log("Response status code 400");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 400 " + message);

        String response = buildResponse(httpResponse);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        log("Sending Response");
        out.flush();
        out.close();
    }

    private void notFound(Socket socket) throws IOException {
        log("File not found");
        log("Response status code 404");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 404 File Not Found");

        String response = buildResponse(httpResponse);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        log("Sending Response");
        out.flush();
        out.close();
    }

    private void illegalAccess(Socket socket) throws IOException {
        log("Illegal Access");
        log("Response status code 404");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 404 File Not Found");

        String response = buildResponse(httpResponse);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        log("Sending Response");
        out.flush();
        out.close();
    }

    private HttpResponse processRequest(HttpRequest request) throws IOException {
        log("Trying to process request");
        HttpResponse response = new HttpResponse();
        if (request.isGet()) {
            if (request.isFile()) {
                String fileContent = getFileContent(request.getFileName());
                response.setBody(fileContent);
                String fullPath = path + "/" + request.getFileName();
                Path path = new File(fullPath).toPath();
                try {
                    String mimeType = Files.probeContentType(path);
                    response.addHeader("Content-Type", mimeType);
                    if (attachment) {
                        response.addHeader("Content-Disposition", "attachment");
                    } else {
                        response.addHeader("Content-Disposition", "inline");
                    }
                } catch (IOException e) {
                    log("Couldn't get file type. Header Content-Type is not included" + e.getMessage());
                }
            } else {
                var fileNamesList = getDirectoryFiles(request.getDirectory());
                //TODO check if files is empty
                var fileNamesString = getDirectoryFilesAsString(fileNamesList);
                response.setBody(fileNamesString);
            }
        } else {
            //write to file
            File file = new File(path + "/" + request.getFileName());
            log("Creating new if it does not exist");
            FileWriter myWriter = new FileWriter(file);
            log("Writing content to file");
            myWriter.write(request.getBody());
            myWriter.close();
        }
        return response;
    }

    private void ok(Socket socket, HttpResponse httpResponse) throws IOException {
        log("Request was processed successfully");
        log("Response status code: 200");
        httpResponse.setStatus("HTTP/1.0 200 Success");

        String response = buildResponse(httpResponse);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.write(response);
        log("Sending Response");
        out.flush();
        out.close();
    }

    private String buildResponse(HttpResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(response.getStatus()).append("\r\n");
        if (!response.getHeaders().isEmpty()) {
            for (var header : response.getHeaders().entrySet()) {
                stringBuilder.append(header.getKey()).append(":").append(header.getValue()).append("\r\n");
            }
        }
        stringBuilder.append("\r\n");
        if (response.getBody() != null && !response.getBody().isEmpty()) {
            stringBuilder.append(response.getBody()).append("\r\n");
        }
        return stringBuilder.toString();
    }

    private List<String> getDirectoryFiles(String request) {
        log("Getting directory files list");
        File folder = new File(path + request);
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
        log("trying to open file");
        File file = new File(path + "/" + fileName);
        Scanner scanner = new Scanner(file);
        StringBuilder data = new StringBuilder();
        log("Reading file content");
        while (scanner.hasNextLine()) {
            data.append(scanner.nextLine()).append("\r\n");
        }
        if (!data.isEmpty()) {
            data.delete(data.length() - 2, data.length());
        }
        scanner.close();
        return data.toString();
    }

    private HttpRequest parseRequest(BufferedReader in) throws IOException, IllegalAccessException, MissingHeaderException, CommandParseException {
        //TODO validate input
        HttpRequest request = new HttpRequest();


        //read the first line
        String line = in.readLine();
        log("Trying to parse");
        String requestLine = line;
        String[] requestLineArguments = requestLine.split(" ");
        String method = requestLineArguments[0];

        boolean isGet = method.toLowerCase(Locale.ROOT).equals("get");
        String path = requestLineArguments[1];
        if (isGet) {
            log("Method is GET");
            request.setMethod("get");
            //check if directory or file
            if (path.endsWith("/")) {
                if (path.contains("..")) {
                    log("Illegal Access trial encountered and stopped");
                    throw new IllegalAccessException();
                }
                log("Requesting directory file list");
                request.setFile(false);
                request.setDirectory(path);
            } else {
                request.setFile(true);
                String targetFileName = path.substring(1);
                if (targetFileName.contains("..")) {
                    log("Illegal Access trial encountered and stopped");
                    throw new IllegalAccessException();
                }
                log("Requesting specific file");
                request.setFileName(targetFileName);
                log("File name: " + targetFileName);
            }
        } else {
            log("Method is POST");
            request.setMethod("post");
            String targetFileName = path.substring(1);
            if (targetFileName.contains("..")) {
                log("Illegal Access trial encountered and stopped");
                throw new IllegalAccessException();
            }
            if (targetFileName.isBlank()) {
                log("target file name cannot be empty");
                throw new CommandParseException("Target file name cannot be empty");
            }
            log("File name: " + targetFileName);

            request.setFileName(targetFileName);
            //read any further headers if any
            line = in.readLine();
            while (!line.equals("")) {
                int endOfKey = line.indexOf(":");
                String key = line.substring(0, endOfKey);
                String value = line.substring(endOfKey + 1);
                request.addHeadersPair(key, value);
                log("Header => key: " + key + ", Value: " + value);
                line = in.readLine();
            }

            //read body
            String contentHeader = request.getHeaders("Content-Length");
            if (contentHeader == null || contentHeader.isEmpty()) {
                log("Content-Length header is missing");
                throw new MissingHeaderException("Content-Length header is missing");
            }
            int contentLength = Integer.parseInt(request.getHeaders("Content-Length").trim());
            char[] bodyArray = new char[contentLength];
            in.read(bodyArray, 0, contentLength);
            //TODO put a timeout if contentLength is longer that actual body in request
            String body = new String(bodyArray);
            log("Body is: " + body);
            request.setBody(body);
        }
        return request;
    }

    void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }
}
