/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package remote;

import common.SalaryRecord;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface PayrollService extends Remote {
    
    // === HR STAFF PAYROLL METHODS ===
    
    /**
     * Get all employees with their current month payment status
     */
    List<Map<String, String>> getEmployeesPaymentStatus() throws RemoteException;
    
    /**
     * Get salary record for specific employee and month
     */
    SalaryRecord getSalaryRecord(String empId, String monthYear) throws RemoteException;
    
    /**
     * Process salary payment for employee
     */
    boolean processSalaryPayment(String empId, String monthYear, 
                                 String processedBy) throws RemoteException;
    
    /**
     * Get payment history for employee
     */
    List<SalaryRecord> getEmployeePaymentHistory(String empId) throws RemoteException;
    
    /**
     * Get all salary records for HR view
     */
    List<SalaryRecord> getAllSalaryRecords() throws RemoteException;
    
    // === EMPLOYEE PAYROLL METHODS ===
    
    /**
     * Get employee's own salary history
     */
    List<SalaryRecord> getMySalaryHistory(String empId) throws RemoteException;
    
    /**
     * Get employee's current month salary status
     */
    SalaryRecord getMyCurrentSalary(String empId) throws RemoteException;
    
    /**
     * Update employee's bank account details
     */
    boolean updateBankAccount(String empId, String bankAccount) throws RemoteException;
   
    // === NEW METHOD: SYNC LEAVE WITH SALARY ===
    
    /**
     * Sync employee leave with salary calculation
     * @param empId Employee ID
     * @param monthYear Month in format "YYYY-MM"
     * @param leaveDays Number of leave days
     * @param isPaid True if paid leave, false if unpaid
     */
    void syncLeaveWithSalary(String empId, String monthYear, int leaveDays, boolean isPaid) 
            throws RemoteException;
    
    /**
     * Remove leave from salary calculation (when leave is rejected)
     * @param empId Employee ID
     * @param monthYear Month in format "YYYY-MM"
     * @param leaveDays Number of leave days to remove
     * @param isPaid True if paid leave, false if unpaid
     */
    void removeLeaveFromSalary(String empId, String monthYear, int leaveDays, boolean isPaid) 
            throws RemoteException;
}
