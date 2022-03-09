package com.aymanshe;

import java.io.IOException;

public class httpfs {

    /**
     * main class
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //TODO get arguments from args

        HttpServer httpServer = new HttpServer(8007,"");
        try {
            httpServer.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
