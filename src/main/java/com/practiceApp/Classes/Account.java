package com.practiceApp.Classes;

public class Account {
    String id;
    Extra extra;

    public Account(String id, boolean premium, String game_server) {
        this.id = id;
        this.extra = new Extra(premium, game_server);
    }

    class Extra {
        boolean premium;
        String game_server;

        public Extra(boolean premium, String game_server) {
            this.premium = premium;
            this.game_server = game_server;
        }
    }
}
