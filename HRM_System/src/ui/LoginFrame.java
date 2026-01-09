package ui;

import javax.  swing.*;
import java.awt.*;
import java.rmi.  Naming;
import remote.HRMService;
import remote.PayrollService;
import ui.hr.HRDashboard;
import ui.employee. EmployeeDashboard;

public class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private HRMService hrService;
    private PayrollService payrollService;
    
    public LoginFrame() {
        setTitle("HRM System - Login");
        setSize(450, 400);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
        connectToServer();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory. createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("HRM SYSTEM");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 32));
        titleLabel. setForeground(new Color(52, 73, 94));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Human Resource Management");
        subtitleLabel.setFont(new Font("Arial", Font. PLAIN, 14));
        subtitleLabel. setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box. createRigidArea(new Dimension(0, 5)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // User ID Panel
        JPanel userIdPanel = new JPanel(new BorderLayout(10, 5));
        userIdPanel.setBackground(Color.WHITE);
        userIdPanel.setMaximumSize(new Dimension(370, 65));  
        
        JLabel userIdLabel = new JLabel("User ID / Email:");
        userIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        userIdField = new JTextField();
        userIdField.setFont(new Font("Arial", Font. PLAIN, 14));
        userIdField.setPreferredSize(new Dimension(370, 35));
        
        userIdPanel.add(userIdLabel, BorderLayout.NORTH);
        userIdPanel.add(userIdField, BorderLayout.  CENTER);
        
        // Password Panel
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 5));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setMaximumSize(new Dimension(370, 65));  
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font. PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(370, 35));
        
        passwordPanel.add(passwordLabel, BorderLayout.NORTH);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        mainPanel.add(userIdPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordPanel);
        mainPanel. add(Box.createRigidArea(new Dimension(0, 20)));
        
        // ✅ FIX: Login Button with proper styling
        loginButton = new JButton("LOGIN");
        loginButton. setFont(new Font("Arial", Font. BOLD, 16));
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton. setForeground(Color. WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton. setContentAreaFilled(true);  
        loginButton.setOpaque(true); 
        loginButton. setPreferredSize(new Dimension(370, 45));
        loginButton.setMaximumSize(new Dimension(370, 45));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton. setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        
        // Enter key support
        passwordField.addActionListener(e -> handleLogin());
        
        mainPanel.add(loginButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font. ITALIC, 12));
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        
        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel infoLabel = new JLabel("<html><center>Default Credentials: <br>HR:  hr / hr123</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel. setForeground(new Color(127, 140, 141));
        infoPanel.add(infoLabel);
        
        add(mainPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout. SOUTH);
    }
    
    private void connectToServer() {
        statusLabel.setText("Connecting to server...");
        statusLabel.setForeground(new Color(52, 152, 219));
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                hrService = (HRMService) Naming.lookup("rmi://127.0.0.1:1098/HRMService");
                payrollService = (PayrollService) Naming.lookup("rmi://127.0.0.1:1098/PayrollService");
                return true;
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        statusLabel. setText("Connected to server");
                        statusLabel. setForeground(new Color(39, 174, 96));
                    }
                } catch (Exception e) {
                    statusLabel.setText("Server connection failed!");
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Cannot connect to server!\nMake sure HRMServer is running on port 1098.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(false);
                }
            }
        };
        worker.execute();
    }
    
    private void handleLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField. getPassword());
        
        if (userId.  isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both User ID and Password");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("Logging in.. .");
        statusLabel.setText("Authenticating..  .");
        statusLabel.setForeground(new Color(52, 152, 219));
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return hrService.authenticate(userId, password);
            }
            
            @Override
            protected void done() {
                try {
                    boolean authenticated = get();
                    
                    if (authenticated) {
                        statusLabel.setText("Login successful!");
                        statusLabel. setForeground(new Color(39, 174, 96));
                        openDashboard(userId);
                        dispose();
                    } else {
                        statusLabel.setText("Invalid credentials!");
                        statusLabel.setForeground(Color.RED);
                        passwordField.setText("");
                        loginButton.setEnabled(true);
                        loginButton.setText("LOGIN");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Login error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                }
            }
        };
        worker.  execute();
    }
    
    private void openDashboard(String userId) {
        SwingUtilities.invokeLater(() -> {
            // Check if user is an HR user
            SwingWorker<Boolean, Void> checkWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return hrService.isHRUser(userId);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean isHR = get();
                        if (isHR) {
                            new HRDashboard(hrService, payrollService, userId).setVisible(true);
                        } else {
                            new EmployeeDashboard(hrService, payrollService, userId).setVisible(true);
                        }
                    } catch (Exception e) {
                        // If check fails, default to employee dashboard
                        System.err.println("Error checking HR status: " + e.getMessage());
                        new EmployeeDashboard(hrService, payrollService, userId).setVisible(true);
                    }
                }
            };
            checkWorker.execute();
        });
    }
}