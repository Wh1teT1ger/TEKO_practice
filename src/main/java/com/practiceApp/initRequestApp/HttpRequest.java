package com.practiceApp.initRequestApp;

import com.google.gson.*;
import com.practiceApp.Classes.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HttpRequest {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new Exception("Method not received");
            }
            String method = args[0];
            String uri = "https://gate-test-02.teko.io/api/initiators/default/" + method;
            String json;
            if (args.length == 1) {
                switch (method) {
                    case "initPayment" -> json = getTestInitPayment();
                    case "getPaymentStatus", "getPaymentById" -> json = getTestPaymentsById();
                    case "getPaymentsByTag" -> json = getTestPaymentsByTag();
                    default -> json = "";
                }
            } else {
                json = readFile(args[1]);
                System.out.println("Json get from file" + args[1]);
            }
            String result = sendRequest(uri, json);
            System.out.println(result);
        } catch (Exception ex) {

            System.out.println(ex.getMessage());
        }

    }

    private static String getTestInitPayment() {
        Client client = new Client("praktika_2022", "app");
        String product = "spotify";
        Payment payment = new Payment(10000, 643, 3);
        Payer src = new Payer("mc", "79163332211", "mts_ru");
        Order order = new Order("1122334455", 1428430631, "transaction", "mobile_app", "some_value");
        String tag = "Europe";
        initPaymentData initData = new initPaymentData(client, product, payment, src, order);
        initData.setTag(tag);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(initData);
    }

    private static String getTestPaymentsByTag() {
        Client client = new Client("praktika_2022", "app");
        String tag = "Europe";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(new getPaymentTagData(client, tag));
    }

    private static String getTestPaymentsById() {
        Client client = new Client("praktika_2022", "app");
        String tx_id = "62d9962860b2a1a7c1d61c81";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(new getPaymentIdData(client, tx_id));
    }

    private static String readFile(String fileName) throws FileNotFoundException {
        FileReader reader = new FileReader(fileName);
        StringBuffer buffer = new StringBuffer();
        int i;

        try {
            while ((i = reader.read()) != -1) {
                buffer.append((char) i);
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
        return buffer.toString();
    }

    private static String sendRequest(String uri, String json) throws IOException {
        byte[] key = "TestSecret".getBytes();
        System.out.println("Request to " + uri);
        HmacUtils hm256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, key);
        String hmac = Base64.encodeBase64String(hm256.hmac(json));
        String result;

        HttpPost request = new HttpPost(uri);
        request.addHeader("content-type", "application/json");
        System.out.println("content-type: application/json");
        request.addHeader("Signature", hmac);
        System.out.println("Signature: " + hmac);
        System.out.println(json);
        // send a JSON data
        request.setEntity(new StringEntity(json));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            System.out.println("Response");
            System.out.println(response.getStatusLine().toString());
            result = EntityUtils.toString(response.getEntity());

        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(JsonParser.parseString(result));

    }
}
