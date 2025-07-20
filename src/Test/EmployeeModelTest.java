package Test;

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

    @Test
    @DisplayName("Should calculate age correctly")
    void testAgeCalculation() {
        // Arrange
        LocalDate birthDate = LocalDate.now().minusYears(25);
        employee.setBirthday(birthDate);
        
        // Act
        int age = employee.getAge();
        
        // Assert
        assertEquals(25, age, "Age should be calculated correctly");
    }

    @Test
    @DisplayName("Should return zero age for null birthday")
    void testNullBirthdayReturnsZeroAge() {
        // Arrange
        employee.setBirthday(null);
        
        // Act
        int age = employee.getAge();
        
        // Assert
        assertEquals(0, age, "Age should be 0 if birthday is null");
    }

    @Test
    @DisplayName("Should validate employee ID")
    void testEmployeeIdValidation() {
        // Test valid employee ID
        assertDoesNotThrow(() -> employee.setEmployeeId(10001));
        assertEquals(10001, employee.getEmployeeId());
        
        // Test invalid employee ID
        assertThrows(IllegalArgumentException.class, 
            () -> employee.setEmployeeId(-1));
        assertThrows(IllegalArgumentException.class, 
            () -> employee.setEmployeeId(0));
    }

    @Test
    @DisplayName("Should validate basic salary")
    void testBasicSalaryValidation() {
        // Test valid salary
        assertDoesNotThrow(() -> employee.setBasicSalary(50000.0));
        assertEquals(50000.0, employee.getBasicSalary());
        
        // Test invalid salary
        assertThrows(IllegalArgumentException.class, 
            () -> employee.setBasicSalary(-1000.0));
    }
}