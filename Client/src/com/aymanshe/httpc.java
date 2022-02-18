package com.aymanshe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class httpc {

    /**
     * main class
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //region Validations
        //check if the minimum number of argument is provided
    	//todo consider doing an interactive app with a while
        if (args.length == 0) {
            System.out.println("The command cannot be empty");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        if (!args[0].equals("httpc")) {
            System.out.println("The command should start with httpc");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        if (args.length < 2) {
            System.out.println("The command has too few arguments");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        //endregion

        try {
            //parse into request
            HttpRequest httpRequest = CommandParser.parse(args);

            if (!httpRequest.getMethod().equals("HELP")) {
                // send the request and display content
                HttpClient httpClient = new HttpClient();
                HttpResponse httpResponse = httpClient.sendRequest(httpRequest);
                displayResult(httpRequest, httpResponse);
            } else {
                displayHelp(args);
            }
        } catch (CommandParseException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void displayHelp(String[] args) {
        if (args.length>2){
            if (args[2].equals("get")){
                System.out.println("usage: httpc get [-v] [-h key:value] URL");
                System.out.println("Get executes a HTTP GET request for a given URL.");
                System.out.println("\t-v\tPrints the detail of the response such as protocol, status, and headers.");
                System.out.println("\t-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.");
            } else if (args[2].equals("post")){
                System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL");
                System.out.println("Post executes a HTTP POST request for a given URL with inline data or from file.");
                System.out.println("\t-v\tPrints the detail of the response such as protocol, status, and headers.");
                System.out.println("\t-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.");
                System.out.println("\t-d 'string'\tAssociates an inline data to the body HTTP POST request.");
                System.out.println("\t-f file\tAssociates the content of a file to the body HTTP POST request.");
                System.out.println("");
                System.out.println("Either [-d] or [-f] can be used but not both.");
            }
        }else{
            System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
            System.out.println("Usage:");
            System.out.println("\thttpc command [arguments]");
            System.out.println("The commands are:");
            System.out.println("\tget\texecutes a HTTP GET request and prints the response.");
            System.out.println("\tpost\texecutes a HTTP POST request and prints the response.");
            System.out.println("\thelp\tprints this screen.");
            System.out.println("");
            System.out.println("Use \"httpc help [command]\" for more information about a command.");
        }
    }

    private static void displayResult(HttpRequest request, HttpResponse response) {
        if (request.isVerbose()){
            System.out.println(response.getStatus());
            System.out.println(response.getHeaders());
        }
        if (request.isWriteToFile()){
            try {
                File file = new File(request.getFileName());
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(request.getFileName());
                fileWriter.write(response.getBody());
                fileWriter.close();
                System.out.println("--- response body was successfully written to the file ---");
            } catch (IOException e) {
                System.out.println("An error occurred while writing response to the file.");
            }
        }
        System.out.println("--- response body ---");
        System.out.println(response.getBody());
    }

}
