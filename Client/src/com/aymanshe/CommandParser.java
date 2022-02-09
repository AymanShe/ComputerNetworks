package com.aymanshe;

class CommandParser {
    //httpc (get|post) [-v] (-h "k:v")* [-d inline-data] [-f file] URL

    public static HttpRequest parse(String[] args) throws CommandParseException {
        //region method
        HttpRequest httpRequest = new HttpRequest();
        if (!args[1].equals("get") && !args[1].equals("post")) {
            throw new CommandParseException("The command after httpc can be get or post only. You entered: " + args[1]);
        }
        httpRequest.setMethod(args[1]);
        //endregion

        //region arguments
        //todo split the arguments for get and post
        //todo handle duplicate argument
        for (int i = 2; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-v":
                    httpRequest.setVerbose(true);
                    break;
                default:
            }
        }
        //endregion

        //region url
        String fullUrl = args[args.length - 1];
        String url = fullUrl.replace("'", "");
        //protocol
        int endOfProtocolIndex = url.indexOf("http://");
        if (endOfProtocolIndex == -1) {
            endOfProtocolIndex = url.indexOf("https://");
            if (endOfProtocolIndex == -1) {
                throw new CommandParseException("Please check the format of the url. you entered: " + fullUrl);
            }else{
                endOfProtocolIndex +=8;
            }
        }else{
            endOfProtocolIndex += 7;
        }
        String protocol = url.substring(0, endOfProtocolIndex);
        url = url.substring(endOfProtocolIndex);

        //host
        int endOfHostIndex = url.indexOf("/");
        if (endOfHostIndex == -1) {
            throw new CommandParseException("Please check the format of the url. you entered: " + fullUrl);
        }
        String host = url.substring(0, endOfHostIndex);
        url = url.substring(endOfHostIndex);
        httpRequest.setAddress(host);

        //path
        String path = url;
        httpRequest.setPath(path);

        //endregion

        return httpRequest;
    }
}
