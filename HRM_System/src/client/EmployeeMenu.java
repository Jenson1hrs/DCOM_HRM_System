/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
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

public class EmployeeMenu {
    public static void main(String[] args) {
        try {
           System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            System.setProperty("java.rmi.server.useLocalHostname", "true");
            
            // Set timeout properties
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
            System.setProperty("sun.rmi.transport.proxy.connectTimeout", "5000");
            
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== EMPLOYEE CLIENT ===");
            System.out.println("Connecting to server...");
            
            // Connect to HRM Service
            HRMService hrService = (HRMService) Naming.lookup("rmi://127.0.0.1:1098/HRMService");
            
            System.out.println("✅ Connected to HRM Service!");
            
            // Authentication
            System.out.print("\nEmployee ID: ");
            String employeeId = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            if (!hrService.authenticate(employeeId, password)) {
                System.out.println("❌ Login failed! Invalid credentials.");
                scanner.close();
                return;
            }
            
            System.out.println("✅ Login successful!\n");
            
            // Connect to Payroll Service
            PayrollService payrollService = (PayrollService) Naming.lookup("rmi://127.0.0.1:1098/PayrollService");
            System.out.println("✅ Connected to Payroll Service!");
            
            boolean running = true;
            while (running) {
                System.out.println("=== EMPLOYEE MENU ===");
                System.out.println("1. View My Profile");
                System.out.println("2. Update My Profile");
                System.out.println("3. Check Leave Balance");
                System.out.println("4. Apply for Leave");
                System.out.println("5. Check Leave Status");
                System.out.println("6. Manage Family Members");
                System.out.println("7. Salary & Payment"); 
                System.out.println("8. Exit");
                System.out.print("Choose option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (choice) {
                    case 1: // View Profile
                        System.out.println("\n--- My Profile ---");
                        Employee profile = hrService.getEmployeeProfile(employeeId);
                        if (profile != null) {
                            System.out.println("Employee ID: " + profile.getEmployeeId());
                            System.out.println("Name: " + profile.getFirstName() + " " + profile.getLastName());
                            System.out.println("IC/Passport: " + profile.getIcPassport());
                            System.out.println("Email: " + profile.getEmail());
                            System.out.println("Phone: " + profile.getPhone());
                            System.out.println("Department: " + profile.getDepartment());
                            System.out.println("Position: " + profile.getPosition());
                            System.out.println("Join Date: " + profile.getJoinDate());
                            System.out.println("Leave Balance: " + profile.getLeaveBalance() + " days");
                        } else {
                            System.out.println("❌ Profile not found!");
                        }
                        break;
                        
                    case 2: // Update Profile
                        System.out.println("\n--- Update My Profile ---");
                        Employee myProfile = hrService.getEmployeeProfile(employeeId);
                        
                        if (myProfile == null) {
                            System.out.println("❌ Profile not found!");
                            break;
                        }
                        
                        System.out.println("Enter new details (press Enter to keep current):");
                        
                        System.out.print("New Email [" + myProfile.getEmail() + "]: ");
                        String newEmail = scanner.nextLine();
                        if (!newEmail.isEmpty()) myProfile.setEmail(newEmail);
                        
                        System.out.print("New Phone [" + myProfile.getPhone() + "]: ");
                        String newPhone = scanner.nextLine();
                        if (!newPhone.isEmpty()) myProfile.setPhone(newPhone);
                        
                        System.out.print("New Department [" + myProfile.getDepartment() + "]: ");
                        String newDept = scanner.nextLine();
                        if (!newDept.isEmpty()) myProfile.setDepartment(newDept);
                        
                        System.out.print("New Position [" + myProfile.getPosition() + "]: ");
                        String newPos = scanner.nextLine();
                        if (!newPos.isEmpty()) myProfile.setPosition(newPos);
                        
                        boolean updated = hrService.updateEmployeeProfile(myProfile);
                        System.out.println(updated ? "✅ Profile updated!" : "❌ Update failed.");
                        break;
                        
                    case 3: // Check Leave Balance
                        System.out.println("\n--- Leave Balance ---");
                        int balance = hrService.checkLeaveBalance(employeeId);
                        if (balance >= 0) {
                            System.out.println("Available leave: " + balance + " days");
                        } else {
                            System.out.println("❌ Could not retrieve leave balance.");
                        }
                        break;
                        
                    case 4: // Apply for Leave
                        System.out.println("\n--- Apply for Leave ---");
                        System.out.print("Number of days: ");
                        int days = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        System.out.print("Reason: ");
                        String reason = scanner.nextLine();
                        
                        String leaveResult = hrService.applyForLeave(employeeId, days, reason);
                        System.out.println("\n" + leaveResult);
                        break;
                        
                    case 5: // View Leave History (replaces "Check Leave Status")
                        System.out.println("\n--- My Leave History ---");
                        List<Map<String, String>> myLeaves = hrService.getEmployeeLeaveHistory(employeeId);

                        if (myLeaves.isEmpty()) {
                            System.out.println("No leave applications found.");
                        } else {
                            System.out.println("═══════════════════════════════════════");
                            for (int i = 0; i < myLeaves.size(); i++) {
                                Map<String, String> leave = myLeaves.get(i);
                                System.out.println((i + 1) + ". Leave ID: " + leave.get("applicationId"));
                                System.out.println("   Days: " + leave.get("days"));
                                System.out.println("   Reason: " + leave.get("reason"));
                                System.out.println("   Status: " + leave.get("status"));
                                System.out.println("───────────────────────────────────");
                            }

                            System.out.print("\nEnter number to view details (1-" + myLeaves.size() + ") or 0 to go back: ");
                            int viewChoice = scanner.nextInt();
                            scanner.nextLine();

                            if (viewChoice > 0 && viewChoice <= myLeaves.size()) {
                                Map<String, String> selected = myLeaves.get(viewChoice - 1);
                                System.out.println("\n📋 Leave Details:");
                                System.out.println("Leave ID: " + selected.get("applicationId"));
                                System.out.println("Days: " + selected.get("days"));
                                System.out.println("Reason: " + selected.get("reason"));
                                System.out.println("Status: " + selected.get("status"));

                                // Show if pending
                                if (selected.get("status").equals("Pending")) {
                                    System.out.println("\nℹ️  This leave is pending HR approval.");
                                }
                            }
                        }
                        break;
                        
                    case 6: // Manage Family Members
                        System.out.println("\n--- Family Members ---");
                        System.out.println("1. View Family Members");
                        System.out.println("2. Add Family Member");
                        System.out.println("3. Remove Family Member");
                        System.out.print("Choose: ");
                        int famChoice = scanner.nextInt();
                        scanner.nextLine();
                        
                        switch (famChoice) {
                            case 1: // View
                                List<FamilyMember> family = hrService.getFamilyMembers(employeeId);
                                if (family.isEmpty()) {
                                    System.out.println("No family members registered.");
                                } else {
                                    System.out.println("Your Family Members:");
                                    for (FamilyMember fam : family) {
                                        System.out.println("- " + fam.getName() + " (" + fam.getRelationship() + 
                                                         ") - IC: " + fam.getIcNumber());
                                    }
                                }
                                break;
                                
                            case 2: // Add
                                System.out.print("Family Member Name: ");
                                String famName = scanner.nextLine();
                                System.out.print("Relationship (Spouse/Child/Parent): ");
                                String relationship = scanner.nextLine();
                                System.out.print("IC Number: ");
                                String famIc = scanner.nextLine();
                                System.out.print("Date of Birth (YYYY-MM-DD, optional): ");
                                String dob = scanner.nextLine();
                                
                                FamilyMember newMember = new FamilyMember(famName, relationship, famIc);
                                if (!dob.isEmpty()) {
                                    newMember.setDateOfBirth(dob);
                                }
                                
                                boolean added = hrService.addFamilyMember(employeeId, newMember);
                                System.out.println(added ? "✅ Family member added!" : "❌ Failed to add.");
                                break;
                                
                            case 3: // Remove
                                System.out.print("Enter Family Member IC Number to remove: ");
                                String removeIc = scanner.nextLine();
                                boolean removed = hrService.removeFamilyMember(employeeId, removeIc);
                                System.out.println(removed ? "✅ Family member removed!" : "❌ Not found.");
                                break;
                                
                            default:
                                System.out.println("❌ Invalid choice!");
                        }
                        break;
                        
                    case 7:   //SALARY & PAYMENT MENU       
                        handleSalaryMenu(employeeId, hrService, payrollService, scanner); 
                        
                    case 8: // Exit
                        running = false;
                        System.out.println("👋 Goodbye!");
                        break;
                        
                    default:
                        System.out.println("❌ Invalid option!");
                }
                
                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("❌ Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== NEW METHOD: SALARY MENU HANDLER =====
    private static void handleSalaryMenu(String empId, HRMService hrService, 
                                         PayrollService payrollService, Scanner scanner) {
        try {
            boolean inSalaryMenu = true;
            while (inSalaryMenu) {
                System.out.println("\n=== SALARY & PAYMENT ===");
                System.out.println("1. Check Current Month Salary");
                System.out.println("2. View Salary History");
                System.out.println("3. Update Bank Account Details");
                System.out.println("4. Back to Main Menu");
                System.out.print("Choose option: ");
                
                int salaryChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (salaryChoice) {
                    case 1: // Check Current Month Salary
                        System.out.println("\n--- Current Month Salary ---");
                        SalaryRecord currentSalary = payrollService.getMyCurrentSalary(empId);
                        
                        if (currentSalary != null) {
                            System.out.println("Month: " + currentSalary.getFormattedMonth());
                            System.out.println("Base Salary: RM" + String.format("%.2f", currentSalary.getBaseSalary()));
                            System.out.println("Working Days: " + currentSalary.getWorkingDays() + "/22");
                            System.out.println("Paid Leave Days: " + currentSalary.getPaidLeaveDays());
                            System.out.println("Unpaid Leave Days: " + currentSalary.getUnpaidLeaveDays());
                            System.out.println("Deductions: RM" + String.format("%.2f", currentSalary.getDeductions()));
                            System.out.println("Net Salary: RM" + String.format("%.2f", currentSalary.getNetSalary()));
                            System.out.println("Payment Status: " + currentSalary.getPaymentStatus());
                            
                            if (currentSalary.getPaymentDate() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                System.out.println("Paid on: " + sdf.format(currentSalary.getPaymentDate()));
                            }
                        } else {
                            System.out.println("❌ No salary record found for current month.");
                        }
                        break;
                        
                    case 2: // View Salary History
                        System.out.println("\n--- Salary History ---");
                        List<SalaryRecord> history = payrollService.getMySalaryHistory(empId);
                        
                        if (history.isEmpty()) {
                            System.out.println("No salary records found.");
                        } else {
                            System.out.println("═══════════════════════════════════════");
                            for (int i = 0; i < history.size(); i++) {
                                SalaryRecord record = history.get(i);
                                System.out.println((i + 1) + ". " + record.getFormattedMonth());
                                System.out.println("   Amount: RM" + String.format("%.2f", record.getNetSalary()));
                                System.out.println("   Status: " + record.getPaymentStatus());
                                System.out.println("───────────────────────────────────");
                            }
                            
                            // Option to view details
                            System.out.print("\nEnter number to view details (1-" + history.size() + ") or 0 to go back: ");
                            int viewChoice = scanner.nextInt();
                            scanner.nextLine();
                            
                            if (viewChoice > 0 && viewChoice <= history.size()) {
                                SalaryRecord selected = history.get(viewChoice - 1);
                                System.out.println("\n📋 Salary Details:");
                                System.out.println("Month: " + selected.getFormattedMonth());
                                System.out.println("Base Salary: RM" + String.format("%.2f", selected.getBaseSalary()));
                                System.out.println("Working Days: " + selected.getWorkingDays());
                                System.out.println("Paid Leave Days: " + selected.getPaidLeaveDays());
                                System.out.println("Unpaid Leave Days: " + selected.getUnpaidLeaveDays());
                                System.out.println("Deductions: RM" + String.format("%.2f", selected.getDeductions()));
                                System.out.println("Net Salary: RM" + String.format("%.2f", selected.getNetSalary()));
                                System.out.println("Status: " + selected.getPaymentStatus());
                                
                                if (selected.getPaymentDate() != null) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                    System.out.println("Paid on: " + sdf.format(selected.getPaymentDate()));
                                    System.out.println("Processed by: " + selected.getProcessedBy());
                                }
                            }
                        }
                        break;
                        
                    case 3: // Update Bank Account
                        System.out.println("\n--- Update Bank Account ---");
                        
                        // Get current employee profile to show current bank account
                        Employee profile = hrService.getEmployeeProfile(empId);
                        if (profile != null) {
                            System.out.println("Current Bank Account: " + profile.getBankAccount());
                        }
                        
                        System.out.print("Enter new bank account number: ");
                        String newAccount = scanner.nextLine();
                        
                        boolean updated = payrollService.updateBankAccount(empId, newAccount);
                        if (updated) {
                            System.out.println("✅ Bank account updated successfully!");
                        } else {
                            System.out.println("❌ Failed to update bank account.");
                        }
                        break;
                        
                    case 4: // Back to Main Menu
                        inSalaryMenu = false;
                        System.out.println("Returning to main menu...");
                        break;
                        
                    default:
                        System.out.println("❌ Invalid option!");
                }
                
                if (inSalaryMenu) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Salary menu error: " + e.getMessage());
        }
    }
}

