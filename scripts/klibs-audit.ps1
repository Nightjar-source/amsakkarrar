#requires -Version 5.1
<#
.SYNOPSIS
  فحص اعتماديات Kotlin Multiplatform مقابل أحدث إصدار مستقر عبر klibs.io MCP.

.DESCRIPTION
  يقرأ الحزم من gradle/libs.versions.toml ويستدعي أداة getLatestVersion
  على https://api.klibs.io/mcp ثم يعرض جدولًا بالإصدارات.

.EXAMPLE
  pwsh -File scripts/klibs-audit.ps1
#>
[CmdletBinding()]
param(
    [string]$TomlPath
)

$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

if (-not $TomlPath) {
    $TomlPath = Join-Path (Split-Path -Parent $PSScriptRoot) 'gradle/libs.versions.toml'
}

$endpoint = 'https://api.klibs.io/mcp'
$headers = @{ Accept = 'application/json, text/event-stream' }

if (-not (Test-Path $TomlPath)) {
    throw "لم يتم العثور على ملف الإصدارات: $TomlPath"
}

$packages = @()
foreach ($line in Get-Content $TomlPath) {
    if ($line -match '^\s*[\w.\-]+\s*=\s*\{\s*module\s*=\s*"([^":]+):([^"]+)"') {
        $packages += [pscustomobject]@{ Group = $Matches[1]; Artifact = $Matches[2] }
    }
}
$packages = $packages | Sort-Object Group, Artifact -Unique

if ($packages.Count -eq 0) {
    throw 'لم يتم استخراج أي حزمة من ملف الإصدارات.'
}

function Invoke-KlibsLatestVersion {
    param([string]$GroupId, [string]$ArtifactId)

    $payload = @{
        jsonrpc = '2.0'
        id      = 1
        method  = 'tools/call'
        params  = @{
            name      = 'getLatestVersion'
            arguments = @{ groupId = $GroupId; artifactId = $ArtifactId }
        }
    } | ConvertTo-Json -Depth 8 -Compress

    $response = Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers `
        -ContentType 'application/json' -Body $payload -TimeoutSec 30

    $text = $response.result.content[0].text
    return $text | ConvertFrom-Json
}

$rows = foreach ($pkg in $packages) {
    try {
        $result = Invoke-KlibsLatestVersion -GroupId $pkg.Group -ArtifactId $pkg.Artifact
        [pscustomobject]@{
            Package   = '{0}:{1}' -f $pkg.Group, $pkg.Artifact
            Latest    = if ($result.latestVersion) { $result.latestVersion.version } else { '-' }
            Stable    = if ($result.latestStableVersion) { $result.latestStableVersion.version } else { '(none)' }
            Kotlin    = if ($result.latestStableVersion) { $result.latestStableVersion.kotlinVersion } else { '-' }
            Found     = [bool]$result.packageFound
        }
    }
    catch {
        [pscustomobject]@{
            Package = '{0}:{1}' -f $pkg.Group, $pkg.Artifact
            Latest  = 'ERROR'
            Stable  = 'ERROR'
            Kotlin  = '-'
            Found   = $false
        }
    }
}

Write-Host ''
Write-Host ('klibs.io MCP audit - {0:yyyy-MM-dd HH:mm}' -f (Get-Date)) -ForegroundColor Cyan
Write-Host ('Packages: {0}' -f @($rows).Count)
Write-Host ''
$rows | Format-Table -AutoSize | Out-String -Width 200 | Write-Host

$missing = @($rows | Where-Object { -not $_.Found })
if ($missing.Count -gt 0) {
    Write-Host 'حزم غير مفهرسة على klibs (تحقق من الإحداثيات أو أنها ليست KMP):' -ForegroundColor Yellow
    $missing | ForEach-Object { Write-Host ('  - ' + $_.Package) }
}
