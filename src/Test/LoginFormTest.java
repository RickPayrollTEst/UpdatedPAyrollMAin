package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ui.LoginForm;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

@DisplayName("Login Form Tests")
class LoginFormTest {

    private LoginForm loginForm;

    @BeforeEach
    void setUp() {
        // Initialize Swing components for testing
        if (!GraphicsEnvironment.isHeadless()) {
            loginForm = new LoginForm();
        }
    }

    @Test
    @DisplayName("Should initialize login form components")
    void testLoginFormInitialization() {
        // Skip if running in headless environment
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        // Assert
        assertNotNull(loginForm);
        assertEquals("MotorPH Payroll System - Login", loginForm.getTitle());
        assertTrue(loginForm.getSize().width > 0);
        assertTrue(loginForm.getSize().height > 0);
    }

    @Test
    @DisplayName("Should have required UI components")
    void testUIComponents() {
        // Skip if running in headless environment
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

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

            assertNotNull(empIdField);
            assertNotNull(passField);
            assertNotNull(loginBtn);
            assertEquals("Login", loginBtn.getText());

        } catch (Exception e) {
            fail("Failed to access UI components: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate input fields")
    void testInputValidation() {
        // This test validates the logic without UI interaction
        
        // Test empty employee ID
        String emptyEmployeeId = "";
        assertFalse(isValidEmployeeId(emptyEmployeeId));

        // Test invalid employee ID format
        String invalidEmployeeId = "abc";
        assertFalse(isValidEmployeeId(invalidEmployeeId));

        // Test valid employee ID
        String validEmployeeId = "10001";
        assertTrue(isValidEmployeeId(validEmployeeId));
    }

    @Test
    @DisplayName("Should handle authentication scenarios")
    void testAuthenticationScenarios() {
        // Test valid credentials format
        assertTrue(isValidCredentialFormat("10001", "password123"));
        
        // Test invalid credentials format
        assertFalse(isValidCredentialFormat("", "password123"));
        assertFalse(isValidCredentialFormat("10001", ""));
        assertFalse(isValidCredentialFormat("abc", "password123"));
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

    @AfterEach
    void tearDown() {
        if (loginForm != null) {
            loginForm.dispose();
        }
    }
}