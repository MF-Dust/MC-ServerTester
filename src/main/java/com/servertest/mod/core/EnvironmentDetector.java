package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;

/**
 * Detects whether the mod is running in a CI environment (GitHub Actions)
 */
public class EnvironmentDetector {
    
    private final EnvironmentProvider environmentProvider;
    
    /**
     * Create a new EnvironmentDetector with the default environment provider
     */
    public EnvironmentDetector() {
        this(new EnvironmentProvider.DefaultEnvironmentProvider());
    }
    
    /**
     * Create a new EnvironmentDetector with a custom environment provider
     * @param environmentProvider The environment provider to use
     */
    public EnvironmentDetector(EnvironmentProvider environmentProvider) {
        this.environmentProvider = environmentProvider;
    }
    
    /**
     * Check if the current environment is a CI environment
     * @return true if running in GitHub Actions or other CI environment
     */
    public boolean isInCIEnvironment() {
        // Check for GitHub Actions environment variable
        String githubActions = environmentProvider.getenv("GITHUB_ACTIONS");
        if ("true".equalsIgnoreCase(githubActions)) {
            ServerTestMod.LOGGER.debug("[SERVER-TEST] GitHub Actions environment detected");
            return true;
        }
        
        // Check for generic CI environment variable
        String ci = environmentProvider.getenv("CI");
        if ("true".equalsIgnoreCase(ci)) {
            ServerTestMod.LOGGER.debug("[SERVER-TEST] CI environment detected");
            return true;
        }
        
        // Check if running in headless mode (common in CI)
        String headless = environmentProvider.getProperty("java.awt.headless");
        if ("true".equalsIgnoreCase(headless)) {
            ServerTestMod.LOGGER.debug("[SERVER-TEST] Headless mode detected");
            return true;
        }
        
        return false;
    }
    
    /**
     * Get information about the detected environment
     * @return String describing the environment
     */
    public String getEnvironmentInfo() {
        if (isInCIEnvironment()) {
            StringBuilder info = new StringBuilder("CI Environment detected: ");
            
            if ("true".equalsIgnoreCase(environmentProvider.getenv("GITHUB_ACTIONS"))) {
                info.append("GitHub Actions");
            } else if ("true".equalsIgnoreCase(environmentProvider.getenv("CI"))) {
                info.append("Generic CI");
            } else {
                info.append("Headless mode");
            }
            
            return info.toString();
        }
        
        return "Development environment";
    }
}