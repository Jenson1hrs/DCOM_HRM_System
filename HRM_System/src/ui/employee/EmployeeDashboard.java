package ui.employee;

import javax. swing.*;
import java.awt.*;
import remote.HRMService;
import remote.PayrollService;
import common.Employee;
import common.SalaryRecord;
import ui. ChangePasswordDialog;
import ui.LoginFrame;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmployeeDashboard extends JFrame {
    private HRMService hrService;
    private PayrollService payrollService;
    private String employeeId;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public EmployeeDashboard(HRMService hrService, PayrollService payrollService, String employeeId) {
        this.hrService = hrService;
        this.payrollService = payrollService;
        this.employeeId = employeeId;
        
        setTitle("Employee Dashboard - HRM System");
        setSize(1100, 650);
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
        
        JLabel titleLabel = new JLabel("HRM SYSTEM - Employee Portal");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(employeeId);
        userLabel.setForeground(Color.WHITE);
        userLabel. setFont(new Font("Arial", Font.PLAIN, 14));
        
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
        topBar.add(userPanel, BorderLayout.EAST);
        
        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        addMenuButton(sidebar, "Dashboard", "dashboard");
        addMenuButton(sidebar, "My Profile", "profile");
        addMenuButton(sidebar, "Leave", "leave");
        addMenuButton(sidebar, "Salary", "salary");
        
        // Content Panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        // Add panels
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(new MyProfilePanel(hrService, employeeId), "profile");
        contentPanel.add(new LeavePanel(hrService, employeeId), "leave");
        contentPanel.add(new SalaryPanel(hrService, payrollService, employeeId), "salary");
        
        // Layout
        add(topBar, BorderLayout. NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout. CENTER);
    }
    
    private void addMenuButton(JPanel sidebar, String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font. PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 73, 94));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(180, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, panelName);
            if (panelName.equals("dashboard")) {
                loadDashboardData();
            }
        });
        
        button.addMouseListener(new java.awt. event.MouseAdapter() {
            public void mouseEntered(java. awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event. MouseEvent evt) {
                button.setBackground(new Color(52, 73, 94));
            }
        });
        
        sidebar.add(button);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Arial", Font. BOLD, 28));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(welcomeLabel, BorderLayout. WEST);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        
        JLabel leaveBalanceLabel = new JLabel(".. .", SwingConstants.CENTER);
        JLabel salaryLabel = new JLabel("...", SwingConstants.CENTER);
        JLabel departmentLabel = new JLabel(".. .", SwingConstants.CENTER);
        
        statsPanel.add(createStatCard("Leave Balance", leaveBalanceLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Current Salary", salaryLabel, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Department", departmentLabel, new Color(155, 89, 182)));
        
        // Quick Actions Panel
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                "Quick Actions",
                0,
                0,
                new Font("Arial", Font.BOLD, 16)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JButton applyLeaveBtn = createActionButton("Apply for Leave", new Color(52, 152, 219));
        applyLeaveBtn.addActionListener(e -> cardLayout.show(contentPanel, "leave"));
        
        JButton viewProfileBtn = createActionButton("View Profile", new Color(155, 89, 182));
        viewProfileBtn.addActionListener(e -> cardLayout.show(contentPanel, "profile"));
        
        JButton viewSalaryBtn = createActionButton("View Salary", new Color(46, 204, 113));
        viewSalaryBtn.addActionListener(e -> cardLayout.show(contentPanel, "salary"));
        
        JButton changePasswordBtn = createActionButton("Change Password", new Color(230, 126, 34));
        changePasswordBtn.addActionListener(e -> showChangePassword());
        
        actionsPanel.add(applyLeaveBtn);
        actionsPanel.add(viewProfileBtn);
        actionsPanel.add(viewSalaryBtn);
        actionsPanel.add(changePasswordBtn);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(actionsPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Store references for updating
        panel.putClientProperty("leaveBalanceLabel", leaveBalanceLabel);
        panel.putClientProperty("salaryLabel", salaryLabel);
        panel.putClientProperty("departmentLabel", departmentLabel);
        
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
        
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font. PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 80));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void loadDashboardData() {
        JPanel dashboardPanel = (JPanel) contentPanel.getComponent(0);
        JLabel leaveBalanceLabel = (JLabel) dashboardPanel.getClientProperty("leaveBalanceLabel");
        JLabel salaryLabel = (JLabel) dashboardPanel.getClientProperty("salaryLabel");
        JLabel departmentLabel = (JLabel) dashboardPanel.getClientProperty("departmentLabel");
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Employee employee;
            SalaryRecord salary;
            
            @Override
            protected Void doInBackground() throws Exception {
                employee = hrService.getEmployeeProfile(employeeId);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String currentMonth = sdf.format(new Date());
                salary = payrollService.getSalaryRecord(employeeId, currentMonth);
                
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    if (employee != null) {
                        leaveBalanceLabel.setText(employee.getLeaveBalance() + " days");
                        departmentLabel.setText(employee.getDepartment());
                        
                        if (salary != null) {
                            salaryLabel.setText("RM " + String.format("%.0f", salary.getNetSalary()));
                        } else {
                            salaryLabel.setText("N/A");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor. HAND_CURSOR));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
    }
    
    private void showChangePassword() {
        new ChangePasswordDialog(this, hrService, employeeId).setVisible(true);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}