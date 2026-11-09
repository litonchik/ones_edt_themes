[CmdletBinding()]
param([string]$JavaHome = 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full', [string]$EdtCopy = '')
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $EdtCopy) { $EdtCopy = Join-Path $projectRoot '.work/edt-test' }
$copy = (Resolve-Path -LiteralPath $EdtCopy).Path
if (-not $copy.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Только тестовая копия внутри проекта' }
$plugins = Join-Path $copy 'plugins'
if (-not (Get-ChildItem -LiteralPath $plugins -Filter 'ru.litonchik.edt.themes_*.jar')) { throw 'Сначала установите ZIP в тестовую копию через p2-test.ps1' }
$stage = Join-Path $projectRoot ('.work/probe-stage-'+[Guid]::NewGuid().ToString('N').Substring(0,8))
$taskTemp = Join-Path $projectRoot '.work/jar-temp'
New-Item -ItemType Directory -Force -Path $stage,$taskTemp | Out-Null
$classpath = (Get-ChildItem -LiteralPath $plugins -Filter '*.jar' | Where-Object {$_.Name -notmatch '(\.source_|\.nl_)'}).FullName -join ';'
$argsFile = Join-Path $stage 'javac.args'
$sources = @(Join-Path $projectRoot 'tests/workbench/WorkbenchProbe.java') + @(Join-Path $projectRoot 'tests/workbench/ProbePerspective.java')
@('--release','17','-encoding','UTF-8','-classpath',('"'+$classpath.Replace('\','/')+'"'),'-d',('"'+$stage.Replace('\','/')+'"')) + ($sources | ForEach-Object {'"'+$_.Replace('\','/')+'"'}) | Set-Content -Encoding utf8 $argsFile
& (Join-Path $JavaHome 'bin/javac.exe') ('@'+$argsFile)
if ($LASTEXITCODE -ne 0) { throw 'Probe javac failed' }
Remove-Item -LiteralPath $argsFile
$manifest = Join-Path $stage 'MANIFEST.MF'
$manifestLines = @('Manifest-Version: 1.0','Bundle-ManifestVersion: 2','Bundle-SymbolicName: ru.litonchik.edt.themes.probe;singleton:=true','Bundle-Version: 0.1.0','Fragment-Host: ru.litonchik.edt.themes','Bundle-RequiredExecutionEnvironment: JavaSE-17','Require-Bundle: org.eclipse.core.net,org.eclipse.core.commands,org.eclipse.equinox.p2.core,org.eclipse.equinox.p2.repository,org.eclipse.equinox.p2.metadata.repository,org.eclipse.equinox.p2.artifact.repository,org.eclipse.equinox.app,org.eclipse.ui.ide,org.eclipse.xtext.ui,org.eclipse.xtext,org.eclipse.emf.ecore,org.eclipse.jface.text,org.eclipse.e4.ui.model.workbench,org.eclipse.compare,org.eclipse.ui.navigator,org.eclipse.team.core,org.eclipse.egit.core,org.eclipse.egit.ui,org.eclipse.jgit,com._1c.g5.v8.dt.qw.ui,com._1c.g5.v8.dt.compare.ui','')
$wrappedManifest = foreach($line in $manifestLines) { while($line.Length -gt 70) { $line.Substring(0,70); $line = ' ' + $line.Substring(70) }; $line }
$wrappedManifest | Set-Content -Encoding utf8 $manifest
@'
<?xml version="1.0" encoding="UTF-8"?>
<?eclipse version="3.4"?>
<fragment>
 <extension point="org.eclipse.core.runtime.applications" id="ru.litonchik.edt.themes.probe.probe"><application cardinality="singleton-global" thread="main" visible="true"><run class="ru.litonchik.edt.themes.testing.WorkbenchProbe"/></application></extension>
 <extension point="org.eclipse.ui.perspectives"><perspective id="ru.litonchik.edt.themes.probe.perspective" name="Probe" class="ru.litonchik.edt.themes.testing.ProbePerspective"/><perspective id="ru.litonchik.edt.themes.probe.gallery" name="Palette gallery" class="ru.litonchik.edt.themes.testing.ProbePerspective$Gallery"/></extension>
 <extension point="org.eclipse.ui.perspectives"><perspective id="ru.litonchik.edt.themes.probe.separators" name="Separator probe" class="ru.litonchik.edt.themes.testing.ProbePerspective"/></extension>
 <extension point="org.eclipse.ui.views"><view id="ru.litonchik.edt.themes.probe.ui" name="UI fixture" class="ru.litonchik.edt.themes.testing.WorkbenchProbe$UiPanelView"/></extension>
 <extension point="org.eclipse.ui.editors"><editor id="ru.litonchik.edt.themes.probe.protected" name="Protected content" class="ru.litonchik.edt.themes.testing.WorkbenchProbe$ProtectedContentEditor"/></extension>
 <extension point="org.eclipse.ui.decorators"><decorator id="ru.litonchik.edt.themes.probe.iconOverlay" label="Icon status fixture" state="true" lightweight="true" class="ru.litonchik.edt.themes.testing.WorkbenchProbe$IconOverlay"><enablement><objectClass name="ru.litonchik.edt.themes.testing.WorkbenchProbe$IconRow"/></enablement></decorator></extension>
</fragment>
'@ | Set-Content -Encoding utf8 (Join-Path $stage 'fragment.xml')
& (Join-Path $JavaHome 'bin/jar.exe') "-J-Djava.io.tmpdir=$taskTemp" --create --file (Join-Path $plugins 'ru.litonchik.edt.themes.probe_0.1.0.jar') --manifest $manifest -C $stage .
if ($LASTEXITCODE -ne 0) { throw 'Probe jar failed' }
Write-Output 'Test-only fragment собран в копии EDT. В release он не включён.'
