package com.servertest.mod.core;

/**
 * Interface for handling system exit operations
 * Allows for testing by providing a mockable interface
 */
public interface SystemExitHandler {
    /**
     * Exit the system with the given status code
     * @param statusCode The exit status code
     */
    void exit(int statusCode);
    
    /**
     * Default implementation that calls System.exit()
     */
    class DefaultSystemExitHandler implements SystemExitHandler {
        @Override
        public void exit(int statusCode) {
            System.exit(statusCode);
        }
    }
}