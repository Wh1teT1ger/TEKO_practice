package com.practiceApp.server;

import java.io.IOException;

// server of merchant
public class HttpServer {
    public static void main(String[] args) {
        try {
            ServerListener serverListenerThread = new ServerListener(80);
            serverListenerThread.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
