package ui. hr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import remote.HRMService;
import java.util.List;
import java.util.Map;

public class LeaveManagementPanel extends JPanel {
    private HRMService hrService;
    private JTable leaveTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    
    public LeaveManagementPanel(HRMService hrService) {
        this.hrService = hrService;
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadLeaveApplications();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Leave Applications Management");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        filterCombo = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        filterCombo.setFont(new Font("Arial", Font. PLAIN, 14));
        filterCombo.addActionListener(e -> filterLeaveApplications());
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadLeaveApplications());
        
        actionPanel.add(filterLabel);
        actionPanel.add(filterCombo);
        actionPanel.add(refreshBtn);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout. EAST);
        
        // Table
        String[] columns = {"Application ID", "Employee ID", "Employee Name", "Days", "Reason", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        leaveTable = new JTable(tableModel);
        leaveTable.setFont(new Font("Arial", Font. PLAIN, 13));
        leaveTable.setRowHeight(35);
        
        // Custom header renderer
        leaveTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        leaveTable.getTableHeader().setFont(new Font("Arial", Font. BOLD, 13));
        leaveTable.getTableHeader().setBackground(new Color(52, 73, 94));
        leaveTable.getTableHeader().setForeground(Color.WHITE);
        leaveTable.getTableHeader().setOpaque(true);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaveTable.setSelectionBackground(new Color(70, 130, 180));
        leaveTable.setSelectionForeground(Color.WHITE);
        leaveTable.setGridColor(new Color(220, 220, 220));
        
        // Set column widths
        leaveTable. setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(130);  // Application ID
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Employee ID
        leaveTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Employee Name
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(60);   // Days
        leaveTable. getColumnModel().getColumn(4).setPreferredWidth(200);  // Reason
        leaveTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Status
        leaveTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Actions ✅ REDUCED width
        
        // Custom renderer for Actions column
        leaveTable.getColumn("Actions").setCellRenderer(new LeaveActionsCellRenderer());
        
        // Mouse listener for button clicks
        leaveTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = leaveTable.rowAtPoint(evt.getPoint());
                int col = leaveTable.columnAtPoint(evt.getPoint());
                
                if (row >= 0 && col == 6) { // Actions column
                    String appId = (String) tableModel.getValueAt(row, 0);
                    String status = (String) tableModel.getValueAt(row, 5);
                    String empName = (String) tableModel.getValueAt(row, 2);
                    
                    if (status.contains("Pending")) {
                        // Show dialog to choose approve or reject
                        Object[] options = {"Approve", "Reject", "Cancel"};
                        int choice = JOptionPane.showOptionDialog(
                            LeaveManagementPanel.this,
                            "Choose action for leave application from " + empName,
                            "Leave Action",
                            JOptionPane. YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                        );
                        
                        if (choice == 0) { // Approve
                            handleApproveLeave(appId, empName);
                        } else if (choice == 1) { // Reject
                            handleRejectLeave(appId, empName);
                        }
                    } else {
                        showLeaveDetails(appId);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        scrollPane. setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory. createEmptyBorder(15, 0, 0, 0));
        
        JLabel totalLabel = createStatLabel("Total Applications:  0", new Color(52, 152, 219));
        JLabel pendingLabel = createStatLabel("Pending:  0", new Color(243, 156, 18));
        JLabel approvedLabel = createStatLabel("Approved: 0", new Color(46, 204, 113));
        
        statsPanel.add(totalLabel);
        statsPanel.add(pendingLabel);
        statsPanel.add(approvedLabel);
        
        // Store for updates
        putClientProperty("totalLabel", totalLabel);
        putClientProperty("pendingLabel", pendingLabel);
        putClientProperty("approvedLabel", approvedLabel);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    // Custom header renderer to fix visibility
    private class CustomHeaderRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        
        public CustomHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
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
    
    // Custom renderer with single "Actions" button
    private class LeaveActionsCellRenderer extends JPanel implements TableCellRenderer {
        private JButton actionsBtn;
        private JButton viewBtn;
        
        public LeaveActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            
            // Single "Actions" button for pending items
            actionsBtn = new JButton("Actions");
            actionsBtn.setBackground(new Color(52, 152, 219)); 
            actionsBtn.setForeground(Color.WHITE);
            actionsBtn.setFocusPainted(false);
            actionsBtn.setBorderPainted(false);
            actionsBtn.setContentAreaFilled(true);
            actionsBtn.setPreferredSize(new Dimension(85, 28));
            actionsBtn.setFont(new Font("Arial", Font.BOLD, 12));
            actionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // View button for processed items
            viewBtn = new JButton("View");
            viewBtn.setBackground(new Color(149, 165, 166));
            viewBtn.setForeground(Color. WHITE);
            viewBtn.setFocusPainted(false);
            viewBtn.setBorderPainted(false);
            viewBtn.setContentAreaFilled(true);
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
            
            // Get status from table
            String status = (String) table.getValueAt(row, 5);
            
            if (status != null && status.contains("Pending")) {
                // Show single "Actions" button
                actionsBtn.setBackground(new Color(52, 152, 219));
                actionsBtn.setForeground(Color.WHITE);
                add(actionsBtn);
            } else {
                // Show view button for approved/rejected
                viewBtn.setBackground(new Color(149, 165, 166));
                viewBtn.setForeground(Color. WHITE);
                add(viewBtn);
            }
            
            revalidate();
            repaint();
            
            return this;
        }
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font. BOLD, 16));
        label.setForeground(color);
        label.setOpaque(true);
        label.setBackground(new Color(236, 240, 241));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }
    
    private void loadLeaveApplications() {
        tableModel.setRowCount(0);
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return hrService.getAllLeaveApplications();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> leaves = get();
                    
                    int total = leaves.size();
                    int pending = 0;
                    int approved = 0;
                    
                    for (Map<String, String> leave : leaves) {
                        String status = leave.get("status");
                        
                        if (status.contains("Pending")) pending++;
                        if (status.contains("Approved")) approved++;
                        
                        tableModel.addRow(new Object[]{
                            leave.get("applicationId"),
                            leave.get("employeeId"),
                            leave.get("employeeName"),
                            leave.get("days"),
                            leave.get("reason"),
                            status,
                            "" // Actions column
                        });
                    }
                    
                    // Update statistics
                    JLabel totalLabel = (JLabel) getClientProperty("totalLabel");
                    JLabel pendingLabel = (JLabel) getClientProperty("pendingLabel");
                    JLabel approvedLabel = (JLabel) getClientProperty("approvedLabel");
                    
                    totalLabel. setText("Total Applications: " + total);
                    pendingLabel.setText("Pending: " + pending);
                    approvedLabel.setText("Approved: " + approved);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeaveManagementPanel.this,
                        "Error loading leave applications: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void filterLeaveApplications() {
        String filter = (String) filterCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return hrService.getAllLeaveApplications();
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> leaves = get();
                    
                    for (Map<String, String> leave : leaves) {
                        String status = leave.get("status");
                        
                        boolean match = filter. equals("All") ||
                            (filter.equals("Pending") && status.contains("Pending")) ||
                            (filter.equals("Approved") && status.contains("Approved")) ||
                            (filter.equals("Rejected") && status.contains("Rejected"));
                        
                        if (match) {
                            tableModel.addRow(new Object[]{
                                leave.get("applicationId"),
                                leave.get("employeeId"),
                                leave. get("employeeName"),
                                leave.get("days"),
                                leave.get("reason"),
                                status,
                                ""
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeaveManagementPanel. this,
                        "Error filtering:  " + e.getMessage(),
                        "Error", JOptionPane. ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void handleApproveLeave(String appId, String empName) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve leave application for " + empName + "?",
            "Approve Leave",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = hrService.updateLeaveStatus(appId, "Approved", "HR");
                if (success) {
                    JOptionPane.showMessageDialog(this, "Leave approved successfully!", "Success", JOptionPane. INFORMATION_MESSAGE);
                    loadLeaveApplications();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to approve leave!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error:  " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleRejectLeave(String appId, String empName) {
        String reason = JOptionPane.showInputDialog(this,
            "Enter rejection reason for " + empName + ":",
            "Reject Leave",
            JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && ! reason.trim().isEmpty()) {
            try {
                boolean success = hrService.updateLeaveStatus(appId, "Rejected:  " + reason, "HR");
                if (success) {
                    JOptionPane.showMessageDialog(this, "Leave rejected!", "Success", JOptionPane. INFORMATION_MESSAGE);
                    loadLeaveApplications();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reject leave!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane. ERROR_MESSAGE);
            }
        }
    }
    
    private void showLeaveDetails(String appId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(appId)) {
                String details = String.format(
                    "Leave Application Details:\n\n" +
                    "Application ID: %s\n" +
                    "Employee:  %s (%s)\n" +
                    "Days: %s\n" +
                    "Reason: %s\n" +
                    "Status: %s",
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 2),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 3),
                    tableModel.getValueAt(i, 4),
                    tableModel.getValueAt(i, 5)
                );
                JOptionPane.showMessageDialog(this, details, "Leave Details", JOptionPane. INFORMATION_MESSAGE);
                break;
            }
        }
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