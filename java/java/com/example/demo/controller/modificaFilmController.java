package com.example.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Film;
import com.example.demo.service.FilmService;
import com.example.demo.service.keycloack.KeycloakPermissionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.core.annotation.AuthenticationPrincipal;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import jakarta.servlet.http.HttpSession;

@Controller  //se fosse restcontroller non potrebbe fare le chiamate agli html templates
@RequestMapping("/api/film")
@CrossOrigin(
    origins = "https://localhost",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
    )
public class modificaFilmController {

        @Autowired
        private FilmService filmService;

        @Autowired
        private KeycloakPermissionService keycloakPermissionService;


        private static final Logger logger = LoggerFactory.getLogger(modificaFilmController.class);

        private RequestCache requestCache = new HttpSessionRequestCache();


    //-------------------------CONTROLLO POLICY KEYCLOAL--------------------------------
    @GetMapping("/updateFilm")
    public String editFilmPage(@RequestParam Long id,
                               Model model,
                               @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
                               @AuthenticationPrincipal OidcUser oidcUser,
                               HttpServletRequest request,   // <--- Serve per salvare la richiesta
                               HttpServletResponse response) { // <--- Serve per salvare la richiesta
 
        // 1. VERIFICA TEMPO DI AUTENTICAZIONE
        if (oidcUser != null) {
            Instant authTime = oidcUser.getIdToken().getClaimAsInstant("auth_time");
            if (authTime == null) {
                authTime = oidcUser.getIdToken().getIssuedAt();
            }
 
            long secondsSinceLogin = ChronoUnit.SECONDS.between(authTime, Instant.now());
           
            // Impostiamo il limite (es. 60 secondi)
            if (secondsSinceLogin > 60) {
               
                // Salviamo la richiesta corrente (URL + parametri) nella cache di Spring Security
                requestCache.saveRequest(request, response);
               
                System.out.println("DEBUG - TEMPO SCADUTO! Redirect al login... Salvata destinazione: " + request.getRequestURI());
                return "redirect:/oauth2/authorization/keycloak?force=true";
            }
        }
 
        // 2. RECUPERO TOKEN E PERMESSI
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        boolean allowed = keycloakPermissionService.isAllowed(accessToken, "film", "update");

        if (!allowed) {
            return "access-denied";
        }
        int id_film = Math.toIntExact(id);
        Film film = filmService.getFilmById(id_film);
        model.addAttribute("film", film);
 
        return "modificaFilm";
    }


    @PostMapping("/salvaFilm")
    @ResponseBody 
    public ResponseEntity<String> aggiornaFilm(@RequestBody Film filmDatiDalForm,
                                                @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        
        try {
            
            // Recupero token
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            // CHIEDO A KEYCLOAK LA DECISIONE DI AUTORIZZAZIONE     NECESSARIO ANCHE QUI SE VIENE FATTA UNA RICHIESTA AD ESEMPIO CON POSTMAN puo chiamare questo endpoint /salvaFilm e modificare i prezzi o le descrizioni dei film
            boolean allowed = keycloakPermissionService.isAllowed(
                    accessToken,
                    "film",          // risorsa definita in Keycloak
                    "update"         // scope azione definito in Keycloak
            );

            if (!allowed) {
            // Restituiamo 403 Forbidden se non è admin
            return new ResponseEntity<>("Accesso negato: non hai i permessi di modifica.", HttpStatus.FORBIDDEN);
            }


            int idFilm = Math.toIntExact(filmDatiDalForm.getId());  //recuperiamo tutto il film perché alcuni campi non possono essere modificati
            Film filmEsistente = filmService.getFilmById(idFilm);

            if (filmEsistente == null) {
                return ResponseEntity.badRequest().body("Film non trovato");
            }


            // --- 2. SANITIZZAZIONE INPUT ---
            // Puliamo la descrizione prima di salvarla
            if (filmDatiDalForm.getDescrizione() != null) {
            String descrizionePulita = Jsoup.clean(filmDatiDalForm.getDescrizione(), Safelist.none());
            filmEsistente.setDescrizione(descrizionePulita);}


            // //INPUT NON CONTROLLATO RISCHIO XSS
            // filmEsistente.setDescrizione(filmDatiDalForm.getDescrizione());


            // --- AGGIUNTA LOG PER WAZUH ---
            logger.info("AUDIT_LOG [FILM_UPDATE] Modifica Film ID: {} - Descrizione: {}", 
                         idFilm, filmDatiDalForm.getDescrizione());

            filmEsistente.setNoleggio(filmDatiDalForm.getNoleggio());
            filmEsistente.setAcquisto(filmDatiDalForm.getAcquisto());


            // Salviamo le modifiche sul database
            filmService.saveFilm(filmEsistente);

            return ResponseEntity.ok("Modifica avvenuta con successo");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Errore nel server: " + e.getMessage());
        }
    }

    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaFilm(@PathVariable Long id,
                                              @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        try {
            // 1. Controllo Sicurezza Keycloak
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            // Verifichiamo se l'utente ha il permesso di eliminare (scope "delete" o "update" a tua scelta)
            boolean allowed = keycloakPermissionService.isAllowed(
                    accessToken,
                    "delete_film",          // Risorsa
                    "delete"         // Scope: Assicurati che "delete" esista in Keycloak, altrimenti usa "update"
            );

            if (!allowed) {
                return new ResponseEntity<>("Accesso negato: non hai i permessi per eliminare.", HttpStatus.FORBIDDEN);
            }

            // 2. Controllo esistenza
            int idFilm = Math.toIntExact(id);
            Film filmEsistente = filmService.getFilmById(idFilm);

            if (filmEsistente == null) {
                return ResponseEntity.badRequest().body("Film non trovato");
            }

            // 3. Eliminazione effettiva
            // Assicurati che nel tuo FilmService esista un metodo deleteFilmById o simile
            filmService.deleteFilmById(idFilm); 

            return ResponseEntity.ok("Film eliminato correttamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Errore nel server: " + e.getMessage());
        }
    }
    
    
}
