package com.practiceApp.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.*;
import java.net.Socket;

public class ServerConnectionHandler extends Thread {

    private final Socket socket;

    public ServerConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.setProperty("DEBUG.MONGO", "false");

        // connect to DB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("merchantdb");
        OutputStream outputStream = null;
        System.out.println(socket);

        try {
            // parse request
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Request request = new Request(reader);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject json = gson.fromJson(request.data, JsonObject.class);
            String data;
            switch (request.path) {
                case "/isPaymentPossible" -> data = RequestHandler.isPaymentPossible(json, database);
                case "/resumePayment" -> data = RequestHandler.resumePayment(json, database);
                case "/cancelPayment" -> data = RequestHandler.cancelPayment(json, database);
                case "/rollbackPayment" -> data = RequestHandler.rollbackPayment(json, database);
                default -> data = RequestHandler.errorMessage(402, "Bad request" + request.path);
            }

            // crate response
            outputStream = socket.getOutputStream();
            Response response = new Response();
            response.setStatusLine(200, "OK");
            response.addHeader("Content-Type", "application/json");
            response.setEntity(data);
            response.send(outputStream);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}

