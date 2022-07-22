package com.practiceApp.Classes;

public class responseData {
    boolean success;
    Result result;

    public responseData(boolean success, String id, long start_t, Payment payment) {
        this.success = success;
        this.result = new Result(id, start_t, payment);
    }

    public responseData(boolean success, String id, long start_t) {
        this.success = success;
        this.result = new Result(id, start_t);
    }

    public responseData(boolean success, int code, String description) {
        this.success = success;
        this.result = new Result(code, description);
    }


    class Result {
        Transaction tx;
        int code;
        String description;
        Payment src_payment;

        public Result(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public Result(String id, long start_t) {
            this.tx = new Transaction(id, start_t);
        }

        public Result(String id, long start_t, Payment payment) {
            this.tx = new Transaction(id, start_t);
            this.src_payment = payment;
        }
    }
}
