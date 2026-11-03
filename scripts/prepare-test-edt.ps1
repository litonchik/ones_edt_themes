[CmdletBinding()]
param([string]$EdtHome = 'C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64', [string]$Destination = '')
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$source = (Resolve-Path -LiteralPath $EdtHome).Path
if (-not $Destination) { $Destination = Join-Path $projectRoot '.work/edt-test' }
$destination = [IO.Path]::GetFullPath($Destination)
if (-not $destination.StartsWith($projectRoot + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase) -or $destination -eq $source) { throw 'Небезопасное назначение копии EDT' }
if (Test-Path -LiteralPath $destination) { throw "Копия уже существует: $destination. Автоматическое удаление запрещено." }
if (-not (Test-Path -LiteralPath (Join-Path $source '1cedt.ini'))) { throw 'Нет 1cedt.ini' }
New-Item -ItemType Directory -Path $destination -Force | Out-Null
# Перечисление используется для копирования всех дочерних элементов.
Get-ChildItem -LiteralPath $source -Force | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $destination -Recurse -Force }
$workspace = Join-Path $projectRoot '.work/workspace-probe'
New-Item -ItemType Directory -Force -Path $workspace | Out-Null
Write-Output "Копия: $destination"
Write-Output "Рабочая область: $workspace"
