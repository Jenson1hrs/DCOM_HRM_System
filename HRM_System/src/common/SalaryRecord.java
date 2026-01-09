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
    public int getTotalLeaves() { return paidLeaveDays + unpaidLeaveDays; }  // Total leaves in month
    
    // Get display values based on policy: first 4 are paid, rest are unpaid
    public int getDisplayPaidLeaveDays() {
        int total = getTotalLeaves();
        return Math.min(4, total);  // First 4 are paid
    }
    
    public int getDisplayUnpaidLeaveDays() {
        int total = getTotalLeaves();
        return Math.max(0, total - 4);  // From 5th onwards are unpaid
    }
    
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
        // Calculate total leaves taken in the month
        int totalLeaves = paidLeaveDays + unpaidLeaveDays;
        
        // First 4 leaves are paid (no deduction)
        // From the 5th leave onwards, each leave deducts 50
        if (totalLeaves <= 4) {
            // First 4 leaves: no deduction
            this.deductions = 0;
        } else {
            // From 5th leave onwards: deduct 50 per leave
            int unpaidLeavesCount = totalLeaves - 4;
            this.deductions = unpaidLeavesCount * 50.0;
        }
        
        // Calculate net salary (ensure it doesn't go below 0)
        this.netSalary = Math.max(0, baseSalary - deductions);
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
    
    // Remove leave days (for HR system integration - when leave is rejected)
    public void removeLeave(int days, boolean isPaid) {
        if (isPaid) {
            this.paidLeaveDays = Math.max(0, this.paidLeaveDays - days);
        } else {
            this.unpaidLeaveDays = Math.max(0, this.unpaidLeaveDays - days);
        }
        
        // Restore working days
        this.workingDays = Math.min(22, this.workingDays + days);
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
        int totalLeaves = getTotalLeaves();
        return String.format(
            "Base: RM%.2f | Working Days: %d/22 | Total Leaves: %d days (Paid: %d, Unpaid: %d) | Deductions: RM%.2f | Net: RM%.2f",
            baseSalary, workingDays, totalLeaves, paidLeaveDays, unpaidLeaveDays, deductions, netSalary
        );
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s - %s - RM%.2f [%s]", 
            recordId, employeeId, getFormattedMonth(), netSalary, paymentStatus);
    }
}