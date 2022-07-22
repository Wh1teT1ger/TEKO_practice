package com.practiceApp.server;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.practiceApp.Classes.Payment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.Document;

import com.practiceApp.Classes.*;


import java.util.UUID;
import java.sql.Timestamp;

public class RequestHandler {
    public static String isPaymentPossible(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");

        if (checkJsonIsPaymentPossible(json)) {
            Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);
            String checkProduct = json.get("product").getAsString();
            Document account = merchantAccount.find(new Document("_id", "merchant_account_id")).first();
            Document product = products.find(new Document("_id", checkProduct)).first();
            int amount;
            if (account.isEmpty()) {
                Document doc = new Document();
                doc.append("_id", "merchant_account_id");
                doc.append("value", 10000000);
                merchantAccount.insertOne(doc);
                amount = 10000000;
            } else {
                amount = account.getInteger("value");
            }
            int amountProducts;
            if (product.isEmpty()) {
                Document doc = new Document();
                doc.append("_id", checkProduct);
                doc.append("amount", 10);
                products.insertOne(doc);
                amountProducts = 10;
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
            data = errorMessage(402, "Bad json");
        }
        return data;
    }

    public static String resumePayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        if (checkJsonResumePayment(json)) {
            Document account = merchantAccount.find(new Document("_id", "merchant_account_id")).first();
            int getAccount = account.getInteger("value");

            String productId = json.get("product").getAsString();
            Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
            Document doc = tempTransactions.find(new Document("_id", tx.getId())).first();
            Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);
            Payment scrPayment = gson.fromJson(json.getAsJsonObject("src_payment"), Payment.class);

            Document product = products.find(new Document("_id", productId)).first();
            int getAmount = product.getInteger("amount");

            if (!doc.isEmpty()) {
                merchantAccount.updateOne(Filters.eq("_id", "merchant_account_id"),
                        Updates.set("value", getAccount - payment.getAmount() + scrPayment.getAmount()));

                products.updateOne(Filters.eq("_id", productId),
                        Updates.set("amount", getAmount - 1));

                data = PaymentData(tx).toString();
                tempTransactions.deleteOne(doc);

                Document transaction = new Document();
                transaction.append("tx", json.getAsJsonObject("partner_tx").toString());
                transaction.append("product", json.getAsJsonObject("product").toString());
                transaction.append("payment", json.getAsJsonObject("payment").toString());
                transaction.append("src_payment", json.getAsJsonObject("src_payment").toString());
                transactions.insertOne(transaction);
            } else{
                data = errorMessage(402, "Transaction didnt start");
            }
        } else {
            data = errorMessage(402, "Bad json");
        }

        return data;
    }

    public static String cancelPayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");

        if(checkJsonCancelPayment(json)){
            Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
            Document doc = tempTransactions.find(new Document("_id", tx.getId())).first();
            tempTransactions.deleteOne(doc);
            data = PaymentData(tx).toString();
        } else{
            data = errorMessage(402, "Bad json");
        }

        return data;
    }

    public static String rollbackPayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccount = database.getCollection("merchant_account");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        if (checkJsonRollbackPayment(json)) {
            Document account = merchantAccount.find(new Document("_id", "merchant_account_id")).first();
            int getAccount = account.getInteger("value");

            String productId = json.get("product").getAsString();
            Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
            Document doc = transactions.find(new Document("transaction", tx.getId())).first();
            Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);

            Document product = products.find(new Document("_id", productId)).first();
            int getAmount = product.getInteger("amount");

            if (!doc.isEmpty()) {
                merchantAccount.updateOne(Filters.eq("_id", "merchant_account_id"),
                        Updates.set("value", getAccount + payment.getAmount()));

                products.updateOne(Filters.eq("_id", productId),
                        Updates.set("amount", getAmount + 1));

                data = PaymentData(tx).toString();
                transactions.deleteOne(doc);
            } else{
                data = errorMessage(402, "Transaction transaction is missing");
            }
        } else {
            data = errorMessage(402, "Bad json");
        }

        return data;
    }

    public static boolean checkJsonIsPaymentPossible(JsonObject json) {
        return json.has("client") && json.has("product") && json.has("payment")
                && json.has("order") && json.has("tx")
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

    public static responseData isPaymentPossibleData(JsonObject json) {
        Payment payment = new Payment(json);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return new responseData("true", UUID.randomUUID().toString(), timestamp.getTime(), payment);
    }

    public static responseData PaymentData(Transaction tx) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return new responseData("true", tx.getId(), tx.getStart_t(), timestamp.getTime());
    }

    public static String errorMessage(int code, String message) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        responseData data = new responseData("false", code, message);
        return gson.toJson(data);
    }
}
