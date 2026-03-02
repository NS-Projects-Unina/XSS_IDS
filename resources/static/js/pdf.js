// Aspettiamo che il DOM sia completamente caricato
document.addEventListener('DOMContentLoaded', () => {
    const btnExport = document.getElementById('btnExport');
    const inputStelle = document.getElementById('stelle');
    const resultContainer = document.getElementById('resultContainer');
    const resultDiv = document.getElementById('result');
    const loader = document.getElementById('loader');
 
    // Funzione per il click del pulsante 
    btnExport.addEventListener('click', async () => {
        const valoreStelle = inputStelle.value;
 
        // Reset interfaccia
        resultContainer.style.display = 'none';
        loader.style.display = 'block';
        resultDiv.innerHTML = "<i>Generazione catalogo in corso...</i>";
       
        try {
            // Chiamata diretta all'endpoint locale
            const url = `https://localhost/api/film/export/pdf?stelle=${encodeURIComponent(valoreStelle)}`;
            console.log("Inviando richiesta a:", url);
 
            const response = await fetch(url);
 
            if (response.ok) {
                const data = await response.text();
                // Iniettiamo la risposta del server
                resultDiv.innerHTML = data;
                resultContainer.style.display = 'block';
            } else {
                throw new Error(`Server status: ${response.status}`);
            }
 
        } catch (err) {
            console.error("Errore fetch dettagliato:", err);
            // Fallback in caso di errore
            resultDiv.innerHTML = `<b style="color:red;">Errore di connessione:</b><br>
                                   Assicurati che il server Spring Boot sia attivo su http://localhost:8080`;
            resultContainer.style.display = 'block';
        } finally {
            loader.style.display = 'none';
        }
    });
});