/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {
    
    // Hash password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simpler hash if SHA-256 not available
            return simpleHash(password);
        }
    }
    
    // Simple hash fallback (for demonstration)
    private static String simpleHash(String password) {
        int hash = 7;
        for (int i = 0; i < password.length(); i++) {
            hash = hash * 31 + password.charAt(i);
        }
        return Integer.toString(hash);
    }
    
    // Verify password against stored hash
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(storedHash);
    }
    
    // Generate random password for new employees
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    // For debugging/development
    public static void main(String[] args) {
        String testPass = "password123";
        String hashed = hashPassword(testPass);
        System.out.println("Password: " + testPass);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verify correct: " + verifyPassword(testPass, hashed));
        System.out.println("Verify wrong: " + verifyPassword("wrongpass", hashed));
        System.out.println("Random password: " + generateRandomPassword());
    }
}