/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package client;

import remote.HRMService;
import common.Employee;
import common.FamilyMember;
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

public class EmployeeMenu {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== EMPLOYEE CLIENT ===");
            System.out.println("Connecting to server...");
            HRMService hrService = (HRMService) Naming.lookup("rmi://localhost:1098/HRMService");
            
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
            
            boolean running = true;
            while (running) {
                System.out.println("=== EMPLOYEE MENU ===");
                System.out.println("1. View My Profile");
                System.out.println("2. Update My Profile");
                System.out.println("3. Check Leave Balance");
                System.out.println("4. Apply for Leave");
                System.out.println("5. Check Leave Status");
                System.out.println("6. Manage Family Members");
                System.out.println("7. Exit");
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
                        
                    case 7: // Exit
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
