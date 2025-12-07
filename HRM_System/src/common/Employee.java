/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee implements Serializable {
    private static final long serialVersionUID = 2L; 
    
    private String employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    private String email;
    private String phone;
    private String department;
    private String position;
    private String joinDate;
    private int leaveBalance;
    private double monthlySalary = 4500.00; // Default salary
    private String bankAccount = "Not Set";
    
    private List<FamilyMember> familyMembers = new ArrayList<>();

    public Employee(String firstName, String lastName, String icPassport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.leaveBalance = 20;
        this.joinDate = java.time.LocalDate.now().toString();
    }    

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIcPassport() {
        return icPassport;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public int getLeaveBalance() {
        return leaveBalance;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public String getBankAccount() {
        return bankAccount;
    }
    
    public List<FamilyMember> getFamilyMembers() {
        return familyMembers;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIcPassport(String icPassport) {
        this.icPassport = icPassport;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public void setLeaveBalance(int leaveBalance) {
        this.leaveBalance = leaveBalance;
    }
    
    public void setFamilyMembers(List<FamilyMember> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    public void addFamilyMember(FamilyMember member) {
        familyMembers.add(member);
    }
    
    public boolean removeFamilyMember(String icNumber) {
        return familyMembers.removeIf(member -> 
            member.getIcNumber() != null && 
            member.getIcNumber().equals(icNumber)
        );
    }
    
    // ===== HELPER METHODS =====
    @Override
    public String toString() {
        return employeeId + ": " + firstName + " " + lastName;
    }
    
    public String getFamilyInfo() {
        if (familyMembers.isEmpty()) {
            return "No family members registered.";
        }
        
        StringBuilder info = new StringBuilder();
        for (FamilyMember member : familyMembers) {
            info.append("- ").append(member.toString()).append("\n");
        }
        return info.toString();
    } 
    
}
