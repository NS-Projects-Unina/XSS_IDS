

/**
 * Legge un cookie per nome
 */
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

/**
 * Ottiene il token CSRF dal cookie
 */
function getCsrfToken() {
    return getCookie('XSRF-TOKEN');
}


function inviaModifiche() {
    // 1. Recupero i valori tramite ID
  
    var idFilm = document.getElementById("id").value;
    var descrizione = document.getElementById("descrizione").value;
    var prezzoNoleggio = document.getElementById("noleggio").value;
    var prezzoAcquisto = document.getElementById("acquisto").value;

    console.log(idFilm)
    console.log(descrizione)
    console.log(prezzoNoleggio)
    console.log(prezzoAcquisto)

    // 2. Controllo base (opzionale)
    if (!idFilm) {
        alert("Errore: ID del film mancante.");
        return;
    }

    // const patternVietati = /<[^>]*>/; // Cerca qualsiasi tag HTML
    // if (patternVietati.test(descrizione)) {
    //     alert("Attenzione: La descrizione contiene caratteri non validi (< o >). Rimuovili per salvare.");
    //     return;
    // }

    // 3. Creo l'oggetto JSON
   
    var datiDaInviare = {
        id: parseInt(idFilm),
        descrizione: descrizione,
        noleggio: parseFloat(prezzoNoleggio),
        acquisto: parseFloat(prezzoAcquisto)
       
    };

    // 4. Recupero il token CSRF
    const csrfToken = getCsrfToken();

    // 4. Invio con FETCH API
    fetch('/api/film/salvaFilm', { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': csrfToken      //Il token CSRF protegge la tua applicazione dagli attacchi Cross-Site Request Forgery (falsificazione di richieste tra siti).
        },
        body: JSON.stringify(datiDaInviare)
    })
    .then(response => {
        if (response.ok) {
            alert("Salvataggio riuscito!");
          
        } else {
            alert("Errore durante il salvataggio.");
        }
    })
    .catch(error => {
        console.error('Errore:', error);
        alert("Errore di comunicazione.");
    });
}

function eliminaFilm() {
    // Recupero l'ID del film dal campo nascosto
    var idFilm = document.getElementById("id").value;

    if (!idFilm) {
        alert("Errore: ID del film mancante.");
        return;
    }

    // Chiedo conferma all'utente
    if (!confirm("Sei sicuro di voler eliminare definitivamente questo film? L'operazione non è reversibile.")) {
        return; // L'utente ha cliccato "Annulla"
    }

    // Recupero il token CSRF 
    const csrfToken = getCsrfToken();

    // Invio richiesta DELETE
    // Nota: passiamo l'ID nell'URL
    fetch('/api/film/elimina/' + idFilm, { 
        method: 'DELETE',
        headers: {
            'X-XSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            alert("Film eliminato con successo!");
            // Reindirizza al catalogo o alla home dopo l'eliminazione
            window.location.href = "/catalogo.html"; 
        } else {
            // Se il server risponde con errore 
            return response.text().then(text => { throw new Error(text) });
        }
    })
    .catch(error => {
        console.error('Errore:', error);
        alert("Errore durante l'eliminazione: " + error.message);
    });
}