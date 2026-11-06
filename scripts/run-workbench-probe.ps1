[CmdletBinding()]
param([ValidateSet('CandidateIcons','CandidateIconsRestart','CandidateSelectionReproduce','CandidateSelection','CandidateSelectionRestart','CandidateGitLightRestart','CandidateGitReproduce','CandidateGit','CandidateGitRestart','CandidateRulers','CandidateRulersRestart','CandidateUi','CandidateUiRestart','CandidateUiGallery','Candidate','CandidateRestart','CandidateSurfaces','CandidateAllSurfaces','CandidateGalleryPrepare','CandidateGallery','LightGalleryPrepare','LightGallery','Light','LightRestart','Semantic','NativeSwitch','LightSurfaces','Surfaces','GalleryPrepare','Gallery','Apply','Restore','Reconcile','Cleanup','FontApply','FontRestore','Preview','Palette','PaletteRestart','PaletteLayout','OneDark','OneDarkRestart','Dracula','DraculaRestart','GitHub','GitHubRestart','Winter','WinterRestart','WinterSeparators','WinterSeparatorsRestart','NightOwl','NightOwlRestart','Ayu','AyuRestart','Shades','ShadesRestart','Noctis','NoctisRestart','VisualStudio','VisualStudioRestart')][string]$Phase = 'Apply', [string]$JavaHome = 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full', [string]$EdtCopy = '', [string]$WorkRoot = '', [ValidatePattern('^[a-z][a-z0-9-]*$')][string]$ProfileId = 'winter-is-coming-light')
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $EdtCopy) { $EdtCopy = Join-Path $projectRoot '.work/edt-test' }
$copy = (Resolve-Path -LiteralPath $EdtCopy).Path
if (-not $copy.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Только тестовая копия внутри проекта' }
if (-not $WorkRoot) { $WorkRoot = Join-Path $projectRoot '.work' }
$WorkRoot = [IO.Path]::GetFullPath($WorkRoot)
if (-not $WorkRoot.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Только тестовые данные внутри проекта' }
$fragment = Join-Path $copy 'plugins/ru.litonchik.edt.themes.probe_0.1.0.jar'
if ($Phase -ne 'Reconcile' -and -not (Test-Path -LiteralPath $fragment)) { throw 'Сначала соберите test-only fragment по docs/testing/mvp-validation.md' }
$info = Join-Path $copy 'configuration/org.eclipse.equinox.simpleconfigurator/bundles.info'
if ($Phase -eq 'Reconcile' -and (Test-Path -LiteralPath $fragment)) {
    $backup = Join-Path $projectRoot ('.work/removed-probe-'+[Guid]::NewGuid().ToString('N')+'.jar')
    Move-Item -LiteralPath $fragment -Destination $backup
    $lines = Get-Content -LiteralPath $info | Where-Object {$_ -notmatch '^ru\.litonchik\.edt\.themes\.probe,'}
    $lines | Set-Content -LiteralPath $info -Encoding utf8
}
if ($Phase -ne 'Reconcile' -and -not (Select-String -LiteralPath $info -Pattern '^ru.litonchik.edt.themes.probe,' -Quiet)) {
    Add-Content -LiteralPath $info -Value 'ru.litonchik.edt.themes.probe,0.1.0,plugins/ru.litonchik.edt.themes.probe_0.1.0.jar,4,false'
}
$taskHome = Join-Path $WorkRoot 'test-home'
$taskAppData = Join-Path $WorkRoot 'test-appdata'
$taskLocalAppData = Join-Path $WorkRoot 'test-localappdata'
$taskTemp = Join-Path $WorkRoot 'jar-temp'
$images = Join-Path $WorkRoot 'probe-images'
New-Item -ItemType Directory -Force -Path $taskHome,$taskAppData,$taskLocalAppData,$taskTemp,$images | Out-Null
$saved = @{}
foreach ($name in @('APPDATA','LOCALAPPDATA','USERPROFILE','TEMP','TMP')) { $saved[$name] = [Environment]::GetEnvironmentVariable($name,'Process') }
try {
    $env:APPDATA = $taskAppData; $env:LOCALAPPDATA = $taskLocalAppData; $env:USERPROFILE = $taskHome; $env:TEMP = $taskTemp; $env:TMP = $taskTemp
    $ini = Get-Content -LiteralPath (Join-Path $copy '1cedt.ini')
    $vmIndex = [Array]::IndexOf($ini,'-vmargs')
    $vmArgs = $ini[($vmIndex+1)..($ini.Length-1)] | Where-Object {$_ -ne '-showsplash' -and $_ -notlike '-De1c.dt.monitoring.host=*'}
    $launcher = Get-ChildItem -LiteralPath (Join-Path $copy 'plugins') -Filter 'org.eclipse.equinox.launcher_*.jar' | Select-Object -First 1
    $report = Join-Path $WorkRoot ('runtime-'+$Phase.ToLowerInvariant()+'.txt')
    if (Test-Path -LiteralPath $report) { Remove-Item -LiteralPath $report }
    $fixture = Join-Path $projectRoot 'tests/fixtures/theme-sample.bsl'
    if ($Phase -in @('Light','LightRestart','Semantic','NativeSwitch')) { $fixture = Join-Path $projectRoot 'tests/fixtures/semantic-sample.bsl' }
    $application = if ($Phase -eq 'Reconcile') {'org.eclipse.equinox.p2.reconciler.application'} else {'ru.litonchik.edt.themes.probe.probe'}
    $arguments = @($vmArgs) + @("-Duser.home=$taskHome","-Djava.io.tmpdir=$taskTemp","-Djdk.net.unixdomain.tmpdir=$taskTemp","-Dedt.probe.phase=$($Phase.ToLowerInvariant())","-Dedt.probe.profile=$ProfileId","-Dedt.probe.report=$report","-Dedt.probe.images=$images","-Dedt.probe.fixture=$fixture",'-jar',$launcher.FullName,'-install',$copy,'-configuration',(Join-Path $copy 'configuration'),'-data',(Join-Path $WorkRoot 'workspace-probe'),'-nosplash','-clean','-application',$application,'-consolelog')
    & (Join-Path $JavaHome 'bin/java.exe') @arguments *> (Join-Path $WorkRoot ('runtime-'+$Phase.ToLowerInvariant()+'.log'))
    $exitCode = $LASTEXITCODE
    if (Test-Path -LiteralPath $report) { Get-Content -LiteralPath $report -Tail 20 }
    if ($exitCode -ne 0) { throw "Workbench probe: $exitCode. См. .work/runtime-$($Phase.ToLowerInvariant()).log" }
} finally { foreach ($name in $saved.Keys) { [Environment]::SetEnvironmentVariable($name,$saved[$name],'Process') } }
