package com.example.demo.controller;

import com.example.demo.model.Film;
import com.example.demo.model.FilmRepository;
import com.example.demo.service.CatalogoService;
import com.example.demo.service.keycloack.KeycloakPermissionService;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/film")
@CrossOrigin(
    origins = "https://localhost",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
    )                                                                 
    public class CatalogoController {


    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private FilmRepository filmservice;

    @Autowired
    private KeycloakPermissionService keycloakPermissionService;
    // --------------------------------------------------------------------
    // Endpoint per recuperare tutti i film
    // --------------------------------------------------------------------

    @GetMapping("/catalogo")
    public RedirectView catalogo() {
    
    return new RedirectView("/catalogo.html");
}

    @GetMapping("/all")
    public List<Film> getAllFilms(@AuthenticationPrincipal OidcUser user) {
    System.out.println("Utente loggato: " + user.getPreferredUsername());
    // System.out.println("Ruoli: " + user.getAuthorities());
    // System.out.println("Claims"+user.getClaims());
    return catalogoService.getAllFilms();
}


    @PostMapping("/aggiungiFilm")
    public ResponseEntity<String> aggiungifilm(@RequestBody Film nuovoFilm, @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
    try {
        // --- 1. VALIDAZIONE BASE ---
        if (nuovoFilm.getTitolo() == null || nuovoFilm.getTitolo().isEmpty()) {
            return new ResponseEntity<>("Il titolo è obbligatorio", HttpStatus.BAD_REQUEST);
        }

        // --- 2. CONTROLLO PERMESSI (Il tuo codice esistente) ---
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        boolean allowed = keycloakPermissionService.isAllowed(accessToken, "nuovo_film", "aggiungi");

        if (!allowed) {
            return new ResponseEntity<>("Non hai i permessi", HttpStatus.FORBIDDEN);
        }

        // Blocchiamo prezzi negativi o durate senza senso
        if (nuovoFilm.getNoleggio() < 0 || nuovoFilm.getAcquisto() < 0) {
             return new ResponseEntity<>("I prezzi non possono essere negativi", HttpStatus.BAD_REQUEST);
        }

        // --- 3. SANITIZZAZIONE (NUOVO CODICE) ---
        // Jsoup.clean rimuove tutti i tag HTML (<script>, <img>, <div>, ecc.)
        // Safelist.none() significa "non permettere nessun tag HTML, voglio solo testo puro"
        
        if (nuovoFilm.getDescrizione() != null) {
            String descrizionePulita = Jsoup.clean(nuovoFilm.getDescrizione(), Safelist.none());
            nuovoFilm.setDescrizione(descrizionePulita);
        }

        //DIFESA XSS PER IL TITOLO
        if (nuovoFilm.getTitolo() != null) {
            String titoloPulito = Jsoup.clean(nuovoFilm.getTitolo(), Safelist.none());
            nuovoFilm.setTitolo(titoloPulito);
        }
        
        // --- 4. PROTEZIONE ID (MASS ASSIGNMENT) ---
        // Importante! Se qualcuno manda un ID JSON, potrebbe sovrascrivere un film esistente.
        // Forziamo l'ID a null o 0 per essere sicuri che crei un NUOVO record.
        nuovoFilm.setId(null); 

        // --- 5. SALVATAGGIO ---
        filmservice.save(nuovoFilm);

        return new ResponseEntity<>("Film aggiunto con successo!", HttpStatus.OK);

    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>("Errore nel salvataggio: ", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

        @GetMapping("/utente")
        public Map<String, String> getLoggedUser(@AuthenticationPrincipal OidcUser principal) {
            if (principal != null) {
                // Keycloak solitamente mette l'username in "preferred_username"
                String username = principal.getPreferredUsername();
                
                // Fallback: se preferred_username è null, usiamo il "name" o "sub"
                if (username == null || username.isEmpty()) {
                    username = principal.getName();
                }
                
                return Collections.singletonMap("username", username);
            }
            // Se per qualche motivo non c'è un utente loggato (ma Spring Security dovrebbe bloccarlo prima)
            return Collections.singletonMap("username", "Utente non riconosciuto");
        }    
    

}


