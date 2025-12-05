/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import remote.HRMService;
import java.rmi.Naming;
import java.util.Scanner;

public class HRStaffClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== HR Staff Client ===");
            HRMService hrService = (HRMService) Naming.lookup("rmi://localhost:1099/HRMService");
            
            System.out.print("User ID: ");
            String userId = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            if (hrService.authenticate(userId, password)) {
                System.out.println("Login successful!");
                
                System.out.print("Enter First Name: ");
                String firstName = scanner.nextLine();
                System.out.print("Enter Last Name: ");
                String lastName = scanner.nextLine();
                System.out.print("Enter IC/Passport: ");
                String icPassport = scanner.nextLine();
                
                String result = hrService.registerEmployee(firstName, lastName, icPassport);
                System.out.println(result);
                
            } else {
                System.out.println("Login failed!");
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Client error: " + e);
        }
    }
}
