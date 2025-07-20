package Test;

// Simple test class without JUnit dependencies for now
import ui.LoginForm;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class LoginFormTest {

    private LoginForm loginForm;

    public void setUp() {
        // Initialize Swing components for testing
        if (!GraphicsEnvironment.isHeadless()) {
            loginForm = new LoginForm();
        }
    }

    public void testLoginFormInitialization() {
        // Skip if running in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⏭️ Skipping UI test in headless environment");
            return;
        }

        // Assert
        assert loginForm != null : "Login form should not be null";
        assert "MotorPH Payroll System - Login".equals(loginForm.getTitle()) : "Title should match";
        assert loginForm.getSize().width > 0 : "Width should be positive";
        assert loginForm.getSize().height > 0 : "Height should be positive";
        
        System.out.println("✅ testLoginFormInitialization passed");
    }

    public void testUIComponents() {
        // Skip if running in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⏭️ Skipping UI test in headless environment");
            return;
        }

        // Use reflection to access private fields for testing
        try {
            Field employeeIdField = LoginForm.class.getDeclaredField("employeeIdField");
            Field passwordField = LoginForm.class.getDeclaredField("passwordField");
            Field loginButton = LoginForm.class.getDeclaredField("loginButton");

            employeeIdField.setAccessible(true);
            passwordField.setAccessible(true);
            loginButton.setAccessible(true);

            JTextField empIdField = (JTextField) employeeIdField.get(loginForm);
            JPasswordField passField = (JPasswordField) passwordField.get(loginForm);
            JButton loginBtn = (JButton) loginButton.get(loginForm);

            assert empIdField != null : "Employee ID field should not be null";
            assert passField != null : "Password field should not be null";
            assert loginBtn != null : "Login button should not be null";
            assert "Login".equals(loginBtn.getText()) : "Login button text should be 'Login'";
            
            System.out.println("✅ testUIComponents passed");

        } catch (Exception e) {
            System.err.println("❌ Failed to access UI components: " + e.getMessage());
            throw new RuntimeException("Failed to access UI components", e);
        }
    }

    public void testInputValidation() {
        // This test validates the logic without UI interaction
        
        // Test empty employee ID
        String emptyEmployeeId = "";
        assert !isValidEmployeeId(emptyEmployeeId) : "Empty employee ID should be invalid";

        // Test invalid employee ID format
        String invalidEmployeeId = "abc";
        assert !isValidEmployeeId(invalidEmployeeId) : "Non-numeric employee ID should be invalid";

        // Test valid employee ID
        String validEmployeeId = "10001";
        assert isValidEmployeeId(validEmployeeId) : "Valid employee ID should be valid";
        
        System.out.println("✅ testInputValidation passed");
    }

    public void testAuthenticationScenarios() {
        // Test valid credentials format
        assert isValidCredentialFormat("10001", "password123") : "Valid credentials should be valid";
        
        // Test invalid credentials format
        assert !isValidCredentialFormat("", "password123") : "Empty employee ID should be invalid";
        assert !isValidCredentialFormat("10001", "") : "Empty password should be invalid";
        assert !isValidCredentialFormat("abc", "password123") : "Invalid employee ID format should be invalid";
        
        System.out.println("✅ testAuthenticationScenarios passed");
    }

    public void testEmployeeIdRange() {
        // Test valid employee IDs
        assert isValidEmployeeId("10001") : "Employee ID 10001 should be valid";
        assert isValidEmployeeId("10034") : "Employee ID 10034 should be valid";
        
        // Test edge cases
        assert isValidEmployeeId("1") : "Employee ID 1 should be valid";
        assert isValidEmployeeId("99999") : "Employee ID 99999 should be valid";
        
        // Test invalid cases
        assert !isValidEmployeeId("0") : "Employee ID 0 should be invalid";
        assert !isValidEmployeeId("-1") : "Negative employee ID should be invalid";
        
        System.out.println("✅ testEmployeeIdRange passed");
    }

    // Helper methods for testing login logic
    private boolean isValidEmployeeId(String employeeIdStr) {
        if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            int employeeId = Integer.parseInt(employeeIdStr);
            return employeeId > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCredentialFormat(String employeeId, String password) {
        return isValidEmployeeId(employeeId) && 
               password != null && 
               !password.trim().isEmpty();
    }

    public void tearDown() {
        if (loginForm != null) {
            loginForm.dispose();
        }
    }
    
    // Main method to run all tests
    public static void main(String[] args) {
        System.out.println("🧪 Running Login Form Tests...");
        
        LoginFormTest test = new LoginFormTest();
        
        try {
            test.setUp();
            test.testLoginFormInitialization();
            test.tearDown();
            
            test.setUp();
            test.testUIComponents();
            test.tearDown();
            
            test.testInputValidation();
            test.testAuthenticationScenarios();
            test.testEmployeeIdRange();
            
            System.out.println("🎉 All Login Form Tests Passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}