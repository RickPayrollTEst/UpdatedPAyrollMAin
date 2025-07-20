package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoExtensions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dao.*;
import model.*;
import service.PayrollCalculator;
import service.JasperReportService;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive JUnit test suite for MotorPH Payroll System
 * Implements proper unit testing with JUnit 5 and Mockito
 */
@ExtendWith(MockitoExtensions.class)
@DisplayName("MotorPH Payroll System - Unit Tests")
class MotorPHPayrollSystemTest {

    @Mock
    private EmployeeDAO mockEmployeeDAO;
    
    @Mock
    private AttendanceDAO mockAttendanceDAO;
    
    @Mock
    private PayrollDAO mockPayrollDAO;
    
    @Mock
    private Connection mockConnection;

    private Employee testEmployee;
    private Attendance testAttendance;
    private Payroll testPayroll;
    private PayrollCalculator payrollCalculator;

    @BeforeAll
    static void setUpClass() {
        System.out.println("🧪 Starting MotorPH Payroll System Unit Tests");
    }

    @AfterAll
    static void tearDownClass() {
        System.out.println("✅ MotorPH Payroll System Unit Tests Completed");
    }

    @BeforeEach
    void setUp() {
        // Initialize test data
        setupTestEmployee();
        setupTestAttendance();
        setupTestPayroll();
        
        // Initialize services
        payrollCalculator = new PayrollCalculator();
        
        System.out.println("🔧 Test setup completed for: " + testEmployee.getFullName());
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        testEmployee = null;
        testAttendance = null;
        testPayroll = null;
    }

    // ===============================
    // EMPLOYEE MODEL TESTS
    // ===============================

    @Nested
    @DisplayName("Employee Model Tests")
    class EmployeeModelTests {

        @Test
        @DisplayName("Should create valid employee with required fields")
        void testCreateValidEmployee() {
            // Arrange
            Employee employee = new Employee();
            
            // Act
            employee.setEmployeeId(12345);
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setBasicSalary(50000.0);
            employee.setStatus("Regular");
            employee.setPosition("Software Developer");
            
            // Assert
            assertAll("Employee creation",
                () -> assertEquals(12345, employee.getEmployeeId()),
                () -> assertEquals("John", employee.getFirstName()),
                () -> assertEquals("Doe", employee.getLastName()),
                () -> assertEquals("John Doe", employee.getFullName()),
                () -> assertEquals(50000.0, employee.getBasicSalary()),
                () -> assertEquals("Regular", employee.getStatus()),
                () -> assertEquals("Software Developer", employee.getPosition())
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid first name")
        void testInvalidFirstNameThrowsException() {
            // Arrange
            Employee employee = new Employee();
            
            // Act & Assert
            assertAll("Invalid first name tests",
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setFirstName(null)),
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setFirstName("")),
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setFirstName("   "))
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid last name")
        void testInvalidLastNameThrowsException() {
            // Arrange
            Employee employee = new Employee();
            
            // Act & Assert
            assertAll("Invalid last name tests",
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setLastName(null)),
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setLastName("")),
                () -> assertThrows(IllegalArgumentException.class, 
                    () -> employee.setLastName("   "))
            );
        }

        @Test
        @DisplayName("Should throw exception for negative basic salary")
        void testNegativeBasicSalaryThrowsException() {
            // Arrange
            Employee employee = new Employee();
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> employee.setBasicSalary(-1000.0));
        }

        @Test
        @DisplayName("Should calculate total allowances correctly")
        void testTotalAllowancesCalculation() {
            // Arrange
            Employee employee = new Employee();
            employee.setRiceSubsidy(1500.0);
            employee.setPhoneAllowance(1000.0);
            employee.setClothingAllowance(500.0);
            
            // Act
            double totalAllowances = employee.getTotalAllowances();
            
            // Assert
            assertEquals(3000.0, totalAllowances, 0.01, 
                "Total allowances should be sum of all individual allowances");
        }

        @Test
        @DisplayName("Should calculate age correctly")
        void testAgeCalculation() {
            // Arrange
            Employee employee = new Employee();
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
            Employee employee = new Employee();
            employee.setBirthday(null);
            
            // Act
            int age = employee.getAge();
            
            // Assert
            assertEquals(0, age, "Age should be 0 if birthday is null");
        }
    }

    // dummy setup methods
    private void setupTestEmployee() {
        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmployeeId(1);
    }

    private void setupTestAttendance() {
        testAttendance = new Attendance();
    }

    private void setupTestPayroll() {
        testPayroll = new Payroll();
    }
}
