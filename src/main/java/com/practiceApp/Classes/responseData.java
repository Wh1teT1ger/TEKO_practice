package com.practiceApp.Classes;

public class responseData {
    String success;
    Result result;


    public responseData(String success, String id, long start_t, Payment payment) {
        this.success = success;
        this.result = new Result(id, start_t, payment);
    }

    public responseData(String success, String id, long start_t, long finish_t) {
        this.success = success;
        this.result = new Result(id, start_t, finish_t);
    }

    public responseData(String success, int code, String description) {
        this.success = success;
        this.result = new Result(code, description);
    }

    public String getId(){
        return this.result.tx.id;
    }

    class Result {
        public Transaction tx;
        int code;
        String description;
        Payment src_payment;

        public Transaction getTx() {
            return tx;
        }

        public Result(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public Result(String id, long start_t, long finish_t) {
            this.tx = new Transaction(id, start_t, finish_t);
        }

        public Result(String id, long start_t, Payment payment) {
            this.tx = new Transaction(id, start_t);
            this.src_payment = payment;
        }
    }
}
