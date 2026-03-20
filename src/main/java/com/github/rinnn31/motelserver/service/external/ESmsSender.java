package com.github.rinnn31.motelserver.service.external;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.properties.ESmsProperties;
import com.github.rinnn31.motelserver.utils.JsonHelper;

@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "esms")
public class ESmsSender implements ISmsSender {
    private static final String API_URL = "https://api.esms.vn/MainService.svc/json/SendMultipleMessage_V4_get";

    private static final int OTP_SMS_TYPE = 2;

    private final ESmsProperties esmsProperties;

    public ESmsSender(ESmsProperties esmsProperties) {
        this.esmsProperties = esmsProperties;
    }

    @Override
    public boolean sendMessage(String phoneNumber, String message) {
        var client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(phoneNumber, message)))
            .build();
        
        try {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            var body = JsonHelper.fromJson(response.body());
            return body != null && body.get("CodeResult") != null && ((Number)body.get("CodeResult")).intValue() == 100;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String createRequestBody(String phoneNumber, String message) {
        return String.format("""
            {
                "ApiKey": "%s",
                "SecretKey": "%s",
                "BrandName": "%s",
                "SmsType": %d,
                "Phone": "%s",
                "Content": "%s"
            }
        """, esmsProperties.apiKey(), esmsProperties.apiSecret(), esmsProperties.brandName(), OTP_SMS_TYPE, phoneNumber, message);
    }
}
