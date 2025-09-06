package com.servertest.mod;

import com.servertest.mod.core.EnvironmentDetector;
import com.servertest.mod.core.ServerMonitor;
import com.servertest.mod.core.InfoCollector;
import com.servertest.mod.core.ShutdownManager;
import com.servertest.mod.core.ErrorHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main mod class for Server Test Mod
 * Coordinates all components for automated server testing in CI environments
 */
@Mod(ServerTestMod.MODID)
public class ServerTestMod {
    public static final String MODID = "servertest";
    public static final Logger LOGGER = LogManager.getLogger();
    
    private static EnvironmentDetector environmentDetector;
    private static ServerMonitor serverMonitor;
    private static InfoCollector infoCollector;
    private static ShutdownManager shutdownManager;
    
    public ServerTestMod() {
        try {
            // Set up global exception handler
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
            
            // Register the setup method for modloading
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(this::setup);
            
            LOGGER.info("[SERVER-TEST] Server Test Mod initializing...");
        } catch (Exception e) {
            ErrorHandler.handleCriticalError(
                ErrorHandler.ErrorType.GENERAL,
                "Failed to initialize Server Test Mod",
                e
            );
        }
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        ErrorHandler.executeCriticalOperation(() -> {
            // Initialize core components
            environmentDetector = new EnvironmentDetector();
            ErrorHandler.validateNotNull(environmentDetector, "EnvironmentDetector", ErrorHandler.ErrorType.GENERAL);
            
            infoCollector = new InfoCollector();
            ErrorHandler.validateNotNull(infoCollector, "InfoCollector", ErrorHandler.ErrorType.GENERAL);
            
            shutdownManager = new ShutdownManager();
            ErrorHandler.validateNotNull(shutdownManager, "ShutdownManager", ErrorHandler.ErrorType.GENERAL);
            
            serverMonitor = new ServerMonitor(environmentDetector, infoCollector, shutdownManager);
            ErrorHandler.validateNotNull(serverMonitor, "ServerMonitor", ErrorHandler.ErrorType.GENERAL);
            
            // Only register event handlers if we're in a CI environment
            if (environmentDetector.isInCIEnvironment()) {
                LOGGER.info("[SERVER-TEST] CI environment detected, registering event handlers");
                registerEventHandlers();
            } else {
                LOGGER.info("[SERVER-TEST] Not in CI environment, mod will remain inactive");
            }
        }, ErrorHandler.ErrorType.GENERAL, "Failed to setup Server Test Mod components");
    }
    
    /**
     * Register event handlers for server lifecycle monitoring
     */
    private void registerEventHandlers() {
        ErrorHandler.executeCriticalOperation(() -> {
            MinecraftForge.EVENT_BUS.register(serverMonitor);
            LOGGER.debug("[SERVER-TEST] Event handlers registered successfully");
        }, ErrorHandler.ErrorType.GENERAL, "Failed to register event handlers");
    }
    
    /**
     * Global uncaught exception handler
     */
    private void handleUncaughtException(Thread thread, Throwable throwable) {
        ErrorHandler.handleCriticalError(
            ErrorHandler.ErrorType.GENERAL,
            "Uncaught exception in thread " + thread.getName(),
            throwable
        );
    }
    
    // Getters for components (useful for testing)
    public static EnvironmentDetector getEnvironmentDetector() {
        return environmentDetector;
    }
    
    public static ServerMonitor getServerMonitor() {
        return serverMonitor;
    }
    
    public static InfoCollector getInfoCollector() {
        return infoCollector;
    }
    
    public static ShutdownManager getShutdownManager() {
        return shutdownManager;
    }
}