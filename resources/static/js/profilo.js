document.addEventListener("DOMContentLoaded", async () => {
    try {
        // --- 1. CHIAMATA GET (Semplificata) ---
        // Nota: Non serve X-XSRF-TOKEN per le GET
        const response = await fetch('/api/profilo', {
            method: 'GET', 
            headers: {
                'Content-Type': 'application/json'
                // Il Cookie di sessione parte in automatico qui
            }
        });
        
        // --- 2. DIAGNOSTICA ERRORI ---
        if (!response.ok) {
        
            console.error("Errore HTTP:", response.status);
            
            if (response.status === 401) {
                alert("Sessione scaduta o non valida. Effettua di nuovo il login.");
                window.location.href = '/login';
                return;
            }
            if (response.status === 403) {
                alert("Accesso Negato (403). Non hai i permessi per vedere questo profilo.");
                return;
            }
            throw new Error("Errore generico: " + response.status);
        }

        // --- 3. SUCCESSO ---
        const user = await response.json();
        console.log("Dati utente ricevuti:", user); // DEBUG nel browser

        // Riempie i campi HTML 
        const setText = (id, val) => {
            const el = document.getElementById(id);
            if(el) el.innerText = val || '-';
        };

        setText('prof-username', user.username);
        setText('prof-email', user.email);
        setText('prof-firstname', user.nome);
        setText('prof-lastname', user.cognome);
        setText('prof-fullname', (user.nome && user.cognome) ? `${user.nome} ${user.cognome}` : user.username);
        setText('prof-id', user.id);
    

    } catch (e) {
        console.error("Errore fetch profilo:", e);
    }
});