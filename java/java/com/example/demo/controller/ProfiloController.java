package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profilo")
@CrossOrigin(
    origins = "https://localhost",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET}
    ) 
public class ProfiloController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfilo(@AuthenticationPrincipal OidcUser principal) {
        
        // Se l'utente non è loggato, principal sarà null
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> map = new HashMap<>();
        
        // Keycloak popola questi campi standard
        map.put("username", principal.getPreferredUsername());
        map.put("email", principal.getEmail());
        map.put("nome", principal.getGivenName());
        map.put("cognome", principal.getFamilyName());
        map.put("id", principal.getSubject());
    

        return ResponseEntity.ok(map);
    }
}