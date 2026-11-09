[CmdletBinding()]
param([Parameter(Mandatory)][string]$Zip)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Zip).Path)
try {
    $plugins = @($archive.Entries | Where-Object {$_.FullName -match '^plugins/.+\.jar$'})
    $features = @($archive.Entries | Where-Object {$_.FullName -match '^features/.+\.jar$'})
    if ($plugins.Count -ne 1 -or $plugins[0].FullName -notmatch '^plugins/ru\.litonchik\.edt\.themes_0\.1\.30\.[^/]+\.jar$') { throw 'В ZIP должны быть только один собственный bundle и одна feature' }
    if ($features.Count -ne 1 -or $features[0].FullName -notmatch '^features/ru\.litonchik\.edt\.themes\.feature_0\.1\.30\.[^/]+\.jar$') { throw 'Неверная feature' }
    foreach($name in @('content.jar','artifacts.jar')) { if(-not $archive.GetEntry($name)) { throw "Нет p2 metadata: $name" } }
    $memory = [IO.MemoryStream]::new(); $input = $plugins[0].Open(); $input.CopyTo($memory); $input.Dispose(); $memory.Position = 0
    $bundle = [IO.Compression.ZipArchive]::new($memory,[IO.Compression.ZipArchiveMode]::Read)
    try {
        if ($bundle.Entries.FullName -match '/testing/') { throw 'Test-only код попал в release' }
        $classes = @($bundle.Entries | Where-Object {$_.FullName -like '*.class'})
        if (-not $classes.Count) { throw 'Нет Java classes' }
        foreach ($entry in $classes) {
            $stream = $entry.Open(); $bytes = [byte[]]::new(8); $read = $stream.Read($bytes,0,8); $stream.Dispose()
            if ($read -ne 8 -or ($bytes[6]*256+$bytes[7]) -ne 61) { throw "Неверный class-file major: $($entry.FullName)" }
        }
        foreach ($name in @('resources/palettes/darcula.properties','resources/palettes/monokai.properties','resources/palettes/one-dark-pro.properties','resources/palettes/dracula.properties','resources/palettes/github-dark-default.properties','resources/palettes/github-light-default.properties','resources/palettes/winter-is-coming-light.properties','resources/edt-2026.1-light-css-colors.properties','resources/edt-2026.1-light-native-preferences.properties','resources/palettes/winter-is-coming-dark-blue.properties','resources/palettes/night-owl.properties','resources/palettes/night-owl-light.properties','resources/palettes/ayu-dark.properties','resources/palettes/ayu-light.properties','resources/palettes/shades-of-purple.properties','resources/palettes/noctis-minimus.properties','resources/palettes/noctis-lux.properties','resources/palettes/noctis-hibernus.properties','resources/palettes/noctis-lilac.properties','resources/palettes/visual-studio-dark-cpp.properties','resources/palettes/visual-studio-light-cpp.properties','resources/third-party-notices.txt','resources/licenses/edt-themes-LICENSE.txt','resources/licenses/one-dark-pro-LICENSE.txt','resources/licenses/dracula-LICENSE.txt','resources/licenses/github-theme-LICENSE.txt','resources/licenses/primer-primitives-LICENSE.txt','resources/licenses/winter-is-coming-LICENSE.txt','resources/licenses/night-owl-LICENSE.txt','resources/licenses/ayu-LICENSE.txt','resources/licenses/shades-of-purple-LICENSE.txt','resources/licenses/noctis-LICENSE.txt','resources/licenses/visual-studio-dark-cpp-LICENSE.txt','resources/edt-2026.1-settings.properties','resources/edt-2026.1-native-preferences.properties','resources/chroma-icons.properties','resources/icons/chroma/source.json','resources/licenses/v8-chroma-LICENSE.txt','resources/icons/chroma/png/light/16/meta-catalog.png','resources/icons/chroma/png/dark/32/meta-catalog.png','resources/icons/update.png','resources/icons/update@2x.png','resources/icons/update.svg','plugin.xml')) { if(-not $bundle.GetEntry($name)) { throw "Нет ресурса: $name" } }
        Write-Output "ZIP: собственные bundle/feature; $($classes.Count) classes, major 61; p2 metadata, девятнадцать палитр и MIT notices палитр, v8-chroma и проекта присутствуют."
    } finally { $bundle.Dispose(); $memory.Dispose() }
} finally { $archive.Dispose() }
