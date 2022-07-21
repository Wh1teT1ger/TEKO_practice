package com.practiceApp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {

    private final ServerSocket serverSocket;

    public ServerListener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ServerConnectionHandler connection = new ServerConnectionHandler(socket);
                connection.start();

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
