package ui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set RMI properties
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.setProperty("java. rmi.server.useLocalHostname", "true");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
        
        // Launch login frame
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}