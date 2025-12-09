/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import remote.HRMService;
import remote.PayrollService;
import common.Employee;
import common.FamilyMember;
import common.SalaryRecord;
import java.rmi.Naming;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemTest {
    
    private static HRMService hrService;
    private static PayrollService payrollService;
    private static int passedTests = 0;
    private static int failedTests = 0;
    
    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(70));
            System.out.println(" COMPLETE HRM SYSTEM TEST SUITE");
            System.out.println("=".repeat(70));
            
            // Connect to server
            System.out.println("\n Connecting to server...");
            hrService = (HRMService) Naming.lookup("rmi://localhost:1098/HRMService");
            payrollService = (PayrollService) Naming.lookup("rmi://localhost:1098/PayrollService");
            System.out.println(" Connected to both services");
            
            // Run all test categories
            System.out.println("\n" + "═".repeat(70));
            System.out.println("1. TESTING AUTHENTICATION & SECURITY");
            System.out.println("═".repeat(70));
            testAuthentication();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("2. TESTING EMPLOYEE MANAGEMENT");
            System.out.println("═".repeat(70));
            testEmployeeManagement();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("3. TESTING LEAVE MANAGEMENT");
            System.out.println("═".repeat(70));
            testLeaveManagement();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("4. TESTING PAYROLL SYSTEM");
            System.out.println("═".repeat(70));
            testPayrollSystem();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("5. TESTING FAMILY MANAGEMENT");
            System.out.println("═".repeat(70));
            testFamilyManagement();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("6. TESTING INPUT VALIDATION");
            System.out.println("═".repeat(70));
            testInputValidation();
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("7. TESTING ASYNC OPERATIONS");
            System.out.println("═".repeat(70));
            testAsyncOperations();
            
            // Summary
            System.out.println("\n" + "=".repeat(70));
            System.out.println(" TEST RESULTS SUMMARY");
            System.out.println("=".repeat(70));
            System.out.println(" Passed: " + passedTests);
            System.out.println(" Failed: " + failedTests);
            System.out.println(" Success Rate: " + 
                String.format("%.1f%%", (passedTests * 100.0 / (passedTests + failedTests))));
            
            if (failedTests == 0) {
                System.out.println("\n ALL TESTS PASSED! System is ready!");
            } else {
                System.out.println("\n️  Some tests failed. Check above for details.");
            }
            
        } catch (Exception e) {
            System.err.println(" Test suite failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===== TEST CATEGORIES =====
    
    private static void testAuthentication() {
        System.out.println("\n1.1 Testing HR Authentication...");
        try {
            boolean result = hrService.authenticate("hr", "hr123");
            assertTest(result, "HR login with correct password");
            
            result = hrService.authenticate("hr", "wrong");
            assertTest(!result, "HR login with wrong password (should fail)");
        } catch (Exception e) { failTest("HR Authentication", e); }
        
        System.out.println("\n1.2 Testing Employee Authentication...");
        try {
            boolean result = hrService.authenticate("EMP001", "password123");
            assertTest(result, "Employee EMP001 login");
            
            result = hrService.authenticate("EMP001", "wrong");
            assertTest(!result, "Wrong password (should fail)");
            
            result = hrService.authenticate("NONEXISTENT", "password123");
            assertTest(!result, "Non-existent user (should fail)");
        } catch (Exception e) { failTest("Employee Authentication", e); }
        
        System.out.println("\n1.3 Testing Password Change...");
        try {
            // Note: In real test, you'd need to change back
            // boolean result = hrService.changePassword("EMP001", "password123", "newpass123");
            // assertTest(result, "Password change");
            System.out.println("   ⚠️  Password change test skipped (would lock account)");
        } catch (Exception e) { failTest("Password Change", e); }
    }
    
    private static void testEmployeeManagement() {
        System.out.println("\n2.1 Testing Get Employee Profile...");
        try {
            Employee emp = hrService.getEmployeeProfile("EMP001");
            assertTest(emp != null, "Get existing employee profile");
            assertTest("John".equals(emp.getFirstName()), "Correct first name");
            assertTest("Doe".equals(emp.getLastName()), "Correct last name");
        } catch (Exception e) { failTest("Get Profile", e); }
        
        System.out.println("\n2.2 Testing Get All Employees...");
        try {
            List<Employee> employees = hrService.getAllEmployees();
            assertTest(employees.size() >= 2, "At least 2 sample employees");
            System.out.println("   Found " + employees.size() + " employees");
        } catch (Exception e) { failTest("Get All Employees", e); }
        
        System.out.println("\n2.3 Testing Update Profile...");
        try {
            Employee emp = hrService.getEmployeeProfile("EMP001");
            String originalPhone = emp.getPhone();
            emp.setPhone("999-8887777");
            
            boolean updated = hrService.updateEmployeeProfile(emp);
            assertTest(updated, "Update profile");
            
            // Verify update
            Employee updatedEmp = hrService.getEmployeeProfile("EMP001");
            assertTest("999-8887777".equals(updatedEmp.getPhone()), "Phone updated");
            
            // Restore original
            emp.setPhone(originalPhone);
            hrService.updateEmployeeProfile(emp);
        } catch (Exception e) { failTest("Update Profile", e); }
    }
    
    private static void testLeaveManagement() {
        System.out.println("\n3.1 Testing Leave Balance...");
        try {
            int balance = hrService.checkLeaveBalance("EMP001");
            assertTest(balance >= 0, "Get leave balance");
            System.out.println("   EMP001 leave balance: " + balance + " days");
        } catch (Exception e) { failTest("Leave Balance", e); }
        
        System.out.println("\n3.2 Testing Apply for Leave...");
        try {
            Employee emp = hrService.getEmployeeProfile("EMP001");
            int initialBalance = emp.getLeaveBalance();
            
            if (initialBalance >= 1) {
                String result = hrService.applyForLeave("EMP001", 1, "Test leave");
                assertTest(result.contains("✅"), "Apply for 1 day leave");
                
                // Check balance reduced
                emp = hrService.getEmployeeProfile("EMP001");
                assertTest(emp.getLeaveBalance() == initialBalance - 1, "Leave balance reduced");
            } else {
                System.out.println("   Skip: Insufficient leave balance");
            }
        } catch (Exception e) { failTest("Apply Leave", e); }
        
        System.out.println("\n3.3 Testing Leave History...");
        try {
            List<Map<String, String>> history = hrService.getEmployeeLeaveHistory("EMP001");
            System.out.println("   Found " + history.size() + " leave records for EMP001");
        } catch (Exception e) { failTest("Leave History", e); }
    }
    
    private static void testPayrollSystem() {
        System.out.println("\n4.1 Testing Salary Record Retrieval...");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String currentMonth = sdf.format(new Date());
            
            SalaryRecord record = payrollService.getSalaryRecord("EMP001", currentMonth);
            assertTest(record != null, "Get salary record");
            System.out.println("   EMP001 salary: RM" + String.format("%.2f", record.getNetSalary()));
        } catch (Exception e) { failTest("Salary Record", e); }
        
        System.out.println("\n4.2 Testing Payment Status...");
        try {
            List<Map<String, String>> status = payrollService.getEmployeesPaymentStatus();
            assertTest(!status.isEmpty(), "Get payment status");
            System.out.println("   Payment status for " + status.size() + " employees");
        } catch (Exception e) { failTest("Payment Status", e); }
        
        System.out.println("\n4.3 Testing Salary History...");
        try {
            List<SalaryRecord> history = payrollService.getMySalaryHistory("EMP001");
            System.out.println("   EMP001 salary history: " + history.size() + " records");
        } catch (Exception e) { failTest("Salary History", e); }
        
        System.out.println("\n4.4 Testing Bank Account Update...");
        try {
            boolean updated = payrollService.updateBankAccount("EMP001", "TEST-1234-5678");
            assertTest(updated, "Update bank account");
            
            Employee emp = hrService.getEmployeeProfile("EMP001");
            assertTest("TEST-1234-5678".equals(emp.getBankAccount()), "Bank account updated");
            
            // Restore original
            payrollService.updateBankAccount("EMP001", "1234-5678-9012");
        } catch (Exception e) { failTest("Bank Account", e); }
    }
    
    private static void testFamilyManagement() {
        System.out.println("\n5.1 Testing Get Family Members...");
        try {
            List<FamilyMember> family = hrService.getFamilyMembers("EMP001");
            System.out.println("   EMP001 family members: " + family.size());
        } catch (Exception e) { failTest("Get Family", e); }
        
        System.out.println("\n5.2 Testing Add Family Member...");
        try {
            FamilyMember newMember = new FamilyMember("Test Child", "Child", "T1234567");
            boolean added = hrService.addFamilyMember("EMP001", newMember);
            assertTest(added, "Add family member");
            
            List<FamilyMember> family = hrService.getFamilyMembers("EMP001");
            boolean found = family.stream().anyMatch(f -> "T1234567".equals(f.getIcNumber()));
            assertTest(found, "New member in list");
            
            // Cleanup
            hrService.removeFamilyMember("EMP001", "T1234567");
        } catch (Exception e) { failTest("Add Family", e); }
    }
    
    private static void testInputValidation() {
        System.out.println("\n6.1 Testing Invalid Input Handling...");
        System.out.println("     Manual test required: Try invalid names/IC in HR menu");
        System.out.println("   Expected: Clear error messages, no crashes");
    }
    
    private static void testAsyncOperations() {
        System.out.println("\n7.1 Testing Async Report Generation...");
        try {
            String result = hrService.generateYearlyReport("EMP001");
            assertTest(result.contains("started") || result.contains("Report generated"), 
                      "Report generation");
            System.out.println("   Result: " + result.substring(0, Math.min(50, result.length())) + "...");
        } catch (Exception e) { failTest("Async Report", e); }
    }
    
    // ===== HELPER METHODS =====
    
    private static void assertTest(boolean condition, String testName) {
        if (condition) {
            System.out.println(testName);
            passedTests++;
        } else {
            System.out.println( testName + " - FAILED");
            failedTests++;
        }
    }
    
    private static void failTest(String testName, Exception e) {
        System.out.println(testName + " - EXCEPTION: " + e.getMessage());
        failedTests++;
    }
}
