/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import remote.HRMService;
import remote.PayrollService;
import common.Employee;          
import common.FamilyMember;      
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*; 

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    // SIMPLE STORAGE - No Database!
    private Map<String, Employee> employees = new HashMap<>();
    private Map<String, String> leaveApplications = new HashMap<>();
    
    // Separate map for family members
    private Map<String, List<FamilyMember>> familyMembers = new HashMap<>();
    
    public HRMServiceImpl() throws RemoteException {
        super();
        
        try {
            // Try to load existing data first
            loadDataFromFile();
            System.out.println("✅ Loaded existing data from file");
            
        } catch (Exception e) {
            System.out.println("⚠️  Could not load saved data: " + e.getMessage());
            System.out.println("🔄 Creating fresh data...");
            
            // Initialize fresh data
            employees = new HashMap<>();
            familyMembers = new HashMap<>();
            leaveApplications = new HashMap<>();
            
            // Add sample data
            Employee emp1 = new Employee("John", "Doe", "A1234567");
            emp1.setEmployeeId("EMP001");
            emp1.setEmail("john.doe@company.com");
            emp1.setPhone("012-3456789");
            emp1.setDepartment("IT");
            emp1.setPosition("Software Developer");
            emp1.setJoinDate("2023-01-15");
            emp1.setMonthlySalary(5000.00); // Set salary
            emp1.setBankAccount("1234-5678-9012");
            employees.put("EMP001", emp1);
            familyMembers.put("EMP001", new ArrayList<>());
            
            // Add sample family member for EMP001
            FamilyMember spouse1 = new FamilyMember("Sarah Doe", "Spouse", "S1234567");
            spouse1.setDateOfBirth("1990-05-20");
            emp1.addFamilyMember(spouse1);
            familyMembers.get("EMP001").add(spouse1);
            
            Employee emp2 = new Employee("Jane", "Smith", "B9876543");
            emp2.setEmployeeId("EMP002");
            emp2.setEmail("jane.smith@company.com");
            emp2.setPhone("012-9876543");
            emp2.setDepartment("HR");
            emp2.setPosition("HR Manager");
            emp2.setJoinDate("2022-03-10");
            emp2.setMonthlySalary(6000.00); // Set salary
            emp2.setBankAccount("9876-5432-1098");
            employees.put("EMP002", emp2);
            familyMembers.put("EMP002", new ArrayList<>());
            
            // Add sample leave applications
            leaveApplications.put("LV123456789", "EMP001|3|Annual Vacation|Pending");
            leaveApplications.put("LV987654321", "EMP002|2|Medical Leave|Approved");
            
            // Save the initial data
            try {
                saveDataToFile();
            } catch (IOException saveError) {
                System.out.println("⚠️  Could not save initial data: " + saveError.getMessage());
            }
        }
        
        System.out.println("\n✅ HRM Service ready!");
        System.out.println("   HR Login: hr / hr123");
        System.out.println("   Employee Login: EMP001 / password123");
        System.out.println("   Employee count: " + employees.size());
    }
    
    // ===== FILE PERSISTENCE METHODS =====
    
    private void saveDataToFile() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("hrm_data.dat"))) {
            // Save all data to file
            Map<String, Object> allData = new HashMap<>();
            allData.put("employees", employees);
            allData.put("familyMembers", familyMembers);
            allData.put("leaveApplications", leaveApplications);
            
            oos.writeObject(allData);
            System.out.println("💾 HRM data saved to file: hrm_data.dat");
        }
    }
    
    private void loadDataFromFile() throws IOException, ClassNotFoundException {
        File file = new File("hrm_data.dat");
        if (!file.exists()) {
            System.out.println("📁 No saved data file found. Starting fresh.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("hrm_data.dat"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> allData = (Map<String, Object>) ois.readObject();
            
            employees = (Map<String, Employee>) allData.get("employees");
            familyMembers = (Map<String, List<FamilyMember>>) allData.get("familyMembers");
            leaveApplications = (Map<String, String>) allData.get("leaveApplications");
            
            System.out.println("📂 Loaded HRM data:");
            System.out.println("   Employees: " + employees.size());
            System.out.println("   Leave Applications: " + leaveApplications.size());
        }
    }
    
    // ===== HR STAFF METHODS =====
    @Override
    public String registerEmployee(String firstName, String lastName, String icPassport) throws RemoteException {
        String empId = "EMP" + String.format("%03d", employees.size() + 1);
        Employee emp = new Employee(firstName, lastName, icPassport);
        emp.setEmployeeId(empId);
        emp.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@company.com");
        emp.setPhone("012-0000000");
        emp.setDepartment("New Hire");
        emp.setPosition("Trainee");
        emp.setMonthlySalary(3000.00); // Default starting salary
        emp.setBankAccount("Not set"); // Default bank account
        
        employees.put(empId, emp);
        familyMembers.put(empId, new ArrayList<>());
        
        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("⚠️  Could not auto-save: " + e.getMessage());
        }

        return "✅ Employee registered!\n" +
               "   ID: " + empId + "\n" +
               "   Name: " + firstName + " " + lastName + "\n" +
               "   Default password: password123\n" +
               "   Default salary: RM3000.00";
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        System.out.println("🔍 Returning " + employees.size() + " employees");
        return new ArrayList<>(employees.values());
    }
    
    @Override
    public String generateYearlyReport(String employeeId) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) return "❌ Employee not found!";
        
        StringBuilder report = new StringBuilder();
        report.append("=== YEARLY EMPLOYEE REPORT ===\n\n");
        
        // Basic employee info
        report.append("Employee ID: ").append(emp.getEmployeeId()).append("\n");
        report.append("Name: ").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("\n");
        report.append("IC/Passport: ").append(emp.getIcPassport()).append("\n");
        report.append("Email: ").append(emp.getEmail()).append("\n");
        report.append("Phone: ").append(emp.getPhone()).append("\n");
        report.append("Department: ").append(emp.getDepartment()).append("\n");
        report.append("Position: ").append(emp.getPosition()).append("\n");
        report.append("Join Date: ").append(emp.getJoinDate()).append("\n");
        report.append("Monthly Salary: RM").append(String.format("%.2f", emp.getMonthlySalary())).append("\n");
        report.append("Bank Account: ").append(emp.getBankAccount()).append("\n");
        report.append("Leave Balance: ").append(emp.getLeaveBalance()).append(" days\n\n");
        
        // Family details
        report.append("=== FAMILY MEMBERS ===\n");
        List<FamilyMember> family = familyMembers.getOrDefault(employeeId, new ArrayList<>());
        if (family.isEmpty()) {
            report.append("No family members registered.\n");
        } else {
            for (FamilyMember fm : family) {
                report.append("- ").append(fm.getName())
                      .append(" (").append(fm.getRelationship()).append(")")
                      .append(" - IC: ").append(fm.getIcNumber());
                if (fm.getDateOfBirth() != null && !fm.getDateOfBirth().isEmpty()) {
                    report.append(" - DOB: ").append(fm.getDateOfBirth());
                }
                report.append("\n");
            }
        }
        
        // Leave history
        report.append("\n=== LEAVE HISTORY ===\n");
        int count = 0;
        for (Map.Entry<String, String> entry : leaveApplications.entrySet()) {
            if (entry.getValue().startsWith(employeeId + "|")) {
                String[] parts = entry.getValue().split("\\|");
                report.append("Application: ").append(entry.getKey()).append("\n");
                report.append("  Days: ").append(parts[1])
                      .append(", Reason: ").append(parts[2])
                      .append(", Status: ").append(parts[3]).append("\n");
                count++;
            }
        }
        if (count == 0) {
            report.append("No leave records found.\n");
        }
        
        report.append("\nReport generated on: ").append(new Date());
        return report.toString();
    }
    
    // ===== EMPLOYEE METHODS =====
    @Override
    public Employee getEmployeeProfile(String employeeId) throws RemoteException {
        return employees.get(employeeId);
    }
    
    @Override
    public boolean updateEmployeeProfile(Employee employee) throws RemoteException {
        if (employees.containsKey(employee.getEmployeeId())) {
            // Preserve existing family members
            List<FamilyMember> existingFamily = employees.get(employee.getEmployeeId()).getFamilyMembers();
            employee.setFamilyMembers(existingFamily);
            
            employees.put(employee.getEmployeeId(), employee);
            
            // Auto-save
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("⚠️  Could not auto-save: " + e.getMessage());
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public int checkLeaveBalance(String employeeId) throws RemoteException {
        Employee emp = employees.get(employeeId);
        return (emp != null) ? emp.getLeaveBalance() : -1;
    }
    
    // ===== UPDATED applyForLeave METHOD =====
    @Override
    public String applyForLeave(String employeeId, int days, String reason) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) return "❌ Employee not found!";
        
        if (emp.getLeaveBalance() >= days) {
            String appId = "LV" + System.currentTimeMillis();
            leaveApplications.put(appId, employeeId + "|" + days + "|" + reason + "|Pending");
            emp.setLeaveBalance(emp.getLeaveBalance() - days);
            
            // SYNC WITH PAYROLL - Assume all leave is PAID (using leave balance)
            try {
                // Get current month
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String currentMonth = sdf.format(new Date());
                
                // Connect to payroll service
                PayrollService payrollService = (PayrollService) 
                    Naming.lookup("rmi://localhost:1098/PayrollService");
                
                // Sync leave - using leave balance means it's PAID leave
                payrollService.syncLeaveWithSalary(employeeId, currentMonth, days, true);
                
            } catch (Exception e) {
                System.out.println("⚠️  Could not sync leave with payroll: " + e.getMessage());
            }
            
            // Auto-save
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("⚠️  Could not auto-save: " + e.getMessage());
            }
            
            return "✅ Leave application submitted!\n" +
                   "   Application ID: " + appId + "\n" +
                   "   Days: " + days + "\n" +
                   "   New Balance: " + emp.getLeaveBalance() + " days\n" +
                   "   Note: Leave will affect salary calculation for current month.";
        }
        return "❌ Insufficient leave balance!\n" +
               "   Requested: " + days + " days\n" +
               "   Available: " + emp.getLeaveBalance() + " days";
    }
    
    // ===== NEW METHOD: Apply for unpaid leave =====
    @Override
    public String applyForUnpaidLeave(String employeeId, int days, String reason) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) return "❌ Employee not found!";
        
        String appId = "ULV" + System.currentTimeMillis(); // ULV = Unpaid Leave
        leaveApplications.put(appId, employeeId + "|" + days + "|" + reason + "|Unpaid|Pending");
        
        // SYNC WITH PAYROLL - false means UNPAID leave
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String currentMonth = sdf.format(new Date());
            
            PayrollService payrollService = (PayrollService) 
                Naming.lookup("rmi://localhost:1098/PayrollService");
            
            payrollService.syncLeaveWithSalary(employeeId, currentMonth, days, false);
            
        } catch (Exception e) {
            System.out.println("⚠️  Could not sync unpaid leave with payroll: " + e.getMessage());
        }
        
        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("⚠️  Could not auto-save: " + e.getMessage());
        }
        
        return "✅ Unpaid leave application submitted!\n" +
               "   Application ID: " + appId + "\n" +
               "   Days: " + days + "\n" +
               "   Note: Unpaid leave will deduct from salary.";
    }
    
    // ===== FAMILY MEMBER METHODS =====
    @Override
    public boolean addFamilyMember(String empId, FamilyMember member) throws RemoteException {
        if (!employees.containsKey(empId)) {
            return false;
        }
        
        // Add to Employee object
        employees.get(empId).addFamilyMember(member);
        
        // Add to our map
        if (!familyMembers.containsKey(empId)) {
            familyMembers.put(empId, new ArrayList<>());
        }
        familyMembers.get(empId).add(member);
        
        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("⚠️  Could not auto-save: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public List<FamilyMember> getFamilyMembers(String empId) throws RemoteException {
        return familyMembers.getOrDefault(empId, new ArrayList<>());
    }
    
    @Override
    public boolean removeFamilyMember(String empId, String memberIc) throws RemoteException {
        if (!familyMembers.containsKey(empId)) {
            return false;
        }
        
        // Remove from Employee object
        boolean removedFromEmployee = employees.get(empId).removeFamilyMember(memberIc);
        
        // Remove from our map
        boolean removedFromMap = familyMembers.get(empId)
            .removeIf(member -> member.getIcNumber().equals(memberIc));
        
        // Auto-save if any removal happened
        if (removedFromEmployee || removedFromMap) {
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("⚠️  Could not auto-save: " + e.getMessage());
            }
        }
        
        return removedFromEmployee || removedFromMap;
    }
    
    // ===== AUTHENTICATION =====
    @Override
    public boolean authenticate(String userId, String password) throws RemoteException {
        // HR Admin login
        if ("hr".equals(userId) && "hr123".equals(password)) {
            return true;
        }
        
        // Employee login (using employee ID)
        if (employees.containsKey(userId) && "password123".equals(password)) {
            return true;
        }
        
        // Alternative: login with email
        for (Employee emp : employees.values()) {
            if (emp.getEmail() != null && emp.getEmail().equals(userId) && "password123".equals(password)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public List<Map<String, String>> getAllLeaveApplications() throws RemoteException {
        List<Map<String, String>> allLeaves = new ArrayList<>();

        for (Map.Entry<String, String> entry : leaveApplications.entrySet()) {
            String[] parts = entry.getValue().split("\\|");
            if (parts.length >= 4) {
                Map<String, String> leaveInfo = new LinkedHashMap<>();
                leaveInfo.put("applicationId", entry.getKey());
                leaveInfo.put("employeeId", parts[0]);
                leaveInfo.put("days", parts[1]);
                leaveInfo.put("reason", parts[2]);
                leaveInfo.put("status", parts[3]);

                // Get employee name for display
                Employee emp = employees.get(parts[0]);
                if (emp != null) {
                    leaveInfo.put("employeeName", emp.getFirstName() + " " + emp.getLastName());
                } else {
                    leaveInfo.put("employeeName", "Unknown");
                }

                allLeaves.add(leaveInfo);
            }
        }

        return allLeaves;
    }

    @Override
    public boolean updateLeaveStatus(String applicationId, String status, String processedBy) throws RemoteException {
        String currentStatus = leaveApplications.get(applicationId);
        if (currentStatus == null) {
            return false; // Application not found
        }

        String[] parts = currentStatus.split("\\|");
        if (parts.length >= 4) {
            // Reconstruct with new status
            String newStatus = parts[0] + "|" + parts[1] + "|" + parts[2] + "|" + status + " by " + processedBy;
            leaveApplications.put(applicationId, newStatus);
            
            // Auto-save
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("⚠️  Could not auto-save: " + e.getMessage());
            }
            
            return true;
        }

        return false;
    }

    @Override
    public List<Map<String, String>> getEmployeeLeaveHistory(String employeeId) throws RemoteException {
        List<Map<String, String>> employeeLeaves = new ArrayList<>();

        for (Map.Entry<String, String> entry : leaveApplications.entrySet()) {
            if (entry.getValue().startsWith(employeeId + "|")) {
                String[] parts = entry.getValue().split("\\|");
                if (parts.length >= 4) {
                    Map<String, String> leaveInfo = new LinkedHashMap<>();
                    leaveInfo.put("applicationId", entry.getKey());
                    leaveInfo.put("days", parts[1]);
                    leaveInfo.put("reason", parts[2]);
                    leaveInfo.put("status", parts[3]);
                    employeeLeaves.add(leaveInfo);
                }
            }
        }

        return employeeLeaves;
    }
}