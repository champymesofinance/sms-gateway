package com.champymesofinance.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;

public class OrangeSmsService {

    private static final String URL_TEMPLATE =
            "https://api.orange.com/smsmessaging/v1/outbound/tel%%3A%%2B%s/requests";

    private final OrangeAuthManager authManager;
    private final String senderAddress;
    private final String senderName;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrangeSmsService(OrangeAuthManager authManager, String senderAddress, String senderName) {
        this.authManager = authManager;
        this.senderAddress = senderAddress;
        this.senderName = senderName;
        this.httpClient = new OkHttpClient();
    }

    /**
     * Envoie un SMS et retourne le résultat détaillé (succès/échec + code HTTP + message).
     */
    public ResultatEnvoi envoyerSms(String numeroDestinataire, String message) throws IOException {
        String token = authManager.getAccessToken();
        Response response = executerRequete(token, numeroDestinataire, message);

        if (response.code() == 401) {
            System.out.println("[SMS] Token expiré (401). Renouvellement et nouvel essai...");
            response.close();
            authManager.invaliderToken();
            token = authManager.getAccessToken();
            response = executerRequete(token, numeroDestinataire, message);
        }

        return traiterReponse(response, numeroDestinataire);
    }

    private ResultatEnvoi traiterReponse(Response response, String numeroDestinataire) throws IOException {
        try (response) {
            String corps = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                System.err.println("Erreur SMS -> " + numeroDestinataire
                        + " : HTTP " + response.code() + " - " + corps);
                return ResultatEnvoi.echec(response.code(), corps);
            }

            System.out.println("✓ SMS envoyé à " + numeroDestinataire);
            return ResultatEnvoi.ok();
        }
    }

    private Response executerRequete(String token, String numeroDestinataire, String message)
            throws IOException {
        ObjectNode messageRequest = mapper.createObjectNode();
        messageRequest.put("address", "tel:" + numeroDestinataire);
        messageRequest.put("senderAddress", "tel:" + senderAddress);

        ObjectNode textMessage = mapper.createObjectNode();
        textMessage.put("message", message);
        messageRequest.set("outboundSMSTextMessage", textMessage);
        messageRequest.put("senderName", senderName);

        ObjectNode root = mapper.createObjectNode();
        root.set("outboundSMSMessageRequest", messageRequest);

        String json = mapper.writeValueAsString(root);
        String url = String.format(URL_TEMPLATE, senderAddress.replace("+", ""));

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        return httpClient.newCall(request).execute();
    }
}