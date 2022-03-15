package com.aymanshe.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class httpfs {

    /**
     * main class
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int port = 8080;
        String path = ".";
        boolean verbose = false;
        boolean attachment = false;

        // region validations
        if (args.length == 0) {
            System.out.println("The command cannot be empty");
            System.out.println("Please enter the command following this format: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
            return;
        }

        if (!args[0].equals("httpfs")) {
            System.out.println("The command should start with httpfs");
            System.out.println("Please enter the command following this format: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
            return;
        }

        try {
            validateArguments(args);
        } catch (CommandParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    case "-v" -> verbose = true;
                    case "-d" -> {
                        // todo Assuming all double quotation has to be escaped otherwise it will be parsed by java
                        StringBuilder pathString = new StringBuilder(args[++i]);
                        int index = pathString.indexOf("'");
                        if (index != 0) {
                            throw new CommandParseException("the path has to start and end with single quotation.");
                        }
                        pathString = new StringBuilder(pathString.substring(1));
                        index = pathString.lastIndexOf("'");
                        if (index == pathString.length() - 1) {// there is a quotation at the end of the argument, hence it is the end of path
                            pathString = new StringBuilder(pathString.substring(0, pathString.length() - 1));
                        } else {// the body has more parts that are seperated by space
                            boolean reachedEnd = false;
                            while (!reachedEnd) {
                                if (i + 1 >= args.length) {
                                    throw new CommandParseException("path string finished but the closing quotation for inline body was not found");
                                }
                                String next = args[++i];
                                // check if it is the end path by checking the ' and making sure it is not in the middle of the string
                                index = -1;
                                index = next.lastIndexOf("'");//
                                if (index != next.length() - 1) {
                                    // it is not the end of the body
                                    pathString.append(next);
                                    pathString.append(" ");
                                } else {
                                    // it might be the end of the body or a quotation in the middle
                                    if (index == next.length() - 1) {
                                        // it is the end of the path
                                        next = next.substring(0, next.length() - 1);
                                        pathString.append(next);
                                        reachedEnd = true;
                                    } else {
                                        // it is a colon in the middle as part of the body
                                        pathString.append(next);
                                        pathString.append(" ");
                                    }
                                }
                            }
                        }
                        path = pathString.toString();
                    }
                    case "-p" -> port = Integer.parseInt(args[++i]);
                    case "-a" -> attachment = true;
                }
            }

            validatePath(path);

        } catch (CommandParseException e) {
            System.out.println(e.getMessage());
            return;
        }
        // endregion

        if (verbose){
            File file = new File(path);
            System.out.println("Working directory set to " + file.getAbsolutePath());
        }
        HttpServer httpServer = new HttpServer(port, path, verbose, attachment);
        try {
            httpServer.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validatePath(String path) {
        //TODO
    }

    private static void validateArguments(String[] args) throws CommandParseException {
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 1; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-v" -> {
                    if (map.get("-v") != null) {
                        throw new CommandParseException("Duplicate argument -v");
                    }
                    map.put("-v", true);
                }
                case "-d" -> {
                    if (map.get("-d") != null) {
                        throw new CommandParseException("Duplicate argument -d");
                    }
                    map.put("-d", true);
                    while (args[++i].lastIndexOf("'") != args[i].length() - 1) {
                        if (i >= args.length - 1) {
                            throw new CommandParseException("string finished but the closing colon for inline body was not found");
                        }
                    }
                }
                case "-p" -> {
                    if (map.get("-p") != null) {
                        throw new CommandParseException("Duplicate argument -p");
                    }
                    map.put("-p", true);
                    i++;
                }
                default -> throw new CommandParseException("There is an unknown argument: " + args[i]);
            }
        }
    }
}
