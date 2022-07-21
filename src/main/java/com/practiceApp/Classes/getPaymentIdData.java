package com.practiceApp.Classes;

public class getPaymentIdData {
    Client client;
    String tx_id;

    public getPaymentIdData(Client client, String tag) {
        this.client = client;
        this.tx_id = tag;
    }
}
