package com.practiceApp.Classes;

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
}
