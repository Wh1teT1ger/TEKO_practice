package com.practiceApp.server;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.practiceApp.Classes.Payment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.Document;

import com.practiceApp.Classes.*;


import java.util.UUID;
import java.sql.Timestamp;

public class RequestHandler {
    public static String isPaymentPossible(JsonObject json, MongoDatabase database) {
        String data = "";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");

        if (checkJsonIsPaymentPossible(json)) {
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
            if (amount > payment.getAmount()) {
                if (amountProducts > 0) {
                    responseData data1 = isPaymentPossibleData(json);
                    data = gson.toJson(data1);
                    Document doc = new Document();
                    doc.append("_id", data1.getId());
                    tempTransactions.insertOne(doc);

                } else {
                    data = errorMessage(302, "Item out of stock");
                }
            } else {
                data = errorMessage(307, "Not enough money");
            }
        } else {
            data = errorMessage(402, "Bad request");
        }
        return data;
    }

    public static String resumePayment(JsonObject json, MongoDatabase database) {
        String data = "";

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        return data;
    }

    public static String cancelPayment(JsonObject json, MongoDatabase database) {
        String data = "";

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        return data;
    }

    public static String rollbackPayment(JsonObject json, MongoDatabase database) {
        String data = "";

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        return data;
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

    public static responseData isPaymentPossibleData(JsonObject json){
        Payment payment =new Payment(json.getAsJsonObject("payment").get("amount").getAsInt(),
                json.getAsJsonObject("payment").get("currency").getAsInt(),
                json.getAsJsonObject("payment").get("exponent").getAsInt());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return new responseData("true", UUID.randomUUID().toString(), timestamp.getTime(), payment);
    }

    public static String errorMessage(int code, String message) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        responseData data = new responseData("false", code, message);
        return gson.toJson(data);
    }
}
