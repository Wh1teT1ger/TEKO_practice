package com.practiceApp.server;

import java.io.BufferedReader;
import java.io.IOException;

public class Request {
    String data;
    final String request;
    final String path;

    public String getData() {
        return data;
    }

    public String getPath() {
        return path;
    }

    public String getRequest() {
        return request;
    }

    public void setData(String data) {
        this.data = data;
    }

    Request(BufferedReader reader) throws IOException {
        String[] headerLine = reader.readLine().split(" ");
        this.request = headerLine[0];
        this.path = headerLine[1];
        int lenData = -1;
        String line;
        while ((line = reader.readLine()) != null && (line.length() != 0)) {
            System.out.println(line);
            if (line.contains("Content-Length:")) {
                lenData = Integer.parseInt(line.split(" ")[1]);
            }
        }

        StringBuilder dataBuilder = new StringBuilder();
        for (int i = 0; i < lenData; i++) {
            int sym = reader.read();
            dataBuilder.append((char) sym);
        }
        data = dataBuilder.toString();
    }

}
