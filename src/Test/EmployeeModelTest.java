package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.Employee;
import java.time.LocalDate;

@DisplayName("Employee Model Tests")
class EmployeeModelTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Test
    @DisplayName("Should create valid employee")
    void testCreateValidEmployee() {
        // Arrange & Act
        employee.setEmployeeId(12345);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(50000.0);
        
        // Assert
        assertAll("Employee validation",
            () -> assertEquals(12345, employee.getEmployeeId()),
            () -> assertEquals("John", employee.getFirstName()),
            () -> assertEquals("Doe", employee.getLastName()),
            () -> assertEquals("John Doe", employee.getFullName()),
            () -> assertTrue(employee.isValid())
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid first name")
    void testInvalidFirstName() {
        assertAll("Invalid first name",
            () -> assertThrows(IllegalArgumentException.class, 
                () -> employee.setFirstName(null)),
            () -> assertThrows(IllegalArgumentException.class, 
                () -> employee.setFirstName("")),
            () -> assertThrows(IllegalArgumentException.class, 
                () -> employee.setFirstName("   "))
        );
    }

    @Test
    @DisplayName("Should calculate total allowances correctly")
    void testTotalAllowances() {
        // Arrange
        employee.setRiceSubsidy(1500.0);
        employee.setPhoneAllowance(1000.0);
        employee.setClothingAllowance(500.0);
        
        // Act
        double total = employee.getTotalAllowances();
        
        // Assert
        assertEquals(3000.0, total, 0.01);
    }
}