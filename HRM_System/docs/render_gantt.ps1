$ErrorActionPreference = "Stop"

$mmdPath = Join-Path $PSScriptRoot "project_plan_gantt.mmd"
$outPath = Join-Path $PSScriptRoot "project_plan_gantt.png"

if (-not (Test-Path -LiteralPath $mmdPath)) {
    throw "Mermaid source not found: $mmdPath"
}

$code = Get-Content -LiteralPath $mmdPath -Raw
$bytes = [System.Text.Encoding]::UTF8.GetBytes($code)
$b64 = [Convert]::ToBase64String($bytes).TrimEnd("=")
$b64 = $b64.Replace("+", "-").Replace("/", "_")

$url = "https://mermaid.ink/img/$b64"

Invoke-WebRequest -Uri $url -OutFile $outPath
Write-Host "Wrote PNG: $outPath"


