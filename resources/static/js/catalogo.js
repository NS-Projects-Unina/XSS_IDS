/* --- CONFIGURAZIONE --- */
const API_URL = "/api/film/all";
const API_URL_ADD = `/api/film/aggiungiFilm`;
const API_URL_USER = "/api/film/utente"; 

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

/* --- 1. CARICAMENTO DATI DAL DB (Backend -> Frontend) --- */

// Eseguiamo questa funzione appena la pagina è pronta
document.addEventListener("DOMContentLoaded", () => {
    loadFilms();
    loadCurrentUser();
});

async function loadFilms() {
    try {
        // Chiamata al Controller Java
        const response = await fetch(API_URL);
        
        if (!response.ok) {
            throw new Error(`Errore HTTP! Status: ${response.status}`);
        }

        // Convertiamo la risposta in JSON (List<Film>)
        const films = await response.json();
        
        // Popoliamo la tabella HTML
        populateTable(films);

    } catch (error) {
        console.error("Errore nel caricamento dei film:", error);
        document.getElementById("no-result").innerText = "Errore di connessione al server.";
        document.getElementById("no-result").style.display = "block";
    }
}


/* --- FUNZIONE RECUPERO UTENTE --- */
async function loadCurrentUser() {
    try {
        const response = await fetch(API_URL_USER);
        
        if (response.ok) {
            const data = await response.json(); // Riceviamo { "username": "mario" }
            
            // Se abbiamo un username, lo stampiamo nell'HTML
            const usernameSpan = document.getElementById("nav-username");
            if (usernameSpan && data.username) {
              
                usernameSpan.innerText = data.username.charAt(0).toUpperCase() + data.username.slice(1);
            }
        }
    } catch (error) {
        console.error("Errore recupero utente:", error);
        
    }
}


function populateTable(films) {
    const tableBody = document.querySelector("#moviesTable tbody");
    
    // 1. Pulizia sicura della tabella
    tableBody.innerHTML = "";

    // 2. Gestione caso "Nessun Risultato"
    const noResultDiv = document.getElementById("no-result");
    if (films.length === 0) {
        if (noResultDiv) noResultDiv.style.display = "block";
        return;
    } else {
        if (noResultDiv) noResultDiv.style.display = "none";
    }

    // 3. Creazione righe
    films.forEach(film => {
        const row = document.createElement("tr");

        // --- DIFESA XSS ---
        // Questa funzione crea una cella <td> e inserisce testo sicuro.
        // Anche se "text" contiene <script>, verrà stampato a video e non eseguito.
        const addTextCell = (text) => {
            const td = document.createElement("td");
            td.textContent = text; 
            row.appendChild(td);
        };


        //--------------VLUNERABILITà XSS------------------
        // const addTextCell = (text) => {
        //     const td = document.createElement("td");
        //     td.innerHTML = text;   // <--- Questo esegue l'HTML e gli script!
        //     row.appendChild(td);
        // };

       
        addTextCell(film.titolo);

        addTextCell(film.durata_min + " min");

      
        addTextCell(film.descrizione);

        // --- HELPER PER CELLE PREZZO (LINK) ---
        const addPriceCell = (tipo, prezzo, cssClass) => {
            const td = document.createElement("td");
            const link = document.createElement("a");
            
            // Preparazione dati sicuri per URL
            const urlSafeTitle = encodeURIComponent(film.titolo);
            const prezzoFormat = prezzo.toFixed(2);

            // Costruzione Link
            //Link alla pagina html corretto
            link.href = `pagamento.html?film=${urlSafeTitle}&tipo=${tipo}&prezzo=${prezzoFormat}`;
            
            // link.href = `Pagamento_vulnerabile.html?film=${urlSafeTitle}&tipo=${tipo}&prezzo=${prezzoFormat}`;
            link.className = `price-tag ${cssClass}`;
            link.textContent = `€ ${prezzoFormat}`; // Sicuro

            td.appendChild(link);
            row.appendChild(td);
        };

        
        addPriceCell('noleggio', film.noleggio, 'rent-price');

        addPriceCell('acquisto', film.acquisto, 'buy-price');

        const tdModifica = document.createElement("td");
        const linkModifica = document.createElement("a");
        
        
        linkModifica.href = `/api/film/updateFilm?id=${film.id}`; 
        linkModifica.className = "btn-edit";

        // Aggiunta Icona 
        const icon = document.createElement("i");
        icon.className = "fas fa-pencil-alt";
        
        // Aggiungiamo icona e testo al link
        linkModifica.appendChild(icon);
        linkModifica.appendChild(document.createTextNode(" Modifica"));

        tdModifica.appendChild(linkModifica);
        row.appendChild(tdModifica);

       
        const tdRecensione = document.createElement("td");
        tdRecensione.className = "recensione";
        
        let stelleString = '';
        for (let i = 1; i <= 5; i++) {
            stelleString += i <= film.recensione ? '★' : '☆';
        }
    
        tdRecensione.textContent = stelleString;
        
        row.appendChild(tdRecensione);
        tableBody.appendChild(row);
    });
}

