$ErrorActionPreference = 'Stop'

$ips = @(Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue | Where-Object {
  $_.IPAddress -and $_.IPAddress -notlike '169.254*' -and $_.IPAddress -ne '127.0.0.1' -and $_.PrefixOrigin -ne 'WellKnown'
} | Select-Object -ExpandProperty IPAddress)

Write-Host "Abrir en PC:  http://localhost:8080/index.html"
if ($ips.Count -gt 0) {
  $ips | ForEach-Object { Write-Host "Abrir en celular: http://$($_):8080/index.html" }
} else {
  Write-Host "No se detectó IP local. Revisá que PC y celular estén en la misma red WiFi." 
}

Write-Host ""
Write-Host "Servidor web iniciado en 0.0.0.0:8080 (Ctrl+C para detener)"

if (Get-Command py -ErrorAction SilentlyContinue) {
  py -m http.server 8080 --bind 0.0.0.0
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
  python -m http.server 8080 --bind 0.0.0.0
} else {
  throw 'No se encontró Python. Instalá Python o usá otro servidor (ej: npx http-server).'
}

