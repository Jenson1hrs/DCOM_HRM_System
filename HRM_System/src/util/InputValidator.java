/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

public class InputValidator {
    
    // Validate employee ID format (EMP001, EMP002, etc.)
    public static boolean isValidEmployeeId(String id) {
        return id != null && id.matches("EMP\\d{3}");
    }
    
    // Validate names (only letters and spaces, 2-50 chars)
    public static boolean isValidName(String name) {
        return name != null && name.matches("[A-Za-z\\s]{2,50}");
    }
    
    // Validate IC/Passport (alphanumeric, 7-20 chars)
    public static boolean isValidIcPassport(String ic) {
        return ic != null && ic.matches("[A-Z0-9]{7,20}");
    }
    
    // Basic email validation
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    // Phone number (Malaysia format: 012-3456789)
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{3}-\\d{7,8}");
    }
    
    // Strong password: at least 8 chars, letter + number
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
    }
    
    // Remove dangerous characters (SQL injection, XSS)
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        // Remove: quotes, semicolons, comment symbols
        return input.replaceAll("['\"\\;\\-\\-<>]", "");
    }
}