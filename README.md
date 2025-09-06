# Server Test Mod

A specialized Minecraft Forge mod designed for GitHub Actions automation. This mod automatically tests server startup, outputs TPS and mod information, then safely shuts down the server for CI/CD testing purposes.

## Features

- **Automatic CI Detection**: Only activates in GitHub Actions or other CI environments
- **Server Monitoring**: Monitors server startup completion
- **Information Collection**: Collects and outputs TPS and loaded mod information
- **Safe Shutdown**: Automatically shuts down the server after testing
- **Error Handling**: Proper error handling with appropriate exit codes

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.2.0+
- Java 17+

## Building

```bash
./gradlew build
```

## Usage in GitHub Actions

1. Add the built JAR to your server's mods folder
2. Start the server in your GitHub Actions workflow
3. The mod will automatically:
   - Detect the CI environment
   - Wait for server startup completion
   - Output server information
   - Shut down the server

## Project Structure

```
src/main/java/com/servertest/mod/
├── ServerTestMod.java              # Main mod class
├── core/
│   ├── EnvironmentDetector.java    # CI environment detection
│   ├── ServerMonitor.java          # Server lifecycle monitoring
│   ├── InfoCollector.java          # Information collection and output
│   └── ShutdownManager.java        # Safe server shutdown
└── model/
    ├── TestResult.java             # Test result data model
    └── ModInfo.java                # Mod information data model
```

## License

MIT License