/* --- GESTIONE MODALE (POPUP AGGIUNGI FILM) --- */

function apriModale() {
    document.getElementById("modalAggiungi").style.display = "block";
   
    document.getElementById("formFilm").reset(); 
}

function chiudiModale() {
    // Nascondi la modale
    document.getElementById("modalAggiungi").style.display = "none";
}

// Chiudi la modale cliccando fuori 
window.addEventListener("click", function(event) {
    const modal = document.getElementById("modalAggiungi");
    if (event.target == modal) {
        chiudiModale();
    }
});


/* --- 3. COMUNICAZIONE BACKEND (POST - AGGIUNGI FILM) --- */
async function salvaFilm() {
    // 1. Recupero dati dagli input HTML
    const titolo = document.getElementById("inputTitolo").value;
    const durata = document.getElementById("inputDurata").value;
    const descrizione = document.getElementById("inputDescrizione").value;
    const noleggio = parseFloat(document.getElementById("inputNoleggio").value);
    const acquisto = parseFloat(document.getElementById("inputAcquisto").value);
    const voto = parseInt(document.getElementById("inputVoto").value);

    // const patternVietati = /<[^>]*>/; // Cerca qualsiasi tag HTML
    // if (patternVietati.test(descrizione) || patternVietati.test(titolo)) {
    //     alert("Attenzione: La descrizione contiene caratteri non validi (< o >). Rimuovili per salvare.");
    //     return;
    // }

    // 2. Creazione oggetto JSON 
    const nuovoFilm = {
        titolo: titolo,
        durata_min: parseInt(durata),
        descrizione: descrizione,
        noleggio: noleggio,
        acquisto: acquisto,
        recensione: voto
    };

    const csrfToken = getCsrfToken();

    try {
        // 3. Chiamata FETCH POST
        const response = await fetch(API_URL_ADD, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                'X-XSRF-TOKEN': csrfToken      //Il token CSRF protegge la tua applicazione dagli attacchi Cross-Site Request Forgery (falsificazione di richieste tra siti).

            },
            body: JSON.stringify(nuovoFilm)
        });

        if (response.ok) {
            alert("Film aggiunto con successo!");
            chiudiModale();
            loadFilms(); // Ricarica la tabella per mostrare il nuovo film
        } else {
            const text = await response.text();
            console.log('Errore:', text);
            alert("Errore nel salvataggio del film: Status: " + response.status );
        }
    } catch (error) {
        console.error("Errore nella richiesta POST:", error);
        alert("Errore di connessione al backend.");
    }
}


/* --- FUNZIONE RICERCA FILM --- */
function searchMovie() {
    var input = document.getElementById("searchInput");
    var filter = input.value.toUpperCase();
    var table = document.getElementById("moviesTable");
    var tr = table.getElementsByTagName("tr");
    var foundAny = false;

    
    for (var i = 1; i < tr.length; i++) {
        var td = tr[i].getElementsByTagName("td")[0];
        if (td) {
            var txtValue = td.textContent || td.innerText;
            if (txtValue.toUpperCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
                foundAny = true;
            } else {
                tr[i].style.display = "none";
            }
        }       
    }

    var noResultDiv = document.getElementById("no-result");
    if (!foundAny) {
        noResultDiv.style.display = "block";
        table.style.display = "none";
    } else {
        noResultDiv.style.display = "none";
        table.style.display = "table";
    }
}

/* --- GESTIONE MENU UTENTE --- */

// Apre o chiude la tendina
function toggleUserMenu() {
    document.getElementById("userDropdown").classList.toggle("show");
}

// Chiude la tendina se clicchi fuori dall'icona
window.onclick = function(event) {
    if (!event.target.matches('.user-icon')) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        for (var i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}

