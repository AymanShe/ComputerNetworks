package com.aymanshe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class CommandParser {
    // httpc (get|post) [-v] (-h "k:v")* [-d inline-data] [-f file] URL

    public static HttpRequest parse(String[] args) throws CommandParseException {
        // region method
        HttpRequest request = new HttpRequest();
        if (!args[1].equals("get") && !args[1].equals("post") && !args[1].equals("help")) {
            throw new CommandParseException("The command after httpc can be get or post only. You entered: " + args[1]);
        }
        request.setMethod(args[1]);
        // endregion

        if (request.getMethod().toLowerCase(Locale.ROOT).equals("help")) {
            if (args.length > 3) {
                throw new CommandParseException("There are too many arguments. The command after help can be get or post only.");
            }
            if (args.length > 2 && !args[2].equals("get") && !args[2].equals("post")) {
                throw new CommandParseException("The command after help can be get or post only.");
            }
            return request;
        }
        // region arguments
        validateArguments(args);
        int i;
        for (i = 2; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-v" -> request.setVerbose(true);
                case "-h" -> {
                    // todo validate a correct header
                    String header = args[++i];
                    request.addHeaderPair(header);
                }
                case "-d" -> {
                    // todo validate a correct body
                    // todo Assuming all double quotation has to be escaped otherwise it will be parsed by java
                    String body = args[++i];
                    int index = body.indexOf("'");
                    if (index != 0) {
                        throw new CommandParseException("the inline body has to start and end with single quotation.");
                    }
                    body = body.substring(1);
                    index = body.lastIndexOf("'");
                    if (index == body.length() - 1) {// there is a colon at the end of the argument, hence it is the end of
                        // inline body
                        body = body.substring(0, body.length() - 1);
                    } else {// the body has more parts that are seperated by space
                        boolean reachedEnd = false;
                        while (!reachedEnd) {
                            if (i + 1 >= args.length) {
                                throw new CommandParseException(
                                        "string finished but the closing colon for inline body was not found");
                            }
                            String next = args[++i];
                            // check if it is the end body by checking the ' and making sure it is not in
                            // the middle of the string
                            index = -1;
                            index = next.lastIndexOf("'");//
                            if (index != next.length() - 1) {
                                // it is not the end of the body
                                body += next;
                                body += " ";
                            } else {
                                // it might be the end of the body or a colon in the middle
                                if (index == next.length() - 1) {
                                    // it is the end of the body
                                    next = next.substring(0, next.length() - 1);
                                    body += next;
                                    reachedEnd = true;
                                } else {
                                    // it is a colon in the middle as part of the body
                                    body += next;
                                    body += " ";
                                }
                            }
                        }
                    }
                    request.setBody(body);
                    request.setInlineBody(true);
                }
                case "-f" -> {
                    // todo validate a correct path txt and json are accepted only
                    String bodyFilePath = args[++i];
                    try {
                        File file = new File(bodyFilePath);
                        Scanner scanner = new Scanner(file);
                        StringBuilder data = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            data.append(scanner.nextLine());
                        }
                        request.setBody(data.toString());
                        scanner.close();
                    } catch (FileNotFoundException e) {
                        throw new CommandParseException("File not found");
                    }
                    request.setInlineBody(false);
                    request.setBodyFilePath(bodyFilePath);
                }
                case "-o" ->{
                    String fileName = args[++i];
                    request.setWriteToFile(true);
                    request.setFileName(fileName);
                }
            }
        }
        // endregion

        // region url
        validateUrl(i,args);
        String fullUrl = args[args.length - 1];
        String url = fullUrl.replace("'", "");
        // protocol
        int endOfProtocolIndex = url.indexOf("http://");
        if (endOfProtocolIndex == -1) {
            endOfProtocolIndex = url.indexOf("https://");
            if (endOfProtocolIndex == -1) {
                throw new CommandParseException("Please check the format of the url. you entered: " + fullUrl);
            } else {
                endOfProtocolIndex += 8;
            }
        } else {
            endOfProtocolIndex += 7;
        }
        String protocol = url.substring(0, endOfProtocolIndex);
        url = url.substring(endOfProtocolIndex);

        // host
        int endOfHostIndex = url.indexOf("/");
        if (endOfHostIndex == -1) {
            endOfHostIndex = url.length();
        }
        String host = url.substring(0, endOfHostIndex);
        url = url.substring(endOfHostIndex);
        request.setAddress(host);

        // path
        String path = url;
        request.setPath(path);

        // endregion

        return request;
    }

    private static void validateUrl(int i, String[] args) throws CommandParseException {
        if (i+1 > args.length){
            throw new CommandParseException("It appears there is no url provided as a last argument");
        }
    }

    private static void validateArguments(String[] args) throws CommandParseException {
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 2; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-v" -> {
                    if (map.get("-v") != null) {
                        throw new CommandParseException("Duplicate argument -v");
                    }
                    map.put("-v", true);
                }
                case "-h" -> {
                    map.put("-h", true);
                    i++;
                }
                case "-d" -> {
                    if (map.get("-f") != null) {
                        throw new CommandParseException("Either [-d] or [-f] can be used but not both.");
                    }
                    if (map.get("-d") != null) {
                        throw new CommandParseException("Duplicate argument -d");
                    }
                    map.put("-d", true);
                    while(args[++i].lastIndexOf("'") != args[i].length()-1){
                        if (i >= args.length - 1){
                            throw new CommandParseException("string finished but the closing colon for inline body was not found");
                        }
                    }
                }
                case "-f" -> {
                    if (map.get("-d") != null) {
                        throw new CommandParseException("Either [-d] or [-f] can be used but not both.");
                    }
                    if (map.get("-f") != null) {
                        throw new CommandParseException("Duplicate argument -f");
                    }
                    map.put("-f", true);
                    i++;
                }
                case "-o" -> {
                    if (map.get("-o") != null) {
                        throw new CommandParseException("Duplicate argument -o");
                    }
                    map.put("-o", true);
                    i++;
                }
                default -> throw new CommandParseException("There is an unknown argument: " + args[i]);
            }
        }

        //check for get and post
        if (args[1].equals("get")) {
            if (map.get("-d") != null) {
                throw new CommandParseException("argument -d is not allowed with command get");
            }
            if (map.get("-f") != null) {
                throw new CommandParseException("argument -f is not allowed with command get");
            }
        } else {
            if (map.get("-d") == null && map.get("-f") == null) {
                throw new CommandParseException("argument -d or -f must be provided with command post");
            }
        }
    }
}
