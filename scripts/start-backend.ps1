$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDirectory = Join-Path $projectRoot "backend-java"

function Test-Java21 {
    param([string]$JavaExecutable)

    if (-not (Test-Path -LiteralPath $JavaExecutable)) {
        return $false
    }

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $version = (& $JavaExecutable -version 2>&1 | Out-String)
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    return $version -match 'version\s+"21(?:\.|")'
}

$javaCandidates = [System.Collections.Generic.List[string]]::new()

if ($env:JAVA_HOME) {
    $javaCandidates.Add((Join-Path $env:JAVA_HOME "bin\java.exe"))
}

$javaCandidates.Add("C:\Program Files\Zulu\zulu-21\bin\java.exe")

@(
    "C:\Program Files\Zulu",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Microsoft"
) | ForEach-Object {
    if (Test-Path -LiteralPath $_) {
        Get-ChildItem -LiteralPath $_ -Recurse -Filter "java.exe" -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match '\\bin\\java\.exe$' } |
            ForEach-Object { $javaCandidates.Add($_.FullName) }
    }
}

$javaExecutable = $javaCandidates |
    Select-Object -Unique |
    Where-Object { Test-Java21 $_ } |
    Select-Object -First 1

if (-not $javaExecutable) {
    throw "Java JDK 21 was not found. Install it with: winget install --id Azul.Zulu.21.JDK -e --source winget"
}

$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $javaExecutable)
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$windowsTrustStoreOption = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
if (-not $env:MAVEN_OPTS) {
    $env:MAVEN_OPTS = $windowsTrustStoreOption
}
elseif ($env:MAVEN_OPTS -notmatch "javax\.net\.ssl\.trustStoreType") {
    $env:MAVEN_OPTS = "$env:MAVEN_OPTS $windowsTrustStoreOption"
}

$mavenCommand = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
if (-not $mavenCommand) {
    $mavenCommand = Get-Command "mvn" -ErrorAction SilentlyContinue
}

$mavenCandidates = [System.Collections.Generic.List[string]]::new()

if ($mavenCommand) {
    $mavenCandidates.Add($mavenCommand.Source)
}

$jetBrainsDirectory = "C:\Program Files\JetBrains"
if (Test-Path -LiteralPath $jetBrainsDirectory) {
    Get-ChildItem -LiteralPath $jetBrainsDirectory -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object {
            $mavenCandidates.Add(
                (Join-Path $_.FullName "plugins\maven\lib\maven3\bin\mvn.cmd")
            )
        }
}

$mavenCache = Join-Path $env:USERPROFILE ".m2\wrapper\dists"
if (Test-Path -LiteralPath $mavenCache) {
    Get-ChildItem -LiteralPath $mavenCache -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending |
        ForEach-Object { $mavenCandidates.Add($_.FullName) }
}

$mavenExecutable = $mavenCandidates |
    Select-Object -Unique |
    Where-Object { Test-Path -LiteralPath $_ } |
    Select-Object -First 1

if (-not $mavenExecutable) {
    throw "Maven was not found. Install Maven or use the Maven bundled with IntelliJ IDEA."
}

$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/chessarbiter"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "postgres"

function Get-DotEnvValue {
    param([string]$Name)

    $envFile = Join-Path $projectRoot ".env"
    $escapedName = [regex]::Escape($Name)
    $line = Get-Content -LiteralPath $envFile -ErrorAction SilentlyContinue |
        Where-Object { $_ -match "^\s*$escapedName\s*=" } |
        Select-Object -First 1

    if (-not $line) {
        return $null
    }

    return ($line -replace "^\s*$escapedName\s*=\s*", "").Trim().Trim('"').Trim("'")
}

foreach ($name in @("AUTH_SECRET", "ADMIN_EMAIL", "ADMIN_PASSWORD")) {
    if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
        $value = Get-DotEnvValue $name
        if ($value) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

if (-not $env:AUTH_SECRET -or $env:AUTH_SECRET.Length -lt 32) {
    $env:AUTH_SECRET = "local-development-secret-change-me-1234567890"
}

Write-Host "Using Java: $javaExecutable"
Write-Host "Using Maven: $mavenExecutable"
Write-Host "Using PostgreSQL: localhost:5433/chessarbiter"

Push-Location $backendDirectory
try {
    & $mavenExecutable spring-boot:run
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

exit $exitCode
