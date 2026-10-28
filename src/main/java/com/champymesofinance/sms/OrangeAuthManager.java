package com.champymesofinance.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Gestionnaire d'authentification Orange OAuth 2.0.
 * Récupère et met en cache le token d'accès.
 * Le renouvellement est automatique à l'expiration.
 */
public class OrangeAuthManager {

    private static final String TOKEN_URL = "https://api.orange.com/oauth/v3/token";
    private static final String GRANT_TYPE = "client_credentials";

    private final String clientId;
    private final String clientSecret;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    // Cache du token
    private String accessToken;
    private long expirationTime; // timestamp en millis

    public OrangeAuthManager(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = new OkHttpClient();
    }

    /**
     * Retourne un token valide. Le renouvelle automatiquement s'il est expiré.
     * Marge de sécurité de 60 secondes avant expiration réelle.
     */
    public synchronized String getAccessToken() throws IOException {
        if (accessToken == null || isTokenExpired()) {
            System.out.println("[Auth] Token expiré ou absent, renouvellement...");
            renouvelerToken();
        }
        return accessToken;
    }

    private boolean isTokenExpired() {
        // Marge de sécurité de 60 secondes
        return System.currentTimeMillis() >= (expirationTime - 60_000);
    }

    private void renouvelerToken() throws IOException {
        // Encodage Basic Auth : base64(client_id:client_secret)
        String credentials = clientId + ":" + clientSecret;
        String base64Credentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        RequestBody body = new FormBody.Builder()
                .add("grant_type", GRANT_TYPE)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Authorization", "Basic " + base64Credentials)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new IOException("Échec de récupération du token : HTTP "
                        + response.code() + " - " + responseBody);
            }

            JsonNode root = mapper.readTree(responseBody);
            this.accessToken = root.get("access_token").asText();
            long expiresIn = root.get("expires_in").asLong(); // en secondes

            this.expirationTime = System.currentTimeMillis() + (expiresIn * 1000);

            System.out.println("[Auth] Nouveau token obtenu. Expire dans "
                    + expiresIn + " secondes.");
        }
    }

    /**
     * Force le renouvellement du token (utile après une erreur 401).
     */
    public synchronized void invaliderToken() {
        this.accessToken = null;
        this.expirationTime = 0;
    }
}