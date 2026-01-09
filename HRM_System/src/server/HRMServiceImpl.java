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
import util.PasswordUtil;
import util.SecurityLogger;
import util.InputValidator;
import java.util.concurrent.ExecutorService;
import java.io.PrintWriter;

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    // thread pool
    private ExecutorService threadPool;

    
    // SIMPLE STORAGE - No Database!
    private Map<String, Employee> employees = new HashMap<>();
    private Map<String, String> leaveApplications = new HashMap<>();
    
    // Separate map for family members
    private Map<String, List<FamilyMember>> familyMembers = new HashMap<>();
    
    // HR Users storage: userId -> passwordHash
    private Map<String, String> hrUsers = new HashMap<>();
    
    public HRMServiceImpl(ExecutorService threadPool) throws RemoteException {
        super();
        this.threadPool = threadPool;

        try {
            // Try to load existing data first
            loadDataFromFile();
            System.out.println(" Loaded existing data from file");

        } catch (Exception e) {
            System.out.println("️  Could not load saved data: " + e.getMessage());
            System.out.println(" Creating fresh data...");

            // Initialize fresh data
            employees = new HashMap<>();
            familyMembers = new HashMap<>();
            leaveApplications = new HashMap<>();
            hrUsers = new HashMap<>();
            
            // Initialize default HR user
            hrUsers.put("hr", PasswordUtil.hashPassword("hr123"));
            System.out.println("✅ Default HR user 'hr' initialized");
                
            System.out.println("🧵 HRM Service ready with thread pool support");
            
            // Save data
            try {
                saveDataToFile();
            } catch (IOException saveError) {
                System.out.println("⚠️  Could not save initial data: " + saveError.getMessage());
            }
        }

        System.out.println("\n✅ HRM Service ready!");
        System.out.println("   HR Login: hr / hr123");
        System.out.println("   🔒 Password security: ENABLED");
    }
    
    // Session Expiry
    private Map<String, Long> sessionTokens = new HashMap<>(); // token -> expiry time
    private Map<String, String> userSessions = new HashMap<>(); // token -> userId

    // Generate session token
    public String createSession(String userId) throws RemoteException {
        String token = "SESS_" + System.currentTimeMillis() + "_" + userId.hashCode();
        long expiryTime = System.currentTimeMillis() + (30 * 60 * 1000); // 30 minutes

        sessionTokens.put(token, expiryTime);
        userSessions.put(token, userId);

        SecurityLogger.logEvent(userId, "SESSION_CREATED", "SUCCESS", "Token: " + token);
        return token;
    }

    // Validate session
    public boolean validateSession(String token) throws RemoteException {
        if (!sessionTokens.containsKey(token)) {
            SecurityLogger.logError("SYSTEM", "INVALID_SESSION", "Token not found");
            return false;
        }

        long expiryTime = sessionTokens.get(token);
        if (System.currentTimeMillis() > expiryTime) {
            sessionTokens.remove(token);
            userSessions.remove(token);
            SecurityLogger.logError("SYSTEM", "SESSION_EXPIRED", "Token: " + token);
            return false;
        }

        // Renew session
        sessionTokens.put(token, System.currentTimeMillis() + (30 * 60 * 1000));
        return true;
    }

    // Get user from session
    public String getUserFromSession(String token) throws RemoteException {
        return userSessions.get(token);
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
            allData.put("hrUsers", hrUsers);
            
            oos.writeObject(allData);
            System.out.println(" HRM data saved to file: hrm_data.dat");
        }
    }
    
    private void loadDataFromFile() throws IOException, ClassNotFoundException {
        File file = new File("hrm_data.dat");
        if (!file.exists()) {
            System.out.println(" No saved data file found. Starting fresh.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("hrm_data.dat"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> allData = (Map<String, Object>) ois.readObject();
            
            employees = (Map<String, Employee>) allData.get("employees");
            familyMembers = (Map<String, List<FamilyMember>>) allData.get("familyMembers");
            leaveApplications = (Map<String, String>) allData.get("leaveApplications");
            
            // Load HR users if available, otherwise initialize default
            if (allData.containsKey("hrUsers")) {
                hrUsers = (Map<String, String>) allData.get("hrUsers");
            } else {
                hrUsers = new HashMap<>();
                hrUsers.put("hr", PasswordUtil.hashPassword("hr123"));
                System.out.println(" Initialized default HR user 'hr'");
            }
            
            System.out.println(" Loaded HRM data:");
            System.out.println(" Employees: " + employees.size());
            System.out.println(" Leave Applications: " + leaveApplications.size());
            System.out.println(" HR Users: " + hrUsers.size());
        }
    }
    
    
    // ===== HR STAFF METHODS =====

    @Override
    public String registerEmployee(String firstName, String lastName, String icPassport) throws RemoteException {

        // ===== VALIDATION FIRST =====
        if (!InputValidator.isValidName(firstName)) {
            SecurityLogger.logError("HR", "INVALID_INPUT", "Invalid first name: " + firstName);
            return "❌ Invalid first name! Use 2-50 letters only.";
        }

        if (!InputValidator.isValidName(lastName)) {
            SecurityLogger.logError("HR", "INVALID_INPUT", "Invalid last name: " + lastName);
            return "❌ Invalid last name! Use 2-50 letters only.";
        }

        if (!InputValidator.isValidIcPassport(icPassport)) {
            SecurityLogger.logError("HR", "INVALID_INPUT", "Invalid IC/Passport: " + icPassport);
            return "❌ Invalid IC/Passport! Use 7-20 alphanumeric characters.";
        }

        // Sanitize inputs
        firstName = InputValidator.sanitizeInput(firstName);
        lastName = InputValidator.sanitizeInput(lastName);
        icPassport = InputValidator.sanitizeInput(icPassport);

        // ===== NOW CREATE EMPLOYEE =====
        String empId = "EMP" + String.format("%03d", employees.size() + 1);
        Employee emp = new Employee(firstName, lastName, icPassport);
        emp.setEmployeeId(empId);
        emp.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@company.com");
        emp.setPhone("012-0000000");
        emp.setDepartment("New Hire");
        emp.setPosition("Trainee");
        emp.setMonthlySalary(3000.00);
        emp.setBankAccount("Not set");

        // Generate and hash password
        String plainPassword = PasswordUtil.generateRandomPassword();
        emp.setPasswordHash(PasswordUtil.hashPassword(plainPassword));

        employees.put(empId, emp);
        familyMembers.put(empId, new ArrayList<>());

        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("⚠️  Could not auto-save: " + e.getMessage());
        }

        // Log successful registration
        SecurityLogger.logSensitiveAction("HR", "REGISTER_EMPLOYEE", empId);

        return "✅ Employee registered!\n" +
               "   ID: " + empId + "\n" +
               "   Name: " + firstName + " " + lastName + "\n" +
               "   Temporary password: " + plainPassword + "\n" +
               "   ⚠️  Change this password on first login!\n" +
               "   Default salary: RM3000.00";
    }
    
    @Override
    public String registerHRUser(String hrUserId, String newHRUserId, String password) throws RemoteException {
        // ===== AUTHORIZATION CHECK - HR USER ONLY =====
        if (hrUserId == null || !hrUsers.containsKey(hrUserId)) {
            SecurityLogger.logError(hrUserId != null ? hrUserId : "UNKNOWN", "UNAUTHORIZED_ACCESS", 
                "Attempted to register HR user without HR privileges");
            return "❌ Access Denied! Only HR users can register new HR accounts.";
        }
        
        // ===== VALIDATION =====
        if (newHRUserId == null || newHRUserId.trim().isEmpty()) {
            SecurityLogger.logError(hrUserId, "INVALID_INPUT", "Empty HR user ID");
            return "❌ Invalid HR User ID! User ID cannot be empty.";
        }
        
        newHRUserId = newHRUserId.trim().toLowerCase();
        
        // Check if HR user already exists
        if (hrUsers.containsKey(newHRUserId)) {
            SecurityLogger.logError(hrUserId, "DUPLICATE_USER", "HR user already exists: " + newHRUserId);
            return "❌ HR User ID already exists! Please choose a different ID.";
        }
        
        // Validate password
        if (password == null || password.length() < 6) {
            SecurityLogger.logError(hrUserId, "INVALID_INPUT", "Password too short");
            return "❌ Invalid password! Password must be at least 6 characters long.";
        }
        
        // Sanitize inputs
        newHRUserId = InputValidator.sanitizeInput(newHRUserId);
        
        // Hash and store password
        String passwordHash = PasswordUtil.hashPassword(password);
        hrUsers.put(newHRUserId, passwordHash);
        
        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("⚠️  Could not auto-save: " + e.getMessage());
        }
        
        // Log successful registration
        SecurityLogger.logSensitiveAction(hrUserId, "REGISTER_HR_USER", newHRUserId);
        
        return "✅ HR User registered successfully!\n" +
               "   HR User ID: " + newHRUserId + "\n" +
               "   Password: " + password + "\n" +
               "   ⚠️  Save these credentials securely!";
    }
    
    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        System.out.println(" Returning " + employees.size() + " employees");
        return new ArrayList<>(employees.values());
    }
    
    @Override
    public String generateYearlyReport(String employeeId) throws RemoteException {
        System.out.println("📊 Yearly report requested for: " + employeeId);

        // Check if employee exists first (fast check)
        if (!employees.containsKey(employeeId)) {
            return "❌ Employee not found!";
        }

        // Log the request
        SecurityLogger.logDataAccess("HR", "YEARLY_REPORT", employeeId);

        // Submit to thread pool for background processing
        threadPool.submit(() -> {
            try {
                generateReportInBackground(employeeId);
            } catch (Exception e) {
                System.err.println("❌ Error generating report for " + employeeId + ": " + e.getMessage());
            }
        });

        // Return immediately - report will be generated in background
        return "✅ Yearly report generation started for employee: " + employeeId + "\n" +
               "   Report ID: RPT" + System.currentTimeMillis() + "\n" +
               "   Status: Processing in background...\n" +
               "   Yearly Report Saved in File!.\n";
    }

    // Private method that runs in background thread
    private void generateReportInBackground(String employeeId) {
        try {
            System.out.println("🧵 Background thread started for report: " + employeeId);

            // Simulate complex/long report generation
            Thread.sleep(3000); // 3 second delay

            Employee emp = employees.get(employeeId);
            if (emp == null) {
                System.out.println("❌ Employee not found in background: " + employeeId);
                return;
            }

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
            report.append("\nGenerated by thread: ").append(Thread.currentThread().getName());

            // Save report to file (simulated)
            String filename = "reports/report_" + employeeId + "_" + 
                             new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
            saveReportToFile(filename, report.toString());

            System.out.println("✅ Background report completed: " + employeeId);
            System.out.println("   Saved to: " + filename);
            System.out.println("   Thread: " + Thread.currentThread().getName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Report generation interrupted for: " + employeeId);
        } catch (Exception e) {
            System.err.println("❌ Error in background report generation: " + e.getMessage());
        }
    }

    // Helper method to save report to file
    private void saveReportToFile(String filename, String content) {
        try {
            // Create reports directory if it doesn't exist
            File dir = new File("reports");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(filename);
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(content);
            }

            System.out.println("💾 Report saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Could not save report to file: " + e.getMessage());
        }
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
                System.out.println(" Could not auto-save: " + e.getMessage());
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
        if (emp == null) return " Employee not found!";
        
        // Check if balance is sufficient, but DON'T deduct yet - wait for HR approval
        if (emp.getLeaveBalance() >= days) {
            String appId = "LV" + System.currentTimeMillis();
            leaveApplications.put(appId, employeeId + "|" + days + "|" + reason + "|Pending");
            
            // DO NOT deduct balance or sync with payroll here
            // Balance will be deducted only when HR approves the leave
            
            // Auto-save
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println(" Could not auto-save: " + e.getMessage());
            }
            
            return "Leave application submitted!\n" +
                   "   Application ID: " + appId + "\n" +
                   "   Days: " + days + "\n" +
                   "   Current Balance: " + emp.getLeaveBalance() + " days\n" +
                   "   Status: Pending HR approval\n" +
                   "   Note: Balance will be deducted only after HR approval.";
        }
        return "Insufficient leave balance!\n" +
               "   Requested: " + days + " days\n" +
               "   Available: " + emp.getLeaveBalance() + " days";
    }
    
    // ===== NEW METHOD: Apply for unpaid leave =====
    @Override
    public String applyForUnpaidLeave(String employeeId, int days, String reason) throws RemoteException {
        Employee emp = employees.get(employeeId);
        if (emp == null) return " Employee not found!";
        
        String appId = "ULV" + System.currentTimeMillis(); // ULV = Unpaid Leave
        leaveApplications.put(appId, employeeId + "|" + days + "|" + reason + "|Unpaid|Pending");
        
        // DO NOT sync with payroll here - wait for HR approval
        // Payroll will be synced only when HR approves the leave
        
        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("️  Could not auto-save: " + e.getMessage());
        }
        
        return " Unpaid leave application submitted!\n" +
               "   Application ID: " + appId + "\n" +
               "   Days: " + days + "\n" +
               "   Status: Pending HR approval\n" +
               "   Note: Unpaid leave will deduct from salary only after HR approval.";
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
            System.out.println("️  Could not auto-save: " + e.getMessage());
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
                System.out.println("️  Could not auto-save: " + e.getMessage());
            }
        }
        
        return removedFromEmployee || removedFromMap;
    }
    
    // ===== AUTHENTICATION =====
    @Override
    public boolean authenticate(String userId, String password) throws RemoteException {
        String clientIP = "RMI-Client"; // In real RMI: getClientHost()

        try {
            // HR login - check from hrUsers map
            if (hrUsers.containsKey(userId)) {
                String storedHash = hrUsers.get(userId);
                boolean authenticated = PasswordUtil.verifyPassword(password, storedHash);
                
                if (authenticated) {
                    SecurityLogger.logLogin(userId, true, clientIP);
                    System.out.println(" HR authentication successful: " + userId);
                    return true;
                } else {
                    SecurityLogger.logLogin(userId, false, clientIP);
                    SecurityLogger.logError(userId, "AUTH_FAILURE", "Invalid HR password");
                    System.out.println(" HR authentication failed for: " + userId);
                    return false;
                }
            }

            // Employee login by ID
            Employee emp = employees.get(userId);
            if (emp != null) {
                // Check password
                boolean authenticated = false;

                if (emp.getPasswordHash() != null && !emp.getPasswordHash().isEmpty()) {
                    authenticated = PasswordUtil.verifyPassword(password, emp.getPasswordHash());
                } else {
                    // Fallback for old data
                    authenticated = "password123".equals(password);
                }

                if (authenticated) {
                    SecurityLogger.logLogin(userId, true, clientIP);
                    SecurityLogger.logDataAccess(userId, "EMPLOYEE_PROFILE", emp.getEmployeeId());
                    System.out.println(" Employee authentication successful: " + userId);
                } else {
                    SecurityLogger.logLogin(userId, false, clientIP);
                    SecurityLogger.logError(userId, "AUTH_FAILURE", "Invalid password");
                    System.out.println(" Authentication failed for: " + userId);
                }
                return authenticated;
            }

            // Login by email
            for (Employee e : employees.values()) {
                if (e.getEmail() != null && e.getEmail().equals(userId)) {
                    boolean authenticated = false;

                    if (e.getPasswordHash() != null && !e.getPasswordHash().isEmpty()) {
                        authenticated = PasswordUtil.verifyPassword(password, e.getPasswordHash());
                    } else {
                        authenticated = "password123".equals(password);
                    }

                    if (authenticated) {
                        SecurityLogger.logLogin(e.getEmployeeId(), true, clientIP + " (via email)");
                        return true;
                    }
                    break;
                }
            }

            SecurityLogger.logLogin(userId, false, clientIP);
            SecurityLogger.logError("SYSTEM", "USER_NOT_FOUND", "User ID: " + userId);
            return false;

        } catch (Exception e) {
            SecurityLogger.logError(userId, "AUTH_EXCEPTION", e.getMessage());
            throw new RemoteException("Authentication error");
        }
    }
    
    @Override
    public boolean isHRUser(String userId) throws RemoteException {
        if (userId == null) {
            return false;
        }
        return hrUsers.containsKey(userId.toLowerCase());
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
    public boolean changePassword(String userId, String oldPassword, String newPassword) throws RemoteException {
        // HR password change
        if (hrUsers.containsKey(userId)) {
            String storedHash = hrUsers.get(userId);
            // Verify old password
            if (!PasswordUtil.verifyPassword(oldPassword, storedHash)) {
                SecurityLogger.logError(userId, "PASSWORD_CHANGE_FAILED", "Invalid old password");
                System.out.println(" HR password change failed: Old password incorrect");
                return false;
            }
            // Validate new password
            if (newPassword == null || newPassword.length() < 6) {
                SecurityLogger.logError(userId, "PASSWORD_CHANGE_FAILED", "New password too short");
                return false;
            }
            // Update password
            String newHash = PasswordUtil.hashPassword(newPassword);
            hrUsers.put(userId, newHash);
            // Save to file
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("⚠️  Could not auto-save password change: " + e.getMessage());
            }
            SecurityLogger.logSensitiveAction(userId, "PASSWORD_CHANGED", "HR password changed");
            System.out.println(" HR password changed successfully for: " + userId);
            return true;
        }

        // Employee password change
        Employee emp = employees.get(userId);
        if (emp == null || emp.getPasswordHash() == null) {
            System.out.println(" Password change failed: Employee not found - " + userId);
            return false;
        }

        // Verify old password
        if (!PasswordUtil.verifyPassword(oldPassword, emp.getPasswordHash())) {
            System.out.println(" Password change failed: Old password incorrect for " + userId);
            return false;
        }

        // Set new password (hashed)
        emp.setPasswordHash(PasswordUtil.hashPassword(newPassword));

        // Auto-save
        try {
            saveDataToFile();
        } catch (IOException e) {
            System.out.println("️  Could not auto-save password change: " + e.getMessage());
        }
        
        SecurityLogger.logPasswordChange(userId);
        
        System.out.println(" Password changed successfully for: " + userId);
        return true;
    }
    
    @Override
    public boolean updateLeaveStatus(String applicationId, String status, String processedBy) throws RemoteException {
        String currentStatus = leaveApplications.get(applicationId);
        if (currentStatus == null) {
            return false; // Application not found
        }

        String[] parts = currentStatus.split("\\|");
        if (parts.length >= 4) {
            String employeeId = parts[0];
            int leaveDays = Integer.parseInt(parts[1]);
            String originalStatus = (parts.length > 3) ? parts[3] : "";
            
            // Determine if it was paid or unpaid leave
            // Unpaid leaves have format: "employeeId|days|reason|Unpaid|Pending"
            // Paid leaves have format: "employeeId|days|reason|Pending"
            boolean isPaidLeave = !currentStatus.contains("|Unpaid|");
            
            boolean isApproved = status.contains("Approved") || status.contains("approved");
            boolean isRejected = status.contains("Rejected") || status.contains("rejected");
            boolean wasPending = originalStatus.contains("Pending") || originalStatus.contains("pending");
            boolean wasApproved = originalStatus.contains("Approved") || originalStatus.contains("approved");
            
            Employee emp = employees.get(employeeId);
            
            // If APPROVING a pending leave: deduct balance (if paid) and sync with payroll
            if (isApproved && wasPending && emp != null) {
                // For paid leave, check if balance is still sufficient
                if (isPaidLeave && emp.getLeaveBalance() < leaveDays) {
                    System.out.println("⚠️  Cannot approve leave: Insufficient balance for " + employeeId);
                    return false; // Cannot approve if balance is insufficient
                }
                
                // Deduct leave balance only for paid leave
                if (isPaidLeave) {
                    emp.setLeaveBalance(emp.getLeaveBalance() - leaveDays);
                    System.out.println("✅ Deducted " + leaveDays + " days from leave balance for " + employeeId);
                }
                
                // Sync with payroll (for both paid and unpaid leave)
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                    String currentMonth = sdf.format(new Date());
                    
                    PayrollService payrollService = (PayrollService) 
                        Naming.lookup("rmi://localhost:1098/PayrollService");
                    
                    // Sync leave with payroll
                    payrollService.syncLeaveWithSalary(employeeId, currentMonth, leaveDays, isPaidLeave);
                    System.out.println("✅ Synced " + leaveDays + " " + (isPaidLeave ? "paid" : "unpaid") + 
                                      " leave(s) with payroll for " + employeeId);
                    
                } catch (Exception e) {
                    System.out.println("⚠️  Could not sync leave with payroll: " + e.getMessage());
                }
            }
            
            // If REJECTING an APPROVED leave: restore balance and remove from payroll
            // (This handles the case where HR might reject an already-approved leave)
            if (isRejected && wasApproved && emp != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                    String currentMonth = sdf.format(new Date());
                    
                    PayrollService payrollService = (PayrollService) 
                        Naming.lookup("rmi://localhost:1098/PayrollService");
                    
                    // Remove leave from salary calculation
                    payrollService.removeLeaveFromSalary(employeeId, currentMonth, leaveDays, isPaidLeave);
                    
                    // Restore leave balance if it was a paid leave
                    if (isPaidLeave) {
                        emp.setLeaveBalance(emp.getLeaveBalance() + leaveDays);
                        System.out.println("✅ Restored " + leaveDays + " days to leave balance for " + employeeId);
                    }
                    
                    System.out.println("✅ Removed " + leaveDays + " " + (isPaidLeave ? "paid" : "unpaid") + 
                                      " leave(s) from payroll for " + employeeId);
                    
                } catch (Exception e) {
                    System.out.println("⚠️  Could not remove leave from payroll: " + e.getMessage());
                }
            }
            
            // If REJECTING a PENDING leave: do nothing (balance was never deducted)
            // This is the correct behavior - no changes needed
            
            // Reconstruct with new status
            String newStatus = parts[0] + "|" + parts[1] + "|" + parts[2] + "|" + status + " by " + processedBy;
            leaveApplications.put(applicationId, newStatus);
            
            // Auto-save
            try {
                saveDataToFile();
            } catch (IOException e) {
                System.out.println("️  Could not auto-save: " + e.getMessage());
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
    
    @Override
    public List<Map<String, String>> getSecurityLogs() throws RemoteException {
        List<Map<String, String>> logs = new ArrayList<>();

        System.out.println("=== READING SECURITY LOGS ===");

        try {
            // FIXED: Use correct filename
            File logFile = new File("security_audit.log");

            if (!logFile.exists()) {
                System.out.println("❌ Security log file not found:  " + logFile.getAbsolutePath());
                return logs;
            }

            System.out.println("✓ Found log file: " + logFile.getAbsolutePath());
            System.out.println("✓ File size: " + logFile.length() + " bytes");

            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            int lineNumber = 0;

            while ((line = reader. readLine()) != null) {
                lineNumber++;

                // Skip empty lines and separator lines
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("??? ") || 
                    line.contains("Log file: ") || line.contains("Last modified:") || 
                    line. contains("Total entries:")) {
                    continue;
                }

                try {
                    Map<String, String> logEntry = parseSecurityAuditLine(line);

                    if (logEntry != null && !logEntry.isEmpty()) {
                        logs.add(logEntry);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error parsing line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
            }

            reader.close();

            System.out.println("✅ Loaded " + logs.size() + " security logs from " + lineNumber + " lines");

        } catch (IOException e) {
            System.err.println("❌ Error reading security logs: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Parse a single log line from security_audit.log
     * Format: [timestamp] USER=userId | ACTION=action | STATUS=status | DETAILS=details
     */
    private Map<String, String> parseSecurityAuditLine(String line) {
        Map<String, String> logEntry = new HashMap<>();

        try {
            // Example:  [2025-12-27 00:46:41] USER=hr | ACTION=LOGIN_ATTEMPT | STATUS=SUCCESS | DETAILS=Source: RMI-Client

            if (! line.contains("[") || !line.contains("]")) {
                return null;
            }

            // Extract timestamp
            int timestampStart = line.indexOf("[");
            int timestampEnd = line.indexOf("]");
            if (timestampStart == -1 || timestampEnd == -1) {
                return null;
            }

            String timestamp = line.substring(timestampStart + 1, timestampEnd).trim();

            // Extract the rest of the line
            String remainder = line.substring(timestampEnd + 1).trim();

            // Parse USER=, ACTION=, STATUS=, DETAILS=
            String userId = extractField(remainder, "USER=");
            String action = extractField(remainder, "ACTION=");
            String status = extractField(remainder, "STATUS=");
            String details = extractField(remainder, "DETAILS=");

            // Extract IP address if present
            String ipAddress = "N/A";
            if (details.contains("Source: ")) {
                String[] parts = details.split("Source:");
                if (parts.length > 1) {
                    ipAddress = parts[1].trim();
                }
            }

            // Clean up action names for display
            action = cleanupActionName(action);

            logEntry.put("timestamp", timestamp);
            logEntry.put("userId", userId);
            logEntry.put("action", action);
            logEntry.put("description", details);
            logEntry.put("ipAddress", ipAddress);
            logEntry.put("status", status);

            return logEntry;

        } catch (Exception e) {
            System.err.println("❌ Exception parsing log line: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract field value from log line
     * Example: "USER=hr | ACTION=LOGIN" -> extractField(line, "USER=") returns "hr"
     */
    private String extractField(String line, String fieldName) {
        try {
            int startIndex = line.indexOf(fieldName);
            if (startIndex == -1) {
                return "N/A";
            }

            startIndex += fieldName.length();

            // Find the end (either " | " or end of line)
            int endIndex = line.indexOf(" | ", startIndex);
            if (endIndex == -1) {
                endIndex = line. length();
            }

            return line.substring(startIndex, endIndex).trim();

        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Clean up action names for better display
     */
    private String cleanupActionName(String action) {
        return action.replace("LOGIN_ATTEMPT", "Login")
                     .replace("PASSWORD_CHANGE", "Password Change")
                     .replace("DATA_ACCESS", "Data Access")
                     .replace("SENSITIVE_ACTION", "Sensitive Action")
                     .replace("SECURITY_ERROR", "Security Error")
                     .replace("_", " ");
    }
}