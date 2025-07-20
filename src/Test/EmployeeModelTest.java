package Test;

// Simple test class without JUnit dependencies for now
import model.Employee;
import java.time.LocalDate;

public class EmployeeModelTest {

    private Employee employee;

    public void setUp() {
        employee = new Employee();
    }

    public void testCreateValidEmployee() {
        // Arrange & Act
        employee.setEmployeeId(12345);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(50000.0);
        
        // Assert
        assert employee.getEmployeeId() == 12345 : "Employee ID should be 12345";
        assert "John".equals(employee.getFirstName()) : "First name should be John";
        assert "Doe".equals(employee.getLastName()) : "Last name should be Doe";
        assert "John Doe".equals(employee.getFullName()) : "Full name should be John Doe";
        assert employee.isValid() : "Employee should be valid";
        
        System.out.println("✅ testCreateValidEmployee passed");
    }

    public void testInvalidFirstName() {
        // Test null first name
        try {
            employee.setFirstName(null);
            assert false : "Should throw exception for null first name";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Test empty first name
        try {
            employee.setFirstName("");
            assert false : "Should throw exception for empty first name";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Test whitespace first name
        try {
            employee.setFirstName("   ");
            assert false : "Should throw exception for whitespace first name";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        System.out.println("✅ testInvalidFirstName passed");
    }

    public void testTotalAllowances() {
        // Arrange
        employee.setRiceSubsidy(1500.0);
        employee.setPhoneAllowance(1000.0);
        employee.setClothingAllowance(500.0);
        
        // Act
        double total = employee.getTotalAllowances();
        
        // Assert
        assert Math.abs(total - 3000.0) < 0.01 : "Total allowances should be 3000.0";
        
        System.out.println("✅ testTotalAllowances passed");
    }

    public void testAgeCalculation() {
        // Arrange
        LocalDate birthDate = LocalDate.now().minusYears(25);
        employee.setBirthday(birthDate);
        
        // Act
        int age = employee.getAge();
        
        // Assert
        assert age == 25 : "Age should be calculated correctly";
        
        System.out.println("✅ testAgeCalculation passed");
    }

    public void testNullBirthdayReturnsZeroAge() {
        // Arrange
        employee.setBirthday(null);
        
        // Act
        int age = employee.getAge();
        
        // Assert
        assert age == 0 : "Age should be 0 if birthday is null";
        
        System.out.println("✅ testNullBirthdayReturnsZeroAge passed");
    }

    public void testEmployeeIdValidation() {
        // Test valid employee ID
        try {
            employee.setEmployeeId(10001);
            assert employee.getEmployeeId() == 10001 : "Employee ID should be set correctly";
        } catch (Exception e) {
            assert false : "Should not throw exception for valid employee ID";
        }
        
        // Test invalid employee ID
        try {
            employee.setEmployeeId(-1);
            assert false : "Should throw exception for negative employee ID";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            employee.setEmployeeId(0);
            assert false : "Should throw exception for zero employee ID";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        System.out.println("✅ testEmployeeIdValidation passed");
    }

    public void testBasicSalaryValidation() {
        // Test valid salary
        try {
            employee.setBasicSalary(50000.0);
            assert Math.abs(employee.getBasicSalary() - 50000.0) < 0.01 : "Basic salary should be set correctly";
        } catch (Exception e) {
            assert false : "Should not throw exception for valid salary";
        }
        
        // Test invalid salary
        try {
            employee.setBasicSalary(-1000.0);
            assert false : "Should throw exception for negative salary";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        System.out.println("✅ testBasicSalaryValidation passed");
    }
    
    // Main method to run all tests
    public static void main(String[] args) {
        System.out.println("🧪 Running Employee Model Tests...");
        
        EmployeeModelTest test = new EmployeeModelTest();
        
        try {
            test.setUp();
            test.testCreateValidEmployee();
            
            test.setUp();
            test.testInvalidFirstName();
            
            test.setUp();
            test.testTotalAllowances();
            
            test.setUp();
            test.testAgeCalculation();
            
            test.setUp();
            test.testNullBirthdayReturnsZeroAge();
            
            test.setUp();
            test.testEmployeeIdValidation();
            
            test.setUp();
            test.testBasicSalaryValidation();
            
            System.out.println("🎉 All Employee Model Tests Passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}