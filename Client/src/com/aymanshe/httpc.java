package com.aymanshe;
public class httpc {

    /**
     * main class
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //region Validations
        //check if the minimum number of argument is provided
        if (args.length == 0){
            System.out.println("The command cannot be empty");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        if(!args[0].equals("httpc")){
            System.out.println("The command should start with httpc");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        if (args.length < 3){
            System.out.println("The command has too few arguments");
            System.out.println("Please enter the command following this format: httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
            return;
        }

        //endregion

        try {
            //parse into request
            HttpRequest httpRequest = CommandParser.parse(args);

            // send the request and display content
            HttpClient httpClient = new HttpClient();
            httpClient.sendRequest(httpRequest);
        } catch (CommandParseException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            //TODO print a better message
            e.printStackTrace();
        }
    }

}
