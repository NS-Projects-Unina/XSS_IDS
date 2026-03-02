**Descrizione del Progetto**


Il progetto consiste nello sviluppo e nell'analisi di sicurezza di una piattaforma per la gestione di un catalogo cinematografico denominata Ing.Flix. L'obiettivo principale è stato simulare scenari d'attacco realistici e implementare contromisure avanzate per mitigare le vulnerabilità identificate.

**Architettura del Sistema**


L'infrastruttura si avvale di tecnologie moderne per garantire isolamento e controllo:

Back-end: Framework Spring Boot (v.3.5.8).

Identity & Access Management: Keycloak (v. 26.4.5), che gestisce l'autenticazione tramite meccanismi OIDC/OAuth e il controllo degli accessi tramite RBAC (Role-Based Access Control).

Network Security: Proxy inverso gestito tramite NGINX (v.1.28.0)  per la protezione delle comunicazioni.

Database: MySQL per la persistenza dei dati.Secret

Management: Utilizzo di Vault (v.1.21.1) per la gestione sicura delle chiavi.

**Analisi delle Minacce e Attacchi Simulati**


Il team ha eseguito diversi test di penetrazione per valutare la robustezza dell'applicazione:

_Cross-Site Scripting (XSS)_:
Sono state testate tre varianti di XSS sfruttando l'uso di funzioni vulnerabili come innerHTML() nel front-end.

Stored XSS: Iniezione di script malevoli nel campo "Descrizione" dei film per ottenere il Session Hijacking degli amministratori.

Reflected XSS: Utilizzo di URL ingannevoli veicolati tramite phishing per rubare cookie di sessione o indurre azioni non intenzionali (CSRF).

DOM-based XSS: Manipolazione dell'URL tramite ancore (#) per eseguire script lato client, portando al Credential Stealing tramite form di login fasulli.

_Denial of Service (DOS)_:
Sono stati eseguiti test di carico per saturare le risorse del server tramite lo strumento Apache Benchmark e VM Kali Linux:

HTTP Flood: Invio massiccio di richieste HTTP legittime.

Slowloris & Slowdrop: Invio lento di header o simulazione di mancata ricezione per occupare le connessioni del server.

TCP SYN Spoofing: Flood di pacchetti SYN con indirizzi sorgente contraffatti.

_SQL Injection (SQLi)_:
Sfruttando la scorretta gestione dell'input nelle query, sono stati simulati:

Data Manipulation: Cancellazione di record o modifica arbitraria dei prezzi nel catalogo.

Error-Based SQLi: Database Enumeration tramite errori indotti per scoprire tabelle sensibili (es. vault_kv_store).

_OS-Command Injection_:
Esecuzione di comandi arbitrari sul server (es. ipconfig) sfruttando la funzionalità di generazione report PDF tramite terminale.

**Strategie di Difesa e Mitigazione**


Per ogni minaccia è stata implementata una contromisura specifica:

Protezione XSS: Adozione dell'header Content Security Policy (CSP) (whitelist di domini fidati) e sanitizzazione dell'input tramite la libreria Jsoup.

Mitigazione DOS: Configurazione su NGINX di Rate Limiting (max 20 req/s), riduzione dei tempi di keep-alive e troncamento delle connessioni lente.

Rilevamento Intrusioni (IDS): Implementazione di Wazuh (v.4.14.3) come IDS Host-Based per monitorare i log di NGINX, Spring Boot e Keycloak.

Active Response: Configurazione di Wazuh per bloccare automaticamente su Keycloak gli utenti che tentano attacchi SQLi o XSS.

Integrazione VirusTotal: Analisi automatica dei file tramite hash per rilevare malware nel sistema (File Integrity Monitoring).

**Avvio della web-app**


Bisogna avviare keycloak, Vault (effettuando l'unseal), MySQL, NGINX in modo manuale, oppure, configurando opportunatamente il file "env_start.ps1" con i path e le variabili globali corrette del proprio sistema (questa configurazione risulterà essere più veloce). Se il vostro sistema dovesse avere problemi a lanciare il file da terminale, vi consigliamo di crearne uno identico copi-incollando il contenuto del nostro. 

**WAZUH**


Per integrare Wazuh come IDS, bisogna scaricare la versione 4.14.3, configurare opportunatamente il file "ossec.conf" secondo il proprio sistema e opzionalmente richiedere una API key per integrare Virus Total (tramite sito ufficiale).  
