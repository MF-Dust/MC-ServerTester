package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Monitors server startup and coordinates the testing sequence
 * Listens for ServerStartedEvent and ensures server is fully ready before testing
 */
public class ServerMonitor {
    private final EnvironmentDetector environmentDetector;
    private final InfoCollector infoCollector;
    private final ShutdownManager shutdownManager;
    private volatile boolean testSequenceExecuted = false;
    
    public ServerMonitor(EnvironmentDetector environmentDetector, 
                        InfoCollector infoCollector, 
                        ShutdownManager shutdownManager) {
        this.environmentDetector = environmentDetector;
        this.infoCollector = infoCollector;
        this.shutdownManager = shutdownManager;
        
        ServerTestMod.LOGGER.info("[SERVER-TEST] ServerMonitor initialized");
    }
    
    /**
     * Handle server started event - this is where the testing sequence begins
     * This event is fired when the server has fully started and is ready to accept connections
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // Prevent multiple executions
        if (testSequenceExecuted) {
            ServerTestMod.LOGGER.warn("[SERVER-TEST] Test sequence already executed, ignoring duplicate event");
            return;
        }
        
        try {
            ServerTestMod.LOGGER.info("[SERVER-TEST] ServerStartedEvent received - server startup completed");
            ServerTestMod.LOGGER.info("[SERVER-TEST] Environment: {}", environmentDetector.getEnvironmentInfo());
            
            // Verify server is actually ready
            if (isServerReady(event.getServer())) {
                testSequenceExecuted = true;
                ServerTestMod.LOGGER.info("[SERVER-TEST] Server readiness confirmed, beginning test sequence");
                
                // Execute the test sequence
                executeTestSequence(event.getServer());
            } else {
                ErrorHandler.handleCriticalError(
                    ErrorHandler.ErrorType.SERVER_STARTUP,
                    "Server not ready despite ServerStartedEvent",
                    null
                );
            }
            
        } catch (Exception e) {
            ErrorHandler.handleCriticalError(
                ErrorHandler.ErrorType.SERVER_STARTUP, 
                "Error during test sequence", 
                e
            );
        }
    }
    
    /**
     * Verify that the server is actually ready for testing
     * @param server The MinecraftServer instance
     * @return true if server is ready, false otherwise
     */
    private boolean isServerReady(MinecraftServer server) {
        try {
            // Check if server is running
            if (!server.isRunning()) {
                ServerTestMod.LOGGER.warn("[SERVER-TEST] Server is not running");
                return false;
            }
            
            // Check if server thread is alive
            if (!server.getRunningThread().isAlive()) {
                ServerTestMod.LOGGER.warn("[SERVER-TEST] Server thread is not alive");
                return false;
            }
            
            // Additional readiness checks can be added here
            ServerTestMod.LOGGER.debug("[SERVER-TEST] Server readiness checks passed");
            return true;
            
        } catch (Exception e) {
            ServerTestMod.LOGGER.error("[SERVER-TEST] Error checking server readiness: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Execute the complete test sequence
     * @param server The MinecraftServer instance
     */
    private void executeTestSequence(MinecraftServer server) {
        ServerTestMod.LOGGER.info("[SERVER-TEST] Starting information collection...");
        
        try {
            // Collect and output server information
            infoCollector.collectAndOutputServerInfo(server);
            
            ServerTestMod.LOGGER.info("[SERVER-TEST] Information collection completed successfully");
            
            // Schedule server shutdown
            shutdownManager.scheduleShutdown(server);
            
        } catch (Exception e) {
            ErrorHandler.handleCriticalError(
                ErrorHandler.ErrorType.INFO_COLLECTION,
                "Failed to collect server information",
                e
            );
        }
    }
    
    /**
     * Check if the test sequence has been executed
     * @return true if executed, false otherwise
     */
    public boolean isTestSequenceExecuted() {
        return testSequenceExecuted;
    }
    
    /**
     * Reset the test sequence execution flag (mainly for testing purposes)
     */
    public void resetTestSequenceFlag() {
        testSequenceExecuted = false;
    }
}