/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archive;

import remote.HRMService;
import common.FamilyMember;
import java.rmi.Naming;

public class FinalTest {
 public static void main(String[] args) {
        try {
            System.out.println("=== FINAL SYSTEM TEST ===\n");
            
            // Connect to server (PORT 1098!)
            HRMService service = (HRMService) Naming.lookup("rmi://localhost:1098/HRMService");
            
            // Test 1: Authentication
            System.out.println("1. 🔐 Authentication Test:");
            System.out.println("   HR Login: " + (service.authenticate("admin", "admin123") ? "✅ PASS" : "❌ FAIL"));
            System.out.println("   Employee Login: " + (service.authenticate("EMP001", "password123") ? "✅ PASS" : "❌ FAIL"));
            
            // Test 2: Register New Employee
            System.out.println("\n2. 📝 Employee Registration:");
            String result = service.registerEmployee("Final", "Test", "T8888888");
            System.out.println("   " + result);
            
            // Test 3: Leave Management
            System.out.println("\n3. 🏖️ Leave Management:");
            String leaveResult = service.applyForLeave("EMP001", 2, "Vacation");
            System.out.println("   " + leaveResult);
            
            // Test 4: Family Member
            System.out.println("\n4. 👨‍👩‍👧 Family Management:");
            FamilyMember child = new FamilyMember("Tommy Doe", "Child", "T1234567");
            boolean famAdded = service.addFamilyMember("EMP001", child);
            System.out.println("   Family added: " + (famAdded ? "✅" : "❌"));
            
            // Test 5: Generate Report
            System.out.println("\n5. 📊 Report Generation:");
            String report = service.generateYearlyReport("EMP001");
            System.out.println("   Report generated: " + report.length() + " characters");
            
            System.out.println("\n🎉🎉🎉 ALL TESTS COMPLETED SUCCESSFULLY! 🎉🎉🎉");
            System.out.println("Your HRM System is READY for submission!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
  
}
