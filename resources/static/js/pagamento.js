document.addEventListener("DOMContentLoaded", function () {
    // 1. Legge i parametri dalla URL
    const urlParams = new URLSearchParams(window.location.search);
    const nomeFilm = urlParams.get('film');
    const tipoTransazione = urlParams.get('tipo');
    const prezzo = urlParams.get('prezzo');

    console.log("Film:", nomeFilm); // Debug

   
    if (nomeFilm && tipoTransazione && prezzo) {

        // Decodifica e inserimento dati
        const filmTitle = decodeURIComponent(nomeFilm);
        const tipoTesto = (tipoTransazione.toLowerCase() === "noleggio") ? "Noleggio" : "Acquisto";

        //.textContent protegge da XSS
        document.getElementById("film-title").textContent = filmTitle;

        //inner.html Vulnerabile ad XSS-------------------------
        // document.getElementById("film-title").innerHTML = filmTitle;
        
        
        document.getElementById("transaction-type").textContent = tipoTesto;
        document.getElementById("final-price").textContent = `€ ${prezzo}`;

        // --- PUNTO CRUCIALE: AGGIUNGIAMO L'EVENTO AL BOTTONE QUI ---
        const btn = document.querySelector(".btn-confirm");
        if (btn) {
            // Rimuovi 'onclick' dall'HTML e usa questo:
            btn.addEventListener("click", confermaPagamento);
        }

    } else {
    // Se mancano dati, mostra errore in modo sicuro
    document.body.textContent = ""; // Pulisce la pagina
    
    const div = document.createElement("div");
    div.className = "checkout-container";
    
    // Costruisci il messaggio in modo sicuro o usa innerHTML SOLO con stringhe statiche
    div.innerHTML = `
        <div class="order-card">
            <h2>Errore</h2>
            <p>Dati ordine mancanti o non validi.</p>
            <a class="btn-cancel" href="/catalogo.html">Torna al catalogo</a>
        </div>
    `;
    document.body.appendChild(div);
    }
});

// Funzione separata 
function confermaPagamento() {
    // 1. Chiedi conferma all'utente
    const messaggio = `Sei sicuro di voler procedere con il ${document.getElementById("transaction-type").textContent.toLowerCase()} di "${document.getElementById("film-title").textContent}" per ${document.getElementById("final-price").textContent}?`;
    
    if (!confirm(messaggio)) {
        // Se l'utente preme "Annulla", usciamo dalla funzione e non succede nulla
        console.log("Pagamento annullato dall'utente.");
        return;
    }

    // 2. Se l'utente preme "OK", procediamo con il resto del codice
    console.log("Click confermato! Avvio transazione...");
    const btn = document.querySelector(".btn-confirm");

    // Effetto 'Loading'
    btn.disabled = true;
    btn.textContent = "Attendere...";
    btn.style.cursor = "wait";

    // Simulazione invio dati
    setTimeout(() => {
        window.location.href = "/successo-pagamento.html"; 
    }, 1500);
}