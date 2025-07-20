package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dao.EmployeeDAO;
import model.Employee;
import java.time.LocalDate;
import java.util.List;

@DisplayName("Employee DAO Tests")
class EmployeeDAOTest {

    private EmployeeDAO employeeDAO;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("Should retrieve all employees")
    void testGetAllEmployees() {
        // Act
        List<Employee> employees = employeeDAO.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertTrue(employees.size() >= 34); // Should have at least 34 employees from setup
    }

    @Test
    @DisplayName("Should retrieve employee by ID")
    void testGetEmployeeById() {
        // Act
        Employee employee = employeeDAO.getEmployeeById(10001);

        // Assert
        assertNotNull(employee);
        assertEquals(10001, employee.getEmployeeId());
        assertEquals("Garcia", employee.getLastName());
        assertEquals("Manuel III", employee.getFirstName());
    }

    @Test
    @DisplayName("Should return null for non-existent employee")
    void testGetNonExistentEmployee() {
        // Act
        Employee employee = employeeDAO.getEmployeeById(99999);

        // Assert
        assertNull(employee);
    }

    @Test
    @DisplayName("Should search employees by name")
    void testSearchEmployees() {
        // Act
        List<Employee> results = employeeDAO.searchEmployees("Garcia");

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(emp -> 
            emp.getLastName().contains("Garcia") || emp.getFirstName().contains("Garcia")));
    }

    @Test
    @DisplayName("Should get employees by status")
    void testGetEmployeesByStatus() {
        // Act
        List<Employee> regularEmployees = employeeDAO.getEmployeesByStatus("Regular");
        List<Employee> probationaryEmployees = employeeDAO.getEmployeesByStatus("Probationary");

        // Assert
        assertNotNull(regularEmployees);
        assertNotNull(probationaryEmployees);
        assertFalse(regularEmployees.isEmpty());
        
        // Verify all returned employees have correct status
        assertTrue(regularEmployees.stream().allMatch(emp -> "Regular".equals(emp.getStatus())));
        assertTrue(probationaryEmployees.stream().allMatch(emp -> "Probationary".equals(emp.getStatus())));
    }

    @Test
    @DisplayName("Should check if employee exists")
    void testEmployeeExists() {
        // Act & Assert
        assertTrue(employeeDAO.employeeExists(10001));
        assertFalse(employeeDAO.employeeExists(99999));
    }

    @Test
    @DisplayName("Should get employee count by status")
    void testGetEmployeeCountByStatus() {
        // Act
        int regularCount = employeeDAO.getEmployeeCountByStatus("Regular");
        int probationaryCount = employeeDAO.getEmployeeCountByStatus("Probationary");

        // Assert
        assertTrue(regularCount > 0);
        assertTrue(probationaryCount >= 0);
    }

    @Test
    @DisplayName("Should validate employee data before insertion")
    void testEmployeeValidation() {
        // Test null employee
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(null));

        // Test invalid employee ID
        Employee invalidEmployee = new Employee();
        invalidEmployee.setEmployeeId(-1);
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(invalidEmployee));

        // Test missing first name
        invalidEmployee.setEmployeeId(99998);
        invalidEmployee.setFirstName("");
        assertThrows(IllegalArgumentException.class, 
            () -> employeeDAO.insertEmployee(invalidEmployee));
    }
}