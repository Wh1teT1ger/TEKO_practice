package com.practiceApp.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
    // isPaymentPossible request handler
    public static String isPaymentPossible(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccounts = database.getCollection("merchant_accounts");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");

        try {
            if (checkJsonIsPaymentPossible(json)) {
                Client client = gson.fromJson(json.getAsJsonObject("client"), Client.class);
                Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);
                String checkProduct = json.get("product").getAsString();
                Document clientAccount = merchantAccounts.find(new Document("_id", client.getId())).first();
                Document merchantAccount = merchantAccounts.find(new Document("_id", "merchant_account_id")).first();
                Document product = products.find(new Document("_id", checkProduct)).first();
                int value;

                // test create document db
                if (merchantAccount.isEmpty()) {
                    Document doc = new Document();
                    doc.append("_id", "merchant_account_id");
                    doc.append("value", 10000);
                    merchantAccounts.insertOne(doc);
                }
                if (clientAccount.isEmpty()) {
                    Document doc = new Document();
                    doc.append("_id", client.getId());
                    doc.append("value", 10000);
                    merchantAccounts.insertOne(doc);
                    value = 10000000;
                } else {
                    value = clientAccount.getInteger("value");
                }
                int quantityProducts;
                if (product.isEmpty()) {
                    Document doc = new Document();
                    doc.append("_id", checkProduct);
                    doc.append("quantity", 10);
                    products.insertOne(doc);
                    quantityProducts = 10;
                } else {
                    quantityProducts = product.getInteger("quantity");
                }
                // checking the availability of goods and funds
                if (value > payment.getAmount()) {
                    if (quantityProducts > 0) {
                        responseData data1 = isPaymentPossibleData(json);
                        data = gson.toJson(data1);
                        // writing information about the transaction to DB
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
        } catch (JsonSyntaxException | NullPointerException e) {
            data = errorMessage(402, e.getMessage());
        }
        return data;
    }

    public static String resumePayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccounts = database.getCollection("merchant_accounts");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");
        MongoCollection<Document> transactions = database.getCollection("transactions");
        try {
            if (checkJsonResumePayment(json)) {
                Client client = gson.fromJson(json.getAsJsonObject("client"), Client.class);
                Document clientAccount = merchantAccounts.find(new Document("_id", client.getId())).first();
                Document merchantAccount = merchantAccounts.find(new Document("_id", "merchant_account_id")).first();
                int getClientAccount = clientAccount.getInteger("value");
                int getMerchantAccount = merchantAccount.getInteger("value");

                String productId = json.get("product").getAsString();
                Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
                Document doc = tempTransactions.find(new Document("_id", tx.getId())).first();
                Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);
                Payment scrPayment = gson.fromJson(json.getAsJsonObject("src_payment"), Payment.class);

                Document product = products.find(new Document("_id", productId)).first();
                int getQuantity = product.getInteger("quantity");

                if (!doc.isEmpty()) {
                    // cash transaction
                    merchantAccounts.updateOne(Filters.eq("_id", client.getId()),
                            Updates.set("value", getClientAccount - scrPayment.getAmount()));

                    merchantAccounts.updateOne(Filters.eq("_id", "merchant_account_id"),
                            Updates.set("value", getMerchantAccount + payment.getAmount()));

                    // change information about the quantity of goods
                    products.updateOne(Filters.eq("_id", productId),
                            Updates.set("quantity", getQuantity - 1));

                    data = PaymentData(tx).toString();
                    tempTransactions.deleteOne(doc);

                    Document transaction = new Document();
                    transaction.append("tx", json.getAsJsonObject("partner_tx").toString());
                    transaction.append("client", json.getAsJsonObject("client").toString());
                    transaction.append("product", json.getAsJsonObject("product").toString());
                    transaction.append("payment", json.getAsJsonObject("payment").toString());
                    transaction.append("src_payment", json.getAsJsonObject("src_payment").toString());
                    transactions.insertOne(transaction);
                } else {
                    data = errorMessage(402, "Transaction didnt start");
                }
            } else {
                data = errorMessage(402, "Bad json");
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            data = errorMessage(402, e.getMessage());
        }

        return data;
    }

    public static String cancelPayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> tempTransactions = database.getCollection("temp_transactions");

        try {
            if (checkJsonCancelPayment(json)) {
                // delete information about the transaction from DB
                Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
                Document doc = tempTransactions.find(new Document("_id", tx.getId())).first();
                tempTransactions.deleteOne(doc);
                data = PaymentData(tx).toString();
            } else {
                data = errorMessage(402, "Bad json");
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            data = errorMessage(402, e.getMessage());
        }

        return data;
    }

    public static String rollbackPayment(JsonObject json, MongoDatabase database) {
        String data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        MongoCollection<Document> merchantAccounts = database.getCollection("merchant_accounts");
        MongoCollection<Document> products = database.getCollection("products");
        MongoCollection<Document> transactions = database.getCollection("transactions");

        try {
            if (checkJsonRollbackPayment(json)) {
                Client client = gson.fromJson(json.getAsJsonObject("client"), Client.class);
                Document clientAccount = merchantAccounts.find(new Document("_id", client.getId())).first();
                Document merchantAccount = merchantAccounts.find(new Document("_id", "merchant_account_id")).first();
                int getClientAccount = clientAccount.getInteger("value");
                int getMerchantAccount = merchantAccount.getInteger("value");

                String productId = json.get("product").getAsString();
                Transaction tx = gson.fromJson(json.getAsJsonObject("partner_tx"), Transaction.class);
                Document doc = transactions.find(new Document("transaction", tx.getId())).first();
                Payment payment = gson.fromJson(json.getAsJsonObject("payment"), Payment.class);

                Document product = products.find(new Document("_id", productId)).first();
                int getAmount = product.getInteger("amount");
                if (!doc.isEmpty()) {
                    merchantAccounts.updateOne(Filters.eq("_id", "merchant_account_id"),
                            Updates.set("value", getMerchantAccount - payment.getAmount()));

                    merchantAccounts.updateOne(Filters.eq("_id", client.getId()),
                            Updates.set("value", getClientAccount + payment.getAmount()));

                    // return product
                    products.updateOne(Filters.eq("_id", productId),
                            Updates.set("amount", getAmount + 1));

                    data = PaymentData(tx).toString();
                    transactions.deleteOne(doc);
                } else {
                    data = errorMessage(402, "Transaction transaction is missing");
                }
            } else {
                data = errorMessage(402, "Bad json");
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            data = errorMessage(402, e.getMessage());
        }

        return data;
    }

    // json structure validation
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

    // create response data
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
