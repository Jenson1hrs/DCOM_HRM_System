package ui. employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import remote.HRMService;
import remote.PayrollService;
import common.SalaryRecord;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SalaryPanel extends JPanel {
    private HRMService hrService;
    private PayrollService payrollService;
    private String employeeId;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JLabel monthLabel;
    private JLabel baseSalaryLabel;
    private JLabel workingDaysLabel;
    private JLabel totalLeavesLabel;
    private JLabel paidLeaveLabel;
    private JLabel unpaidLeaveLabel;
    private JLabel deductionsLabel;
    private JLabel netSalaryLabel;
    private JLabel statusLabel;
    private JLabel paidOnLabel;
    
    public SalaryPanel(HRMService hrService, PayrollService payrollService, String employeeId) {
        this.hrService = hrService;
        this.payrollService = payrollService;
        this.employeeId = employeeId;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadSalaryData();
    }
    
    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("My Salary & Payment History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout. WEST);
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadSalaryData());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Current Month Salary Panel
        JPanel currentSalaryPanel = createCurrentSalaryPanel();
        
        // History Panel
        JPanel historyPanel = createHistoryPanel();
        
        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, currentSalaryPanel, historyPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createCurrentSalaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Current Month Salary",
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                new Color(52, 73, 94)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        
        monthLabel = new JLabel("Loading...");
        baseSalaryLabel = new JLabel("Loading...");
        workingDaysLabel = new JLabel("Loading...");
        totalLeavesLabel = new JLabel("Loading...");
        paidLeaveLabel = new JLabel("Loading...");
        unpaidLeaveLabel = new JLabel("Loading...");
        deductionsLabel = new JLabel("Loading...");
        netSalaryLabel = new JLabel("Loading...");
        statusLabel = new JLabel("Loading...");
        paidOnLabel = new JLabel("Loading...");
        
        // Ensure labels are visible
        baseSalaryLabel.setVisible(true);
        baseSalaryLabel.setOpaque(false);
        
        int row = 0;
        addSalaryField(detailsPanel, gbc, row++, "Month:", monthLabel);
        addSalaryField(detailsPanel, gbc, row++, "Base Salary:", baseSalaryLabel);
        addSalaryField(detailsPanel, gbc, row++, "Working Days:", workingDaysLabel);
        addSalaryField(detailsPanel, gbc, row++, "Total Leaves:", totalLeavesLabel);
        addSalaryField(detailsPanel, gbc, row++, "  - Paid Leave:", paidLeaveLabel);
        addSalaryField(detailsPanel, gbc, row++, "  - Unpaid Leave:", unpaidLeaveLabel);
        addSalaryField(detailsPanel, gbc, row++, "Deductions:", deductionsLabel);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(400, 2));
        detailsPanel. add(separator, gbc);
        gbc.gridwidth = 1;
        
        // Net Salary 
        gbc.gridx = 0; gbc. gridy = row;
        JLabel netLabel = new JLabel("Net Salary:");
        netLabel.setFont(new Font("Arial", Font.BOLD, 16));
        netLabel.setForeground(new Color(46, 204, 113));
        detailsPanel.add(netLabel, gbc);
        
        gbc.gridx = 1;
        netSalaryLabel. setFont(new Font("Arial", Font.BOLD, 20));
        netSalaryLabel.setForeground(new Color(46, 204, 113));
        detailsPanel. add(netSalaryLabel, gbc);
        
        row++;
        addSalaryField(detailsPanel, gbc, row++, "Payment Status:", statusLabel);
        addSalaryField(detailsPanel, gbc, row++, "Paid On:", paidOnLabel);
        
        panel. add(detailsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "Payment History",
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                new Color(52, 73, 94)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Table
        String[] columns = {"Month", "Net Amount", "Status", "Paid On"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Arial", Font. PLAIN, 13));
        historyTable.setRowHeight(30);
        
        historyTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        historyTable.getTableHeader().setFont(new Font("Arial", Font. BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(52, 73, 94));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setOpaque(true);
        historyTable.getTableHeader().setReorderingAllowed(false);
        
        historyTable.setSelectionMode(ListSelectionModel. SINGLE_SELECTION);
        
        historyTable.setSelectionBackground(new Color(70, 130, 180));
        historyTable.setSelectionForeground(Color.WHITE);
        historyTable.setGridColor(new Color(220, 220, 220));
        
        historyTable. setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(200);  // Month
        historyTable. getColumnModel().getColumn(1).setPreferredWidth(150);  // Net Amount
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Status
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(200);  // Paid On
        
        // Double-click to view details
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = historyTable.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        historyTable.setRowSelectionInterval(row, row);
                        showHistoryDetails(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // ✅ FIX:  Enable scrollbars
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info Label
        JLabel infoLabel = new JLabel("Double-click on a row to view detailed salary breakdown");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Custom header renderer to fix visibility
    private class CustomHeaderRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        
        public CustomHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants. CENTER);
            setFont(new Font("Arial", Font. BOLD, 13));
            setBackground(new Color(52, 73, 94));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
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
    
    private void addSalaryField(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font. BOLD, 14));
        panel.add(jLabel, gbc);
        
        gbc. gridx = 1;
        gbc.weightx = 0.6;
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(52, 73, 94));
        panel.add(valueLabel, gbc);
    }
    
    private void loadSalaryData() {
        // Load current month salary
        SwingWorker<SalaryRecord, Void> currentWorker = new SwingWorker<SalaryRecord, Void>() {
            @Override
            protected SalaryRecord doInBackground() throws Exception {
                return payrollService.getMyCurrentSalary(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    SalaryRecord record = get();
                    
                    if (record != null) {
                        monthLabel.setText(record.getFormattedMonth());
                        // Format base salary with proper formatting
                        double baseSalary = record.getBaseSalary();
                        baseSalaryLabel.setText("RM " + String.format("%.2f", baseSalary));
                        baseSalaryLabel.setVisible(true);
                        baseSalaryLabel.revalidate();
                        baseSalaryLabel.repaint();
                        workingDaysLabel. setText(record.getWorkingDays() + " / 22 days");
                        int totalLeaves = record.getTotalLeaves();
                        totalLeavesLabel.setText(totalLeaves + " days" + 
                            (totalLeaves > 4 ? " (First 4 paid, " + (totalLeaves - 4) + " @ RM50 each)" : ""));
                        paidLeaveLabel.setText(record.getDisplayPaidLeaveDays() + " days");
                        unpaidLeaveLabel.setText(record.getDisplayUnpaidLeaveDays() + " days");
                        deductionsLabel.setText("RM " + String.format("%.2f", record.getDeductions()));
                        netSalaryLabel. setText("RM " + String. format("%.2f", record. getNetSalary()));
                        
                        String status = record.getPaymentStatus();
                        statusLabel.setText(status);
                        statusLabel.setForeground(status.equals("PAID") ? new Color(46, 204, 113) : new Color(231, 76, 60));
                        
                        if (record.getPaymentDate() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            paidOnLabel.setText(sdf.format(record.getPaymentDate()));
                        } else {
                            paidOnLabel.setText("Not paid yet");
                        }
                    } else {
                        monthLabel. setText("No record found");
                        baseSalaryLabel.setText("N/A");
                        workingDaysLabel.setText("N/A");
                        totalLeavesLabel.setText("N/A");
                        paidLeaveLabel.setText("N/A");
                        unpaidLeaveLabel.setText("N/A");
                        deductionsLabel. setText("N/A");
                        netSalaryLabel.setText("N/A");
                        statusLabel.setText("N/A");
                        paidOnLabel.setText("N/A");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SalaryPanel.this,
                        "Error loading current salary: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        currentWorker.execute();
        
        // Load salary history
        tableModel.setRowCount(0);
        
        SwingWorker<List<SalaryRecord>, Void> historyWorker = new SwingWorker<List<SalaryRecord>, Void>() {
            @Override
            protected List<SalaryRecord> doInBackground() throws Exception {
                return payrollService.getMySalaryHistory(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    List<SalaryRecord> history = get();
                    
                    if (history == null || history.isEmpty()) {
                        tableModel.addRow(new Object[]{"No salary records", "", "", ""});
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        
                        for (SalaryRecord record : history) {
                            String paidOn = "Not paid";
                            if (record.getPaymentDate() != null) {
                                try {
                                    paidOn = sdf.format(record. getPaymentDate());
                                } catch (Exception dateEx) {
                                    paidOn = "Invalid date";
                                }
                            }
                            
                            tableModel.addRow(new Object[]{
                                record.getFormattedMonth(),
                                "RM " + String.format("%.2f", record.getNetSalary()),
                                record.getPaymentStatus(),
                                paidOn
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane. showMessageDialog(SalaryPanel.this,
                        "Error loading salary history: " + e. getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        historyWorker.execute();
    }
    
    private void showHistoryDetails(int row) {
        String month = (String) tableModel.getValueAt(row, 0);
        
        if (month == null || month.equals("No salary records")) {
            return;
        }
        
        // Extract month in yyyy-MM format
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy");
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM");
            Date date = displayFormat.parse(month);
            final String monthKey = apiFormat.format(date);
            
            SwingWorker<SalaryRecord, Void> worker = new SwingWorker<SalaryRecord, Void>() {
                @Override
                protected SalaryRecord doInBackground() throws Exception {
                    List<SalaryRecord> history = payrollService.getMySalaryHistory(employeeId);
                    
                    // Find matching record
                    for (SalaryRecord r : history) {
                        if (r.getMonthYear().equals(monthKey)) {
                            return r;
                        }
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        SalaryRecord record = get();
                        
                        if (record != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String paidOn = "Not paid";
                            if (record.getPaymentDate() != null) {
                                try {
                                    paidOn = sdf.format(record.getPaymentDate());
                                } catch (Exception dateEx) {
                                    paidOn = "Invalid date";
                                }
                            }
                            
                            String processedBy = "N/A";
                            if (record.getProcessedBy() != null && !record.getProcessedBy().isEmpty()) {
                                processedBy = record.getProcessedBy();
                            }
                            
                            int totalLeaves = record.getTotalLeaves();
                            String details = String. format(
                                "Salary Details - %s\n\n" +
                                "Base Salary: RM %.2f\n" +
                                "Working Days: %d / 22\n" +
                                "Total Leaves: %d days\n" +
                                "  - Paid Leave: %d days\n" +
                                "  - Unpaid Leave: %d days\n" +
                                "Deductions: RM %.2f%s\n" +
                                "---------------------\n" +
                                "Net Salary: RM %.2f\n\n" +
                                "Status: %s\n" +
                                "Paid On: %s\n" +
                                "Processed By:  %s",
                                record.getFormattedMonth(),
                                record.getBaseSalary(),
                                record.getWorkingDays(),
                                totalLeaves,
                                record.getDisplayPaidLeaveDays(),
                                record.getDisplayUnpaidLeaveDays(),
                                record.getDeductions(),
                                totalLeaves > 4 ? " (First 4 paid, " + (totalLeaves - 4) + " @ RM50 each)" : "",
                                record.getNetSalary(),
                                record.getPaymentStatus(),
                                paidOn,
                                processedBy
                            );
                            
                            JOptionPane.showMessageDialog(SalaryPanel.this,
                                details,
                                "Salary Details",
                                JOptionPane. INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(SalaryPanel.this,
                                "No salary record found for this month",
                                "Not Found",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane. showMessageDialog(SalaryPanel.this,
                            "Error:  " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        e. printStackTrace();
                    }
                }
            };
            worker. execute();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error parsing date: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font. BOLD, 13));
    }
}