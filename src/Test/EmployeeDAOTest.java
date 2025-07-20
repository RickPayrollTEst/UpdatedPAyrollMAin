package Test;

// Simple test class without JUnit dependencies for now
import dao.EmployeeDAO;
import model.Employee;
import java.time.LocalDate;
import java.util.List;

public class EmployeeDAOTest {

    private EmployeeDAO employeeDAO;
    private Employee testEmployee;

    public void setUp() {
        employeeDAO = new EmployeeDAO();
        setupTestEmployee();
    }

    private void setupTestEmployee() {
        testEmployee = new Employee();
        testEmployee.setEmployeeId(99999); // Use a test ID that won't conflict
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("Employee");
        testEmployee.setBasicSalary(30000.0);
        testEmployee.setStatus("Regular");
        testEmployee.setPosition("Test Position");
        testEmployee.setBirthday(LocalDate.of(1990, 1, 1));
        testEmployee.setRiceSubsidy(1500.0);
        testEmployee.setPhoneAllowance(1000.0);
        testEmployee.setClothingAllowance(800.0);
    }

    public void testGetAllEmployees() {
        // Act
        List<Employee> employees = employeeDAO.getAllEmployees();

        // Assert
        assert employees != null : "Employees list should not be null";
        assert !employees.isEmpty() : "Employees list should not be empty";
        assert employees.size() >= 34 : "Should have at least 34 employees from setup";
        
        System.out.println("✅ testGetAllEmployees passed - Found " + employees.size() + " employees");
    }

    public void testGetEmployeeById() {
        // Act
        Employee employee = employeeDAO.getEmployeeById(10001);

        // Assert
        assert employee != null : "Employee should not be null";
        assert employee.getEmployeeId() == 10001 : "Employee ID should be 10001";
        assert "Garcia".equals(employee.getLastName()) : "Last name should be Garcia";
        assert "Manuel III".equals(employee.getFirstName()) : "First name should be Manuel III";
        
        System.out.println("✅ testGetEmployeeById passed");
    }

    public void testGetNonExistentEmployee() {
        // Act
        Employee employee = employeeDAO.getEmployeeById(99999);

        // Assert
        assert employee == null : "Non-existent employee should return null";
        
        System.out.println("✅ testGetNonExistentEmployee passed");
    }

    public void testSearchEmployees() {
        // Act
        List<Employee> results = employeeDAO.searchEmployees("Garcia");

        // Assert
        assert results != null : "Search results should not be null";
        assert !results.isEmpty() : "Search results should not be empty";
        assert results.stream().anyMatch(emp -> 
            emp.getLastName().contains("Garcia") || emp.getFirstName().contains("Garcia")) : 
            "Results should contain Garcia";
        
        System.out.println("✅ testSearchEmployees passed - Found " + results.size() + " results");
    }

    public void testGetEmployeesByStatus() {
        // Act
        List<Employee> regularEmployees = employeeDAO.getEmployeesByStatus("Regular");
        List<Employee> probationaryEmployees = employeeDAO.getEmployeesByStatus("Probationary");

        // Assert
        assert regularEmployees != null : "Regular employees list should not be null";
        assert probationaryEmployees != null : "Probationary employees list should not be null";
        assert !regularEmployees.isEmpty() : "Regular employees list should not be empty";
        
        // Verify all returned employees have correct status
        assert regularEmployees.stream().allMatch(emp -> "Regular".equals(emp.getStatus())) : 
            "All regular employees should have Regular status";
        assert probationaryEmployees.stream().allMatch(emp -> "Probationary".equals(emp.getStatus())) : 
            "All probationary employees should have Probationary status";
        
        System.out.println("✅ testGetEmployeesByStatus passed");
    }

    public void testEmployeeExists() {
        // Act & Assert
        assert employeeDAO.employeeExists(10001) : "Employee 10001 should exist";
        assert !employeeDAO.employeeExists(99999) : "Employee 99999 should not exist";
        
        System.out.println("✅ testEmployeeExists passed");
    }

    public void testGetEmployeeCountByStatus() {
        // Act
        int regularCount = employeeDAO.getEmployeeCountByStatus("Regular");
        int probationaryCount = employeeDAO.getEmployeeCountByStatus("Probationary");

        // Assert
        assert regularCount > 0 : "Regular employee count should be greater than 0";
        assert probationaryCount >= 0 : "Probationary employee count should be non-negative";
        
        System.out.println("✅ testGetEmployeeCountByStatus passed - Regular: " + regularCount + ", Probationary: " + probationaryCount);
    }

    public void testEmployeeValidation() {
        // Test null employee
        try {
            employeeDAO.insertEmployee(null);
            assert false : "Should throw exception for null employee";
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test invalid employee ID
        Employee invalidEmployee = new Employee();
        invalidEmployee.setEmployeeId(-1);
        try {
            employeeDAO.insertEmployee(invalidEmployee);
            assert false : "Should throw exception for invalid employee ID";
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test missing first name
        invalidEmployee.setEmployeeId(99998);
        invalidEmployee.setFirstName("");
        try {
            employeeDAO.insertEmployee(invalidEmployee);
            assert false : "Should throw exception for missing first name";
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        System.out.println("✅ testEmployeeValidation passed");
    }
    
    // Main method to run all tests
    public static void main(String[] args) {
        System.out.println("🧪 Running Employee DAO Tests...");
        
        EmployeeDAOTest test = new EmployeeDAOTest();
        
        try {
            test.setUp();
            test.testGetAllEmployees();
            
            test.setUp();
            test.testGetEmployeeById();
            
            test.setUp();
            test.testGetNonExistentEmployee();
            
            test.setUp();
            test.testSearchEmployees();
            
            test.setUp();
            test.testGetEmployeesByStatus();
            
            test.setUp();
            test.testEmployeeExists();
            
            test.setUp();
            test.testGetEmployeeCountByStatus();
            
            test.setUp();
            test.testEmployeeValidation();
            
            System.out.println("🎉 All Employee DAO Tests Passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}