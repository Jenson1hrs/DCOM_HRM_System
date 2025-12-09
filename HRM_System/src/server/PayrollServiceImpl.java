/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import remote.PayrollService;
import remote.HRMService;
import common.SalaryRecord;
import common.Employee;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;

public class PayrollServiceImpl extends UnicastRemoteObject implements PayrollService {
    private HRMService hrService;
    private Map<String, SalaryRecord> salaryRecords; // key: empId_monthYear
    private ExecutorService threadPool;
    
    public PayrollServiceImpl(HRMService hrService, ExecutorService threadPool) throws RemoteException {
        super();
        this.hrService = hrService;
        this.threadPool = threadPool;  // Store thread pool
        this.salaryRecords = new HashMap<>();
        initializeSampleData();
        
        System.out.println(" Payroll Service ready with thread pool support");
    }
    
    private void initializeSampleData() {
        try {
            // Get current month in format "2024-04"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String currentMonth = sdf.format(new Date());
            
            // Create sample salary records for existing employees
            List<Employee> employees = hrService.getAllEmployees();
            for (Employee emp : employees) {
                String key = emp.getEmployeeId() + "_" + currentMonth;
                SalaryRecord record = new SalaryRecord(
                    emp.getEmployeeId(), 
                    currentMonth, 
                    emp.getMonthlySalary()
                );
                
                // Set sample working days and leave
                // Assume employee worked 20 days and took 2 days PAID leave
                // So: workingDays = 20, paidLeaveDays = 2, total = 22
                record.setWorkingDays(22); // Full month first
                record.addLeave(2, true);  // Then add 2 days PAID leave
                
                // Mark some as paid for demo
                if (emp.getEmployeeId().equals("EMP002")) {
                    record.setPaymentStatus("PAID", "hr");
                }
                
                salaryRecords.put(key, record);
            }
            
            System.out.println("💰 Payroll Service initialized with " + 
                              salaryRecords.size() + " salary records");
            
        } catch (Exception e) {
            System.out.println("⚠️  Could not initialize payroll data: " + e.getMessage());
        }
    }
    
    // ===== HR METHODS =====
    
