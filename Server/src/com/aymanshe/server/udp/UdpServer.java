package com.aymanshe.server.udp;

import com.aymanshe.shared.Packet;
import com.aymanshe.shared.PacketTypes;
import com.aymanshe.server.CommandParseException;
import com.aymanshe.server.HttpRequest;
import com.aymanshe.server.HttpResponse;
import com.aymanshe.server.MissingHeaderException;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class UdpServer {

    int port;
    String path;
    boolean verbose;
    boolean attachment;

    public UdpServer(int port, String path, boolean verbose, boolean attachment) {
        this.port = port;
        this.path = path;
        this.verbose = verbose;
        this.attachment = attachment;
    }

    public void run() throws IOException {
        listenAndRespond();
    }

    private void listenAndRespond() throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            log("EchoServer is listening at " + channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();
                //todo process packet
                Packet response = processPacket(packet);

                if (response == null){
                    continue;
                }


                String test = new String(response.getPayload(), StandardCharsets.UTF_8);

                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.
                log("Sending Response");

                channel.send(response.toBuffer(), router);
                log("Response sent");

            }
        }
    }

    private Packet processPacket(Packet packet) {
        Packet output = null;
        switch (packet.getType()){
            case PacketTypes.SYN:
                output = packet.toBuilder()
                        .setType(PacketTypes.SYN_ACK)
                        .setSequenceNumber(packet.getSequenceNumber()+1)
                        .create();
                break;
            case PacketTypes.ACK:
                // todo wait for data
                break;
            case PacketTypes.DATA:
                String payload;
                try {
                    HttpRequest request = parseRequest(packet);
                    HttpResponse response = processRequest(request);

                    payload = ok(response);
                } catch (FileNotFoundException e) {
                    payload = notFound();
                } catch (IllegalAccessException e) {
                    payload = illegalAccess();
                } catch (IOException e) {
                    payload = internal();
                } catch (MissingHeaderException | CommandParseException e) {
                    payload = badRequest(e.getMessage());
                }
                output = packet.toBuilder()
                        .setType(PacketTypes.DATA)
                        .setPayload(payload.getBytes(StandardCharsets.UTF_8))
                        .create();
                break;
            case PacketTypes.FIN:
                // todo send ack
                // todo close
                // todo send fin
                break;
        }
        return output;
    }

    private String ok(HttpResponse httpResponse) {
        log("Request was processed successfully");
        log("Response status code: 200");
        httpResponse.setStatus("HTTP/1.0 200 Success");

        return buildResponse(httpResponse);
    }

    private String internal()  {
        log("Internal exception");
        log("Response status code 500");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 500 Unexpected Error. Try Again");

        return buildResponse(httpResponse);
    }

    private String badRequest(String message)  {
        log("Bad request");
        log("Response status code 400");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 400 " + message);

        return buildResponse(httpResponse);
    }

    private String notFound() {
        log("File not found");
        log("Response status code 404");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 404 File Not Found");

        return buildResponse(httpResponse);
    }

    private String illegalAccess() {
        log("Illegal Access");
        log("Response status code 404");
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus("HTTP/1.0 404 File Not Found");

        return buildResponse(httpResponse);
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

    private HttpRequest parseRequest(Packet packet) throws IOException, IllegalAccessException, MissingHeaderException, CommandParseException {
        //TODO validate input
        String payload = new String(packet.getPayload(), StandardCharsets.UTF_8);
        HttpRequest request = new HttpRequest();

        BufferedReader in = new BufferedReader(new StringReader(payload));
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

    private String getDirectoryFilesAsString(List<String> filenames) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String fileName : filenames) {
            stringBuilder.append(fileName).append("\r\n");
        }
        return stringBuilder.toString();
    }
}