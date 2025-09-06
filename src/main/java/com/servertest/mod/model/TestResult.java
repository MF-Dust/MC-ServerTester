package com.servertest.mod.model;

import java.util.List;

/**
 * Data model representing the result of a server test
 */
public class TestResult {
    private final double tps;
    private final long tickTime;
    private final List<ModInfo> loadedMods;
    private final boolean success;
    private final String errorMessage;
    
    public TestResult(double tps, long tickTime, List<ModInfo> loadedMods, boolean success, String errorMessage) {
        this.tps = tps;
        this.tickTime = tickTime;
        this.loadedMods = loadedMods;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Get the server TPS
     * @return TPS value
     */
    public double getTps() {
        return tps;
    }
    
    /**
     * Get the average tick time in milliseconds
     * @return tick time in ms
     */
    public long getTickTime() {
        return tickTime;
    }
    
    /**
     * Get the list of loaded mods
     * @return list of ModInfo objects
     */
    public List<ModInfo> getLoadedMods() {
        return loadedMods;
    }
    
    /**
     * Check if the test was successful
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the error message if the test failed
     * @return error message or null if successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("TestResult{tps=%.1f, tickTime=%dms, modsLoaded=%d}", 
                tps, tickTime, loadedMods.size());
        } else {
            return String.format("TestResult{failed, error='%s'}", errorMessage);
        }
    }
}