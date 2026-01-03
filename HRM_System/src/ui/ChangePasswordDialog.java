package ui;

import javax.swing.*;
import java.awt.*;
import remote. HRMService;

public class ChangePasswordDialog extends JDialog {
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private HRMService hrService;
    private String userId;
    
    public ChangePasswordDialog(Frame parent, HRMService hrService, String userId) {
        super(parent, "Change Password", true);
        this.hrService = hrService;
        this. userId = userId;
        
        setSize(450, 350); 
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel. add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));  
        
        // Old Password
        JPanel oldPanel = createPasswordPanel("Current Password:", oldPasswordField = new JPasswordField());
        mainPanel.add(oldPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));  
        
        // New Password
        JPanel newPanel = createPasswordPanel("New Password:", newPasswordField = new JPasswordField());
        mainPanel.add(newPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));  
        
        // Confirm Password
        JPanel confirmPanel = createPasswordPanel("Confirm Password:", confirmPasswordField = new JPasswordField());
        mainPanel. add(confirmPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15))); 
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton changeButton = new JButton("Change Password");
        styleButton(changeButton, new Color(52, 152, 219));
        changeButton.addActionListener(e -> handleChangePassword());
        
        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(149, 165, 166));
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel. add(changeButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private JPanel createPasswordPanel(String label, JPasswordField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5)); 
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(400, 60)); 
        
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        field.setPreferredSize(new Dimension(340, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color. WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font. BOLD, 13));
        button.setPreferredSize(new Dimension(150, 35));
    }
    
    private void handleChangePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (! newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New passwords don't match!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!", "Validation Error", JOptionPane. ERROR_MESSAGE);
            return;
        }
        
        // Change password
        try {
            boolean success = hrService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                JOptionPane. showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error:  " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}