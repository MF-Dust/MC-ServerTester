# Validation script for the Server Test Mod JAR file

$ErrorActionPreference = "Stop"

$JAR_FILE = "build/libs/servertest-1.0.0.jar"

Write-Host "=== JAR Validation Script ===" -ForegroundColor Cyan

# Check if JAR file exists
if (-not (Test-Path $JAR_FILE)) {
    Write-Host "❌ JAR file not found: $JAR_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "✅ JAR file found: $JAR_FILE" -ForegroundColor Green

# Check JAR file size
$JAR_SIZE = (Get-Item $JAR_FILE).Length
Write-Host "📦 JAR size: $JAR_SIZE bytes" -ForegroundColor Yellow

if ($JAR_SIZE -lt 10240) {
    Write-Host "❌ JAR file seems too small (< 10KB)" -ForegroundColor Red
    exit 1
}

Write-Host "✅ JAR size is reasonable" -ForegroundColor Green

# Check JAR contents using Java jar command
Write-Host "📋 JAR contents:" -ForegroundColor Yellow
& jar -tf $JAR_FILE | Select-Object -First 20

# Verify essential files exist in JAR
$REQUIRED_FILES = @(
    "META-INF/mods.toml",
    "com/servertest/mod/ServerTestMod.class",
    "com/servertest/mod/core/EnvironmentDetector.class",
    "com/servertest/mod/core/ServerMonitor.class",
    "com/servertest/mod/core/InfoCollector.class",
    "com/servertest/mod/core/ShutdownManager.class"
)

$jarContents = & jar -tf $JAR_FILE

foreach ($file in $REQUIRED_FILES) {
    if ($jarContents -contains $file) {
        Write-Host "✅ Found: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ Missing: $file" -ForegroundColor Red
        exit 1
    }
}

# Check mods.toml content
Write-Host "📄 mods.toml content:" -ForegroundColor Yellow
& jar -xf $JAR_FILE META-INF/mods.toml
if (Test-Path "META-INF/mods.toml") {
    Get-Content "META-INF/mods.toml" | Select-Object -First 20
    Remove-Item -Recurse -Force META-INF -ErrorAction SilentlyContinue
    Write-Host "✅ mods.toml extracted and verified" -ForegroundColor Green
} else {
    Write-Host "❌ Could not extract mods.toml" -ForegroundColor Red
    exit 1
}

Write-Host "🎉 JAR validation completed successfully!" -ForegroundColor Green