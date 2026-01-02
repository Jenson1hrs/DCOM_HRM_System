package ui. employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import remote.HRMService;
import java.util.List;
import java.util.Map;

public class LeavePanel extends JPanel {
    private HRMService hrService;
    private String employeeId;
    private JTable leaveTable;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;
    
    public LeavePanel(HRMService hrService, String employeeId) {
        this.hrService = hrService;
        this.employeeId = employeeId;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadLeaveData();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("My Leave Management");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        
        JButton applyLeaveBtn = new JButton("Apply for Leave");
        styleButton(applyLeaveBtn, new Color(46, 204, 113));
        applyLeaveBtn.addActionListener(e -> showApplyLeaveDialog());
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadLeaveData());
        
        actionPanel.add(applyLeaveBtn);
        actionPanel.add(refreshBtn);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);
        
        // Balance Panel
        JPanel balancePanel = new JPanel();
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balancePanel.setBackground(new Color(52, 152, 219));
        balancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        balancePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JLabel balanceTitleLabel = new JLabel("Available Leave Balance");
        balanceTitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        balanceTitleLabel.setForeground(Color.WHITE);
        balanceTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        balanceLabel = new JLabel("Loading...");
        balanceLabel. setFont(new Font("Arial", Font.BOLD, 36));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        balancePanel.add(Box.createVerticalGlue());
        balancePanel.add(balanceTitleLabel);
        balancePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        balancePanel.add(balanceLabel);
        balancePanel.add(Box.createVerticalGlue());
        
        // Table
        String[] columns = {"Application ID", "Days", "Reason", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        leaveTable = new JTable(tableModel);
        leaveTable.setFont(new Font("Arial", Font. PLAIN, 13));
        leaveTable.setRowHeight(35);
        
        leaveTable. getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        leaveTable.getTableHeader().setFont(new Font("Arial", Font. BOLD, 13));
        leaveTable.getTableHeader().setBackground(new Color(52, 73, 94));
        leaveTable.getTableHeader().setForeground(Color.WHITE);
        leaveTable.getTableHeader().setOpaque(true);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        leaveTable.setSelectionBackground(new Color(70, 130, 180));
        leaveTable.setSelectionForeground(Color.WHITE);
        leaveTable.setGridColor(new Color(220, 220, 220));
        
        leaveTable. setAutoResizeMode(JTable. AUTO_RESIZE_OFF);
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(180);  // Application ID
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Days
        leaveTable. getColumnModel().getColumn(2).setPreferredWidth(250);  // Reason
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(200);  // Status
        leaveTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Actions
        
        // Custom renderer for Actions column with proper button
        leaveTable.getColumn("Actions").setCellRenderer(new LeaveActionsCellRenderer());
        
        // Mouse listener for view button
        leaveTable.addMouseListener(new java.awt. event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = leaveTable.rowAtPoint(evt.getPoint());
                int col = leaveTable.columnAtPoint(evt.getPoint());
                
                if (row >= 0 && col == 4) { // Actions column
                    showLeaveDetails(row);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(balancePanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
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
    
    // Custom renderer for Actions column
    private class LeaveActionsCellRenderer extends JPanel implements TableCellRenderer {
        private JButton viewBtn;
        
        public LeaveActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            
            viewBtn = new JButton("View");
            viewBtn.setBackground(new Color(52, 152, 219));
            viewBtn.setForeground(Color.WHITE);
            viewBtn.setFocusPainted(false);
            viewBtn.setBorderPainted(false);
            viewBtn.setContentAreaFilled(true);
            viewBtn.setPreferredSize(new Dimension(80, 28));
            viewBtn.setFont(new Font("Arial", Font.BOLD, 12));
            viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            
            // Always show view button with proper colors
            viewBtn.setBackground(new Color(52, 152, 219));
            viewBtn.setForeground(Color.WHITE);
            add(viewBtn);
            
            revalidate();
            repaint();
            
            return this;
        }
    }
    
    private void loadLeaveData() {
        // Load leave balance
        SwingWorker<Integer, Void> balanceWorker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return hrService.checkLeaveBalance(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    int balance = get();
                    balanceLabel.setText(balance + " days");
                } catch (Exception e) {
                    balanceLabel.setText("Error");
                }
            }
        };
        balanceWorker.execute();
        
        // Load leave history
        tableModel.setRowCount(0);
        
        SwingWorker<List<Map<String, String>>, Void> historyWorker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return hrService.getEmployeeLeaveHistory(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, String>> leaves = get();
                    
                    if (leaves. isEmpty()) {
                        tableModel.addRow(new Object[]{"No leave records", "", "", "", ""});
                    } else {
                        for (Map<String, String> leave : leaves) {
                            tableModel.addRow(new Object[]{
                                leave.get("applicationId"),
                                leave.get("days"),
                                leave. get("reason"),
                                leave.get("status"),
                                ""
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeavePanel.this,
                        "Error loading leave history: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        historyWorker. execute();
    }
    
    private void showApplyLeaveDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply for Leave", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Apply for Leave");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Days
        gbc.gridy = 1; gbc. gridwidth = 1;
        JLabel daysLabel = new JLabel("Number of Days:");
        daysLabel.setFont(new Font("Arial", Font. PLAIN, 14));
        panel.add(daysLabel, gbc);
        
        gbc.gridx = 1;
        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 30, 1);
        JSpinner daysSpinner = new JSpinner(spinnerModel);
        daysSpinner.setFont(new Font("Arial", Font. PLAIN, 14));
        ((JSpinner.DefaultEditor) daysSpinner.getEditor()).getTextField().setEditable(false);
        panel.add(daysSpinner, gbc);
        
        // Reason
        gbc.gridx = 0; gbc. gridy = 2;
        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(reasonLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JTextArea reasonArea = new JTextArea(5, 20);
        reasonArea.setFont(new Font("Arial", Font.PLAIN, 14));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        panel.add(reasonScroll, gbc);
        
        // Buttons
        gbc.gridy = 4; gbc.gridwidth = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton submitBtn = new JButton("Submit Application");
        styleButton(submitBtn, new Color(46, 204, 113));
        submitBtn.addActionListener(e -> {
            int days = (Integer) daysSpinner.getValue();
            String reason = reasonArea. getText().trim();
            
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a reason for leave!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            submitLeaveApplication(days, reason);
            dialog.dispose();
        });
        
        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void submitLeaveApplication(int days, String reason) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return hrService.applyForLeave(employeeId, days, reason);
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    
                    if (result.contains("✅")) {
                        JOptionPane. showMessageDialog(LeavePanel. this,
                            result,
                            "Success",
                            JOptionPane. INFORMATION_MESSAGE);
                        loadLeaveData(); // Refresh data
                    } else {
                        JOptionPane.showMessageDialog(LeavePanel.this,
                            result,
                            "Information",
                            JOptionPane. WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LeavePanel.this,
                        "Error:  " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker. execute();
    }
    
    private void showLeaveDetails(int row) {
        String appId = (String) tableModel.getValueAt(row, 0);
        String days = (String) tableModel.getValueAt(row, 1);
        String reason = (String) tableModel.getValueAt(row, 2);
        String status = (String) tableModel.getValueAt(row, 3);
        
        if (appId. equals("No leave records")) {
            return;
        }
        
        String details = String.format(
            "Leave Application Details\n\n" +
            "Application ID: %s\n" +
            "Days Requested: %s\n" +
            "Reason: %s\n" +
            "Status: %s\n\n" +
            "%s",
            appId,
            days,
            reason,
            status,
            status. contains("Pending") ? "ℹ️ This application is pending HR approval." : ""
        );
        
        JOptionPane.showMessageDialog(this, details, "Leave Details", JOptionPane.INFORMATION_MESSAGE);
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