package ui.employee;

import javax. swing.*;
import java.awt.*;
import remote.HRMService;
import common.Employee;
import common.FamilyMember;
import java.util.List;

public class MyProfilePanel extends JPanel {
    private HRMService hrService;
    private String employeeId;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField positionField;
    private JTextField bankAccountField;
    private JLabel empIdLabel;
    private JLabel nameLabel;
    private JLabel icLabel;
    private JLabel joinDateLabel;
    private JLabel salaryLabel;
    private JLabel leaveBalanceLabel;
    private DefaultListModel<String> familyListModel;
    private Employee currentEmployee;
    
    public MyProfilePanel(HRMService hrService, String employeeId) {
        this.hrService = hrService;
        this.employeeId = employeeId;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadProfile();
    }
    
    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        // Main Content with Scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout. Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        
        // Personal Information Section
        mainPanel.add(createSectionPanel("Personal Information", createPersonalInfoPanel()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Contact Details Section
        mainPanel.add(createSectionPanel("Contact Details", createContactPanel()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Employment Details Section
        mainPanel.add(createSectionPanel("Employment Details", createEmploymentPanel()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Family Members Section
        mainPanel.add(createSectionPanel("Family Members", createFamilyPanel()));
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createSectionPanel(String title, JPanel contentPanel) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(Color.WHITE);
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                title,
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                new Color(52, 73, 94)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, contentPanel.getPreferredSize().height + 60));
        sectionPanel.add(contentPanel, BorderLayout.CENTER);
        return sectionPanel;
    }
    
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        empIdLabel = new JLabel("Loading...");
        nameLabel = new JLabel("Loading.. .");
        icLabel = new JLabel("Loading...");
        
        addReadOnlyField(panel, gbc, 0, "Employee ID:", empIdLabel);
        addReadOnlyField(panel, gbc, 1, "Full Name:", nameLabel);
        addReadOnlyField(panel, gbc, 2, "IC/Passport:", icLabel);
        
        return panel;
    }
    
    private JPanel createContactPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        emailField = new JTextField(25);
        phoneField = new JTextField(25);
        
        addEditableField(panel, gbc, 0, "Email:", emailField);
        addEditableField(panel, gbc, 1, "Phone:", phoneField);
        
        // Update Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        
        JButton updateContactBtn = new JButton("Update Contact Details");
        styleButton(updateContactBtn, new Color(52, 152, 219));
        updateContactBtn.addActionListener(e -> updateContactDetails());
        panel.add(updateContactBtn, gbc);
        
        return panel;
    }
    
    private JPanel createEmploymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        departmentField = new JTextField(25);
        positionField = new JTextField(25);
        joinDateLabel = new JLabel("Loading...");
        salaryLabel = new JLabel("Loading...");
        bankAccountField = new JTextField(25);
        leaveBalanceLabel = new JLabel("Loading...");
        
        addEditableField(panel, gbc, 0, "Department:", departmentField);
        addEditableField(panel, gbc, 1, "Position:", positionField);
        addReadOnlyField(panel, gbc, 2, "Join Date:", joinDateLabel);
        addReadOnlyField(panel, gbc, 3, "Monthly Salary:", salaryLabel);
        addEditableField(panel, gbc, 4, "Bank Account:", bankAccountField);
        addReadOnlyField(panel, gbc, 5, "Leave Balance:", leaveBalanceLabel);
        
        // Update Buttons
        gbc.gridx = 0;
        gbc. gridy = 6;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 10, 5, 5);
        
        JButton updateEmploymentBtn = new JButton("Update Employment Info");
        styleButton(updateEmploymentBtn, new Color(52, 152, 219));
        updateEmploymentBtn.addActionListener(e -> updateEmploymentDetails());
        panel.add(updateEmploymentBtn, gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(15, 5, 5, 10);
        
        JButton updateBankBtn = new JButton("Update Bank Account");
        styleButton(updateBankBtn, new Color(155, 89, 182));
        updateBankBtn.addActionListener(e -> updateBankAccount());
        panel.add(updateBankBtn, gbc);
        
        return panel;
    }
    
    private JPanel createFamilyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Family List
        familyListModel = new DefaultListModel<>();
        JList<String> familyList = new JList<>(familyListModel);
        familyList. setFont(new Font("Arial", Font. PLAIN, 14));
        familyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane listScroll = new JScrollPane(familyList);
        listScroll.setPreferredSize(new Dimension(500, 120));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsPanel.setBackground(Color.WHITE);
        
        JButton addFamilyBtn = new JButton("Add Family Member");
        styleButton(addFamilyBtn, new Color(46, 204, 113));
        addFamilyBtn.addActionListener(e -> showAddFamilyDialog());
        
