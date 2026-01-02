package ui.hr;

import javax.swing.*;
import java.awt.*;
import remote.HRMService;
import remote.PayrollService;
import common.Employee;
import ui. ChangePasswordDialog;
import ui.LoginFrame;
import java.util.List;
import java.util.Map;

public class HRDashboard extends JFrame {
    private HRMService hrService;
    private PayrollService payrollService;
    private String userId;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public HRDashboard(HRMService hrService, PayrollService payrollService, String userId) {
        this.hrService = hrService;
        this. payrollService = payrollService;
        this.userId = userId;
        
        setTitle("HR Dashboard - HRM System");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadDashboardData();
    }
    
private void initComponents() {
    // Top Bar
    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setBackground(new Color(52, 73, 94));
    topBar.setPreferredSize(new Dimension(getWidth(), 60));
    topBar.setBorder(BorderFactory. createEmptyBorder(10, 20, 10, 20));
    
    JLabel titleLabel = new JLabel("HRM SYSTEM - HR Dashboard");
    titleLabel.setFont(new Font("Arial", Font. BOLD, 20));
    titleLabel.setForeground(Color.WHITE);
    
    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    userPanel.setOpaque(false);
    
    JLabel userLabel = new JLabel(userId);
    userLabel.setForeground(Color.WHITE);
    userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    
    JButton changePasswordBtn = new JButton("Change Password");
    styleSecondaryButton(changePasswordBtn);
    changePasswordBtn.addActionListener(e -> showChangePassword());
    
    JButton logoutBtn = new JButton("Logout");
    styleSecondaryButton(logoutBtn);
    logoutBtn.addActionListener(e -> handleLogout());
    
    userPanel.add(userLabel);
    userPanel.add(changePasswordBtn);
    userPanel.add(logoutBtn);
    
    topBar.add(titleLabel, BorderLayout.WEST);
    topBar.add(userPanel, BorderLayout. EAST);
    
    // Sidebar - UPDATED WITH NEW MENU ITEMS
    JPanel sidebar = new JPanel();
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setBackground(new Color(44, 62, 80));
    sidebar.setPreferredSize(new Dimension(200, getHeight()));
    sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    
    addMenuButton(sidebar, "Dashboard", "dashboard");
    addMenuButton(sidebar, "Employees", "employees");
    addMenuButton(sidebar, "Leave Mgmt", "leaves");
    addMenuButton(sidebar, "Payroll", "payroll");
    addMenuButton(sidebar, "Reports", "reports");        // NEW
    addMenuButton(sidebar, "Security Logs", "security");  // NEW
    
    // Content Panel with CardLayout
    cardLayout = new CardLayout();
    contentPanel = new JPanel(cardLayout);
    contentPanel.setBackground(Color.WHITE);
    
    // Add panels - UPDATED WITH NEW PANELS
    contentPanel.add(createDashboardPanel(), "dashboard");
    contentPanel.add(new EmployeeManagementPanel(hrService), "employees");
    contentPanel.add(new LeaveManagementPanel(hrService), "leaves");
    contentPanel.add(new PayrollManagementPanel(hrService, payrollService), "payroll");
    contentPanel.add(new ReportsPanel(hrService, payrollService), "reports");        // NEW
    contentPanel.add(new SecurityLogsPanel(hrService), "security");                   // NEW
    
    // Layout
    add(topBar, BorderLayout.NORTH);
    add(sidebar, BorderLayout.WEST);
    add(contentPanel, BorderLayout.CENTER);
}
    
    private void addMenuButton(JPanel sidebar, String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font. PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(44, 62, 80));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, panelName);
            if (panelName.equals("dashboard")) {
                loadDashboardData();
            }
        });
        
        button.addMouseListener(new java. awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 73, 94));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(44, 62, 80));
            }
        });
        
        sidebar. add(button);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        
        JLabel totalEmpLabel = new JLabel("Loading...", SwingConstants.CENTER);
        JLabel pendingLeavesLabel = new JLabel("Loading...", SwingConstants.CENTER);
        JLabel unpaidEmpLabel = new JLabel("Loading...", SwingConstants.CENTER);
        JLabel totalPayrollLabel = new JLabel("Loading...", SwingConstants.CENTER);
        
        statsPanel. add(createStatCard("Total Employees", totalEmpLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Pending Leaves", pendingLeavesLabel, new Color(230, 126, 34)));
        statsPanel.add(createStatCard("Unpaid Employees", unpaidEmpLabel, new Color(231, 76, 60)));
        statsPanel.add(createStatCard("Monthly Payroll", totalPayrollLabel, new Color(46, 204, 113)));
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // Store references for updating
        panel.putClientProperty("totalEmpLabel", totalEmpLabel);
        panel.putClientProperty("pendingLeavesLabel", pendingLeavesLabel);
        panel.putClientProperty("unpaidEmpLabel", unpaidEmpLabel);
        panel.putClientProperty("totalPayrollLabel", totalPayrollLabel);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color. darker(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabelCard = new JLabel(title);
        titleLabelCard.setFont(new Font("Arial", Font. PLAIN, 14));
        titleLabelCard. setForeground(Color.WHITE);
        titleLabelCard. setAlignmentX(Component. CENTER_ALIGNMENT);
        
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabelCard);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private void loadDashboardData() {
        JPanel dashboardPanel = (JPanel) contentPanel.getComponent(0);
        JLabel totalEmpLabel = (JLabel) dashboardPanel.getClientProperty("totalEmpLabel");
        JLabel pendingLeavesLabel = (JLabel) dashboardPanel.getClientProperty("pendingLeavesLabel");
        JLabel unpaidEmpLabel = (JLabel) dashboardPanel.getClientProperty("unpaidEmpLabel");
        JLabel totalPayrollLabel = (JLabel) dashboardPanel.getClientProperty("totalPayrollLabel");
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int totalEmployees = 0;
            int pendingLeaves = 0;
            int unpaidEmployees = 0;
            double totalPayroll = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                // Get total employees
                List<Employee> employees = hrService.getAllEmployees();
                totalEmployees = employees. size();
                
                // Calculate total payroll
                for (Employee emp : employees) {
                    totalPayroll += emp.getMonthlySalary();
                }
                
                // Get pending leaves
                List<Map<String, String>> leaves = hrService.getAllLeaveApplications();
                for (Map<String, String> leave : leaves) {
                    if (leave.get("status").contains("Pending")) {
                        pendingLeaves++;
                    }
                }
                
                // Get unpaid employees
                List<Map<String, String>> paymentStatus = payrollService.getEmployeesPaymentStatus();
                for (Map<String, String> emp : paymentStatus) {
                    if ("UNPAID".equals(emp.get("status"))) {
                        unpaidEmployees++;
                    }
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                totalEmpLabel.setText(String. valueOf(totalEmployees));
                pendingLeavesLabel.setText(String.valueOf(pendingLeaves));
                unpaidEmpLabel.setText(String.valueOf(unpaidEmployees));
                totalPayrollLabel.setText("RM " + String.format("%.0f", totalPayroll));
            }
        };
        worker.execute();
    }
    
    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font. PLAIN, 12));
    }
    
    private void showChangePassword() {
        new ChangePasswordDialog(this, hrService, userId).setVisible(true);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}