/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import remote.HRMService;
import common.Employee;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    private Map<String, Employee> employees = new HashMap<>();
    private Map<String, String> leaveApplications = new HashMap<>();
    
    public HRMServiceImpl() throws RemoteException {
        super();
        // Sample data for testing
        Employee emp1 = new Employee("John", "Doe", "A1234567");
        emp1.setEmployeeId("EMP001");
        employees.put("EMP001", emp1);
        
        Employee emp2 = new Employee("Jane", "Smith", "B9876543");
        emp2.setEmployeeId("EMP002");
        employees.put("EMP002", emp2);
    }
    
    // HR Staff Methods
    @Override
    public String registerEmployee(String firstName, String lastName, String icPassport) throws RemoteException {
        String employeeId = "EMP" + String.format("%03d", employees.size() + 1);
        Employee newEmployee = new Employee(firstName, lastName, icPassport);
        newEmployee.setEmployeeId(employeeId);
        employees.put(employeeId, newEmployee);
        return "Employee registered successfully! Employee ID: " + employeeId;
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        return new ArrayList<>(employees.values());
    }
    
    @Override
    public String generateYearlyReport(String employeeId) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) {
            return "Employee not found!";
        }
        return "=== Yearly Report ===\n" +
               "Employee: " + emp.getFirstName() + " " + emp.getLastName() + "\n" +
               "ID: " + emp.getEmployeeId() + "\n" +
               "IC/Passport: " + emp.getIcPassport() + "\n" +
               "Leave Balance: " + emp.getLeaveBalance() + " days\n" +
               "Report Date: " + new Date();
    }
    
    // Employee Methods
    @Override
    public Employee getEmployeeProfile(String employeeId) throws RemoteException {
        return employees.get(employeeId);
    }
    
    @Override
    public boolean updateEmployeeProfile(Employee employee) throws RemoteException {
        if (employees.containsKey(employee.getEmployeeId())) {
            employees.put(employee.getEmployeeId(), employee);
            return true;
        }
        return false;
    }
    
    @Override
    public int checkLeaveBalance(String employeeId) throws RemoteException {
        Employee emp = employees.get(employeeId);
        return (emp != null) ? emp.getLeaveBalance() : -1;
    }
    
    @Override
    public String applyForLeave(String employeeId, int days, String reason) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) return "Employee not found!";
        
        if (emp.getLeaveBalance() >= days) {
            // Deduct leave
            emp.setLeaveBalance(emp.getLeaveBalance() - days);
            
            // Create application ID
            String applicationId = "LV" + System.currentTimeMillis();
            leaveApplications.put(applicationId, 
                "Employee: " + employeeId + 
                ", Days: " + days + 
                ", Reason: " + reason + 
                ", Status: Pending");
            
            return "Leave application submitted! Application ID: " + applicationId;
        } else {
            return "Insufficient leave balance! You have " + emp.getLeaveBalance() + " days left.";
        }
    }
    
    @Override
    public String checkLeaveStatus(String employeeId, String applicationId) throws RemoteException {
        String status = leaveApplications.get(applicationId);
        return (status != null) ? status : "Application not found!";
    }
    
    // ⭐⭐⭐ THIS IS THE MISSING METHOD! ⭐⭐⭐
    @Override
    public boolean authenticate(String userId, String password) throws RemoteException {
        // Simple authentication - replace with database in real system
        if ("admin".equals(userId) && "admin123".equals(password)) {
            return true;  // HR Staff
        }
        if ("employee".equals(userId) && "emp123".equals(password)) {
            return true;  // Employee
        }
        // Check if userId is an employee ID
        if (employees.containsKey(userId) && "password123".equals(password)) {
            return true;
        }
        return false;
    }
}
