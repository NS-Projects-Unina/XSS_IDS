package com.example.demo.service.AccountTemporary;
 

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
 
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
 
@Service
public class AccountExpirationService {
 
    private final Keycloak keycloak;
 
    // ================= CONFIGURAZIONE =================
    private static final String REALM_NAME = "Ing.Flix";
    private static final String EXPIRATION_ATTR = "account_expiration_date";
    private static final int BATCH_SIZE = 100;
    // ==================================================
 
    public AccountExpirationService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
 
    /**
     *Controllo automatico per disabilitare account scaduti.
     * modifica il CRON come preferisci.
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void disableExpiredAccounts() {
 
        System.out.println(
                "[AUDIT START] Inizio scansione account nel realm '" + REALM_NAME + "'"
        );
 
        int firstResult = 0;
        boolean moreUsers = true;
        int totalProcessed = 0;
        int totalDisabled = 0;
 
        try {
            while (moreUsers) {
 
                List<UserRepresentation> users = keycloak.realm(REALM_NAME)
                        .users()
                        .list(firstResult, BATCH_SIZE);
 
                if (users == null || users.isEmpty()) {
                    moreUsers = false;
                    continue;
                }
 
                for (UserRepresentation user : users) {
                    try {
                        boolean disabled = processSingleUser(user);
                        if (disabled) {
                            totalDisabled++;
                        }
                    } catch (Exception e) {
                        System.err.println(
                                "[ERROR] Errore durante il controllo dell'utente '" +
                                user.getUsername() + "': " + e.getMessage()
                        );
                    }
                    totalProcessed++;
                }
 
                firstResult += BATCH_SIZE;
 
                System.out.println(
                        "[INFO] Processati " + totalProcessed + " utenti finora..."
                );
            }
        } catch (Exception e) {
            System.err.println(
                    "[CRITICAL ERROR] Errore critico durante il job AC-2(2)"
            );
            e.printStackTrace();
        }
 
        System.out.println(
                "[AUDIT END] Scansione completata. Utenti controllati: " +
                totalProcessed + ". Utenti disabilitati oggi: " + totalDisabled
        );
    }
 
    /**
     * Controlla un singolo utente e lo disabilita se scaduto.
    */
    private boolean processSingleUser(UserRepresentation user) {
 
        // Ignora utenti già disabilitati
        if (!Boolean.TRUE.equals(user.isEnabled())) {
            return false;
        }
 
        // Ignora utenti senza attributi
        if (user.getAttributes() == null) {
            return false;
        }
 
        List<String> expirationValues =
                user.getAttributes().get(EXPIRATION_ATTR);
 
        if (expirationValues == null || expirationValues.isEmpty()) {
            return false;
        }
 
        String expDateStr = expirationValues.get(0);
        LocalDate today = LocalDate.now();
 
        try {
            LocalDate expDate = LocalDate.parse(expDateStr);
 
            if (expDate.isBefore(today) || expDate.isEqual(today)) {
                disableUserOnKeycloak(user, expDateStr);
                return true;
            }
 
        } catch (DateTimeParseException e) {
            System.out.println(
                    "[WARNING] Utente '" + user.getUsername() +
                    "' con data scadenza non valida: '" + expDateStr +
                    "' (formato richiesto YYYY-MM-DD)"
            );
        }
 
        return false;
    }
 
    /**
     * Disabilita effettivamente l'utente su Keycloak.
     */
    private void disableUserOnKeycloak(UserRepresentation user, String expiredDate) {
 
        user.setEnabled(false);
 
        keycloak.realm(REALM_NAME)
                .users()
                .get(user.getId())
                .update(user);
 
        System.out.println(
                "[ACTION] Utente '" + user.getUsername() +
                "' (ID: " + user.getId() +
                ") DISABILITATO automaticamente. Data scadenza: " +
                expiredDate
        );
    }
}
