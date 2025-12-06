/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import remote.HRMService;
import java.rmi.Naming;

public class HealthCheck {
    public static void main(String[] args) throws Exception {
        HRMService service = (HRMService) Naming.lookup("rmi://localhost:1098/HRMService");
        
        System.out.println("🩺 System Health Check:");
        System.out.println("1. Connection: ✅ LIVE");
        System.out.println("2. Employee Count: " + service.getAllEmployees().size());
        System.out.println("3. Sample Employee Leave: " + service.checkLeaveBalance("EMP001") + " days");
        System.out.println("4. Server Status: ✅ HEALTHY");
    }
}