param(
    [ValidateSet("backup", "restore", "list")]
    [string]$Action = "backup",
    [string]$BackupDir = (Join-Path $env:USERPROFILE ".amsakkarrar-telegram\backups"),
    [string]$File = "",
    [string]$Destination = "",
    [switch]$Force
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Security
Add-Type -AssemblyName System.IO.Compression.FileSystem

$ConfDir = Join-Path $env:USERPROFILE ".amsakkarrar-telegram"
$SessionFile = Join-Path $ConfDir "user.session"
$ConfigFile = Join-Path $ConfDir "config.json"
$Scope = [System.Security.Cryptography.DataProtectionScope]::CurrentUser

function Get-BackupFiles {
    @("config.json", "user.session", "user.session-journal", "login.json") |
        ForEach-Object { Join-Path $ConfDir $_ } |
        Where-Object { Test-Path -LiteralPath $_ }
}

function New-TempZip {
    Join-Path ([System.IO.Path]::GetTempPath()) ("ams-tg-" + [guid]::NewGuid().ToString("N") + ".zip")
}

function Invoke-Backup {
    if (-not (Test-Path -LiteralPath $SessionFile)) {
        throw "No Telegram session found at $SessionFile. Run telegram_login.py first."
    }
    if (-not (Test-Path -LiteralPath $ConfigFile)) {
        throw "No Telegram config found at $ConfigFile."
    }

    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $target = Join-Path $BackupDir "telegram-login-$stamp.dpapi"
    $tempZip = New-TempZip
    $verifyZip = New-TempZip

    try {
        $files = Get-BackupFiles
        Compress-Archive -LiteralPath $files -DestinationPath $tempZip -CompressionLevel Optimal
        $plain = [System.IO.File]::ReadAllBytes($tempZip)
        $protected = [System.Security.Cryptography.ProtectedData]::Protect($plain, $null, $Scope)
        [System.IO.File]::WriteAllBytes($target, $protected)

        # Verify that the archive decrypts and contains the expected entries.
        $check = [System.Security.Cryptography.ProtectedData]::Unprotect(
            [System.IO.File]::ReadAllBytes($target), $null, $Scope)
        [System.IO.File]::WriteAllBytes($verifyZip, $check)
        $archive = [System.IO.Compression.ZipFile]::OpenRead($verifyZip)
        try {
            $names = $archive.Entries | ForEach-Object { $_.FullName }
        } finally {
            $archive.Dispose()
        }
        if ($names -notcontains "user.session") {
            throw "Backup verification failed: user.session is missing."
        }

        Write-Output "BACKUP_OK:$target"
        Write-Output "SIZE:$((Get-Item -LiteralPath $target).Length)"
        Write-Output "ENTRIES:$($names -join ',')"
    } finally {
        Remove-Item -LiteralPath $tempZip -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $verifyZip -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-Restore {
    $targetDir = if ($Destination) { $Destination } else { $ConfDir }
    if (-not $File) {
        $latest = Get-ChildItem -LiteralPath $BackupDir -Filter "*.dpapi" -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $latest) { throw "No backup found in $BackupDir." }
        $File = $latest.FullName
    }
    if (-not (Test-Path -LiteralPath $File)) {
        throw "Backup file not found: $File"
    }
    $targetSession = Join-Path $targetDir "user.session"
    if ((Test-Path -LiteralPath $targetSession) -and -not $Force) {
        throw "A session already exists at $targetSession. Re-run with -Force to overwrite it."
    }

    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
    $tempZip = New-TempZip
    try {
        $plain = [System.Security.Cryptography.ProtectedData]::Unprotect(
            [System.IO.File]::ReadAllBytes($File), $null, $Scope)
        [System.IO.File]::WriteAllBytes($tempZip, $plain)
        Expand-Archive -LiteralPath $tempZip -DestinationPath $targetDir -Force
        Write-Output "RESTORE_OK:$targetDir"
    } finally {
        Remove-Item -LiteralPath $tempZip -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-List {
    if (-not (Test-Path -LiteralPath $BackupDir)) {
        Write-Output "NO_BACKUPS:$BackupDir"
        return
    }
    Get-ChildItem -LiteralPath $BackupDir -Filter "*.dpapi" |
        Sort-Object LastWriteTime -Descending |
        ForEach-Object { Write-Output "$($_.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))`t$($_.Length)`t$($_.FullName)" }
}

switch ($Action) {
    "backup" { Invoke-Backup }
    "restore" { Invoke-Restore }
    "list" { Invoke-List }
}
