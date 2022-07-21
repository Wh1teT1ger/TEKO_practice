package com.practiceApp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerConnectionHandler extends Thread{

        private final Socket socket;

        public ServerConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            System.out.println(socket);

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                if (inputStream!= null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
                if (outputStream!=null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
                if (socket!= null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
}
