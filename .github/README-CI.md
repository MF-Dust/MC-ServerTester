# Server Test Mod - CI Integration Guide

This document explains how to use the Server Test Mod in your GitHub Actions workflows.

## Overview

The Server Test Mod is designed specifically for GitHub Actions automation. When running in a CI environment, it will:

1. Detect that it's running in GitHub Actions (via environment variables)
2. Wait for the server to fully start
3. Collect and output TPS information
4. Collect and output loaded mod information
5. Safely shut down the server
6. Exit with appropriate status codes

## Quick Start

### 1. Add the mod to your project

Copy the `servertest-1.0.0.jar` file to your `mods/` directory or build it from source.

### 2. Use the provided workflow

Copy one of the provided workflow files to your `.github/workflows/` directory:

- `build-test.yml` - For building and unit testing only
- `test-server.yml` - For full server integration testing

### 3. Customize for your needs

Modify the workflow files to match your project structure and requirements.

## Environment Variables

The mod detects CI environments using these variables:
- `GITHUB_ACTIONS=true`
- `CI=true`

## Expected Output

When running successfully, you should see output like:

```
[SERVER-TEST] Environment detected: GitHub Actions
[SERVER-TEST] Server startup completed
[SERVER-TEST] TPS: 20.0 (Average tick time: 50.0ms)
[SERVER-TEST] Loaded Mods (3 total):
[SERVER-TEST] - minecraft (1.20.1)
[SERVER-TEST] - forge (47.4.0)
[SERVER-TEST] - servertest (1.0.0)
[SERVER-TEST] Server test completed successfully
[SERVER-TEST] Shutting down server...
```

## Troubleshooting

### Server doesn't start
- Check Java version (requires Java 17+)
- Verify Forge version compatibility
- Check server logs for startup errors

### Mod doesn't activate
- Ensure `GITHUB_ACTIONS=true` or `CI=true` is set
- Check that the mod is properly loaded
- Verify mod compatibility with Forge version

### Server doesn't shut down
- Check for infinite loops in other mods
- Verify no other processes are keeping the server alive
- Use timeout in your workflow (recommended: 300 seconds)

## Integration Examples

### Basic Integration Test
```yaml
- name: Test server with mod
  run: |
    export GITHUB_ACTIONS=true
    timeout 300 ./gradlew runServer
```

### With Log Analysis
```yaml
- name: Analyze server logs
  run: |
    if grep -q "[SERVER-TEST] Server test completed" run/logs/latest.log; then
      echo "✅ Test passed"
    else
      echo "❌ Test failed"
      exit 1
    fi
```

## Build Configuration

The mod includes these Gradle tasks for CI:

- `ciBuild` - Complete CI build with verification
- `verifyJar` - Verify JAR file integrity
- `test` - Run unit tests with CI-friendly output

## Supported Versions

- Minecraft: 1.20.1
- Forge: 47.4.0+
- Java: 17+

## License

MIT License - See LICENSE file for details.