package com.example.demo.service.keycloack;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.keycloak.representations.AccessTokenResponse;


@Service
public class KeycloakPermissionService {

    @Autowired
    private AuthzClient authzClient;

    public boolean isAllowed(String accessToken, String resource, String scope) {
        AuthorizationRequest req = new AuthorizationRequest();
        req.addPermission(resource, scope);
        try {
            AccessTokenResponse response =
                    authzClient.authorization(accessToken).authorize(req);

            return response != null; // ALLOW
        } catch (Exception e) {
            System.out.println("Errore: "+e);
            return false; // DENY
        }
    }
}
