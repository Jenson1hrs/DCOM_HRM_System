package ui. hr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import remote.HRMService;
import remote.PayrollService;
import common.SalaryRecord;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PayrollManagementPanel extends JPanel {
    private HRMService hrService;
    private PayrollService payrollService;
    private JTable payrollTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JLabel totalEmployeesLabel;
    private JLabel paidLabel;
    private JLabel unpaidLabel;
    private JLabel totalAmountLabel;
    
    public PayrollManagementPanel(HRMService hrService, PayrollService payrollService) {
        this.hrService = hrService;
        this.payrollService = payrollService;
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadPayrollData();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String currentMonth = sdf.format(new Date());
        
        JLabel titleLabel = new JLabel("Payroll Management - " + currentMonth);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Arial", Font. PLAIN, 14));
        
        filterCombo = new JComboBox<>(new String[]{"All", "Unpaid Only", "Paid Only"});
        filterCombo.setFont(new Font("Arial", Font. PLAIN, 14));
        filterCombo.addActionListener(e -> filterPayrollData());
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadPayrollData());
        
        actionPanel.add(filterLabel);
        actionPanel.add(filterCombo);
        actionPanel.add(refreshBtn);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);
        
        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory. createEmptyBorder(0, 0, 15, 0));
        
        totalEmployeesLabel = createStatLabel("Total:  0", new Color(52, 152, 219));
        paidLabel = createStatLabel("Paid: 0", new Color(46, 204, 113));
        unpaidLabel = createStatLabel("Unpaid: 0", new Color(231, 76, 60));
        totalAmountLabel = createStatLabel("Total:  RM 0", new Color(155, 89, 182));
        
        statsPanel.add(totalEmployeesLabel);
        statsPanel.add(paidLabel);
        statsPanel.add(unpaidLabel);
        statsPanel.add(totalAmountLabel);
        
        // Table
        String[] columns = {"Employee ID", "Name", "Department", "Base Salary", "Net Salary", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        payrollTable = new JTable(tableModel);
        payrollTable.setFont(new Font("Arial", Font. PLAIN, 13));
        payrollTable.setRowHeight(35);
        
        // Custom header renderer
        payrollTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        payrollTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        payrollTable.getTableHeader().setBackground(new Color(52, 73, 94));
        payrollTable.getTableHeader().setForeground(Color.WHITE);
        payrollTable.getTableHeader().setOpaque(true);
        payrollTable.getTableHeader().setReorderingAllowed(false);
        
        payrollTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection colors (THIS WAS MISSING!)
        payrollTable.setSelectionBackground(new Color(70, 130, 180));  // Little dark blue
        payrollTable.setSelectionForeground(Color.WHITE);
        payrollTable.setGridColor(new Color(220, 220, 220));
        
        // Set column widths
        payrollTable.setAutoResizeMode(JTable. AUTO_RESIZE_OFF);
        payrollTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Employee ID
        payrollTable. getColumnModel().getColumn(1).setPreferredWidth(150);  // Name
        payrollTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Department
        payrollTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Base Salary
        payrollTable. getColumnModel().getColumn(4).setPreferredWidth(120);  // Net Salary
        payrollTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Status
        payrollTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Actions
        
        // Custom renderer for Actions column
        payrollTable.getColumn("Actions").setCellRenderer(new PayrollActionsCellRenderer());
        
        // Mouse listener for actions
        payrollTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event. MouseEvent evt) {
                int row = payrollTable.rowAtPoint(evt.getPoint());
                int col = payrollTable.columnAtPoint(evt.getPoint());
                
                if (row >= 0 && col == 6) { // Actions column
                    String empId = (String) tableModel.getValueAt(row, 0);
                    String status = (String) tableModel.getValueAt(row, 5);
                    
                    if ("UNPAID".equals(status)) {
                        handleProcessPayment(empId);
                    } else {
                        showPaymentDetails(empId);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Enable scrollbars
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout. CENTER);
    }
    
    // Custom header renderer to fix visibility
    private class CustomHeaderRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        
        public CustomHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font. BOLD, 13));
            setBackground(new Color(52, 73, 94));
            setForeground(Color.WHITE);
            setBorder(BorderFactory. createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(40, 55, 70)),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
            ));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            return this;
        }
    }
    
    // Custom renderer for Actions buttons
    private class PayrollActionsCellRenderer extends JPanel implements TableCellRenderer {
        private JButton processBtn;
        private JButton viewBtn;
        
        public PayrollActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout. CENTER, 5, 5));
            setOpaque(true);
            
            processBtn = new JButton("Process");
            processBtn.setBackground(new Color(46, 204, 113));
            processBtn.setForeground(Color.WHITE);
            processBtn.setFocusPainted(false);
            processBtn.setBorderPainted(false);
            processBtn.setContentAreaFilled(true);
            processBtn.setPreferredSize(new Dimension(80, 28));
            processBtn.setFont(new Font("Arial", Font. BOLD, 12));
            processBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            viewBtn = new JButton("View");
            viewBtn.setBackground(new Color(52, 152, 219));
            viewBtn. setForeground(Color.WHITE);
            viewBtn.setFocusPainted(false);
            viewBtn.setBorderPainted(false);
            viewBtn. setContentAreaFilled(true);
            viewBtn.setPreferredSize(new Dimension(70, 28));
            viewBtn.setFont(new Font("Arial", Font.BOLD, 12));
            viewBtn.setCursor(new Cursor(Cursor. HAND_CURSOR));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            removeAll();
            
            // Set background
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }
            
            String status = (String) tableModel.getValueAt(row, 5);
            
            if ("UNPAID".equals(status)) {
                // Ensure button colors are set
                processBtn.setBackground(new Color(46, 204, 113));
                processBtn.setForeground(Color. WHITE);
                add(processBtn);
            } else {
                // Ensure button colors are set
                viewBtn. setBackground(new Color(52, 152, 219));
                viewBtn.setForeground(Color.WHITE);
                add(viewBtn);
            }
            
            revalidate();
            repaint();
            
            return this;
        }
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color. darker(), 2),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        return label;
    }
    
    // Helper method to safely parse salary strings
    private double parseSalary(String salaryStr) {
        try {
            // Remove "RM", commas, and spaces
            String cleaned = salaryStr.replace("RM", "").replace(",", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            System.err.println("Error parsing salary: " + salaryStr);
            e.printStackTrace();
            return 0.0;
        }
    }
    
    // Helper method to format salary for display
    private String formatSalary(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return "RM " + df.format(amount);
    }
    
    private void loadPayrollData() {
        tableModel.setRowCount(0);
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return payrollService.getEmployeesPaymentStatus();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> paymentStatus = get();
                    
                    int total = paymentStatus.size();
                    int paid = 0;
                    int unpaid = 0;
                    double totalAmount = 0;
                    
                    for (Map<String, String> emp : paymentStatus) {
                        String status = emp.get("status");
                        
                        // Safely parse net salary
                        double netSalary = parseSalary(emp.get("netSalary"));
                        
                        if ("PAID".equals(status)) paid++;
                        if ("UNPAID".equals(status)) unpaid++;
                        
                        totalAmount += netSalary;
                        
                        tableModel.addRow(new Object[]{
                            emp.get("employeeId"),
                            emp. get("name"),
                            emp.get("department"),
                            emp.get("salary"),
                            emp.get("netSalary"),
                            status,
                            "Click to action"
                        });
                    }
                    
                    // Update statistics
                    totalEmployeesLabel.setText("Total: " + total);
                    paidLabel. setText("Paid: " + paid);
                    unpaidLabel. setText("Unpaid: " + unpaid);
                    totalAmountLabel.setText("Total: " + formatSalary(totalAmount));
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PayrollManagementPanel.this,
                        "Error loading payroll data:\n" + e.getMessage() + "\n\nCheck console for details.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void filterPayrollData() {
        String filter = (String) filterCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return payrollService.getEmployeesPaymentStatus();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> paymentStatus = get();
                    
                    for (Map<String, String> emp : paymentStatus) {
                        String status = emp.get("status");
                        
                        boolean match = filter.equals("All") ||
                            (filter.equals("Unpaid Only") && "UNPAID".equals(status)) ||
                            (filter.equals("Paid Only") && "PAID".equals(status));
                        
                        if (match) {
                            tableModel.addRow(new Object[]{
                                emp.get("employeeId"),
                                emp.get("name"),
                                emp.get("department"),
                                emp.get("salary"),
                                emp.get("netSalary"),
                                status,
                                "Click to action"
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PayrollManagementPanel.this,
                        "Error filtering:  " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void handleProcessPayment(String empId) {
        SwingWorker<SalaryRecord, Void> worker = new SwingWorker<SalaryRecord, Void>() {
            @Override
            protected SalaryRecord doInBackground() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String currentMonth = sdf.format(new Date());
                return payrollService.getSalaryRecord(empId, currentMonth);
            }
            
            @Override
            protected void done() {
                try {
                    SalaryRecord record = get();
                    
                    if (record == null) {
                        JOptionPane.showMessageDialog(PayrollManagementPanel.this,
                            "No salary record found for employee " + empId + "\n\n" +
                            "This employee may not have a salary record for this month.",
                            "No Record Found",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Build the details string WITHOUT special characters
                    StringBuilder detailsBuilder = new StringBuilder();
                    detailsBuilder.append("Process Salary Payment\n\n");
                    detailsBuilder.append("Employee ID: ").append(empId).append("\n");
                    detailsBuilder.append("Month: ").append(record.getFormattedMonth()).append("\n");
                    detailsBuilder. append("Base Salary: RM ").append(String.format("%.2f", record.getBaseSalary())).append("\n");
                    detailsBuilder.append("Working Days: ").append(record.getWorkingDays()).append("/22\n");
                    detailsBuilder.append("Total Leaves: ").append(record.getTotalLeaves()).append(" days\n");
                    detailsBuilder.append("  - Paid Leave: ").append(record.getDisplayPaidLeaveDays()).append(" days\n");
                    detailsBuilder.append("  - Unpaid Leave: ").append(record.getDisplayUnpaidLeaveDays()).append(" days\n");
                    detailsBuilder.append("Deductions: RM ").append(String.format("%.2f", record.getDeductions())).append("\n");
                    if (record.getTotalLeaves() > 4) {
                        detailsBuilder.append("  (First 4 leaves paid, ").append(record.getTotalLeaves() - 4)
                                      .append(" unpaid leaves @ RM50 each)\n");
                    }
                    detailsBuilder.append("---------------------\n");
                    detailsBuilder.append("Net Salary: RM ").append(String.format("%.2f", record.getNetSalary())).append("\n\n");
                    detailsBuilder.append("Confirm payment? ");
                    
                    String details = detailsBuilder.toString();
                    
                    int confirm = JOptionPane.showConfirmDialog(
                        PayrollManagementPanel.this,
                        details,
                        "Confirm Payment",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        processPayment(empId, record);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PayrollManagementPanel.this,
                        "Error:  " + e.getMessage() + "\n\nCheck console for details.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker. execute();
    }
    
    private void processPayment(String empId, SalaryRecord record) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String currentMonth = sdf.format(new Date());
                return payrollService.processSalaryPayment(empId, currentMonth, "HR");
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        String successMsg = "Payment processed successfully!\n\n" +
                                          "Amount: RM " + String.format("%.2f", record.getNetSalary()) + "\n" +
                                          "Employee: " + empId;
                        
                        JOptionPane.showMessageDialog(
                            PayrollManagementPanel.this,
                            successMsg,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        loadPayrollData();
                    } else {
                        JOptionPane.showMessageDialog(
                            PayrollManagementPanel.this,
                            "Payment failed!\n\nEmployee may already be paid for this month.",
                            "Error",
                            JOptionPane. ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        PayrollManagementPanel.this,
                        "Error:  " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
    
    private void showPaymentDetails(String empId) {
        SwingWorker<SalaryRecord, Void> worker = new SwingWorker<SalaryRecord, Void>() {
            @Override
            protected SalaryRecord doInBackground() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String currentMonth = sdf.format(new Date());
                return payrollService.getSalaryRecord(empId, currentMonth);
            }
            
            @Override
            protected void done() {
                try {
                    SalaryRecord record = get();
                    
                    if (record != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String paymentDate = record.getPaymentDate() != null ? 
                            dateFormat.format(record.getPaymentDate()) : "N/A";
                        
                        StringBuilder detailsBuilder = new StringBuilder();
                        detailsBuilder. append("Payment Details\n\n");
                        detailsBuilder.append("Employee ID: ").append(empId).append("\n");
                        detailsBuilder.append("Month: ").append(record.getFormattedMonth()).append("\n");
                        detailsBuilder.append("Base Salary: RM ").append(String.format("%.2f", record.getBaseSalary())).append("\n");
                        detailsBuilder.append("Working Days: ").append(record.getWorkingDays()).append("/22\n");
                        detailsBuilder.append("Total Leaves: ").append(record.getTotalLeaves()).append(" days\n");
                        detailsBuilder.append("  - Paid Leave: ").append(record.getDisplayPaidLeaveDays()).append(" days\n");
                        detailsBuilder.append("  - Unpaid Leave: ").append(record.getDisplayUnpaidLeaveDays()).append(" days\n");
                        detailsBuilder.append("Deductions: RM ").append(String.format("%.2f", record. getDeductions())).append("\n");
                        if (record.getTotalLeaves() > 4) {
                            detailsBuilder.append("  (First 4 leaves paid, ").append(record.getTotalLeaves() - 4)
                                          .append(" unpaid leaves @ RM50 each)\n");
                        }
                        detailsBuilder.append("Net Salary: RM ").append(String.format("%.2f", record.getNetSalary())).append("\n");
                        detailsBuilder. append("Status: ").append(record.getPaymentStatus()).append("\n");
                        detailsBuilder. append("Paid On: ").append(paymentDate).append("\n");
                        detailsBuilder.append("Processed By: ");
                        detailsBuilder.append(record. getProcessedBy() != null ? record.getProcessedBy() : "N/A");
                        
                        String details = detailsBuilder.toString();
                        
                        JOptionPane.showMessageDialog(
                            PayrollManagementPanel.this,
                            details,
                            "Payment Details",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            PayrollManagementPanel.this,
                            "No payment record found for this employee.",
                            "Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        PayrollManagementPanel.this,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font. PLAIN, 13));
    }
}