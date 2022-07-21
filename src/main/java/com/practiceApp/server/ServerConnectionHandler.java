package com.practiceApp.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;

public class ServerConnectionHandler extends Thread {

    private final Socket socket;

    public ServerConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        System.out.println(socket);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Request request = new Request(reader);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject json = gson.fromJson(request.data, JsonObject.class);

            switch (request.path) {
                case "/isPaymentPossible" -> {

                }
                case "/resumePayment" -> {

                }
                case "/cancelPayment" -> {

                }
                case "/rollbackPayment" -> {

                }
            }

            outputStream = socket.getOutputStream();
            Response response = new Response();
            response.setStatusLine(200, "OK");
            response.addHeader("Content-Type", "application/json");
            response.setEntity(request.data);
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

    public static boolean checkJsonIsPaymentPossible(JsonObject json) {
        return json.has("client") && json.has("product") && json.has("payment")
                && json.has("order") && json.has("tx") && json.has("src_cls")
                && json.has("src_payment");
    }

    public static boolean checkJsonResumePayment(JsonObject json) {
        return checkJsonIsPaymentPossible(json) && json.has("partner_tx");
    }

    public static boolean checkJsonCancelPayment(JsonObject json) {
        return checkJsonResumePayment(json) && json.has("code")&& json.has("description");
    }

    public static boolean checkJsonRollbackPayment(JsonObject json) {
        return json.has("client") && json.has("tx")&& json.has("partner_tx");
    }
}
