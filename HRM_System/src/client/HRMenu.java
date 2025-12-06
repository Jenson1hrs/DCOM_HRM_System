/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import remote.HRMService;
import common.Employee;
import common.FamilyMember;
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

public class HRMenu {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== HR STAFF CLIENT ===");
            System.out.println("Connecting to server...");
            HRMService hrService = (HRMService) Naming.lookup("rmi://localhost:1098/HRMService");
            
            // Authentication
            System.out.print("\nUser ID: ");
            String userId = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            if (!hrService.authenticate(userId, password)) {
                System.out.println("❌ Login failed! Access denied.");
                scanner.close();
                return;
            }
            
            System.out.println("✅ Login successful!\n");
            
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
                System.out.println("8. Exit");
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
                        System.out.println(added ? "✅ Family member added!" : "❌ Failed to add family member.");
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
                            System.out.println("❌ Employee not found!");
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
                        System.out.println(updated ? "✅ Profile updated!" : "❌ Update failed.");
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
                            System.out.println("═══════════════════════════════════════");
                            for (int i = 0; i < allLeaves.size(); i++) {
                                Map<String, String> leave = allLeaves.get(i);
                                System.out.println((i + 1) + ". " + leave.get("employeeName") + " (" + leave.get("employeeId") + ")");
                                System.out.println("   Leave ID: " + leave.get("applicationId"));
                                System.out.println("   Days: " + leave.get("days") + " | Reason: " + leave.get("reason"));
                                System.out.println("   Status: " + leave.get("status"));
                                System.out.println("───────────────────────────────────");
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
                                        System.out.println(success ? "✅ Leave approved!" : "❌ Failed to approve.");
                                        break;
                                    case 2: // Reject
                                        success = hrService.updateLeaveStatus(appId, "Rejected: " + reason, userId);
                                        System.out.println(success ? "✅ Leave rejected!" : "❌ Failed to reject.");
                                        break;
                                    case 3: // Cancel
                                        System.out.println("Action cancelled.");
                                        break;
                                    default:
                                        System.out.println("❌ Invalid action!");
                                }
                            } else if (leaveChoice != 0) {
                                System.out.println("❌ Invalid selection!");
                            }
                            break;    
                        
                        
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
}
