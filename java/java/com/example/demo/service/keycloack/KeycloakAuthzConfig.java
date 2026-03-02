package com.example.demo.service.keycloack;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class KeycloakAuthzConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public AuthzClient authzClient() {
        // Estrai auth-server-url e realm dall'issuer-uri
        String authServerUrl = issuerUri.substring(0, issuerUri.lastIndexOf("/realms"));
        String realm = issuerUri.substring(issuerUri.lastIndexOf("/") + 1);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("secret", clientSecret);

        Configuration configuration = new Configuration(
            authServerUrl,           
            realm,                   // Ing.Flix
            clientId,                // Flix
            credentials,             //secret
            null
        );
        

        return AuthzClient.create(configuration);
    }
}