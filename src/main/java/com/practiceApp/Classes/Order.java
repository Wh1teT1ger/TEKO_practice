package com.practiceApp.Classes;

public class Order {
    Transaction transaction;
    String cls;
    Extra extra;

    public Order(String id, long start_t, String cls, String from, String some_key) {
        this.transaction = new Transaction(id, start_t);
        this.cls = cls;
        this.extra = new Extra(from, some_key);
    }

    class Extra {
        String from;
        String some_key;

        public Extra(String from, String some_key) {
            this.from = from;
            this.some_key = some_key;
        }
    }
}
