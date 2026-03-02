const resultContainer = document.getElementById('result-container');
const searchForm = document.getElementById('searchForm');
const userInput = document.getElementById('userInput');

// --- 1. FUNZIONE PER CHIAMARE IL SERVER ---
function inviaRichiesta(valore) {
    if (!valore) return;

    fetch(`/api/film/richiesta?richiesta=${encodeURIComponent(valore)}`)
        .then(response => response.text())
        .then(htmlRisposta => {
            // VULNERABILITÀ: Iniezione diretta della risposta del server
            resultContainer.innerHTML = htmlRisposta;
        })
        .catch(err => console.error("Errore nel fetch:", err));
}

// --- 2. GESTIONE CARICAMENTO PAGINA ---

const urlParams = new URLSearchParams(window.location.search);
const richiestaInUrl = urlParams.get('richiesta');
if (richiestaInUrl) {
    inviaRichiesta(richiestaInUrl);
}

// --- 3. GESTIONE INPUT MANUALE (Digitazione nel box) ---
searchForm.addEventListener('submit', function(e) {
    e.preventDefault(); // Impedisce il ricaricamento della pagina
    const valoreDigitato = userInput.value;
    
    // Eseguiamo la richiesta al server
    inviaRichiesta(valoreDigitato);
    
    
    const nuovoUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?richiesta=' + encodeURIComponent(valoreDigitato);
    window.history.pushState({path: nuovoUrl}, '', nuovoUrl);
});