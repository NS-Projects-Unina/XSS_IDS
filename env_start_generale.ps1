$KeycloakPath = "path_to_keycloak"  #percorso fino alla cartella keycloak con all'interno la cartella bin

$VaultPath = "path_to_vault"

$NginxPath= "path_to_nginx"

$KcCommand= "cd '$KeycloakPath'; .\bin\kc.bat start-dev --http-port=8180 --http-host=127.0.0.1 --proxy-headers=xforwarded --hostname-strict=false --log file --log-level=info --log-level-org.keycloak.events=debug" 
             
Start-Process powershell -ArgumentList "-NoExit", "-Command", $KcCommand


# --- Avvio Vault ---
Write-Host "Avvio di Vault in una nuova finestra..." -ForegroundColor Cyan
$env:VAULT_ADDR="http://127.0.0.1:8200"

$VaultCommand = "
    `$env:VAULT_ADDR = '$VAULT_ADDR';
    `$env:VAULT_MYSQL_PASSWORD = 'PASSWORD DA INSERIRE'; 
    cd '$VaultPath'; 
    .\vault server -config='$VaultPath'
"

Start-Process powershell -ArgumentList "-NoExit", "-Command", $VaultCommand

# --- Avvio Nginx ---
Write-Host "Avvio di Nginx..." -ForegroundColor Cyan
# Nginx preferisce essere avviato con 'start nginx' per gestire correttamente i processi worker
$NginxCommand = "cd '$NginxPath'; start nginx"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $NginxCommand

Write-Host " Impostazione variabili d'ambiente..." -ForegroundColor Green
$env:SPRING_CLOUD_VAULT_APP_ROLE_ROLE_ID = "ROLE_ID"
$env:SPRING_CLOUD_VAULT_APP_ROLE_SECRET_ID = "ROLE_SECRET_ID"     #queste variabili d'ambiente servono per poter collegare l'app a vault e prendere i segreti

# --- Verifica ---
Write-Host "Ambiente pronto!" -ForegroundColor Green
Write-Host "Role ID: $($env:SPRING_CLOUD_VAULT_APP_ROLE_ROLE_ID)"
Write-Host "Secret ID: $($env:SPRING_CLOUD_VAULT_APP_ROLE_SECRET_ID)"
Write-Host "Ora puoi avviare la tua applicazione in questa finestra." -ForegroundColor Yellow