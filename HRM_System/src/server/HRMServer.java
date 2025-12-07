/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HRMServer {
    public static void main(String[] args) {
        try {
            System.out.println("=== STARTING HRM & PAYROLL SERVER ===");
            
            // Create or get registry
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(1098);
                registry.list();
                System.out.println("Using existing registry on port 1098");
            } catch (Exception e) {
                System.out.println("Creating new registry on port 1098");
                registry = LocateRegistry.createRegistry(1098);
            }
            
            // Create HRM Service
            System.out.println("\nCreating HRM Service...");
            HRMServiceImpl hrService = new HRMServiceImpl();
            Naming.rebind("rmi://localhost:1098/HRMService", hrService);
            
            // Create Payroll Service (depends on HRM Service)
            System.out.println("Creating Payroll Service...");
            PayrollServiceImpl payrollService = new PayrollServiceImpl(hrService);
            Naming.rebind("rmi://localhost:1098/PayrollService", payrollService);
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ SERVER IS READY!");
            System.out.println("=".repeat(50));
            System.out.println("\n📡 Services Available:");
            System.out.println("1. HRM Service: rmi://localhost:1098/HRMService");
            System.out.println("2. Payroll Service: rmi://localhost:1098/PayrollService");
            
            System.out.println("\n🔑 Sample Logins:");
            System.out.println("HR Staff: hr / hr123");
            System.out.println("Employee: EMP001 / password123");
            
            System.out.println("\n⚠️  Keep this window open while clients are running!");
            
            // Keep server running
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("❌ SERVER ERROR: " + e);
            e.printStackTrace();
        }
    }
}
