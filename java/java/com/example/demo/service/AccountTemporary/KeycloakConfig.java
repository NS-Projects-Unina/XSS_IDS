package com.example.demo.service.AccountTemporary; 
 
import jakarta.ws.rs.client.ClientBuilder; 
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class KeycloakConfig {
 
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;
 
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
 
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
 
    @Bean
    public Keycloak keycloak() {
        // Estrazione Server URL e Realm dall'Issuer URI
        String serverUrl = issuerUri.substring(0, issuerUri.indexOf("/realms/"));
        String realm = issuerUri.substring(issuerUri.lastIndexOf("/") + 1);
 
        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
        clientBuilder.connectionPoolSize(10);
 
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(clientBuilder.build()) 
                .build();
    }
}
