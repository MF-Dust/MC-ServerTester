package com.servertest.mod;

import com.servertest.mod.core.EnvironmentProvider;
import java.util.HashMap;
import java.util.Map;

/**
 * Test utility for providing mock environment variables and system properties
 */
public class TestEnvironmentProvider implements EnvironmentProvider {
    
    private final Map<String, String> envVars = new HashMap<>();
    private final Map<String, String> properties = new HashMap<>();
    
    public TestEnvironmentProvider setEnv(String name, String value) {
        envVars.put(name, value);
        return this;
    }
    
    public TestEnvironmentProvider setProperty(String name, String value) {
        properties.put(name, value);
        return this;
    }
    
    @Override
    public String getenv(String name) {
        return envVars.get(name);
    }
    
    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }
    
    // Predefined providers for common test scenarios
    public static TestEnvironmentProvider githubActions() {
        return new TestEnvironmentProvider()
            .setEnv("GITHUB_ACTIONS", "true")
            .setEnv("CI", "true")
            .setProperty("java.awt.headless", "true");
    }
    
    public static TestEnvironmentProvider jenkins() {
        return new TestEnvironmentProvider()
            .setEnv("CI", "true")
            .setEnv("JENKINS_URL", "http://jenkins.example.com")
            .setProperty("java.awt.headless", "true");
    }
    
    public static TestEnvironmentProvider development() {
        return new TestEnvironmentProvider();
    }
}