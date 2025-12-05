/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import common.Employee;
import java.util.List;

public interface HRMService extends Remote{
    String registerEmployee(String firstName, String lastName, String icPassport) throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    String generateYearlyReport(String employeeId) throws RemoteException;

    Employee getEmployeeProfile(String employeeId) throws RemoteException;
    boolean updateEmployeeProfile(Employee employee) throws RemoteException;
    int checkLeaveBalance(String employeeId) throws RemoteException;
    String applyForLeave(String employeeId, int days, String reason) throws RemoteException;
    String checkLeaveStatus(String employeeId, String applicationId) throws RemoteException;
    
    // Common Methods
    boolean authenticate(String userId, String password) throws RemoteException;

}

