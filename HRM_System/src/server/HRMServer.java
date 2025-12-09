/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HRMServer {
    // Thread pool for handling multiple clients
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    
    public static void main(String[] args) {
        try {
            System.out.println("=== STARTING MULTI-THREADED HRM & PAYROLL SERVER ===");
            System.out.println("🧵 Thread pool initialized: 10 concurrent threads available");
            
            // Create or get registry
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(1098);
                registry.list();
                System.out.println("✅ Using existing registry on port 1098");
            } catch (Exception e) {
                System.out.println("📡 Creating new registry on port 1098");
                registry = LocateRegistry.createRegistry(1098);
            }
            
            // Create HRM Service
            System.out.println("\n🔧 Creating HRM Service with thread pool support...");
            HRMServiceImpl hrService = new HRMServiceImpl(threadPool);
            Naming.rebind("rmi://localhost:1098/HRMService", hrService);
            
            // Create Payroll Service (depends on HRM Service)
            System.out.println("💰 Creating Payroll Service with thread pool support...");
            PayrollServiceImpl payrollService = new PayrollServiceImpl(hrService, threadPool);
            Naming.rebind("rmi://localhost:1098/PayrollService", payrollService);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ MULTI-THREADED SERVER IS READY!");
            System.out.println("=".repeat(60));
            System.out.println("\n📡 Services Available:");
            System.out.println("  1. HRM Service: rmi://localhost:1098/HRMService");
            System.out.println("  2. Payroll Service: rmi://localhost:1098/PayrollService");
            
            System.out.println("\n⚡ Performance Features:");
            System.out.println("  • Thread Pool Size: 10 concurrent clients");
            System.out.println("  • Background processing enabled");
            System.out.println("  • Non-blocking I/O operations");
            
            System.out.println("\n🔐 Sample Logins:");
            System.out.println("  HR Staff: hr / hr123");
            System.out.println("  Employee: EMP001 / password123");
            
            System.out.println("\n⚠️  Keep this window open while clients are running!");
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🔻 Server shutting down...");
                shutdownThreadPool();
            }));
            
            // Monitor thread pool activity
            startThreadPoolMonitor();
            
            // Keep server running
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("❌ SERVER ERROR: " + e);
            e.printStackTrace();
            shutdownThreadPool();
        }
    }
    
    // Monitor thread pool activity (optional)
    private static void startThreadPoolMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (!threadPool.isShutdown()) {
                try {
                    Thread.sleep(30000); // Check every 30 seconds
                    System.out.println("\n[🧵 THREAD POOL STATS] Active: " + 
                            Thread.activeCount() + " | Pool: " + threadPool);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    // Graceful shutdown of thread pool
    private static void shutdownThreadPool() {
        System.out.println("🧵 Shutting down thread pool...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            System.out.println("✅ Thread pool shut down successfully");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}