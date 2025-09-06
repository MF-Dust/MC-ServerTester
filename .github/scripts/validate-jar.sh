#!/bin/bash

# Validation script for the Server Test Mod JAR file

set -e

JAR_FILE="build/libs/servertest-1.0.0.jar"

echo "=== JAR Validation Script ==="

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo "✅ JAR file found: $JAR_FILE"

# Check JAR file size
JAR_SIZE=$(stat -c%s "$JAR_FILE" 2>/dev/null || stat -f%z "$JAR_FILE" 2>/dev/null || wc -c < "$JAR_FILE")
echo "📦 JAR size: $JAR_SIZE bytes"

if [ "$JAR_SIZE" -lt 10240 ]; then
    echo "❌ JAR file seems too small (< 10KB)"
    exit 1
fi

echo "✅ JAR size is reasonable"

# Check JAR contents
echo "📋 JAR contents:"
jar -tf "$JAR_FILE" | head -20

# Verify essential files exist in JAR
REQUIRED_FILES=(
    "META-INF/mods.toml"
    "com/servertest/mod/ServerTestMod.class"
    "com/servertest/mod/core/EnvironmentDetector.class"
    "com/servertest/mod/core/ServerMonitor.class"
    "com/servertest/mod/core/InfoCollector.class"
    "com/servertest/mod/core/ShutdownManager.class"
)

for file in "${REQUIRED_FILES[@]}"; do
    if jar -tf "$JAR_FILE" | grep -q "^$file$"; then
        echo "✅ Found: $file"
    else
        echo "❌ Missing: $file"
        exit 1
    fi
done

# Check mods.toml content
echo "📄 mods.toml content:"
jar -xf "$JAR_FILE" META-INF/mods.toml
if [ -f "META-INF/mods.toml" ]; then
    cat META-INF/mods.toml | head -20
    rm -rf META-INF
    echo "✅ mods.toml extracted and verified"
else
    echo "❌ Could not extract mods.toml"
    exit 1
fi

echo "🎉 JAR validation completed successfully!"