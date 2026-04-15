package com.github.rinnn31.motelserver.service.external;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.rinnn31.motelserver.config.properties.InfoBipProperties;
import com.infobip.ApiClient;
import com.infobip.ApiException;
import com.infobip.ApiKey;
import com.infobip.BaseUrl;
import com.infobip.api.SmsApi;
import com.infobip.model.SmsDestination;
import com.infobip.model.SmsMessage;
import com.infobip.model.SmsRequest;
import com.infobip.model.SmsTextContent;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "infobip")
public class InfoBipSmsSender {
    private final InfoBipProperties properties;

    private ApiClient apiClient;

    private SmsApi smsApi;

    public InfoBipSmsSender(InfoBipProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.apiClient = ApiClient.forApiKey(ApiKey.from(properties.apiKey()))
                                  .withBaseUrl(BaseUrl.from(properties.baseUrl()))
                                  .build();
        this.smsApi = new SmsApi(apiClient);
    }

    public boolean sendMessage(String phoneNumber, String message) {
        var smsMessage = new SmsMessage()
            .sender("MotelManager")
            .addDestinationsItem(new SmsDestination().to(phoneNumber))
            .content(new SmsTextContent().text(message));

        try {
            var request = new SmsRequest().addMessagesItem(smsMessage);
            smsApi.sendSmsMessages(request).execute();
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }
}
