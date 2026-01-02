/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import common.Employee;
import common.FamilyMember;
import java.util.List;
import java.util.Map;

public interface HRMService extends Remote {
    // === HR STAFF METHODS ===
    String registerEmployee(String firstName, String lastName, String icPassport) throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    String generateYearlyReport(String employeeId) throws RemoteException;
    
    // === SIMPLIFIED LEAVE MANAGEMENT ===
    List<Map<String, String>> getAllLeaveApplications() throws RemoteException;
    boolean updateLeaveStatus(String applicationId, String status, String processedBy) throws RemoteException;
    
    // === EMPLOYEE METHODS ===
    Employee getEmployeeProfile(String employeeId) throws RemoteException;
    boolean updateEmployeeProfile(Employee employee) throws RemoteException;
    int checkLeaveBalance(String employeeId) throws RemoteException;
    String applyForLeave(String employeeId, int days, String reason) throws RemoteException;
    
    // NEW METHOD: Apply for unpaid leave
    String applyForUnpaidLeave(String employeeId, int days, String reason) throws RemoteException;
    
    List<Map<String, String>> getEmployeeLeaveHistory(String employeeId) throws RemoteException;
    
    // === FAMILY MEMBER METHODS ===
    boolean addFamilyMember(String empId, FamilyMember member) throws RemoteException;
    List<FamilyMember> getFamilyMembers(String empId) throws RemoteException;
    boolean removeFamilyMember(String empId, String memberIc) throws RemoteException;
    
    // === AUTHENTICATION ===
    boolean authenticate(String userId, String password) throws RemoteException;
    
    boolean changePassword(String userId, String oldPassword, String newPassword) throws RemoteException;
    
    // Session Expiry
    String createSession(String userId) throws RemoteException;
    boolean validateSession(String token) throws RemoteException;
    String getUserFromSession(String token) throws RemoteException;
    
    // === SECURITY LOGS === 
    /**
     * Get all security logs from the system
     * @return List of log entries with timestamp, userId, action, description, ipAddress, status
     * @throws RemoteException
     */
    List<Map<String, String>> getSecurityLogs() throws RemoteException;
}