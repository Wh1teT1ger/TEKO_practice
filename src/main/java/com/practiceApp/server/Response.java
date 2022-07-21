package com.practiceApp.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> entityHeaders = new HashMap<>();
    private String entity;

    public void addHeader(String key, String value){
        entityHeaders.put(key, value);
    }

    public void setEntity(String entity){
        this.entity = entity;
    }

    public void setStatusLine(int code, String massage){
        statusCode = code;
        statusMessage = massage;
    }

    public void send(OutputStream out) throws IOException {
        out.write(("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n").getBytes());
        for (String headerName : entityHeaders.keySet())  {
            out.write((headerName + ": " + entityHeaders.get(headerName) + "\r\n").getBytes());
        }
        out.write("\r\n".getBytes());
        if (entity != null)  {
            out.write(entity.getBytes());
        }
    }



}
