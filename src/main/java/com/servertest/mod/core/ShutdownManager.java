package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;
import net.minecraft.server.MinecraftServer;

/**
 * Manages safe server shutdown after testing is complete
 */
public class ShutdownManager {
    
    /**
     * Schedule a safe server shutdown
     */
    public void scheduleShutdown(MinecraftServer server) {
        try {
            ServerTestMod.LOGGER.info("[SERVER-TEST] Scheduling server shutdown...");
            
            // Schedule shutdown on the server thread to ensure thread safety
            server.execute(() -> {
                try {
                    performShutdown(server);
                } catch (Exception e) {
                    ErrorHandler.handleCriticalError(
                        ErrorHandler.ErrorType.SHUTDOWN,
                        "Error during shutdown",
                        e
                    );
                }
            });
            
        } catch (Exception e) {
            ErrorHandler.handleCriticalError(
                ErrorHandler.ErrorType.SHUTDOWN,
                "Error scheduling shutdown",
                e
            );
        }
    }
    
    /**
     * Perform the actual server shutdown
     */
    private void performShutdown(MinecraftServer server) {
        ServerTestMod.LOGGER.info("[SERVER-TEST] Initiating server shutdown...");
        
        try {
            // Use the safe shutdown method
            server.halt(false);
            
            ServerTestMod.LOGGER.info("[SERVER-TEST] Server shutdown completed successfully");
            
            // Exit with success status
            System.exit(ErrorHandler.SUCCESS);
            
        } catch (Exception e) {
            ErrorHandler.handleNonCriticalError(
                ErrorHandler.ErrorType.SHUTDOWN,
                "Failed to shutdown server gracefully, attempting force shutdown",
                e
            );
            
            // Force shutdown as last resort
            System.exit(ErrorHandler.SHUTDOWN_ERROR);
        }
    }
    
    /**
     * Emergency shutdown method for critical errors
     */
    public void emergencyShutdown(String reason) {
        ErrorHandler.handleCriticalError(
            ErrorHandler.ErrorType.GENERAL,
            "Emergency shutdown triggered: " + reason,
            null
        );
    }
}