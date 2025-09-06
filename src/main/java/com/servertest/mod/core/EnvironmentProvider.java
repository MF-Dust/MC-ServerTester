package com.servertest.mod.core;

/**
 * Interface for accessing environment variables and system properties
 * Allows for testing by providing a mockable interface
 */
public interface EnvironmentProvider {
    /**
     * Get an environment variable
     * @param name The name of the environment variable
     * @return The value of the environment variable, or null if not set
     */
    String getenv(String name);
    
    /**
     * Get a system property
     * @param name The name of the system property
     * @return The value of the system property, or null if not set
     */
    String getProperty(String name);
    
    /**
     * Default implementation that uses System.getenv() and System.getProperty()
     */
    class DefaultEnvironmentProvider implements EnvironmentProvider {
        @Override
        public String getenv(String name) {
            return System.getenv(name);
        }
        
        @Override
        public String getProperty(String name) {
            return System.getProperty(name);
        }
    }
}