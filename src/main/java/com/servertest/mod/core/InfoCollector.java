package com.servertest.mod.core;

import com.servertest.mod.ServerTestMod;
import com.servertest.mod.model.TestResult;
import com.servertest.mod.model.ModInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects server information (TPS, mod list) and outputs it in a formatted way
 */
public class InfoCollector {
    
    /**
     * Collect all server information and output it
     */
    public void collectAndOutputServerInfo(MinecraftServer server) {
        try {
            // Collect TPS information
            double tps = calculateTPS(server);
            long tickTime = getAverageTickTime(server);
            
            // Collect mod information
            List<ModInfo> loadedMods = collectModInfo();
            
            // Create test result
            TestResult result = new TestResult(tps, tickTime, loadedMods, true, null);
            
            // Output the information
            outputTestResult(result);
            
        } catch (Exception e) {
            ErrorHandler.handleNonCriticalError(
                ErrorHandler.ErrorType.INFO_COLLECTION,
                "Error collecting server information",
                e
            );
            
            // Output error result
            TestResult errorResult = new TestResult(0.0, 0L, new ArrayList<>(), false, e.getMessage());
            outputTestResult(errorResult);
            
            // Re-throw to let caller handle critical failure
            throw new RuntimeException("Failed to collect server information", e);
        }
    }
    
    /**
     * Calculate current server TPS
     */
    private double calculateTPS(MinecraftServer server) {
        // Get the average tick time in nanoseconds
        float avgTickTime = server.getAverageTickTime();
        
        // Convert to TPS (20 TPS = 50ms per tick = 50,000,000 nanoseconds per tick)
        double tps = 1000000000.0 / avgTickTime;
        
        // Cap at 20 TPS (perfect performance)
        return Math.min(tps, 20.0);
    }
    
    /**
     * Get average tick time in milliseconds
     */
    private long getAverageTickTime(MinecraftServer server) {
        // Convert from nanoseconds to milliseconds
        return (long) (server.getAverageTickTime() / 1000000.0f);
    }
    
    /**
     * Collect information about all loaded mods
     */
    private List<ModInfo> collectModInfo() {
        List<ModInfo> modInfoList = new ArrayList<>();
        
        for (IModInfo modInfo : ModList.get().getMods()) {
            ModInfo info = new ModInfo(
                modInfo.getModId(),
                modInfo.getVersion().toString(),
                modInfo.getDisplayName()
            );
            modInfoList.add(info);
        }
        
        return modInfoList;
    }
    
    /**
     * Output the test result in a formatted way
     */
    private void outputTestResult(TestResult result) {
        if (result.isSuccess()) {
            // Output TPS information
            ServerTestMod.LOGGER.info("[SERVER-TEST] TPS: {} (Average tick time: {}ms)", 
                String.format("%.1f", result.getTps()), 
                result.getTickTime());
            
            // Output mod information
            ServerTestMod.LOGGER.info("[SERVER-TEST] Loaded Mods ({} total):", result.getLoadedMods().size());
            
            for (ModInfo modInfo : result.getLoadedMods()) {
                ServerTestMod.LOGGER.info("[SERVER-TEST] - {} ({})", 
                    modInfo.getDisplayName(), 
                    modInfo.getVersion());
            }
        } else {
            ServerTestMod.LOGGER.error("[SERVER-TEST] Test failed: {}", result.getErrorMessage());
        }
    }
}