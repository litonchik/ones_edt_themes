[CmdletBinding()]
param(
    [ValidateSet('Check','Test','Verify')][string]$Action = 'Verify',
    [string]$MavenHome = 'C:\apps\apache-maven-3.9.11',
    [string]$JavaHome = 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full',
    [string]$EdtHome = 'C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64'
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$maven = Join-Path $MavenHome 'bin/mvn.cmd'
$java = Join-Path $JavaHome 'bin/java.exe'
foreach ($required in @($maven,$java,(Join-Path $JavaHome 'bin/javac.exe'),(Join-Path $EdtHome '.eclipseproduct'))) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Не найден обязательный файл: $required" }
}
$javaVersion = (& $java -version 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'version "17\.') { throw "Нужна Java 17: $javaVersion" }
$taskTemp = Join-Path $projectRoot '.work/java-temp'
New-Item -ItemType Directory -Force -Path $taskTemp | Out-Null
$saved = @{}
foreach ($name in @('JAVA_HOME','MAVEN_BASEDIR','MAVEN_SKIP_RC','MAVEN_OPTS')) { $saved[$name] = [Environment]::GetEnvironmentVariable($name,'Process') }
try {
    $env:JAVA_HOME = $JavaHome
    $env:MAVEN_BASEDIR = $projectRoot
    $env:MAVEN_SKIP_RC = 'true'
    $env:MAVEN_OPTS = $saved['MAVEN_OPTS'] + ' -Djava.io.tmpdir="' + $taskTemp + '" -Djdk.net.unixdomain.tmpdir="' + $taskTemp + '"'
    if ($Action -eq 'Check') {
        & $maven --version
        if ($LASTEXITCODE -ne 0) { throw "Maven Check: $LASTEXITCODE" }
        Write-Output "Target: $EdtHome"
        return
    }
    $cache = Join-Path $projectRoot '.cache/m2'
    $work = Join-Path $projectRoot '.work'
    New-Item -ItemType Directory -Force -Path $cache,$work | Out-Null
    $common = @('--batch-mode','--no-transfer-progress',"-Dmaven.repo.local=$cache",'-Dtycho.localArtifacts=ignore')
    $unit = Join-Path $projectRoot 'tests/unit/pom.xml'
    if (Test-Path -LiteralPath $unit) {
        & $maven @common -f $unit clean test
        if ($LASTEXITCODE -ne 0) { throw "Unit tests: $LASTEXITCODE" }
    } elseif ($Action -eq 'Test') { throw 'Unit module ещё не создан' }
    if ($Action -eq 'Test') { return }
    $target = Join-Path $work 'edt-2026.1.target'
    $escapedHome = [System.Security.SecurityElement]::Escape([IO.Path]::GetFullPath($EdtHome))
    $template = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'targets/edt-2026.1.target')
    [IO.File]::WriteAllText($target,$template.Replace('@EDT_HOME@',$escapedHome),[Text.UTF8Encoding]::new($false))
    & $maven @common -f (Join-Path $projectRoot 'pom.xml') "-Dedt.target=$target" clean verify
    if ($LASTEXITCODE -ne 0) { throw "Tycho Verify: $LASTEXITCODE" }
    $archive = Join-Path $projectRoot 'repositories/ru.litonchik.edt.themes.repository/target/ru.litonchik.edt.themes.repository-0.1.28-SNAPSHOT.zip'
    & (Join-Path $PSScriptRoot 'verify-repository.ps1') -Zip $archive
    $dist = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Force -Path $dist | Out-Null
    $release = Join-Path $dist 'edt-themes-0.1.28.zip'
    Copy-Item -LiteralPath $archive -Destination $release -Force
    $hash = (Get-FileHash -LiteralPath $release -Algorithm SHA256).Hash.ToLowerInvariant()
    [IO.File]::WriteAllText($release+'.sha256',$hash+'  edt-themes-0.1.28.zip'+[Environment]::NewLine,[Text.UTF8Encoding]::new($false))
    Write-Output "ZIP: $release"
} finally {
    foreach ($name in $saved.Keys) { [Environment]::SetEnvironmentVariable($name,$saved[$name],'Process') }
}
