package ui. hr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import remote.HRMService;
import remote.PayrollService;
import common.Employee;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.io.File;
import javax.swing.filechooser. FileNameExtensionFilter;

public class ReportsPanel extends JPanel {
    private HRMService hrService;
    private PayrollService payrollService;
    private JComboBox<String> yearCombo;
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> employeeCombo;
    private JPanel employeeSelectionPanel;
    private JTextArea reportPreviewArea;
    private Map<String, String> employeeMap; // empId -> display name
    
    public ReportsPanel(HRMService hrService, PayrollService payrollService) {
        this.hrService = hrService;
        this.payrollService = payrollService;
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadEmployeeList();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Generate Report",
                0, 0,
                new Font("Arial", Font.BOLD, 16)
            ),
            BorderFactory. createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Report Type
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel typeLabel = new JLabel("Report Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        reportTypeCombo = new JComboBox<>(new String[]{
            "Yearly Summary Report",
            "Employee Report (All)",
            "Specific Employee Report",
            "Payroll Report",
            "Leave Report",
            "Department Report"
        });
        reportTypeCombo. setFont(new Font("Arial", Font.PLAIN, 14));
        reportTypeCombo.setPreferredSize(new Dimension(350, 35));
        reportTypeCombo.addActionListener(e -> toggleEmployeeSelection());
        controlPanel.add(reportTypeCombo, gbc);

        // Employee Selection Panel
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        employeeSelectionPanel = new JPanel(new BorderLayout(10, 10));
        employeeSelectionPanel.setBackground(Color.WHITE);
        employeeSelectionPanel.setVisible(false);

        JLabel empLabel = new JLabel("Select Employee:");
        empLabel.setFont(new Font("Arial", Font.BOLD, 14));

        employeeCombo = new JComboBox<>();
        employeeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        employeeCombo.setPreferredSize(new Dimension(500, 35));
        employeeCombo.setMaximumRowCount(10);

        employeeSelectionPanel.add(empLabel, BorderLayout.WEST);
        employeeSelectionPanel.add(employeeCombo, BorderLayout. CENTER);

        controlPanel.add(employeeSelectionPanel, gbc);

        // Buttons Panel 
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton generateBtn = new JButton("Generate Report");
        styleButton(generateBtn, new Color(52, 152, 219));
        generateBtn.addActionListener(e -> generateReport());

        JButton exportBtn = new JButton("Export to File");
        styleButton(exportBtn, new Color(46, 204, 113));
        exportBtn.addActionListener(e -> exportReport());

        buttonPanel.add(generateBtn);
        buttonPanel.add(exportBtn);

        controlPanel.add(buttonPanel, gbc);

        // Preview Panel
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(149, 165, 166), 2),
                "Report Preview",
                0, 0,
                new Font("Arial", Font.BOLD, 16)
            ),
            BorderFactory. createEmptyBorder(15, 15, 15, 15)
        ));

        reportPreviewArea = new JTextArea();
        reportPreviewArea. setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportPreviewArea.setEditable(false);
        reportPreviewArea. setLineWrap(false);
        reportPreviewArea. setText("Select report type and year, then click 'Generate Report' to preview.");

        JScrollPane scrollPane = new JScrollPane(reportPreviewArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        previewPanel.add(scrollPane, BorderLayout.CENTER);

        // Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlPanel, previewPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.35);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout. CENTER);
    }

    // Load employee list from server
    private void loadEmployeeList() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Employee> employees = hrService.getAllEmployees();
                employeeMap = new java.util.LinkedHashMap<>();
                
                for (Employee emp : employees) {
                    String displayName = emp.getEmployeeId() + " - " + 
                                       emp.getFirstName() + " " + emp.getLastName() + 
                                       " (" + emp.getDepartment() + ")";
                    employeeMap.put(emp.getEmployeeId(), displayName);
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                employeeCombo.removeAllItems();
                for (String displayName : employeeMap.values()) {
                    employeeCombo.addItem(displayName);
                }
                System.out.println("Loaded " + employeeMap.size() + " employees");
            }
        };
        worker.execute();
    }
    
    // Toggle employee selection visibility
    private void toggleEmployeeSelection() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        boolean showEmployeeSelection = "Specific Employee Report".equals(reportType);
        employeeSelectionPanel.setVisible(showEmployeeSelection);
        revalidate();
        repaint();
    }
    
    // Get selected employee ID
    private String getSelectedEmployeeId() {
        String selected = (String) employeeCombo.getSelectedItem();
        if (selected == null) return null;
        
        // Extract employee ID (format: "EMP001 - John Doe (IT)")
        String empId = selected.split(" - ")[0];
        return empId;
    }
    
    // ===== HELPER METHODS FOR SAFE FORMATTING =====
    
    private String formatCurrency(double amount) {
        try {
            return "RM " + String.format("%,.2f", amount);
        } catch (Exception e) {
            System.err.println("Error formatting currency: " + amount);
            return "RM " + amount;
        }
    }
    
    private double parseSalary(String salaryStr) {
        try {
            String cleaned = salaryStr.replace("RM", "")
                                       .replace(",", "")
                                       .replace(" ", "")
                                       .trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            System.err.println("Error parsing salary: [" + salaryStr + "]");
            return 0.0;
        }
    }
    
    // ===== REPORT GENERATION =====
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String year = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
        // Validate employee selection if needed
        if ("Specific Employee Report".equals(reportType)) {
            if (employeeCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                    "Please select an employee first!",
                    "No Employee Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        reportPreviewArea.setText("Generating report...  Please wait.");
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                System.out.println("=== GENERATING REPORT ===");
                System.out.println("Type: " + reportType);
                System.out.println("Year: " + year);
                
                switch (reportType) {
                    case "Yearly Summary Report":  
                        return generateYearlySummaryReport(year);
                    case "Employee Report (All)": 
                        return generateEmployeeReport(year);
                    case "Specific Employee Report": 
                        String empId = getSelectedEmployeeId();
                        System.out.println("Selected Employee: " + empId);
                        return generateSpecificEmployeeReport(empId, year);
                    case "Payroll Report": 
                        return generatePayrollReport(year);
                    case "Leave Report":  
                        return generateLeaveReport(year);
                    case "Department Report":
                        return generateDepartmentReport(year);
                    default:
                        return "Invalid report type";
                }
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    reportPreviewArea.setText(report);
                    reportPreviewArea.setCaretPosition(0);
                    System.out.println("Report generated successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    reportPreviewArea.setText("Error generating report:\n" + e.getMessage() + 
                                             "\n\nCheck console for details.");
                }
            }
        };
        worker.execute();
    }
    
    // ===== NEW METHOD: Generate Specific Employee Report =====
    private String generateSpecificEmployeeReport(String employeeId, String year) throws Exception {
        StringBuilder report = new StringBuilder();
        
        // Get employee data
        Employee emp = hrService.getEmployeeProfile(employeeId);
        
        if (emp == null) {
            return "ERROR: Employee not found:  " + employeeId;
        }
        
        report.append("================================================================================\n");
        report.append("                 EMPLOYEE YEARLY REPORT - ").append(year).append("\n");
        report.append("                 Generated on: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
        report.append("================================================================================\n\n");
        
        // 1. PERSONAL INFORMATION
        report.append("1. PERSONAL INFORMATION\n");
        report.append("   ").append("-". repeat(70)).append("\n");
        report.append("   Employee ID:       ").append(emp.getEmployeeId()).append("\n");
        report.append("   Full Name:        ").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("\n");
        report.append("   IC/Passport:      ").append(emp.getIcPassport()).append("\n");
        report.append("   Email:            ").append(emp.getEmail()).append("\n");
        report.append("   Phone:            ").append(emp.getPhone()).append("\n\n");
        
        // 2. EMPLOYMENT DETAILS
        report.append("2. EMPLOYMENT DETAILS\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        report.append("   Department:       ").append(emp.getDepartment()).append("\n");
        report.append("   Position:         ").append(emp.getPosition()).append("\n");
        report.append("   Join Date:        ").append(emp.getJoinDate()).append("\n");
        report.append("   Monthly Salary:   ").append(formatCurrency(emp.getMonthlySalary())).append("\n");
        report.append("   Bank Account:     ").append(emp.getBankAccount()).append("\n");
        report.append("   Leave Balance:    ").append(emp.getLeaveBalance()).append(" days\n\n");
        
        // 3. FAMILY MEMBERS
        report.append("3. FAMILY MEMBERS\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        List<common.FamilyMember> family = hrService.getFamilyMembers(employeeId);
        if (family.isEmpty()) {
            report.append("   No family members registered.\n\n");
        } else {
            for (common.FamilyMember fm : family) {
                report. append("   - ").append(fm.getName())
                      .append(" (").append(fm.getRelationship()).append(")")
                      .append(" - IC: ").append(fm.getIcNumber());
                if (fm.getDateOfBirth() != null && ! fm.getDateOfBirth().isEmpty()) {
                    report. append(" - DOB: ").append(fm.getDateOfBirth());
                }
                report.append("\n");
            }
            report.append("\n");
        }
        
        // 4. LEAVE HISTORY
        report.append("4. LEAVE HISTORY - ").append(year).append("\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        List<Map<String, String>> leaves = hrService.getEmployeeLeaveHistory(employeeId);
        
        if (leaves.isEmpty()) {
            report.append("   No leave records found.\n\n");
        } else {
            int totalDays = 0;
            for (Map<String, String> leave : leaves) {
                report.append("   Application:  ").append(leave.get("applicationId")).append("\n");
                report.append("     Days:       ").append(leave.get("days")).append("\n");
                report.append("     Reason:    ").append(leave.get("reason")).append("\n");
                report.append("     Status:    ").append(leave.get("status")).append("\n\n");
                
                try {
                    totalDays += Integer.parseInt(leave.get("days"));
                } catch (Exception e) {
                    // Skip if can't parse
                }
            }
            report.append("   Total Leave Days Used: ").append(totalDays).append("\n\n");
        }
        
        // 5. SALARY HISTORY (if available)
        report.append("5. SALARY HISTORY - ").append(year).append("\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        
        try {
            List<common.SalaryRecord> salaryHistory = payrollService.getMySalaryHistory(employeeId);
            
            if (salaryHistory.isEmpty()) {
                report.append("   No salary records found.\n\n");
            } else {
                double totalPaid = 0;
                int paidCount = 0;
                
                for (common.SalaryRecord record : salaryHistory) {
                    report.append("   Month:          ").append(record.getFormattedMonth()).append("\n");
                    report.append("   Base Salary:   ").append(formatCurrency(record.getBaseSalary())).append("\n");
                    report.append("   Working Days:  ").append(record.getWorkingDays()).append("/22\n");
                    report. append("   Paid Leave:    ").append(record.getPaidLeaveDays()).append(" days\n");
                    report.append("   Unpaid Leave:  ").append(record.getUnpaidLeaveDays()).append(" days\n");
                    report.append("   Deductions:    ").append(formatCurrency(record.getDeductions())).append("\n");
                    report.append("   Net Salary:    ").append(formatCurrency(record.getNetSalary())).append("\n");
                    report.append("   Status:        ").append(record.getPaymentStatus()).append("\n\n");
                    
                    if ("PAID".equals(record.getPaymentStatus())) {
                        totalPaid += record.getNetSalary();
                        paidCount++;
                    }
                }
                
                report.append("   ").append("-".repeat(70)).append("\n");
                report.append("   Total Paid (").append(paidCount).append(" months): ")
                      .append(formatCurrency(totalPaid)).append("\n\n");
            }
        } catch (Exception e) {
            report.append("   Unable to retrieve salary history.\n\n");
        }
        
        // 6. SUMMARY
        report.append("6. SUMMARY\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        report.append("   Status:           Active\n");
        report.append("   Performance:       ").append(leaves.size() < 5 ? "Good" : "Review Needed").append("\n");
        report.append("   Leave Usage:      ").append(leaves.size()).append(" applications\n");
        report.append("   Current Balance:   ").append(emp.getLeaveBalance()).append(" days\n\n");
        
        report.append("================================================================================\n");
        report.append("                          END OF REPORT\n");
        report.append("================================================================================\n");
        
        return report.toString();
    }
    
    // ===== EXISTING REPORT METHODS (unchanged) =====
    
    private String generateYearlySummaryReport(String year) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                    YEARLY SUMMARY REPORT - ").append(year).append("\n");
        report.append("                    HRM System - Generated on: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
        report.append("================================================================================\n\n");
        
        List<Employee> employees = hrService.getAllEmployees();
        report.append("1. EMPLOYEE STATISTICS\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        report.append("   Total Employees:  ").append(employees.size()).append("\n");
        
        Map<String, Long> deptCount = new java.util.HashMap<>();
        double totalSalary = 0;
        for (Employee emp : employees) {
            String dept = emp.getDepartment();
            deptCount.put(dept, deptCount.getOrDefault(dept, 0L) + 1);
            totalSalary += emp.getMonthlySalary();
        }
        
        report.append("\n   Employees by Department:\n");
        for (Map.Entry<String, Long> entry : deptCount.entrySet()) {
            report. append("      - ").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(" employees\n");
        }
        
        report.append("\n   Total Monthly Payroll: ").append(formatCurrency(totalSalary)).append("\n");
        report.append("   Average Salary: ").append(formatCurrency(totalSalary / employees.size())).append("\n\n");
        
        List<Map<String, String>> leaves = hrService.getAllLeaveApplications();
        int pending = 0, approved = 0, rejected = 0;
        for (Map<String, String> leave : leaves) {
            String status = leave.get("status");
            if (status.contains("Pending")) pending++;
            else if (status.contains("Approved")) approved++;
            else if (status.contains("Rejected")) rejected++;
        }
        
        report.append("2. LEAVE STATISTICS\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        report.append("   Total Leave Applications: ").append(leaves.size()).append("\n");
        report.append("   - Approved: ").append(approved).append("\n");
        report.append("   - Rejected: ").append(rejected).append("\n");
        report.append("   - Pending: ").append(pending).append("\n\n");
        
        List<Map<String, String>> paymentStatus = payrollService.getEmployeesPaymentStatus();
        int paid = 0, unpaid = 0;
        for (Map<String, String> emp : paymentStatus) {
            if ("PAID".equals(emp. get("status"))) paid++;
            else unpaid++;
        }
        
        report.append("3. PAYROLL STATUS (Current Month)\n");
        report.append("   ").append("-".repeat(70)).append("\n");
        report.append("   Total Employees: ").append(paymentStatus.size()).append("\n");
        report.append("   - Paid: ").append(paid).append("\n");
        report.append("   - Unpaid: ").append(unpaid).append("\n\n");
        
        report.append("================================================================================\n");
        report.append("                          END OF REPORT\n");
        report.append("================================================================================\n");
        
        return report.toString();
    }
    
    private String generateEmployeeReport(String year) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                    EMPLOYEE REPORT (ALL) - ").append(year).append("\n");
        report.append("================================================================================\n\n");
        
        List<Employee> employees = hrService.getAllEmployees();
        
        report.append(String.format("%-10s %-25s %-20s %-15s %-15s\n", 
            "ID", "Name", "Department", "Position", "Salary"));
        report.append("-".repeat(90)).append("\n");
        
        for (Employee emp : employees) {
            String name = emp.getFirstName() + " " + emp.getLastName();
            
            String line = String.format("%-10s %-25s %-20s %-15s ",
                emp.getEmployeeId(),
                name. length() > 25 ? name.substring(0, 22) + "..." : name,
                emp.getDepartment().length() > 20 ? emp.getDepartment().substring(0, 17) + "..." : emp.getDepartment(),
                emp.getPosition().length() > 15 ? emp.getPosition().substring(0, 12) + "..." : emp.getPosition());
            
            report.append(line);
            report.append(formatCurrency(emp.getMonthlySalary())).append("\n");
        }
        
        report.append("\nTotal Employees: ").append(employees.size()).append("\n");
        report.append("================================================================================\n");
        
        return report.toString();
    }
    
    private String generatePayrollReport(String year) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                    PAYROLL REPORT - ").append(year).append("\n");
        report.append("================================================================================\n\n");
        
        List<Map<String, String>> paymentStatus = payrollService.getEmployeesPaymentStatus();
        
        report.append(String.format("%-12s %-25s %-15s %-15s %-10s\n", 
            "Employee ID", "Name", "Base Salary", "Net Salary", "Status"));
        report.append("-". repeat(82)).append("\n");
        
        double totalPaid = 0;
        int errorCount = 0;
        
        for (Map<String, String> emp : paymentStatus) {
            try {
                String name = emp.get("name");
                String empId = emp.get("employeeId");
                String baseSalary = emp.get("salary");
                String netSalaryDisplay = emp.get("netSalary");
                String status = emp.get("status");
                
                double netSalary = parseSalary(netSalaryDisplay);
                
                if ("PAID".equals(status)) {
                    totalPaid += netSalary;
                }
                
                String line = String.format("%-12s %-25s %-15s %-15s %-10s\n",
                    empId,
                    name.length() > 25 ? name.substring(0, 22) + "..." : name,
                    baseSalary,
                    netSalaryDisplay,
                    status);
                
                report. append(line);
                
            } catch (Exception e) {
                System.err.println("Error processing employee record: " + e.getMessage());
                errorCount++;
            }
        }
        
        report. append("\n").append("-".repeat(82)).append("\n");
        report.append("Total Paid this month: ").append(formatCurrency(totalPaid)).append("\n");
        
        if (errorCount > 0) {
            report.append("\nWarning: ").append(errorCount)
                  .append(" records had parsing errors\n");
        }
        
        report.append("================================================================================\n");
        
        return report.toString();
    }
    
    private String generateLeaveReport(String year) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                    LEAVE REPORT - ").append(year).append("\n");
        report.append("================================================================================\n\n");
        
        List<Map<String, String>> leaves = hrService.getAllLeaveApplications();
        
        report.append(String.format("%-15s %-12s %-25s %-8s %-20s\n", 
            "Application ID", "Employee ID", "Employee Name", "Days", "Status"));
        report.append("-". repeat(85)).append("\n");
        
        for (Map<String, String> leave : leaves) {
            String name = leave.get("employeeName");
            String status = leave.get("status");
            
            report.append(String.format("%-15s %-12s %-25s %-8s %-20s\n",
                leave.get("applicationId"),
                leave.get("employeeId"),
                name.length() > 25 ? name.substring(0, 22) + "..." : name,
                leave.get("days"),
                status. length() > 20 ? status.substring(0, 17) + "..." : status));
        }
        
        report.append("\nTotal Leave Applications: ").append(leaves.size()).append("\n");
        report.append("================================================================================\n");
        
        return report.toString();
    }
    
    private String generateDepartmentReport(String year) throws Exception {
        StringBuilder report = new StringBuilder();
        report.append("================================================================================\n");
        report.append("                    DEPARTMENT REPORT - ").append(year).append("\n");
        report.append("================================================================================\n\n");
        
        List<Employee> employees = hrService.getAllEmployees();
        
        Map<String, java.util.List<Employee>> deptMap = new java.util.HashMap<>();
        for (Employee emp : employees) {
            deptMap.computeIfAbsent(emp. getDepartment(), k -> new java.util.ArrayList<>()).add(emp);
        }
        
        for (Map.Entry<String, java.util.List<Employee>> entry : deptMap.entrySet()) {
            String dept = entry.getKey();
            java.util.List<Employee> deptEmps = entry.getValue();
            
            report.append("DEPARTMENT: ").append(dept).append("\n");
            report. append("-".repeat(82)).append("\n");
            report.append("Total Employees: ").append(deptEmps.size()).append("\n");
            
            double totalSalary = 0;
            for (Employee emp : deptEmps) {
                totalSalary += emp.getMonthlySalary();
            }
            
            report.append("Total Monthly Cost: ").append(formatCurrency(totalSalary)).append("\n");
            report.append("Average Salary: ").append(formatCurrency(totalSalary / deptEmps.size())).append("\n\n");
            
            report.append(String.format("%-12s %-30s %-20s %-18s\n", "ID", "Name", "Position", "Salary"));
            report.append("-".repeat(82)).append("\n");
            
            for (Employee emp : deptEmps) {
                String name = emp.getFirstName() + " " + emp.getLastName();
                
                String line = String.format("%-12s %-30s %-20s ",
                    emp.getEmployeeId(),
                    name.length() > 30 ? name.substring(0, 27) + "..." : name,
                    emp.getPosition().length() > 20 ? emp.getPosition().substring(0, 17) + "..." : emp.getPosition());
                
                report.append(line);
                report.append(formatCurrency(emp.getMonthlySalary())).append("\n");
            }
            
            report.append("\n\n");
        }
        
        report.append("================================================================================\n");
        return report.toString();
    }
    
    // ===== EXPORT & PRINT =====
    
    private void exportReport() {
        if (reportPreviewArea.getText().isEmpty() || 
            reportPreviewArea.getText().contains("Select report type")) {
            JOptionPane.showMessageDialog(this,
                "Please generate a report first! ",
                "No Report",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setSelectedFile(new File("HRM_Report_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(reportPreviewArea. getText());
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + fileToSave. getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error exporting report:\n" + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(150, 35));
    }
}