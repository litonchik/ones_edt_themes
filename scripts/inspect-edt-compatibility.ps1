[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$out = Join-Path $projectRoot 'docs/testing/2026-10-30-compatibility/platform'
New-Item -ItemType Directory -Force -Path $out | Out-Null
foreach ($version in @('2026.1.2','2026.1.3','2026.2.2')) {
    $install = Get-ChildItem 'C:/Program Files/1C/1CE/components' -Directory -Filter "1c-edt-$version+*" | Select-Object -First 1
    $plugins = Join-Path $install.FullName 'plugins'
    $dest = Join-Path $out $version
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    Get-Content (Join-Path $install.FullName '1cedt.ini') | Set-Content (Join-Path $dest '1cedt.ini.txt')
    Get-Content (Join-Path $install.FullName 'configuration/org.eclipse.equinox.simpleconfigurator/bundles.info') | Set-Content (Join-Path $dest 'bundles.info.txt')
    foreach ($id in @('com._1c.g5.v8.dt.bsl.ui','com._1c.g5.v8.dt.theming.ui','com._1c.g5.v8.dt.ql.ui','com._1c.g5.v8.dt.ql.dcs.ui','org.eclipse.ui.editors')) {
        $jar = Get-ChildItem $plugins -Filter ($id+'_*.jar') | Select-Object -First 1
        $zip = [IO.Compression.ZipFile]::OpenRead($jar.FullName)
        try {
            foreach($entry in $zip.Entries | Where-Object {$_.FullName -match '^css/dark/.*\.css$' -or $_.FullName -eq 'plugin.xml'}) {
                $reader = [IO.StreamReader]::new($entry.Open())
                try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
                if($entry.FullName -eq 'plugin.xml') {
                    $xml=[xml]$text
                    $points=@($xml.SelectNodes('/plugin/extension-point') | Where-Object {$_.id -like '*ModuleExtension'} | ForEach-Object {$id+'.'+$_.id+' | '+$_.schema})
                    [IO.File]::WriteAllText((Join-Path $dest ($id+'--extension-points.txt')),($points -join [Environment]::NewLine))
                } else {
                    [IO.File]::WriteAllText((Join-Path $dest ($id+'--'+$entry.FullName.Replace('/','_'))),$text)
                }
            }
        } finally { $zip.Dispose() }
        Write-Output "$version $($jar.Name)"
    }
}
foreach($version in @('2026.1.2','2026.2.2')) {
    foreach($file in Get-ChildItem (Join-Path $out '2026.1.3') -Filter '*.css') {
        $other=Join-Path (Join-Path $out $version) $file.Name
        Write-Output "$version $($file.Name) identical=$((Get-FileHash $file.FullName).Hash -eq (Get-FileHash $other).Hash)"
    }
}
