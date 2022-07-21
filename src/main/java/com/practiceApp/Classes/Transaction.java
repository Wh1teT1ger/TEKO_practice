package com.practiceApp.Classes;

import java.util.Optional;

public class Transaction {
    String id;
    long start_t;
    long finish_t;

    public Transaction(String id, long start_t) {
        this.id = id;
        this.start_t = start_t;
    }

    public Transaction(String id, long start_t, long finish_t) {
        this.id = id;
        this.start_t = start_t;
        this.finish_t = finish_t;
    }
}
