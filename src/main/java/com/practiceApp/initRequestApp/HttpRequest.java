package com.practiceApp.initRequestApp;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.FileReader;
import java.io.IOException;

public class HttpRequest {

    public static void main(String[] args) {

            try(FileReader reader = new FileReader("src/main/resources/initP.json"))
            {
                StringBuffer buffer = new StringBuffer();
                int i ;
                try {
                    while ( ( i = reader.read()) != -1) {
                        buffer.append((char)i);
                    }
            }
            catch(IOException ex){

                System.out.println(ex.getMessage());
            }
            String result = sentRequest("https://gate-test-02.teko.io/api/initiators/default/initPayment", buffer.toString());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sentRequest(String uri, String json) throws IOException {

        String result;

        HttpPost request = new HttpPost(uri);
        request.addHeader("content-type", "application/json");

        // send a JSON data
        request.setEntity(new StringEntity(json));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            System.out.println(response.getStatusLine().toString());
            result = EntityUtils.toString(response.getEntity());
        }

        return result;

    }
}
