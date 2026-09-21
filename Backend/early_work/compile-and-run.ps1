Set-Location $PSScriptRoot
$buildDirectory = Join-Path $PSScriptRoot '.build'
$sourceDirectory = Join-Path $PSScriptRoot 'src\main\java'

if (Test-Path $buildDirectory) {
    Remove-Item $buildDirectory -Recurse -Force
}
New-Item -ItemType Directory -Path $buildDirectory | Out-Null

try {
    $sourceFiles = Get-ChildItem -Path $sourceDirectory -Recurse -Filter '*.java' -File |
        Select-Object -ExpandProperty FullName
    javac -d $buildDirectory $sourceFiles

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed." -ForegroundColor Red
        exit 1
    }

    java -cp $buildDirectory terminal_mode.OneCardGame
} finally {
    Remove-Item $buildDirectory -Recurse -Force -ErrorAction SilentlyContinue
}
