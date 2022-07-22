package com.practiceApp.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.practiceApp.Classes.Payment;
import org.bson.Document;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnectionHandler extends Thread {

    private final Socket socket;

    public ServerConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.setProperty("DEBUG.MONGO", "false");

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("merchantdb");
        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");
        OutputStream outputStream = null;
        System.out.println(socket);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Request request = new Request(reader);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject json = gson.fromJson(request.data, JsonObject.class);

            switch (request.path) {
                case "/isPaymentPossible" -> {
                    if(checkJsonIsPaymentPossible(json)) {
                        Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);
                        String checkProduct = gson.fromJson(json.getAsJsonObject("product"), String.class);
                        Document account = merchantAccount.find(new Document("_id", "merchant_account_id")).first();
                        Document product = products.find(new Document("_id", checkProduct)).first();
                        int amount;
                        if (account == null) {
                            Document doc = new Document();
                            doc.append("_id", "merchant_account_id");
                            doc.append("value", 10000000);
                            merchantAccount.insertOne(doc);
                            amount = 0;
                        } else {
                            amount = account.getInteger("value");
                        }
                        int amountProducts;
                        if (product == null) {
                            Document doc = new Document();
                            doc.append("_id", checkProduct);
                            doc.append("amount", 10);
                            merchantAccount.insertOne(doc);
                            amountProducts = 0;
                        } else {
                            amountProducts = product.getInteger("amount");
                        }
                        if(amount > payment.getAmount()){
                            if(amountProducts >0) {
                                Document doc = new Document();
                            } else{

                            }
                        } else{

                        }
                    }

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
        return checkJsonResumePayment(json) && json.has("code") && json.has("description");
    }

    public static boolean checkJsonRollbackPayment(JsonObject json) {
        return json.has("client") && json.has("tx") && json.has("partner_tx");
    }
}
