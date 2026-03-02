package com.example.demo.controller.wazuh;


import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/responses")
public class WazuhActiveResponseController {

    // Logger per tenere traccia delle operazioni 
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");

    private final Keycloak keycloak;

    private static final String REALM_NAME = "Ing.Flix"; //Nome del Realm su Keycloak

    public WazuhActiveResponseController(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Endpoint chiamato dall'Active Response di Wazuh per disabilitare un utente malevolo.
     * URL: POST https://localhost/api/admin/responses/block-user?username=nomeutente
     */
    @PostMapping("/block-user")
    public ResponseEntity<String> blockUser(@RequestParam String username) {
        try {
            // Cerchiamo l'utente nel Realm specifico
            // Il secondo parametro 'true' indica una ricerca esatta per lo username
            List<UserRepresentation> users = keycloak.realm(REALM_NAME)
                    .users()
                    .search(username, true);

            if (users == null || users.isEmpty()) {
                auditLogger.warn("[WAZUH AR] Tentativo di blocco fallito: utente '{}' non trovato.", username);
                return ResponseEntity.status(404).body("Utente non trovato su Keycloak.");
            }

            // Otteniamo la rappresentazione dell'utente (Keycloak restituisce una lista, prendiamo il primo)
            UserRepresentation user = users.get(0);

            // Modifichiamo lo stato dell'account
            user.setEnabled(false);

            // Inviamo l'aggiornamento a Keycloak
            keycloak.realm(REALM_NAME)
                    .users()
                    .get(user.getId())
                    .update(user);

            // Logghiamo l'azione (questo log può essere riletto da Wazuh come conferma)
            auditLogger.info("AUDIT_LOG [BLOCK] - Utente '{}' DISABILITATO via Wazuh Active Response", username);

            return ResponseEntity.ok("Account " + username + " disabilitato correttamente.");

        } catch (Exception e) {
            auditLogger.error("[WAZUH AR] Errore critico durante il blocco di '{}': {}", username, e.getMessage());
            return ResponseEntity.status(500).body("Errore interno: " + e.getMessage());
        }
    }
}
