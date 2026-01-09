package ui. hr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import remote.HRMService;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecurityLogsPanel extends JPanel {
    private HRMService hrService;
    private String userId;
    private JTable logsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    
    public SecurityLogsPanel(HRMService hrService, String userId) {
        this.hrService = hrService;
        this.userId = userId;
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadSecurityLogs();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Security & Activity Logs");
        titleLabel. setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color. WHITE);
        
        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font. PLAIN, 14));
        
        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn, new Color(52, 152, 219));
        searchBtn.addActionListener(e -> searchLogs());
        
        filterCombo = new JComboBox<>(new String[]{"All", "Login", "Logout", "Update", "Create", "Delete"});
        filterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        filterCombo.addActionListener(e -> filterLogs());
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> loadSecurityLogs());
        
        JButton registerHRBtn = new JButton("Register HR User");
        styleButton(registerHRBtn, new Color(155, 89, 182));
        registerHRBtn.addActionListener(e -> showRegisterHRDialog());
        
        actionPanel.add(new JLabel("Search:"));
        actionPanel.add(searchField);
        actionPanel.add(searchBtn);
        actionPanel.add(new JLabel("Filter:"));
        actionPanel.add(filterCombo);
        actionPanel.add(refreshBtn);
        actionPanel.add(registerHRBtn);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout. EAST);
        
        // Table
        String[] columns = {"Timestamp", "User ID", "Action", "Description", "IP Address", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        logsTable = new JTable(tableModel);
        logsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        logsTable.setRowHeight(30);
        
        // Custom header renderer
        logsTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        logsTable.getTableHeader().setFont(new Font("Arial", Font. BOLD, 13));
        logsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        logsTable.getTableHeader().setForeground(Color.WHITE);
        logsTable.getTableHeader().setOpaque(true);
        logsTable.getTableHeader().setReorderingAllowed(false);
        
        logsTable.setSelectionMode(ListSelectionModel. SINGLE_SELECTION);
        
        // Add selection colors
        logsTable.setSelectionBackground(new Color(70, 130, 180));
        logsTable.setSelectionForeground(Color.WHITE);
        logsTable.setGridColor(new Color(220, 220, 220));
        
        // Set column widths properly
        logsTable.setAutoResizeMode(JTable. AUTO_RESIZE_OFF);
        logsTable.getColumnModel().getColumn(0).setPreferredWidth(150);  // Timestamp
        logsTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // User ID
        logsTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Action
        logsTable.getColumnModel().getColumn(3).setPreferredWidth(350);  // Description
        logsTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // IP Address
        logsTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Status
        
        JScrollPane scrollPane = new JScrollPane(logsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Enable scrollbars
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory. createEmptyBorder(15, 0, 0, 0));
        
        JLabel totalLabel = createStatLabel("Total Logs: 0", new Color(52, 152, 219));
        JLabel todayLabel = createStatLabel("Today: 0", new Color(46, 204, 113));
        JLabel failedLabel = createStatLabel("Failed: 0", new Color(231, 76, 60));
        JLabel activeLabel = createStatLabel("Active Users: 0", new Color(155, 89, 182));
        
        statsPanel.add(totalLabel);
        statsPanel. add(todayLabel);
        statsPanel.add(failedLabel);
        statsPanel.add(activeLabel);
        
        putClientProperty("totalLabel", totalLabel);
        putClientProperty("todayLabel", todayLabel);
        putClientProperty("failedLabel", failedLabel);
        putClientProperty("activeLabel", activeLabel);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
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
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color. darker(), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }
    
    private void loadSecurityLogs() {
        tableModel.setRowCount(0);

        // Update UI to show loading
        JLabel totalLabel = (JLabel) getClientProperty("totalLabel");
        if (totalLabel != null) {
            totalLabel.setText("Loading...");
        }

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                System.out.println("=== LOADING SECURITY LOGS FROM SERVER ===");

                try {
                    // DIRECT CALL to backend server method
                    List<Map<String, String>> logs = hrService. getSecurityLogs();
                    System.out.println("✓ Loaded " + logs.size() + " logs from server");
                    return logs;

                } catch (Exception e) {
                    System.err.println("✗ Error loading logs: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> logs = get();

                    System.out.println("Processing " + logs.size() + " logs.. .");

                    int total = logs.size();
                    int today = 0;
                    int failed = 0;
                    java.util.Set<String> activeUsers = new java.util.HashSet<>();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String todayStr = sdf.format(new Date());

                    for (Map<String, String> log :  logs) {
                        System.out.println("Log: " + log);

                        String timestamp = log.getOrDefault("timestamp", "N/A");
                        String userId = log.getOrDefault("userId", "N/A");
                        String action = log.getOrDefault("action", "N/A");
                        String description = log.getOrDefault("description", "N/A");
                        String ipAddress = log.getOrDefault("ipAddress", "N/A");
                        String status = log.getOrDefault("status", "SUCCESS");

                        // Statistics
                        if (timestamp.startsWith(todayStr)) {
                            today++;
                        }

                        if ("FAILED".equalsIgnoreCase(status) || "FAIL".equalsIgnoreCase(status)) {
                            failed++;
                        }

                        if ("Login".equalsIgnoreCase(action) && "SUCCESS".equalsIgnoreCase(status)) {
                            activeUsers. add(userId);
                        }

                        // Add to table
                        tableModel.addRow(new Object[]{
                            timestamp,
                            userId,
                            action,
                            description,
                            ipAddress,
                            status
                        });
                    }

                    // Update stats
                    JLabel totalLabel = (JLabel) getClientProperty("totalLabel");
                    JLabel todayLabel = (JLabel) getClientProperty("todayLabel");
                    JLabel failedLabel = (JLabel) getClientProperty("failedLabel");
                    JLabel activeLabel = (JLabel) getClientProperty("activeLabel");

                    if (totalLabel != null) totalLabel.setText("Total Logs: " + total);
                    if (todayLabel != null) todayLabel.setText("Today: " + today);
                    if (failedLabel != null) failedLabel.setText("Failed: " + failed);
                    if (activeLabel != null) activeLabel.setText("Active Users: " + activeUsers.size());

                    if (total == 0) {
                        tableModel.addRow(new Object[]{"No logs available", "", "", "", "", ""});
                    }

                    System.out.println("=== LOGS LOADED SUCCESSFULLY ===");
                    System.out.println("Total:  " + total + ", Today: " + today + ", Failed: " + failed);

                } catch (Exception e) {
                    System.err.println("=== ERROR LOADING LOGS ===");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(SecurityLogsPanel.this,
                        "Error loading logs from server:\n" + e.getMessage() +
                        "\n\nPlease check:\n" +
                        "1. Server is running\n" +
                        "2. HRMService. getSecurityLogs() is implemented\n" +
                        "3. Check server console for errors",
                        "Error", JOptionPane.ERROR_MESSAGE);

                    tableModel.addRow(new Object[]{
                        "Error loading logs",
                        "",
                        "",
                        e.getMessage(),
                        "",
                        "ERROR"
                    });

                    // Update stats to show error
                    JLabel totalLabel = (JLabel) getClientProperty("totalLabel");
                    if (totalLabel != null) totalLabel.setText("Error");
                }
            }
        };
        worker.execute();
    }
    
    private void searchLogs() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            loadSecurityLogs();
            return;
        }

        tableModel.setRowCount(0);

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                // Load all logs from server
                return hrService.getSecurityLogs();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> logs = get();

                    int matchCount = 0;
                    for (Map<String, String> log : logs) {
                        String userId = log.getOrDefault("userId", "").toLowerCase();
                        String action = log.getOrDefault("action", "").toLowerCase();
                        String description = log.getOrDefault("description", "").toLowerCase();

                        if (userId.contains(searchText) ||
                            action.contains(searchText) ||
                            description. contains(searchText)) {

                            tableModel.addRow(new Object[]{
                                log.getOrDefault("timestamp", "N/A"),
                                log.getOrDefault("userId", "N/A"),
                                log.getOrDefault("action", "N/A"),
                                log.getOrDefault("description", "N/A"),
                                log.getOrDefault("ipAddress", "N/A"),
                                log.getOrDefault("status", "SUCCESS")
                            });
                            matchCount++;
                        }
                    }

                    if (matchCount == 0) {
                        tableModel.addRow(new Object[]{"No matching logs found", "", "", "", "", ""});
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SecurityLogsPanel.this,
                        "Error searching logs:  " + e.getMessage(),
                        "Error", JOptionPane. ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void filterLogs() {
        String filter = (String) filterCombo.getSelectedItem();

        if ("All".equals(filter)) {
            loadSecurityLogs();
            return;
        }

        tableModel.setRowCount(0);

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                // Load all logs from server
                return hrService.getSecurityLogs();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> logs = get();

                    int matchCount = 0;
                    for (Map<String, String> log : logs) {
                        String action = log.getOrDefault("action", "");

                        if (action.equalsIgnoreCase(filter)) {
                            tableModel.addRow(new Object[]{
                                log.getOrDefault("timestamp", "N/A"),
                                log.getOrDefault("userId", "N/A"),
                                action,
                                log.getOrDefault("description", "N/A"),
                                log.getOrDefault("ipAddress", "N/A"),
                                log.getOrDefault("status", "SUCCESS")
                            });
                            matchCount++;
                        }
                    }

                    if (matchCount == 0) {
                        tableModel.addRow(new Object[]{"No " + filter + " logs found", "", "", "", "", ""});
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane. showMessageDialog(SecurityLogsPanel.this,
                        "Error filtering logs: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showRegisterHRDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New HR User", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        JLabel titleLabel = new JLabel("Register New HR User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        JTextField userIdField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("HR User ID:"), gbc);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");

        styleButton(registerBtn, new Color(155, 89, 182));
        styleButton(cancelBtn, new Color(149, 165, 166));

        registerBtn.addActionListener(e -> {
            String newHRUserId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (newHRUserId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "All fields are required!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, 
                    "Password must be at least 6 characters long!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return hrService.registerHRUser(userId, newHRUserId, password);
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        dialog.dispose();

                        if (result.contains("❌")) {
                            JOptionPane.showMessageDialog(SecurityLogsPanel.this,
                                result,
                                "Registration Failed",
                                JOptionPane.ERROR_MESSAGE);
                        } else {
                            // Show success dialog with credentials
                            showHRCredentialsDialog(newHRUserId, password, result);
                        }
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                    } catch (Exception ex) {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(SecurityLogsPanel.this, 
                            "Error: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                    }
                }
            };
            worker.execute();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showHRCredentialsDialog(String hrUserId, String password, String serverResponse) {
        JDialog credDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "HR User Registered Successfully", true);
        credDialog.setSize(500, 350);
        credDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        JLabel successIcon = new JLabel("✓", SwingConstants.CENTER);
        successIcon.setFont(new Font("Arial", Font.BOLD, 72));
        successIcon.setForeground(new Color(155, 89, 182));
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(successIcon);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel titleLabel = new JLabel("HR User Registered Successfully!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel credPanel = new JPanel(new GridBagLayout());
        credPanel.setBackground(new Color(236, 240, 241));
        credPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userIdLabel = new JLabel("HR User ID:");
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        credPanel.add(userIdLabel, gbc);
        
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(hrUserId, 20);
        userIdField.setEditable(false);
        userIdField.setFont(new Font("Arial", Font.BOLD, 16));
        userIdField.setBackground(Color.WHITE);
        userIdField.setForeground(new Color(52, 73, 94));
        credPanel.add(userIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        credPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JTextField passField = new JTextField(password, 20);
        passField.setEditable(false);
        passField.setFont(new Font("Arial", Font.BOLD, 16));
        passField.setBackground(Color.WHITE);
        passField.setForeground(new Color(52, 73, 94));
        credPanel.add(passField, gbc);
        
        mainPanel.add(credPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel warningLabel = new JLabel("<html><center>⚠️ IMPORTANT: Save these credentials! <br>The password cannot be retrieved later.</center></html>");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setForeground(new Color(231, 76, 60));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(warningLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton copyBtn = new JButton("Copy Credentials");
        styleButton(copyBtn, new Color(155, 89, 182));
        copyBtn.addActionListener(e -> {
            String credentials = "HR User ID: " + hrUserId + "\nPassword: " + password;
            java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(credentials);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(credDialog, 
                "Credentials copied to clipboard!", 
                "Copied", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton closeBtn = new JButton("Close");
        styleButton(closeBtn, new Color(149, 165, 166));
        closeBtn.addActionListener(e -> credDialog.dispose());
        
        buttonPanel.add(copyBtn);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(buttonPanel);
        
        credDialog.add(mainPanel);
        credDialog.setVisible(true);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
    }
}