    @Override
    public List<Map<String, String>> getEmployeesPaymentStatus() throws RemoteException {
        List<Map<String, String>> result = new ArrayList<>();
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String currentMonth = sdf.format(new Date());
            
            List<Employee> employees = hrService.getAllEmployees();
            for (Employee emp : employees) {
                Map<String, String> empStatus = new LinkedHashMap<>();
                empStatus.put("employeeId", emp.getEmployeeId());
                empStatus.put("name", emp.getFirstName() + " " + emp.getLastName());
                empStatus.put("department", emp.getDepartment());
                empStatus.put("salary", String.format("RM%.2f", emp.getMonthlySalary()));
                
                // Check payment status
                String key = emp.getEmployeeId() + "_" + currentMonth;
                SalaryRecord record = salaryRecords.get(key);
                
                if (record != null) {
                    empStatus.put("status", record.getPaymentStatus());
                    empStatus.put("netSalary", String.format("RM%.2f", record.getNetSalary()));
                } else {
                    empStatus.put("status", "UNPAID");
                    empStatus.put("netSalary", String.format("RM%.2f", emp.getMonthlySalary()));
                }
                
                result.add(empStatus);
            }
        } catch (Exception e) {
            System.err.println("Error getting payment status: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public SalaryRecord getSalaryRecord(String empId, String monthYear) throws RemoteException {
        String key = empId + "_" + monthYear;
        SalaryRecord record = salaryRecords.get(key);
        
        if (record == null) {
            // Create new record if doesn't exist
            try {
                Employee emp = hrService.getEmployeeProfile(empId);
                if (emp != null) {
                    record = new SalaryRecord(empId, monthYear, emp.getMonthlySalary());
                    record.setWorkingDays(22); // Default
                    salaryRecords.put(key, record);
                }
            } catch (Exception e) {
                System.err.println("Error creating salary record: " + e.getMessage());
            }
        }
        
        return record;
    }
    
    @Override
    public boolean processSalaryPayment(String empId, String monthYear, String processedBy) 
            throws RemoteException {
        
        System.out.println("💰 Salary payment requested for " + empId + " (processing async)");
        
        // Process in background thread
        threadPool.submit(() -> {
            try {
                processPaymentAsync(empId, monthYear, processedBy);
            } catch (RemoteException e) {
                System.err.println("Error processing payment: " + e.getMessage());
            }
        });
        
        return true; // Return immediately, process in background
    }
    
        private void processPaymentAsync(String empId, String monthYear, String processedBy) 
            throws RemoteException {
        
        try {
            // Simulate payment processing delay
            Thread.sleep(2000);
            
            SalaryRecord record = getSalaryRecord(empId, monthYear);
            if (record != null && "UNPAID".equals(record.getPaymentStatus())) {
                record.setPaymentStatus("PAID", processedBy);
                System.out.println("✅ Background payment completed: " + empId + " for " + monthYear);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public List<SalaryRecord> getEmployeePaymentHistory(String empId) throws RemoteException {
        List<SalaryRecord> history = new ArrayList<>();
        
        for (SalaryRecord record : salaryRecords.values()) {
            if (record.getEmployeeId().equals(empId)) {
                history.add(record);
            }
        }
        
        // Sort by month (newest first)
        history.sort((a, b) -> b.getMonthYear().compareTo(a.getMonthYear()));
        
        return history;
    }
    
    @Override
    public List<SalaryRecord> getAllSalaryRecords() throws RemoteException {
        List<SalaryRecord> allRecords = new ArrayList<>(salaryRecords.values());
        
        // Sort by month and employee ID
        allRecords.sort((a, b) -> {
            int monthCompare = b.getMonthYear().compareTo(a.getMonthYear());
            if (monthCompare != 0) return monthCompare;
            return a.getEmployeeId().compareTo(b.getEmployeeId());
        });
        
        return allRecords;
    }
    
    // ===== EMPLOYEE METHODS =====
    
    @Override
    public List<SalaryRecord> getMySalaryHistory(String empId) throws RemoteException {
        return getEmployeePaymentHistory(empId); // Same logic
    }
    
    @Override
    public SalaryRecord getMyCurrentSalary(String empId) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String currentMonth = sdf.format(new Date());
        
        return getSalaryRecord(empId, currentMonth);
    }
    
    @Override
    public boolean updateBankAccount(String empId, String bankAccount) throws RemoteException {
        try {
            Employee emp = hrService.getEmployeeProfile(empId);
            if (emp != null) {
                emp.setBankAccount(bankAccount);
                return hrService.updateEmployeeProfile(emp);
            }
        } catch (Exception e) {
            System.err.println("Error updating bank account: " + e.getMessage());
        }
        
        return false;
    }
    
    // ===== LEAVE SYNC METHOD (NEW) =====
    
    public void syncLeaveWithSalary(String empId, String monthYear, int leaveDays, boolean isPaid) 
            throws RemoteException {
        
        String key = empId + "_" + monthYear;
        SalaryRecord record = salaryRecords.get(key);
        
        if (record == null) {
            // Create new record if doesn't exist
            Employee emp = hrService.getEmployeeProfile(empId);
            if (emp != null) {
                record = new SalaryRecord(empId, monthYear, emp.getMonthlySalary());
                salaryRecords.put(key, record);
            }
        }
        
        if (record != null) {
            // Use the addLeave helper method from SalaryRecord
            record.addLeave(leaveDays, isPaid);
            
            System.out.println("🔄 Synced leave for " + empId + ": " + 
                              leaveDays + " days (" + (isPaid ? "PAID" : "UNPAID") + ")");
        }
    }
}