        JButton removeFamilyBtn = new JButton("Remove Selected");
        styleButton(removeFamilyBtn, new Color(231, 76, 60));
        removeFamilyBtn.addActionListener(e -> removeSelectedFamily(familyList));
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> loadFamilyMembers());
        
        buttonsPanel.add(addFamilyBtn);
        buttonsPanel. add(removeFamilyBtn);
        buttonsPanel.add(refreshBtn);
        
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addReadOnlyField(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font. BOLD, 14));
        panel.add(jLabel, gbc);
        
        gbc. gridx = 1;
        gbc.weightx = 0.7;
        valueLabel. setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(52, 73, 94));
        panel.add(valueLabel, gbc);
    }
    
    private void addEditableField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 30));
        panel.add(field, gbc);
    }
    
    private void loadProfile() {
        SwingWorker<Employee, Void> worker = new SwingWorker<>() {
            @Override
            protected Employee doInBackground() throws Exception {
                return hrService.getEmployeeProfile(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    currentEmployee = get();
                    if (currentEmployee != null) {
                        // Personal Info
                        empIdLabel.setText(currentEmployee.getEmployeeId());
                        nameLabel.setText(currentEmployee.getFirstName() + " " + currentEmployee.getLastName());
                        icLabel.setText(currentEmployee. getIcPassport());
                        
                        // Contact
                        emailField.setText(currentEmployee.getEmail());
                        phoneField.setText(currentEmployee. getPhone());
                        
                        // Employment
                        departmentField.setText(currentEmployee.getDepartment());
                        positionField.setText(currentEmployee.getPosition());
                        joinDateLabel.setText(currentEmployee.getJoinDate());
                        salaryLabel.setText("RM " + String.format("%.2f", currentEmployee.getMonthlySalary()));
                        bankAccountField.setText(currentEmployee.getBankAccount());
                        leaveBalanceLabel.setText(currentEmployee.getLeaveBalance() + " days");
                        
                        // Load family members
                        loadFamilyMembers();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this,
                        "Error loading profile: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void loadFamilyMembers() {
        SwingWorker<List<FamilyMember>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FamilyMember> doInBackground() throws Exception {
                return hrService.getFamilyMembers(employeeId);
            }
            
            @Override
            protected void done() {
                try {
                    List<FamilyMember> family = get();
                    familyListModel.clear();
                    
                    if (family.isEmpty()) {
                        familyListModel.addElement("No family members registered");
                    } else {
                        for (FamilyMember member : family) {
                            String display = String.format("%s (%s) - IC: %s",
                                member.getName(),
                                member.getRelationship(),
                                member.getIcNumber());
                            familyListModel.addElement(display);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this,
                        "Error loading family members: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateContactDetails() {
        if (currentEmployee == null) return;
        
        currentEmployee.setEmail(emailField. getText().trim());
        currentEmployee.setPhone(phoneField.getText().trim());
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return hrService.updateEmployeeProfile(currentEmployee);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MyProfilePanel.this,
                            "Contact details updated successfully! ",
                            "Success",
                            JOptionPane. INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MyProfilePanel.this,
                            "Failed to update contact details! ",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this,
                        "Error:  " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker. execute();
    }
    
    private void updateEmploymentDetails() {
        if (currentEmployee == null) return;
        
        currentEmployee.setDepartment(departmentField.getText().trim());
        currentEmployee.setPosition(positionField.getText().trim());
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return hrService.updateEmployeeProfile(currentEmployee);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MyProfilePanel.this,
                            "Employment details updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MyProfilePanel.this,
                            "Failed to update employment details!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateBankAccount() {
        String newAccount = bankAccountField.getText().trim();
        
        if (newAccount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bank account cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                currentEmployee.setBankAccount(newAccount);
                return hrService.updateEmployeeProfile(currentEmployee);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(MyProfilePanel.this,
                            "Bank account updated successfully! ",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane. showMessageDialog(MyProfilePanel. this,
                            "Failed to update bank account!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyProfilePanel.this,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane. ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showAddFamilyDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Family Member", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        JTextField nameField = new JTextField(20);
        JComboBox<String> relationshipCombo = new JComboBox<>(new String[]{"Spouse", "Child", "Parent", "Sibling"});
        JTextField icField = new JTextField(20);
        JTextField dobField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Relationship:"), gbc);
        gbc.gridx = 1;
        panel.add(relationshipCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("IC Number:"), gbc);
        gbc.gridx = 1;
        panel.add(icField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        panel.add(dobField, gbc);
        
        gbc. gridx = 0; gbc.gridy = 4;
        JLabel hintLabel = new JLabel("(YYYY-MM-DD, optional)");
        hintLabel.setFont(new Font("Arial", Font. ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String relationship = (String) relationshipCombo.getSelectedItem();
            String ic = icField.getText().trim();
            String dob = dobField.getText().trim();
            
            if (name.isEmpty() || ic.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and IC are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                FamilyMember member = new FamilyMember(name, relationship, ic);
                if (! dob.isEmpty()) {
                    member.setDateOfBirth(dob);
                }
                
                boolean success = hrService.addFamilyMember(employeeId, member);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Family member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog. dispose();
                    loadFamilyMembers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add family member!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane. showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel. add(addBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void removeSelectedFamily(JList<String> familyList) {
        String selected = familyList.getSelectedValue();
        if (selected == null || selected.equals("No family members registered")) {
            JOptionPane.showMessageDialog(this, "Please select a family member to remove!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract IC from the display string
        String ic = selected.substring(selected.lastIndexOf("IC: ") + 4);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove this family member?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = hrService.removeFamilyMember(employeeId, ic);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Family member removed!", "Success", JOptionPane. INFORMATION_MESSAGE);
                    loadFamilyMembers();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove family member!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane. showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font. BOLD, 13));
        button.setPreferredSize(new Dimension(200, 35));
    }
}