/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import remote.HRMService;
import remote.PayrollService; 
import common.Employee;
import common.FamilyMember;
import common.SalaryRecord;    
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class HRMenu {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            System.setProperty("java.rmi.server.useLocalHostname", "true");
            
            // Set timeout properties
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
            System.setProperty("sun.rmi.transport.proxy.connectTimeout", "5000");
            
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== HR STAFF CLIENT ===");
            System.out.println("Connecting to server...");
            
            // Connect to HRM Service
            HRMService hrService = (HRMService) Naming.lookup("rmi://127.0.0.1:1098/HRMService");
            
            System.out.println(" Connected to HRM Service!");
            
            // Authentication
            System.out.print("\nUser ID: ");
            String userId = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            if (!hrService.authenticate(userId, password)) {
                System.out.println(" Login failed! Access denied.");
                scanner.close();
                return;
            }
            
            System.out.println(" Login successful!\n");
            
            // Connect to Payroll Service
            PayrollService payrollService = (PayrollService) Naming.lookup("rmi://127.0.0.1:1098/PayrollService");
            System.out.println(" Connected to Payroll Service!");
            
            boolean running = true;
            while (running) {
                System.out.println("=== HR MENU ===");
                System.out.println("1. Register New Employee");
                System.out.println("2. View All Employees");
                System.out.println("3. Generate Yearly Report");
                System.out.println("4. Add Family Member to Employee");
                System.out.println("5. View Employee's Family Members");
                System.out.println("6. Update Employee Profile");
                System.out.println("7. Manage Employee Leaves");
                System.out.println("8. Payroll Management");
                System.out.println("9. View Security Log"); 
                System.out.println("10. Exit");
                System.out.print("Choose option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (choice) {
                    case 1: // Register Employee
                        System.out.println("\n--- Register New Employee ---");
                        System.out.print("First Name: ");
                        String firstName = scanner.nextLine();
                        System.out.print("Last Name: ");
                        String lastName = scanner.nextLine();
                        System.out.print("IC/Passport: ");
                        String icPassport = scanner.nextLine();
                        
                        String result = hrService.registerEmployee(firstName, lastName, icPassport);
                        System.out.println("\n" + result);
                        break;
                        
                    case 2: // View All Employees
                        System.out.println("\n--- All Employees ---");
                        List<Employee> employees = hrService.getAllEmployees();
                        if (employees.isEmpty()) {
                            System.out.println("No employees found.");
                        } else {
                            for (Employee emp : employees) {
                                System.out.println("ID: " + emp.getEmployeeId() + 
                                                 " | Name: " + emp.getFirstName() + " " + emp.getLastName() +
                                                 " | Dept: " + emp.getDepartment() +
                                                 " | Leave: " + emp.getLeaveBalance() + " days");
                            }
                            System.out.println("\nTotal: " + employees.size() + " employees");
                        }
                        break;
                        
                    case 3: // Generate Yearly Report
                        System.out.println("\n--- Generate Yearly Report ---");
                        System.out.print("Enter Employee ID: ");
                        String reportEmpId = scanner.nextLine();
                        String report = hrService.generateYearlyReport(reportEmpId);
                        System.out.println("\n" + report);
                        break;
                        
                    case 4: // Add Family Member
                        System.out.println("\n--- Add Family Member ---");
                        System.out.print("Employee ID: ");
                        String famEmpId = scanner.nextLine();
                        System.out.print("Family Member Name: ");
                        String famName = scanner.nextLine();
                        System.out.print("Relationship (Spouse/Child/Parent): ");
                        String relationship = scanner.nextLine();
                        System.out.print("IC Number: ");
                        String famIc = scanner.nextLine();
                        System.out.print("Date of Birth (YYYY-MM-DD, optional): ");
                        String dob = scanner.nextLine();
                        
                        FamilyMember member = new FamilyMember(famName, relationship, famIc);
                        if (!dob.isEmpty()) {
                            member.setDateOfBirth(dob);
                        }
                        
                        boolean added = hrService.addFamilyMember(famEmpId, member);
                        System.out.println(added ? "✅ Family member added!" : " Failed to add family member.");
                        break;
                        
                    case 5: // View Family Members
                        System.out.println("\n--- View Family Members ---");
                        System.out.print("Employee ID: ");
                        String viewFamEmpId = scanner.nextLine();
                        List<FamilyMember> family = hrService.getFamilyMembers(viewFamEmpId);
                        
                        if (family.isEmpty()) {
                            System.out.println("No family members found.");
                        } else {
                            System.out.println("Family Members:");
                            for (FamilyMember fam : family) {
                                System.out.println("- " + fam.getName() + " (" + fam.getRelationship() + 
                                                 ") - IC: " + fam.getIcNumber());
                            }
                        }
                        break;
                        
                    case 6: // Update Employee Profile
                        System.out.println("\n--- Update Employee Profile ---");
                        System.out.print("Employee ID to update: ");
                        String updateEmpId = scanner.nextLine();
                        
                        Employee existingEmp = hrService.getEmployeeProfile(updateEmpId);
                        if (existingEmp == null) {
                            System.out.println(" Employee not found!");
                            break;
                        }
                        
                        System.out.println("\nCurrent details:");
                        System.out.println("Name: " + existingEmp.getFirstName() + " " + existingEmp.getLastName());
                        System.out.println("Email: " + existingEmp.getEmail());
                        System.out.println("Phone: " + existingEmp.getPhone());
                        System.out.println("Department: " + existingEmp.getDepartment());
                        System.out.println("Position: " + existingEmp.getPosition());
                        
                        System.out.println("\nEnter new details (press Enter to keep current):");
                        
                        System.out.print("New Email [" + existingEmp.getEmail() + "]: ");
                        String newEmail = scanner.nextLine();
                        if (!newEmail.isEmpty()) existingEmp.setEmail(newEmail);
                        
                        System.out.print("New Phone [" + existingEmp.getPhone() + "]: ");
                        String newPhone = scanner.nextLine();
                        if (!newPhone.isEmpty()) existingEmp.setPhone(newPhone);
                        
                        System.out.print("New Department [" + existingEmp.getDepartment() + "]: ");
                        String newDept = scanner.nextLine();
                        if (!newDept.isEmpty()) existingEmp.setDepartment(newDept);
                        
                        System.out.print("New Position [" + existingEmp.getPosition() + "]: ");
                        String newPos = scanner.nextLine();
                        if (!newPos.isEmpty()) existingEmp.setPosition(newPos);
                        
                        boolean updated = hrService.updateEmployeeProfile(existingEmp);
                        System.out.println(updated ? " Profile updated!" : " Update failed.");
                        break;
                     
                    case 7: // Manage Employee Leaves
                            System.out.println("\n--- Employee Leave Management ---");
                            List<Map<String, String>> allLeaves = hrService.getAllLeaveApplications();

                            if (allLeaves.isEmpty()) {
                                System.out.println("No leave applications found.");
                                break;
                            }

                            // Display all leaves with numbers
                            System.out.println("All Leave Applications:");
                            System.out.println("=================================");
                            for (int i = 0; i < allLeaves.size(); i++) {
                                Map<String, String> leave = allLeaves.get(i);
                                System.out.println((i + 1) + ". " + leave.get("employeeName") + " (" + leave.get("employeeId") + ")");
                                System.out.println("   Leave ID: " + leave.get("applicationId"));
                                System.out.println("   Days: " + leave.get("days") + " | Reason: " + leave.get("reason"));
                                System.out.println("   Status: " + leave.get("status"));
                                System.out.println("==============================");
                            }

                            System.out.print("\nSelect leave to manage (1-" + allLeaves.size() + ") or 0 to cancel: ");
                            int leaveChoice = scanner.nextInt();
                            scanner.nextLine();

                            if (leaveChoice > 0 && leaveChoice <= allLeaves.size()) {
                                Map<String, String> selectedLeave = allLeaves.get(leaveChoice - 1);
                                String appId = selectedLeave.get("applicationId");

                                System.out.println("\nSelected: " + selectedLeave.get("employeeName") + 
                                                 " - " + selectedLeave.get("days") + " days - " + selectedLeave.get("reason"));
                                System.out.println("Current Status: " + selectedLeave.get("status"));

                                System.out.println("\n1. Approve");
                                System.out.println("2. Reject");
                                System.out.println("3. Cancel");
                                System.out.print("Choose action: ");
                                int action = scanner.nextInt();
                                scanner.nextLine();

                                String reason = "";
                                if (action == 2) { // Reject
                                    System.out.print("Enter rejection reason: ");
                                    reason = scanner.nextLine();
                                }

                                boolean success = false;
                                switch (action) {
                                    case 1: // Approve
                                        success = hrService.updateLeaveStatus(appId, "Approved", userId);
                                        System.out.println(success ? " Leave approved!" : " Failed to approve.");
                                        break;
                                    case 2: // Reject
                                        success = hrService.updateLeaveStatus(appId, "Rejected: " + reason, userId);
                                        System.out.println(success ? " Leave rejected!" : " Failed to reject.");
                                        break;
                                    case 3: // Cancel
                                        System.out.println("Action cancelled.");
                                        break;
                                    default:
                                        System.out.println(" Invalid action!");
                                }
                            } else if (leaveChoice != 0) {
                                System.out.println(" Invalid selection!");
                            }
                            break;    
                        
                    case 8: //PAYROLL MANAGEMENT
                        handlePayrollMenu(userId, hrService, payrollService, scanner);
                    
                    case 9: // View Security Log
                        viewSecurityLog();
                        break;    
                        
                    case 10: // Exit
                        running = false;
                        System.out.println(" Goodbye!");
                        break;
                        
                    default:
                        System.out.println(" Invalid option!");
                }
                
                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println(" Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ===== NEW METHOD: PAYROLL MENU HANDLER =====
    private static void handlePayrollMenu(String hrUserId, HRMService hrService, 
                                          PayrollService payrollService, Scanner scanner) {
        try {
            boolean inPayrollMenu = true;
            while (inPayrollMenu) {
                System.out.println("\n=== PAYROLL MANAGEMENT ===");
                System.out.println("1. View Employee Payment Status");
                System.out.println("2. Process Salary Payment");
                System.out.println("3. View Payment History");
                System.out.println("4. View All Salary Records");
                System.out.println("5. Back to Main Menu");
                System.out.print("Choose option: ");
                
                int payrollChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (payrollChoice) {
                    case 1: // View Employee Payment Status
                        System.out.println("\n--- Employee Payment Status ---");
                        List<Map<String, String>> paymentStatus = payrollService.getEmployeesPaymentStatus();
                        
                        if (paymentStatus.isEmpty()) {
                            System.out.println("No employees found.");
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
                            String currentMonth = sdf.format(new Date());
                            
                            System.out.println("Current Month: " + currentMonth);
                            System.out.println("=================================");
                            
                            for (int i = 0; i < paymentStatus.size(); i++) {
                                Map<String, String> emp = paymentStatus.get(i);
                                System.out.println((i + 1) + ". " + emp.get("employeeId") + 
                                                 " - " + emp.get("name"));
                                System.out.println("   Department: " + emp.get("department"));
                                System.out.println("   Salary: " + emp.get("salary"));
                                System.out.println("   Net Salary: " + emp.get("netSalary"));
                                System.out.println("   Status: " + emp.get("status"));
                                System.out.println("============================");
                            }
                        }
                        break;
                        
                    case 2: // Process Salary Payment
                        System.out.println("\n--- Process Salary Payment ---");
                        
                        // Show unpaid employees
                        List<Map<String, String>> unpaidEmployees = payrollService.getEmployeesPaymentStatus();
                        List<Map<String, String>> filtered = new java.util.ArrayList<>();
                        
                        for (Map<String, String> emp : unpaidEmployees) {
                            if ("UNPAID".equals(emp.get("status"))) {
                                filtered.add(emp);
                            }
                        }
                        
                        if (filtered.isEmpty()) {
                            System.out.println(" All employees are already paid for this month!");
                            break;
                        }
                        
                        System.out.println("Unpaid Employees:");
                        for (int i = 0; i < filtered.size(); i++) {
                            Map<String, String> emp = filtered.get(i);
                            System.out.println((i + 1) + ". " + emp.get("employeeId") + 
                                             " - " + emp.get("name") + 
                                             " (" + emp.get("netSalary") + ")");
                        }
                        
                        System.out.print("\nSelect employee to pay (1-" + filtered.size() + "): ");
                        int empChoice = scanner.nextInt();
                        scanner.nextLine();
                        
                        if (empChoice > 0 && empChoice <= filtered.size()) {
                            Map<String, String> selectedEmp = filtered.get(empChoice - 1);
                            String empId = selectedEmp.get("employeeId");
                            
                            // Get current month
                            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
                            String currentMonth = monthFormat.format(new Date());
                            
                            // Get salary record details
                            SalaryRecord record = payrollService.getSalaryRecord(empId, currentMonth);
                            
                            if (record != null) {
                                System.out.println("\n Payment Details:");
                                System.out.println("Employee: " + selectedEmp.get("name"));
                                System.out.println("Month: " + record.getFormattedMonth());
                                System.out.println("Working Days: " + record.getWorkingDays());
                                System.out.println("Paid Leave: " + record.getPaidLeaveDays() + " days");
                                System.out.println("Unpaid Leave: " + record.getUnpaidLeaveDays() + " days");
                                System.out.println("Net Salary: RM" + String.format("%.2f", record.getNetSalary()));
                                
                                System.out.print("\nConfirm payment? (yes/no): ");
                                String confirm = scanner.nextLine();
                                
                                if ("yes".equalsIgnoreCase(confirm)) {
                                    boolean success = payrollService.processSalaryPayment(
                                        empId, currentMonth, hrUserId
                                    );
                                    
                                    if (success) {
                                        System.out.println(" Salary payment processed successfully!");
                                        System.out.println("Amount: RM" + String.format("%.2f", record.getNetSalary()));
                                        System.out.println("Employee: " + selectedEmp.get("name"));
                                    } else {
                                        System.out.println(" Payment failed. Employee may already be paid.");
                                    }
                                } else {
                                    System.out.println("Payment cancelled.");
                                }
                            }
                        } else {
                            System.out.println(" Invalid selection!");
                        }
                        break;
                        
                    case 3: // View Payment History
                        System.out.println("\n--- Payment History ---");
                        System.out.print("Enter Employee ID: ");
                        String historyEmpId = scanner.nextLine();
                        
                        List<SalaryRecord> history = payrollService.getEmployeePaymentHistory(historyEmpId);
                        
                        if (history.isEmpty()) {
                            System.out.println("No payment history found for employee " + historyEmpId);
                        } else {
                            System.out.println("Payment History for: " + historyEmpId);
                            System.out.println("=================================");
                            
                            double totalPaid = 0;
                            for (SalaryRecord record : history) {
                                System.out.println("Month: " + record.getFormattedMonth());
                                System.out.println("Net Salary: RM" + String.format("%.2f", record.getNetSalary()));
                                System.out.println("Status: " + record.getPaymentStatus());
                                
                                if (record.getPaymentDate() != null) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    System.out.println("Paid on: " + sdf.format(record.getPaymentDate()));
                                }
                                
                                if ("PAID".equals(record.getPaymentStatus())) {
                                    totalPaid += record.getNetSalary();
                                }
                                
                                System.out.println("==============================");
                            }
                            
                            System.out.println("\n Summary:");
                            System.out.println("Total Records: " + history.size());
                            System.out.println("Total Paid: RM" + String.format("%.2f", totalPaid));
                        }
                        break;
                        
                    case 4: // View All Salary Records
                        System.out.println("\n--- All Salary Records ---");
                        List<SalaryRecord> allRecords = payrollService.getAllSalaryRecords();
                        
                        if (allRecords.isEmpty()) {
                            System.out.println("No salary records found.");
                        } else {
                            System.out.println("Total Records: " + allRecords.size());
                            System.out.println("=================================");
                            
                            String currentMonth = "";
                            for (SalaryRecord record : allRecords) {
                                if (!currentMonth.equals(record.getMonthYear())) {
                                    currentMonth = record.getMonthYear();
                                    System.out.println("\n " + record.getFormattedMonth() + ":");
                                }
                                
                                System.out.println("  " + record.getEmployeeId() + 
                                                 " - RM" + String.format("%.2f", record.getNetSalary()) +
                                                 " [" + record.getPaymentStatus() + "]");
                            }
                        }
                        break;
                        
                    case 5: // Back to Main Menu
                        inPayrollMenu = false;
                        System.out.println("Returning to main menu...");
                        break;
                        
                    default:
                        System.out.println(" Invalid option!");
                }
                
                if (inPayrollMenu) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
            
        } catch (Exception e) {
            System.err.println(" Payroll menu error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void viewSecurityLog() {
    try {
        System.out.println("\n=== SECURITY AUDIT LOG ===\n");
        
        // Connect to server
        HRMService hrService = (HRMService) Naming.lookup("rmi://127.0.0.1:1098/HRMService");
        
        // In real implementation, you'd call a method to get logs
        // For now, we'll read the file directly
        
        File logFile = new File("security_audit.log");
        if (!logFile.exists()) {
            System.out.println("No security log found yet.");
            System.out.println("Perform some actions first (login, register, etc.)");
            return;
        }
        
        System.out.println("Log file: " + logFile.getAbsolutePath());
        System.out.println("Last modified: " + new Date(logFile.lastModified()));
        System.out.println("\n" + "═".repeat(50));
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                lineCount++;
            }
            System.out.println("\n═".repeat(50));
            System.out.println("Total entries: " + lineCount);
        }
        
    } catch (Exception e) {
        System.out.println(" Error reading security log: " + e.getMessage());
    }
}
}