package com.practiceApp.Classes;

import java.util.Optional;

public class initPaymentData {
    Client client;
    String product;
    Payment payment;
    Payer src;
    Account dst;
    Order order;
    String callback;
    String tag;

    public initPaymentData(Client client, String product, Payment payment, Payer src, Order order) {
        this.client = client;
        this.product = product;
        this.payment = payment;
        this.src = src;
        this.order = order;
    }

    public void setDst(Account dst) {
        this.dst = dst;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
