[CmdletBinding()]
param(
    [ValidateSet('Install','Uninstall','Audit')][string]$Action = 'Install',
    [string]$Zip = '',
    [string]$RepositoryUrl = '',
    [string]$EdtCopy = '',
    [string]$JavaHome = 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full',
    [string]$WorkRoot = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $WorkRoot) { $WorkRoot = Join-Path $projectRoot '.work' }
$WorkRoot = [IO.Path]::GetFullPath($WorkRoot)
if (-not $WorkRoot.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Только тестовые данные внутри проекта' }
if (-not $EdtCopy) { $EdtCopy = Join-Path $projectRoot '.work/edt-test' }
$copy = (Resolve-Path -LiteralPath $EdtCopy).Path
if (-not $copy.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Установка разрешена только в тестовую копию внутри проекта' }
$launcher = Get-ChildItem -LiteralPath (Join-Path $copy 'plugins') -Filter 'org.eclipse.equinox.launcher_*.jar' | Select-Object -First 1
$framework = Get-ChildItem -LiteralPath (Join-Path $copy 'plugins') -Filter 'org.eclipse.osgi_*.jar' | Select-Object -First 1
$configurator = Get-ChildItem -LiteralPath (Join-Path $copy 'plugins') -Filter 'org.eclipse.equinox.simpleconfigurator_*.jar' | Select-Object -First 1
$work = Join-Path $projectRoot ('.work/p2-'+[Guid]::NewGuid().ToString('N').Substring(0,8))
$config = Join-Path $work 'configuration'
$taskTemp = Join-Path $projectRoot '.work/jar-temp'
$testHome = Join-Path $projectRoot '.work/test-home'
New-Item -ItemType Directory -Force -Path (Join-Path $config 'org.eclipse.equinox.simpleconfigurator'),$taskTemp,$testHome | Out-Null
$info = Join-Path $copy 'configuration/org.eclipse.equinox.simpleconfigurator/bundles.info'
$before = @(Get-Content -LiteralPath $info | Where-Object {$_ -notmatch '^#|^ru\.litonchik\.edt\.themes' } | ForEach-Object { ($_ -split ',')[0..1] -join ',' })
$pluginRoot = Join-Path $copy 'plugins'
$minimal = Get-Content -LiteralPath $info | Where-Object {$_ -notmatch '^#|^(com\._1c|com\.e1c|com\.1c|com\.company1c|ru\.litonchik)' -and $_ -notmatch '\.source,|\.nl_ru,' } | ForEach-Object {
    $fields = $_ -split ','
    if ($fields.Length -eq 5) {
        $fields[2] = ([uri][IO.Path]::Combine($pluginRoot,[IO.Path]::GetFileName($fields[2]))).AbsoluteUri
        $fields[4] = if ($fields[0] -match '^org\.eclipse\.equinox\.(common|simpleconfigurator|event|app|registry)$' -or $fields[0] -eq 'org.apache.felix.scr') {'true'} else {'false'}
        $fields -join ','
    }
}
@('#encoding=UTF-8','#version=1')+$minimal | Set-Content -Encoding utf8 (Join-Path $config 'org.eclipse.equinox.simpleconfigurator/bundles.info')
@(
    ('osgi.bundles=reference:'+([uri]$configurator.FullName).AbsoluteUri+'@1:start'),
    'org.eclipse.equinox.simpleconfigurator.configUrl=file:org.eclipse.equinox.simpleconfigurator/bundles.info',
    'osgi.bundles.defaultStartLevel=4',
    ('osgi.framework='+([uri]$framework.FullName).AbsoluteUri)
) | Set-Content -Encoding utf8 (Join-Path $config 'config.ini')
$arguments = @("-Duser.home=$testHome","-Djava.io.tmpdir=$taskTemp","-Djdk.net.unixdomain.tmpdir=$taskTemp",'-Xms80m','-Xmx4096m','-jar',$launcher.FullName,'-install',$copy,'-configuration',$config,'-data',(Join-Path $work 'workspace'),'-nosplash','-application','org.eclipse.equinox.p2.director','-destination',$copy,'-profile','DefaultProfile','-bundlepool',$copy,'-roaming','-p2.os','win32','-p2.ws','win32','-p2.arch','x86_64','-consolelog')
if ($Action -eq 'Install') {
    if ($RepositoryUrl) {
        if ($Zip) { throw 'Укажите только Zip или RepositoryUrl' }
        $siteUri = [uri]$RepositoryUrl
        if (-not $siteUri.IsAbsoluteUri -or $siteUri.Scheme -ne 'https') { throw 'Нужен абсолютный HTTPS URL p2-репозитория' }
        $repository = $siteUri.AbsoluteUri
    } else {
        if (-not $Zip) { $Zip = Join-Path $projectRoot 'dist/edt-themes-0.1.28.zip' }
        $zipPath = (Resolve-Path -LiteralPath $Zip).Path
        $repository = 'jar:'+([uri]$zipPath).AbsoluteUri+'!/'
    }
    $arguments += @('-repository',$repository,'-installIU','ru.litonchik.edt.themes.feature.feature.group')
    if (Select-String -LiteralPath $info -Pattern '^ru\.litonchik\.edt\.themes,' -Quiet) {
        $arguments += @('-uninstallIU','ru.litonchik.edt.themes.feature.feature.group')
    }
} elseif ($Action -eq 'Uninstall') {
    $remove = 'ru.litonchik.edt.themes.feature.feature.group'
    # A manually added fragment can become a locked dropins IU and retain its host.
    if (Select-String -LiteralPath $info -Pattern '^ru\.litonchik\.edt\.themes\.probe,' -Quiet) { throw 'Сначала выполните run-workbench-probe.ps1 -Phase Reconcile для удаления test-only fragment' }
    $arguments += @('-uninstallIU',$remove)
}
else { $arguments += '-listInstalledRoots' }
& (Join-Path $JavaHome 'bin/java.exe') @arguments *> (Join-Path $work 'director.log')
$exitCode = $LASTEXITCODE
Get-Content -LiteralPath (Join-Path $work 'director.log') -Tail 15
if ($exitCode -ne 0) { throw "p2 $Action завершился кодом $exitCode. Лог: $work/director.log" }
$after = @(Get-Content -LiteralPath $info | Where-Object {$_ -notmatch '^#|^ru\.litonchik\.edt\.themes' } | ForEach-Object { ($_ -split ',')[0..1] -join ',' })
if (Compare-Object $before $after) { throw 'p2 изменил базовые bundles; результат нельзя принимать' }
if ($Action -ne 'Install') {
    if (Select-String -LiteralPath $info -Pattern '^ru\.litonchik\.edt\.themes,' -Quiet) { throw 'Bundle тем остался после uninstall' }
    $journalFile = Join-Path $WorkRoot 'workspace-probe/.metadata/.plugins/org.eclipse.core.runtime/.settings/ru.litonchik.edt.themes.prefs'
    if (Test-Path -LiteralPath $journalFile) {
        $lines = Get-Content -LiteralPath $journalFile
        if ($lines -match '^activeProfile=') { throw 'Профиль остался активным; сначала восстановите оформление' }
        $pointer = $lines | Where-Object {$_ -match '^journalSlot='} | Select-Object -First 1
        if ($pointer) {
            $slot = $pointer.Substring('journalSlot='.Length)
            if ($lines -notcontains "journal/$slot/count=0") { throw 'Журнал всё ещё содержит управляемые настройки' }
        }
        if ($lines -match '^fontOriginalEffective=') { throw 'Шрифт не восстановлен; сначала верните исходный шрифт на странице плагина' }
        $fontPointer = $lines | Where-Object {$_ -match '^font/journalSlot='} | Select-Object -First 1
        if ($fontPointer) {
            $fontSlot = $fontPointer.Substring('font/journalSlot='.Length)
            if ($lines -notcontains "font/journal/$fontSlot/count=0") { throw 'Журнал шрифта всё ещё содержит управляемые настройки' }
        }
    }
}
if ($Action -eq 'Audit' -and (Select-String -LiteralPath (Join-Path $work 'director.log') -Pattern '^ru\.litonchik\.edt\.themes\.feature\.feature\.group' -Quiet)) { throw 'Feature всё ещё установлена' }
Write-Output "p2 ${Action}: OK; базовые bundles сохранены; $copy"
