package com.practiceApp.initRequestApp;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.FileReader;
import java.io.IOException;

public class HttpRequest {

    public static void main(String[] args) {

        try (FileReader reader = new FileReader("src/main/resources/initP.json")) {
            StringBuffer buffer = new StringBuffer();

            int i;
            try {
                while ((i = reader.read()) != -1) {
                    buffer.append((char) i);
                }
            } catch (IOException ex) {

                System.out.println(ex.getMessage());
            }

            System.out.println(buffer.toString());
            String result = sentRequest("https://gate-test-02.teko.io/api/initiators/default/initPayment", buffer.toString());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sentRequest(String uri, String json) throws IOException {
        byte[] key = "TestSecret".getBytes();

        HmacUtils hm256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, key);
        String hmac = Base64.encodeBase64String(hm256.hmac(json));
        String result;

        HttpPost request = new HttpPost(uri);
        request.addHeader("content-type", "application/json");
        request.addHeader("Signature", hmac);

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
