/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SalaryRecord implements Serializable {
    private static final long serialVersionUID = 3L;
    
    private String recordId;
    private String employeeId;
    private String monthYear; // Format: "2024-04"
    private double baseSalary;
    private int workingDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;  // NEW: Track unpaid leave separately
    private double deductions;
    private double netSalary;
    private String paymentStatus; // "UNPAID", "PAID"
    private Date paymentDate;
    private String processedBy; // HR user ID
    
    // Constructor
    public SalaryRecord(String employeeId, String monthYear, double baseSalary) {
        this.recordId = "SAL" + System.currentTimeMillis();
        this.employeeId = employeeId;
        this.monthYear = monthYear;
        this.baseSalary = baseSalary;
        this.workingDays = 22; // Default working days per month
        this.paidLeaveDays = 0;
        this.unpaidLeaveDays = 0; 
        this.deductions = 0;
        this.netSalary = baseSalary;
        this.paymentStatus = "UNPAID";
        this.paymentDate = null;
        this.processedBy = null;
        
        calculateNetSalary(); // Initial calculation
    }
    
    // ===== GETTERS =====
    public String getRecordId() { return recordId; }
    public String getEmployeeId() { return employeeId; }
    public String getMonthYear() { return monthYear; }
    public double getBaseSalary() { return baseSalary; }
    public int getWorkingDays() { return workingDays; }
    public int getPaidLeaveDays() { return paidLeaveDays; }
    public int getUnpaidLeaveDays() { return unpaidLeaveDays; }  // NEW GETTER
    public double getDeductions() { return deductions; }
    public double getNetSalary() { return netSalary; }
    public String getPaymentStatus() { return paymentStatus; }
    public Date getPaymentDate() { return paymentDate; }
    public String getProcessedBy() { return processedBy; }
    
    // ===== SETTERS =====
    public void setWorkingDays(int days) { 
        this.workingDays = days; 
        calculateNetSalary();
    }
    
    public void setPaidLeaveDays(int days) { 
        this.paidLeaveDays = days; 
        calculateNetSalary();
    }
    
    public void setUnpaidLeaveDays(int days) {  // NEW SETTER
        this.unpaidLeaveDays = days; 
        calculateNetSalary();
    }
    
    public void setPaymentStatus(String status, String processedBy) {
        this.paymentStatus = status;
        this.processedBy = processedBy;
        if ("PAID".equals(status)) {
            this.paymentDate = new Date();
        }
    }
    
    // ===== CALCULATION LOGIC =====
    private void calculateNetSalary() {
        double dailyRate = baseSalary / 22; // Assume 22 working days/month

        // Calculate total absence days (working days less than 22)
        int totalAbsence = 22 - workingDays;

        // Check if we have enough paid leave to cover absence
        int paidCoverage = Math.min(paidLeaveDays, totalAbsence);
        int unpaidAbsence = Math.max(0, totalAbsence - paidCoverage);

        // Only deduct for UNPAID absence
        this.deductions = unpaidAbsence * dailyRate;
        this.netSalary = baseSalary - deductions;
    }
    
    // ===== HELPER METHODS =====
    
    // Add leave days (for HR system integration)
    public void addLeave(int days, boolean isPaid) {
        if (isPaid) {
            this.paidLeaveDays += days;
        } else {
            this.unpaidLeaveDays += days;
        }
        
        // Reduce working days by leave taken
        this.workingDays = Math.max(0, this.workingDays - days);
        calculateNetSalary();
    }
    
    // Format monthYear for display
    public String getFormattedMonth() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy");
            return outputFormat.format(inputFormat.parse(monthYear));
        } catch (Exception e) {
            return monthYear;
        }
    }
    
    // Get daily rate for display
    public double getDailyRate() {
        return baseSalary / 22;
    }
    
    // Get summary for display
    public String getSalarySummary() {
        return String.format(
            "Base: RM%.2f | Working Days: %d/22 | Paid Leave: %d days | Unpaid Leave: %d days | Net: RM%.2f",
            baseSalary, workingDays, paidLeaveDays, unpaidLeaveDays, netSalary
        );
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s - %s - RM%.2f [%s]", 
            recordId, employeeId, getFormattedMonth(), netSalary, paymentStatus);
    }
}