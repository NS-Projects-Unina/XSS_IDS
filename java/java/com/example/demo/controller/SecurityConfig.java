package com.example.demo.controller; 

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;


import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

@Configuration
public class SecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       // Crea un handler che accetta il token dall'header X-XSRF-TOKEN
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null); // Usa il default
        
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/admin/responses/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler) 
            )
            .headers(headers -> headers
               .cacheControl(cache -> cache.disable()) 
                .addHeaderWriter((request, response) -> {
                    // Sovrascriviamo manualmente per essere sicuro 
                    response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                })
                
                // Disabilita X-Frame-Options di Spring (lo mette Nginx)
                .frameOptions(frame -> frame.disable())
                
                // Disabilita HSTS di Spring (lo mette Nginx)
                .httpStrictTransportSecurity(hsts -> hsts.disable())
                
                // Disabilita Content-Type-Options di Spring (lo mette Nginx)
                .contentTypeOptions(ct -> ct.disable())
                
                // Disabilita XSS-Protection di Spring (lo mette Nginx)
                .xssProtection(xss -> xss.disable())
                
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline';" +
                    "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                    "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com; " +
                    "img-src 'self' data: https://images.unsplash.com; " +
                    "base-uri 'self'; " +
                    "frame-ancestors 'self';"
                )
            )
            )

            .authorizeHttpRequests(auth -> auth
            // Endpoint pubblici (accessibili a tutti)
            .requestMatchers("/", "/login.html", "/css/**", "/img/**","index.html").permitAll()
            .requestMatchers("/actuator/health","/actuator/info").permitAll()
            
            //endpoint vulnerabili per test
            .requestMatchers( "/api/film/cerca/**").permitAll()

            //endpoint per la risposta di wazuh di disabilitare un account
            .requestMatchers("/api/admin/responses/**").permitAll()
            // Endpoint che richiedono autenticazione
            .requestMatchers("/api/film").authenticated()

            
                
            .anyRequest().authenticated()  //
            )
            
            .oauth2Login(oauth -> oauth
            
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                )
                
                .defaultSuccessUrl("/api/film/catalogo")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("APPSESSIONID", "XSRF-TOKEN")
            );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler = 
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        
        handler.setPostLogoutRedirectUri("https://localhost/");

        return (request, response, authentication) -> {
            if (authentication != null) {
                String username = authentication.getName();
                // Log conforme alla regola Wazuh
                auditLogger.info("AUDIT_LOG [LOGOUT] - Utente: {}", username);
            }
            // Esegue il redirect standard verso Keycloak
            handler.onLogoutSuccess(request, response, authentication);
        };
    }


    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
 
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
 
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                return customizeAuthorizationRequest(authorizationRequest, request);
            }
 
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customizeAuthorizationRequest(authorizationRequest, request);
            }
 
            private OAuth2AuthorizationRequest customizeAuthorizationRequest(
                    OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
               
                if (authorizationRequest == null) {
                    return null;
                }
 
                // Se l'URL da cui arriviamo ha il parametro "force", diciamo a Keycloak di chiedere la password
                String forceLogin = request.getParameter("force");
                if ("true".equals(forceLogin)) {
                    Map<String, Object> additionalParameters =
                        new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
                   
                    additionalParameters.put("prompt", "login");
                   
                    return OAuth2AuthorizationRequest.from(authorizationRequest)
                            .additionalParameters(additionalParameters)
                            .build();
                }
                return authorizationRequest;
            }
        };
    }


    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        // Il formato deve corrispondere alla regola Wazuh
        auditLogger.info("AUDIT_LOG [LOGIN] - Utente: {}", username);
    }
}
