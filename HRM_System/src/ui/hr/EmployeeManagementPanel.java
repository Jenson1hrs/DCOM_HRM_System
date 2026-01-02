package ui. hr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer. Clipboard;
import remote.HRMService;
import common.Employee;
import java.util.List;

public class EmployeeManagementPanel extends JPanel {
    private HRMService hrService;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public EmployeeManagementPanel(HRMService hrService) {
        this.hrService = hrService;
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
        loadEmployees();
    }
    
    private void initComponents() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font. PLAIN, 14));

        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn, new Color(52, 152, 219));
        searchBtn.addActionListener(e -> searchEmployees());

        JButton addBtn = new JButton("Add New Employee");
        styleButton(addBtn, new Color(46, 204, 113));
        addBtn.addActionListener(e -> showAddEmployeeDialog());

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> loadEmployees());

        actionPanel.add(new JLabel("Search:"));
        actionPanel.add(searchField);
        actionPanel.add(searchBtn);
        actionPanel.add(addBtn);
        actionPanel.add(refreshBtn);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Table with Actions column
        String[] columns = {"ID", "Name", "IC/Passport", "Email", "Department", "Position", "Salary", "Leave", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable. setFont(new Font("Arial", Font. PLAIN, 13));
        employeeTable.setRowHeight(35);

        // Force table header to use custom renderer
        employeeTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());

        // ALSO set these properties (belt-and-suspenders approach)
        employeeTable.getTableHeader().setFont(new Font("Arial", Font. BOLD, 13));
        employeeTable.getTableHeader().setBackground(new Color(52, 73, 94));
        employeeTable.getTableHeader().setForeground(Color.WHITE);
        employeeTable.getTableHeader().setOpaque(true);
        employeeTable.getTableHeader().setReorderingAllowed(false);

        employeeTable.setSelectionMode(ListSelectionModel. SINGLE_SELECTION);

        // Set selection colors for better visibility
        employeeTable. setSelectionBackground(new Color(70, 130, 180));
        employeeTable.setSelectionForeground(Color.WHITE);
        employeeTable.setGridColor(new Color(220, 220, 220));

        // Set column widths
        employeeTable.setAutoResizeMode(JTable. AUTO_RESIZE_OFF);
        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(70);    // ID
        employeeTable. getColumnModel().getColumn(1).setPreferredWidth(120);   // Name
        employeeTable. getColumnModel().getColumn(2).setPreferredWidth(100);   // IC/Passport
        employeeTable.getColumnModel().getColumn(3).setPreferredWidth(180);   // Email
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(100);   // Department
        employeeTable.getColumnModel().getColumn(5).setPreferredWidth(120);   // Position
        employeeTable.getColumnModel().getColumn(6).setPreferredWidth(100);   // Salary
        employeeTable.getColumnModel().getColumn(7).setPreferredWidth(80);    // Leave
        employeeTable.getColumnModel().getColumn(8).setPreferredWidth(120);   // Actions

        // Custom renderer for Actions column
        employeeTable.getColumn("Actions").setCellRenderer(new EmployeeActionsCellRenderer());

        // Mouse listener for actions
        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = employeeTable.rowAtPoint(evt.getPoint());
                int col = employeeTable.columnAtPoint(evt.getPoint());

                if (row >= 0 && col == 8) { // Actions column
                    String empId = (String) tableModel.getValueAt(row, 0);

                    // Show action dialog
                    Object[] options = {"View Details", "Edit Employee", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(
                        EmployeeManagementPanel.this,
                        "Choose action for employee:  " + empId,
                        "Employee Actions",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice == 0) { // View Details
                        showEmployeeDetails(empId);
                    } else if (choice == 1) { // Edit Employee
                        showEditEmployeeDialog(empId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        // Enable scrollbars
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("Click on Actions column to View or Edit employee");
        infoLabel.setFont(new Font("Arial", Font. ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        bottomPanel.add(infoLabel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout. CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Custom header renderer to FORCE white text on dark background
    private class CustomHeaderRenderer extends JLabel implements javax.swing.table.TableCellRenderer {

        public CustomHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants. CENTER);
            setFont(new Font("Arial", Font. BOLD, 13));
            setBackground(new Color(52, 73, 94));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(40, 55, 70)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            return this;
        }
    }

    // EXISTING: Custom cell renderer for Actions column
    private class EmployeeActionsCellRenderer extends JPanel implements TableCellRenderer {
        private JButton actionBtn;

        public EmployeeActionsCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);

            actionBtn = new JButton("Actions");
            actionBtn.setBackground(new Color(52, 152, 219));
            actionBtn.setForeground(Color.WHITE);
            actionBtn.setFocusPainted(false);
            actionBtn.setBorderPainted(false);
            actionBtn.setContentAreaFilled(true);
            actionBtn.setPreferredSize(new Dimension(85, 28));
            actionBtn.setFont(new Font("Arial", Font.BOLD, 12));
            actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            removeAll();

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }

            actionBtn.setForeground(Color.WHITE);
            actionBtn. setFont(new Font("Arial", Font. BOLD, 12));

            add(actionBtn);
            revalidate();
            repaint();

            return this;
        }
    }
    
    private void loadEmployees() {
        tableModel.setRowCount(0);
        
        SwingWorker<List<Employee>, Void> worker = new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                return hrService. getAllEmployees();
            }
            
            @Override
            protected void done() {
                try {
                    List<Employee> employees = get();
                    for (Employee emp : employees) {
                        tableModel.addRow(new Object[]{
                            emp.getEmployeeId(),
                            emp.getFirstName() + " " + emp.getLastName(),
                            emp.getIcPassport(),
                            emp.getEmail(),
                            emp.getDepartment(),
                            emp.getPosition(),
                            "RM " + String.format("%.2f", emp.getMonthlySalary()),
                            emp.getLeaveBalance() + " days",
                            "Click to action"
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(EmployeeManagementPanel. this,
                        "Error loading employees:  " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void searchEmployees() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadEmployees();
            return;
        }
        
        tableModel.setRowCount(0);
        
        SwingWorker<List<Employee>, Void> worker = new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                return hrService.getAllEmployees();
            }
            
            @Override
            protected void done() {
                try {
                    List<Employee> employees = get();
                    for (Employee emp : employees) {
                        String fullName = (emp.getFirstName() + " " + emp.getLastName()).toLowerCase();
                        if (emp.getEmployeeId().toLowerCase().contains(searchText) ||
                            fullName.contains(searchText) ||
                            emp.getDepartment().toLowerCase().contains(searchText)) {
                            
                            tableModel.addRow(new Object[]{
                                emp.getEmployeeId(),
                                emp.getFirstName() + " " + emp.getLastName(),
                                emp.getIcPassport(),
                                emp.getEmail(),
                                emp.getDepartment(),
                                emp.getPosition(),
                                "RM " + String.format("%.2f", emp.getMonthlySalary()),
                                emp.getLeaveBalance() + " days",
                                "Click to action"
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(EmployeeManagementPanel.this,
                        "Error searching:  " + e.getMessage(),
                        "Error", JOptionPane. ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showAddEmployeeDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Employee", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints. HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title
        JLabel titleLabel = new JLabel("Register New Employee");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JTextField icField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Last Name: "), gbc);
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("IC/Passport:"), gbc);
        gbc.gridx = 1;
        panel.add(icField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");

        styleButton(registerBtn, new Color(46, 204, 113));
        styleButton(cancelBtn, new Color(149, 165, 166));

        registerBtn.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String ic = icField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || ic.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "All fields are required!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return hrService.registerEmployee(firstName, lastName, ic);
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        System.out.println("=== SERVER RESPONSE ===");
                        System.out.println(result);
                        System.out.println("======================");

                        String empId = extractEmployeeId(result);
                        String password = extractPassword(result);

                        // Check for error indicators in the response
                        boolean hasError = result.toLowerCase().contains("error: ") ||
                                         result.toLowerCase().contains("invalid") ||
                                         result.toLowerCase().contains("failed") ||
                                         empId.equals("N/A") ||
                                         password.equals("N/A");

                        dialog.dispose();

                        if (hasError) {
                            JOptionPane.showMessageDialog(EmployeeManagementPanel.this,
                                "Failed to register employee:\n\n" + result,
                                "Registration Failed",
                                JOptionPane. ERROR_MESSAGE);
                            registerBtn.setEnabled(true);
                            registerBtn.setText("Register");
                        } else {
                            // Show success dialog only if registration succeeded
                            showCredentialsDialog(result);
                            loadEmployees();
                        }

                    } catch (Exception ex) {
                        dialog.dispose();
                        JOptionPane.showMessageDialog(EmployeeManagementPanel.this, 
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

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showEditEmployeeDialog(String empId) {
        // First, load the employee data
        SwingWorker<Employee, Void> worker = new SwingWorker<Employee, Void>() {
            @Override
            protected Employee doInBackground() throws Exception {
                return hrService.getEmployeeProfile(empId);
            }
            
            @Override
            protected void done() {
                try {
                    Employee emp = get();
                    if (emp == null) {
                        JOptionPane.showMessageDialog(EmployeeManagementPanel.this,
                            "Employee not found! ",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Show edit dialog
                    showEditDialog(emp);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane. showMessageDialog(EmployeeManagementPanel.this,
                        "Error loading employee: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker. execute();
    }
    
    private void showEditDialog(Employee emp) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Employee", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout. Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Edit Employee:  " + emp.getEmployeeId());
        titleLabel.setFont(new Font("Arial", Font. BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Fields
        JTextField emailField = new JTextField(emp.getEmail(), 20);
        JTextField phoneField = new JTextField(emp. getPhone(), 20);
        JTextField deptField = new JTextField(emp.getDepartment(), 20);
        JTextField posField = new JTextField(emp.getPosition(), 20);
        JTextField salaryField = new JTextField(String.valueOf(emp.getMonthlySalary()), 20);
        JTextField bankField = new JTextField(emp. getBankAccount(), 20);
        JSpinner leaveSpinner = new JSpinner(new SpinnerNumberModel(emp. getLeaveBalance(), 0, 100, 1));
        
        int row = 0;
        addFormField(formPanel, gbc, row++, "Employee ID:", new JLabel(emp.getEmployeeId()));
        addFormField(formPanel, gbc, row++, "Name:", new JLabel(emp.getFirstName() + " " + emp.getLastName()));
        addFormField(formPanel, gbc, row++, "IC/Passport:", new JLabel(emp. getIcPassport()));
        addFormField(formPanel, gbc, row++, "Email:", emailField);
        addFormField(formPanel, gbc, row++, "Phone:", phoneField);
        addFormField(formPanel, gbc, row++, "Department:", deptField);
        addFormField(formPanel, gbc, row++, "Position:", posField);
        addFormField(formPanel, gbc, row++, "Monthly Salary:", salaryField);
        addFormField(formPanel, gbc, row++, "Bank Account:", bankField);
        addFormField(formPanel, gbc, row++, "Leave Balance:", leaveSpinner);
        
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout. CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveBtn = new JButton("Save Changes");
        styleButton(saveBtn, new Color(46, 204, 113));
        saveBtn.addActionListener(e -> {
            // Update employee object
            emp.setEmail(emailField.getText().trim());
            emp.setPhone(phoneField. getText().trim());
            emp.setDepartment(deptField.getText().trim());
            emp.setPosition(posField.getText().trim());
            emp.setBankAccount(bankField.getText().trim());
            emp.setLeaveBalance((Integer) leaveSpinner.getValue());
            
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                emp.setMonthlySalary(salary);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid salary format! ",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Save to server
            saveBtn.setEnabled(false);
            saveBtn.setText("Saving.. .");
            
            SwingWorker<Boolean, Void> saveWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return hrService.updateEmployeeProfile(emp);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(dialog,
                                "Employee updated successfully!",
                                "Success",
                                JOptionPane. INFORMATION_MESSAGE);
                            dialog.dispose();
                            loadEmployees();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Failed to update employee!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                            saveBtn.setEnabled(true);
                            saveBtn.setText("Save Changes");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog,
                            "Error:  " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        saveBtn.setEnabled(true);
                        saveBtn. setText("Save Changes");
                    }
                }
            };
            saveWorker.execute();
        });
        
        JButton cancelBtn = new JButton("Cancel");
        styleButton(cancelBtn, new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel. add(buttonPanel);
        
        dialog.add(new JScrollPane(mainPanel));
        dialog.setVisible(true);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font. BOLD, 14));
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(new Font("Arial", Font. PLAIN, 14));
        } else if (field instanceof JLabel) {
            ((JLabel) field).setFont(new Font("Arial", Font.PLAIN, 14));
            ((JLabel) field).setForeground(new Color(52, 73, 94));
        }
        panel.add(field, gbc);
    }
    
    private void showCredentialsDialog(String serverResponse) {
        String employeeId = extractEmployeeId(serverResponse);
        String password = extractPassword(serverResponse);
        
        JDialog credDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Employee Registered Successfully", true);
        credDialog.setSize(550, 450);
        credDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel. setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color. WHITE);
        
        JLabel successIcon = new JLabel("✓", SwingConstants.CENTER);
        successIcon.setFont(new Font("Arial", Font. BOLD, 72));
        successIcon.setForeground(new Color(46, 204, 113));
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(successIcon);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel titleLabel = new JLabel("Employee Registered Successfully!");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel credPanel = new JPanel(new GridBagLayout());
        credPanel.setBackground(new Color(236, 240, 241));
        credPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel empIdLabel = new JLabel("Employee ID:");
        empIdLabel. setFont(new Font("Arial", Font.BOLD, 14));
        credPanel.add(empIdLabel, gbc);
        
        gbc.gridx = 1;
        JTextField empIdField = new JTextField(employeeId, 20);
        empIdField.setEditable(false);
        empIdField.setFont(new Font("Arial", Font.BOLD, 16));
        empIdField.setBackground(Color.WHITE);
        empIdField.setForeground(new Color(52, 73, 94));
        credPanel. add(empIdField, gbc);
        
        gbc. gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font. BOLD, 14));
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
        
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBackground(Color.WHITE);
        JLabel responseTitle = new JLabel("Server Response:");
        responseTitle.setFont(new Font("Arial", Font. ITALIC, 11));
        
        JTextArea responseArea = new JTextArea(serverResponse, 4, 40);
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(responseArea);
        
        responsePanel.add(responseTitle, BorderLayout.NORTH);
        responsePanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(responsePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel warningLabel = new JLabel("<html><center>⚠️ IMPORTANT: Save these credentials! <br>The password cannot be retrieved later.</center></html>");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setForeground(new Color(231, 76, 60));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(warningLabel);
        mainPanel. add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton copyBtn = new JButton("Copy Credentials");
        styleButton(copyBtn, new Color(52, 152, 219));
        copyBtn.addActionListener(e -> {
            String credentials = "Employee ID: " + employeeId + "\nPassword: " + password;
            StringSelection stringSelection = new StringSelection(credentials);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(credDialog, 
                "Credentials copied to clipboard!", 
                "Copied", 
                JOptionPane. INFORMATION_MESSAGE);
        });
        
        JButton closeBtn = new JButton("Close");
        styleButton(closeBtn, new Color(149, 165, 166));
        closeBtn.addActionListener(e -> credDialog.dispose());
        
        buttonPanel.add(copyBtn);
        buttonPanel. add(closeBtn);
        
        mainPanel.add(buttonPanel);
        
        credDialog. add(new JScrollPane(mainPanel));
        credDialog.setVisible(true);
    }
    
    private String extractEmployeeId(String result) {
        System.out.println("Extracting Employee ID from: " + result);
        
        String[] patterns = {"Employee ID: ", "ID: ", "employee ID: ", "EmployeeID: ", "User ID: "};
        
        for (String pattern : patterns) {
            if (result.contains(pattern)) {
                try {
                    int startIndex = result.indexOf(pattern) + pattern.length();
                    String remaining = result.substring(startIndex);
                    String id = remaining.split("[\\n\\r\\s]")[0].trim();
                    
                    if (! id.isEmpty() && ! id.equals("N/A")) {
                        System.out.println("Found Employee ID: " + id);
                        return id;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (result.matches(".*EMP\\d+.*")) {
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("(EMP\\d+)");
                java.util.regex. Matcher m = p.matcher(result);
                if (m.find()) {
                    String id = m.group(1);
                    System.out.println("Found Employee ID by pattern: " + id);
                    return id;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Employee ID not found, returning:  N/A");
        return "N/A";
    }
    
    private String extractPassword(String result) {
        System.out.println("Extracting Password from: " + result);
        
        String[] patterns = {"Generated Password: ", "Password: ", "password: ", "generated password: ", "Default Password: ", "Temporary Password: "};
        
        for (String pattern : patterns) {
            if (result.contains(pattern)) {
                try {
                    int startIndex = result.indexOf(pattern) + pattern.length();
                    String remaining = result.substring(startIndex);
                    String password = remaining.split("[\\n\\r]")[0].trim();
                    password = password.replaceAll("[. ! ?]+$", "");
                    
                    if (!password.isEmpty() && !password.equals("N/A")) {
                        System. out.println("Found Password: " + password);
                        return password;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Password not found, returning: N/A");
        return "N/A";
    }
    
    private void showEmployeeDetails(String empId) {
        SwingWorker<Employee, Void> worker = new SwingWorker<Employee, Void>() {
            @Override
            protected Employee doInBackground() throws Exception {
                return hrService. getEmployeeProfile(empId);
            }
            
            @Override
            protected void done() {
                try {
                    Employee emp = get();
                    if (emp != null) {
                        StringBuilder detailsBuilder = new StringBuilder();
                        detailsBuilder.append("Employee Details:\n\n");
                        detailsBuilder.append("ID:  ").append(emp.getEmployeeId()).append("\n");
                        detailsBuilder.append("Name: ").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("\n");
                        detailsBuilder.append("IC/Passport: ").append(emp.getIcPassport()).append("\n");
                        detailsBuilder.append("Email: ").append(emp.getEmail()).append("\n");
                        detailsBuilder.append("Phone: ").append(emp.getPhone()).append("\n");
                        detailsBuilder.append("Department: ").append(emp.getDepartment()).append("\n");
                        detailsBuilder. append("Position: ").append(emp.getPosition()).append("\n");
                        detailsBuilder.append("Join Date: ").append(emp.getJoinDate()).append("\n");
                        detailsBuilder.append("Monthly Salary: RM ").append(String.format("%.2f", emp.getMonthlySalary())).append("\n");
                        detailsBuilder.append("Bank Account: ").append(emp.getBankAccount()).append("\n");
                        detailsBuilder. append("Leave Balance: ").append(emp.getLeaveBalance()).append(" days");
                        
                        String details = detailsBuilder.toString();
                        
                        JOptionPane.showMessageDialog(EmployeeManagementPanel.this, 
                            details, 
                            "Employee Details", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(EmployeeManagementPanel.this, 
                        "Error:  " + e.getMessage() + "\n\nCheck console for details.", 
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
        button.setFont(new Font("Arial", Font.PLAIN, 13));
    }
}