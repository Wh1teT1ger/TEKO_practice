package com.practiceApp.Classes;

import java.util.Objects;

public class Payer {
    String cls;
    String phone_number;
    String id;
    String payment_system;
    String operator;

    public Payer(String clk, String str1, String str2) {
        this.cls = clk;
        if (Objects.equals(clk, "mc") || Objects.equals(clk, "cpa")) {
            phone_number = str1;
            operator = str2;
        } else if (Objects.equals(clk, "card") || Objects.equals(clk, "e_wlt")) {
            id = str1;
            payment_system = str2;
        }
    }
}
