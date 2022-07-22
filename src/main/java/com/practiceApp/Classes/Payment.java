package com.practiceApp.Classes;

import com.google.gson.JsonObject;

public class Payment {
    int amount;
    int currency;
    int exponent;

    public int getAmount() {
        return amount;
    }

    public int getCurrency() {
        return currency;
    }

    public int getExponent() {
        return exponent;
    }

    public Payment(int amount, int currency, int exponent) {
        this.amount = amount;
        this.currency = currency;
        this.exponent = exponent;
    }

    public Payment(JsonObject json) {
        this.amount = json.getAsJsonObject("payment").get("amount").getAsInt();
        this.currency = json.getAsJsonObject("payment").get("currency").getAsInt();
        this.exponent = json.getAsJsonObject("payment").get("exponent").getAsInt();
    }
}
