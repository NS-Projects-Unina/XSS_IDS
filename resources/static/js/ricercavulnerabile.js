
function renderStelle(voto) {
    if (voto === null || voto === undefined || voto === "") return '<span style="color:#555;">N/D</span>';

    const num = parseFloat(voto);
    if (isNaN(num)) return '<span style="color:#555;">N/D</span>';

    const scala = num > 5 ? num / 2 : num; // normalizza a 0-5
    const pieno = Math.floor(scala);
    const mezzo = (scala - pieno) >= 0.5 ? 1 : 0;
    const vuoto = 5 - pieno - mezzo;

    const stelle = "★".repeat(pieno) + (mezzo ? "½" : "") + "☆".repeat(vuoto);
    return `<span class="stelle">${stelle}</span><span class="voto-num">${num.toFixed(1)}</span>`;
}

/**
 * Formatta il prezzo in euro; restituisce "N/D" se assente.
 */
function renderPrezzo(valore) {
    if (valore === null || valore === undefined || valore === "") {
        return '<span style="color:#555;">N/D</span>';
    }
    const n = parseFloat(valore);
    if (isNaN(n)) return '<span style="color:#555;">N/D</span>';
    return `<span class="badge-prezzo">€ ${n.toFixed(2)}</span>`;
}

/**
 * Formatta la durata in minuti → "Xh Ym" oppure solo "Ym".
 */
function renderDurata(minuti) {
    if (minuti === null || minuti === undefined || minuti === "") {
        return '<span style="color:#555;">N/D</span>';
    }
    const m = parseInt(minuti, 10);
    if (isNaN(m)) return minuti; // stringa grezza
    if (m >= 60) {
        const h = Math.floor(m / 60);
        const r = m % 60;
        return r > 0 ? `${h}h ${r}m` : `${h}h`;
    }
    return `${m}m`;
}

async function searchMovie() {
    const input = document.getElementById("searchInput");
    const filter = input.value.trim();
    const tbody = document.getElementById("movie-list-body");
    const table = document.getElementById("moviesTable");
    const noResultDiv = document.getElementById("no-result");
    const loading = document.getElementById("loading");

    // Input vuoto → nascondi tutto
    if (filter.length === 0) {
        table.style.display = "none";
        noResultDiv.style.display = "none";
        loading.style.display = "none";
        return;
    }

    // Mostra spinner, nascondi risultati precedenti
    loading.style.display = "block";
    table.style.display = "none";
    noResultDiv.style.display = "none";

    try {
        const response = await fetch(`https://localhost/api/film/cerca?titolo=${encodeURIComponent(filter)}`);

        if (!response.ok) {
            throw new Error(`Errore server: ${response.status}`);
        }

        const data = await response.json();

        loading.style.display = "none";
        tbody.innerHTML = "";

        if (!data || data.length === 0) {
            table.style.display = "none";
            noResultDiv.style.display = "block";
            noResultDiv.querySelector("p").textContent = "Nessun film trovato con questo nome.";
        } else {
            noResultDiv.style.display = "none";
            table.style.display = "table";

            let rows = "";

            data.forEach(item => {
               
               
                const id          = item[0] ?? "N/D";
                const titolo      = item[1] || "Titolo non disponibile";
                const durata      = item[2] ?? null;      
                const descrizione = item[3] || "";
                const noleggio    = item[4] ?? null;      
                const acquisto    = item[5] ?? null;      
                const recensione  = item[6] ?? null;     

              
                rows += `
                    <tr>
                        <td class="col-id">${id}</td>
                        <td class="col-titolo">${titolo}</td>
                        <td class="col-durata">${renderDurata(durata)}</td>
                        <td class="col-descrizione">${descrizione || '<span style="color:#555;">—</span>'}</td>
                        <td class="col-prezzo">${renderPrezzo(noleggio)}</td>
                        <td class="col-prezzo">${renderPrezzo(acquisto)}</td>
                        <td class="col-recensione">${renderStelle(recensione)}</td>
                    </tr>
                `;
            });

            tbody.innerHTML = rows;
        }

    } catch (error) {
        console.error("Errore AJAX:", error);
        loading.style.display = "none";
        tbody.innerHTML = "";
        table.style.display = "none";
        noResultDiv.style.display = "block";
        noResultDiv.querySelector("p").textContent =
            "Errore di connessione al server. Controlla che Spring Boot sia in esecuzione.";
    }
}