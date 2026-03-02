function getCsrfToken() {
    return getCookie('XSRF-TOKEN');
}

document.addEventListener('DOMContentLoaded', function () {

    // Selezioniamo il form. Se hai un ID nell'HTML 
    const form = document.getElementById('loginForm'); 
    
    const usernameInput = document.getElementById('username'); 
    const passwordInput = document.getElementById('password');
    const loginResultDiv = document.getElementById('login-result'); 
    
    const API_URL = '/api/login'; 

    // --- FUNZIONE sendLoginRequest ---
    async function sendLoginRequest(username, password) {
        loginResultDiv.textContent = 'Autenticazione in corso...';
        loginResultDiv.style.color = 'orange';

        const params = new URLSearchParams();
        params.append('username', username);
        params.append('password', password); 

        const csrfToken = getCsrfToken();

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    // Cruciale per Spring Boot @RequestParam
                    'Content-Type': 'application/x-www-form-urlencoded', 
                    'X-XSRF-TOKEN': csrfToken
                },
                body: params
            });

            const resultText = await response.text(); 

            if (response.ok) {
                 if (resultText === 'success') {
                    loginResultDiv.textContent = '✅ Login riuscito! Accesso autorizzato.';
                    loginResultDiv.style.color = 'green';
                    window.location.href = 'catalogo.html';
                 } else { 
                    loginResultDiv.textContent = '❌ Credenziali non valide .';
                    loginResultDiv.style.color = 'red';
                 }
            } else {
                loginResultDiv.textContent = `❌ Errore del server (${response.status}).`;
                loginResultDiv.style.color = 'red';
            }

        } catch (error) {
            console.error('Errore di connessione o CORS:', error);
            loginResultDiv.textContent = '⚠️ Impossibile connettersi al server (Verifica porta 8080 e CORS).';
            loginResultDiv.style.color = 'red';
        }
    }


    // --- GESTIONE DELL'EVENTO SUBMIT POTENZIATA ---

    form.addEventListener('submit', function (event) {
        
        // 1. BLOCCO ASSOLUTO: Impedisce al browser di navigare verso l'action="/login"
        event.preventDefault();

        // 2. Controllo compilazione minimale
        if (!usernameInput.value || !passwordInput.value) {
            loginResultDiv.textContent = '⚠ Devi inserire username e password.';
            loginResultDiv.style.color = 'red';
            return;
        }

        // 3. Esegue la chiamata AJAX
        sendLoginRequest(usernameInput.value, passwordInput.value);
    });
});