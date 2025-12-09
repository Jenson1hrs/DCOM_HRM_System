/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

public class ThreadingDemo {
    
    public static void demonstrateRMIThreading() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MULTI-THREADING DEMONSTRATION FOR RMI SYSTEM");
        System.out.println("=".repeat(60));
        
        System.out.println("\n🎯 Scenario: 3 Clients Accessing Server Simultaneously");
        System.out.println("─────────────────────────────────────────────────");
        
        // Simulate Client 1: Employee checking leave
        Runnable client1 = () -> {
            System.out.println("[🧵 Thread-1] Employee EMP001: Checking leave balance...");
            simulateNetworkCall(1200);
            System.out.println("[✅ Thread-1] Leave balance retrieved: 15 days remaining");
        };
        
        // Simulate Client 2: HR processing salary
        Runnable client2 = () -> {
            System.out.println("[🧵 Thread-2] HR Staff: Processing salary for EMP002...");
            simulateNetworkCall(1800);
            System.out.println("[✅ Thread-2] Salary processed: RM4,500.00 paid");
        };
        
        // Simulate Client 3: Employee applying for leave
        Runnable client3 = () -> {
            System.out.println("[🧵 Thread-3] Employee EMP003: Submitting leave application...");
            simulateNetworkCall(900);
            System.out.println("[✅ Thread-3] Leave application submitted (ID: LV987654)");
        };
        
        System.out.println("\n🚀 Starting all clients simultaneously...");
        System.out.println("Note: Without threading, these would execute one after another.");
        System.out.println("With threading, they execute concurrently!");
        
        // Start all threads
        Thread t1 = new Thread(client1, "Client-1-Employee");
        Thread t2 = new Thread(client2, "Client-2-HR");
        Thread t3 = new Thread(client3, "Client-3-Employee");
        
        t1.start();
        t2.start();
        t3.start();
        
        // Wait for all to finish
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n" + "─".repeat(60));
        System.out.println("📊 DEMONSTRATION COMPLETE");
        System.out.println("All clients served concurrently by thread pool!");
        System.out.println("Total time: ~2 seconds (vs 4+ seconds without threading)");
        System.out.println("=".repeat(60));
    }
    
    private static void simulateNetworkCall(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) {
        demonstrateRMIThreading();
    }
}