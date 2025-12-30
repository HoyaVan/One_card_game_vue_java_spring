# PowerShell script to compile and run Main.java with JavaFX support
# This script tries Maven first (easiest), then falls back to direct javac compilation

# Set JavaFX paths
$javafxPath = "$env:USERPROFILE\.m2\repository\org\openjfx"
$javafxControlsDir = "$javafxPath\javafx-controls\17.0.2"
$javafxGraphicsDir = "$javafxPath\javafx-graphics\17.0.2"
$javafxBaseDir = "$javafxPath\javafx-base\17.0.2"


# Use explicit JAR file paths in module path (Java 9+ supports this)
# Get absolute paths to avoid any path resolution issues
$javafxControlsJarAbs = (Resolve-Path "$javafxControlsDir\javafx-controls-17.0.2.jar" -ErrorAction Stop).Path
$javafxGraphicsJarAbs = (Resolve-Path "$javafxGraphicsDir\javafx-graphics-17.0.2.jar" -ErrorAction Stop).Path
$javafxBaseJarAbs = (Resolve-Path "$javafxBaseDir\javafx-base-17.0.2.jar" -ErrorAction Stop).Path

# Build module path using explicit JAR file paths (semicolon-separated for Windows)
$javafxModulePath = "$javafxControlsJarAbs;$javafxGraphicsJarAbs;$javafxBaseJarAbs"

Write-Host "Using JavaFX JAR files directly in module path" -ForegroundColor Gray

# JAR file paths for verification
$javafxControlsJar = "$javafxControlsDir\javafx-controls-17.0.2.jar"
$javafxGraphicsJar = "$javafxGraphicsDir\javafx-graphics-17.0.2.jar"
$javafxBaseJar = "$javafxBaseDir\javafx-base-17.0.2.jar"

# Check if JavaFX JARs exist
if (-not (Test-Path $javafxControlsJar)) {
    Write-Host "ERROR: JavaFX not found at: $javafxControlsJar" -ForegroundColor Red
    Write-Host "Please run 'mvn dependency:resolve' first to download dependencies." -ForegroundColor Yellow
    Write-Host "Or use Cursor's Run button instead (recommended)!" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $javafxGraphicsJar)) {
    Write-Host "ERROR: JavaFX Graphics not found at: $javafxGraphicsJar" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $javafxBaseJar)) {
    Write-Host "ERROR: JavaFX Base not found at: $javafxBaseJar" -ForegroundColor Red
    exit 1
}

Write-Host "Found JavaFX modules." -ForegroundColor Green

# Try Maven first (much easier and handles everything automatically)
$mvnCommand = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCommand) {
    Write-Host "Using Maven to compile and run (recommended)..." -ForegroundColor Green
    mvn compile exec:java
    exit $LASTEXITCODE
}

# Fall back to direct javac compilation
Write-Host "Maven not found. Using direct javac compilation..." -ForegroundColor Yellow
Write-Host "Module path: $javafxModulePath" -ForegroundColor Gray

# Try to list available modules to verify Java can see them
Write-Host "Verifying JavaFX modules are accessible..." -ForegroundColor Gray
$moduleList = java --module-path $javafxModulePath --list-modules 2>&1
if ($moduleList -match "javafx") {
    Write-Host "Found JavaFX modules: $($moduleList | Select-String 'javafx' | ForEach-Object { $_.Line })" -ForegroundColor Green
} else {
    Write-Host "WARNING: Could not verify JavaFX modules. Proceeding anyway..." -ForegroundColor Yellow
}

# Change to code directory
Push-Location code

try {
    # Compile all Java files with JavaFX module path
    # Need to add all JavaFX modules: base, graphics, and controls
    javac --module-path $javafxModulePath --add-modules javafx.base,javafx.graphics,javafx.controls `
        *.java `
        numbergame/*.java `
        onecardgame/*.java `
        onecardgame/cards/*.java `
        wordgame/*.java

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation successful!" -ForegroundColor Green
        Write-Host "Running Main..." -ForegroundColor Green
        Write-Host ""
        
        # Run with JavaFX module path - need all modules
        java --module-path $javafxModulePath --add-modules javafx.base,javafx.graphics,javafx.controls Main
    } else {
        Write-Host "Compilation failed! See errors above." -ForegroundColor Red
        Write-Host ""
        Write-Host "RECOMMENDED SOLUTIONS:" -ForegroundColor Yellow
        Write-Host "1. Use Cursor's Run button (easiest - handles everything automatically)" -ForegroundColor Yellow
        Write-Host "2. Install Maven and use: mvn compile exec:java" -ForegroundColor Yellow
        Write-Host "3. If you see 'duplicate module' errors, the directories contain both main and platform-specific JARs" -ForegroundColor Yellow
        exit 1
    }
} finally {
    Pop-Location
}
