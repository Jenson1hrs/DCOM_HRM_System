/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import common.Employee;
import common.FamilyMember;

public class TestFix {
    public static void main(String[] args) {
        // Test if methods exist
        Employee emp = new Employee("Test", "User", "T999999");
        FamilyMember fam = new FamilyMember("Spouse Name", "Spouse", "S999999");
        
        emp.addFamilyMember(fam);  // Should work now!
        
        System.out.println("Family members: " + emp.getFamilyMembers().size());
        System.out.println("✅ Methods working!");
    }    
}
