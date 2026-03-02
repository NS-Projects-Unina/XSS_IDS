import sys
import json
import re
import subprocess

log_path = "C:\\Program Files (x86)\\ossec-agent\\active-response\\active-responses.log"
error_path = "C:\\Program Files (x86)\\ossec-agent\\active-response\\error.log"

try:
    alert_str = sys.stdin.readline()
    data = json.loads(alert_str)

    # L'alert è dentro parameters.alert
    alert = data.get('parameters', {}).get('alert', {})
    
    full_log = alert.get('full_log', '')

    with open(log_path, "a") as f:
        f.write(f"Full log ricevuto: {full_log}\n")

    username = None
    match = re.search(r'User:\s+(\S+)', full_log)
    if match:
        username = match.group(1)

    if username:
        url = f"https://localhost/api/admin/responses/block-user?username={username}"
        cmd = ["curl.exe", "-k", "-X", "POST", url]
        subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        with open(log_path, "a") as f:
            rule_id = alert.get('rule', {}).get('id', 'N/A')
            f.write(f"Bloccato utente: {username} | Rule ID: {rule_id}\n")
    else:
        with open(log_path, "a") as f:
            f.write(f"Username non trovato. Full log: {full_log}\n")

except Exception as e:
    with open(error_path, "a") as f:
        f.write(f"Errore: {str(e)}\n")