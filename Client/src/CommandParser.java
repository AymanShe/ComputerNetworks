class CommandParser {
    //httpc (get|post) [-v] (-h "k:v")* [-d inline-data] [-f file] URL

    public static HttpRequest parse(String[] args) throws CommandParseException {
        String method = args[0];
        String url = args[1];
        //extract host and path
        int endOfHostIndex = url.indexOf("/");
        String host = url.substring(0 , endOfHostIndex);
        String path = url.substring(endOfHostIndex , url.length());

        return new HttpRequest(host, 80, method,path);
    }
}
