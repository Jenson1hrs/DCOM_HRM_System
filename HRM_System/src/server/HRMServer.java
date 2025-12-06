/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class HRMServer {
        public static void main(String[] args) {
        try {
            System.out.println("Starting RMI Registry...");
            LocateRegistry.createRegistry(1098);
            
            System.out.println("Creating HRM Service...");
            HRMServiceImpl hrService = new HRMServiceImpl();
            
            System.out.println("Binding service to registry...");
            Naming.rebind("rmi://localhost:1098/HRMService", hrService);
            
            System.out.println("✅ HRM Server is ready!");
            System.out.println("Service URL: rmi://localhost:1098/HRMService");
            
        } catch (Exception e) {
            System.err.println("Server error: " + e);
            e.printStackTrace();
        }
    }
}
