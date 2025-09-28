package nslookup_2.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ServerMonitor - Monitor server performance và statistics
 */
public class ServerMonitor {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean monitoring = false;

    public static void startMonitoring() {
        if (monitoring) {
            System.out.println("Monitoring already started");
            return;
        }

        monitoring = true;
        System.out.println("Starting server monitoring...");

        // Monitor every 10 seconds
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("SERVER MONITOR REPORT");
            System.out.println("=".repeat(50));

            // Print thread pool status
            DNSServer.printThreadPoolStatus();

            // Print DNS resolver stats
            DNSResolver.printCacheStats();

            // Cleanup expired cache
            DNSResolver.cleanupExpiredCache();

            System.out.println("=".repeat(50));

        }, 10, 10, TimeUnit.SECONDS);
    }

    public static void stopMonitoring() {
        if (!monitoring) {
            System.out.println("Monitoring not started");
            return;
        }

        monitoring = false;
        scheduler.shutdown();
        System.out.println("Server monitoring stopped");
    }

    public static void main(String[] args) {
        // chay rieng de theo doi server
        startMonitoring();

        // Keep running until interrupted
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            stopMonitoring();
        }
    }
}
