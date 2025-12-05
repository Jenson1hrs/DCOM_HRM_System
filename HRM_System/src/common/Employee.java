/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;

public class Employee implements Serializable {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String icPassport;
    private int leaveBalance;
    
    // Constructor
    public Employee(String firstName, String lastName, String icPassport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.leaveBalance = 20;
    
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

    public int getLeaveBalance() {
        return leaveBalance;
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

    public void setLeaveBalance(int leaveBalance) {
        this.leaveBalance = leaveBalance;
    }
    

}
