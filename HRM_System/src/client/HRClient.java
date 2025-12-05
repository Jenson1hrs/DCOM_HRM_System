/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package client;

import remote.HRMService;
import common.Employee;
import java.rmi.Naming;
import java.util.Scanner;

public class HRClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== Employee Client ===");
            HRMService hrService = (HRMService) Naming.lookup("rmi://localhost:1099/HRMService");
            
            System.out.print("Enter Employee ID: ");
            String employeeId = scanner.nextLine();
            
            System.out.println("\n1. View Profile");
            System.out.println("2. Check Leave Balance");
            System.out.print("Choose: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    Employee profile = hrService.getEmployeeProfile(employeeId);
                    if (profile != null) {
                        System.out.println("Name: " + profile.getFirstName() + " " + profile.getLastName());
                        System.out.println("IC: " + profile.getIcPassport());
                    } else {
                        System.out.println("Employee not found!");
                    }
                    break;
                    
                case 2:
                    int balance = hrService.checkLeaveBalance(employeeId);
                    if (balance >= 0) {
                        System.out.println("Leave Balance: " + balance + " days");
                    } else {
                        System.out.println("Employee not found!");
                    }
                    break;
                    
                default:
                    System.out.println("Invalid choice!");
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Client error: " + e);
        }
    }
}
