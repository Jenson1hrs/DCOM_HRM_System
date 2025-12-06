/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import remote.HRMService;
import common.Employee;
import common.FamilyMember;
import java.rmi.Naming;

public class TestAllFeatures {
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing All HRM Features ===\n");
            
            HRMService service = (HRMService) Naming.lookup("rmi://localhost:1099/HRMService");
            
            // 1. Test Authentication
            System.out.println("1. Testing Authentication:");
            System.out.println("   HR Login: " + service.authenticate("admin", "admin123"));
            System.out.println("   Employee Login: " + service.authenticate("EMP001", "password123"));
            
            // 2. Test Register Employee
            System.out.println("\n2. Testing Employee Registration:");
            String result = service.registerEmployee("Ali", "Ahmad", "C5555555");
            System.out.println("   " + result);
            
            // 3. Test Get All Employees
            System.out.println("\n3. Testing Get All Employees:");
            System.out.println("   Total employees: " + service.getAllEmployees().size());
            
            // 4. Test Add Family Member
            System.out.println("\n4. Testing Add Family Member:");
            FamilyMember spouse = new FamilyMember("Siti Aminah", "Spouse", "S5555555");
            spouse.setDateOfBirth("1995-08-15");
            boolean famSuccess = service.addFamilyMember("EMP003", spouse);
            System.out.println("   Family member added: " + famSuccess);
            
            // 5. Test Leave Application
            System.out.println("\n5. Testing Leave Application:");
            String leaveResult = service.applyForLeave("EMP001", 2, "Medical");
            System.out.println("   " + leaveResult);
            
            // 6. Test Generate Report
            System.out.println("\n6. Testing Yearly Report:");
            String report = service.generateYearlyReport("EMP001");
            System.out.println("   Report generated (length: " + report.length() + " chars)");
            
            System.out.println("\n✅ All tests completed!");
            
        } catch (Exception e) {
            System.err.println("Test error: " + e);
            e.printStackTrace();
        }
    }
}
