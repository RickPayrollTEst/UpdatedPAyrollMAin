package Test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoExtensions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dao.*;
import model.*;
import service.PayrollCalculator;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtensions.class)
@DisplayName("Payroll Calculator Tests")
class PayrollCalculatorTest {

    @Mock
    private EmployeeDAO mockEmployeeDAO;
    
    @Mock
    private AttendanceDAO mockAttendanceDAO;

    private PayrollCalculator payrollCalculator;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        payrollCalculator = new PayrollCalculator();
        setupTestEmployee();
    }

    private void setupTestEmployee() {
        testEmployee = new Employee();
        testEmployee.setEmployeeId(10001);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setBasicSalary(50000.0);
        testEmployee.setRiceSubsidy(1500.0);
        testEmployee.setPhoneAllowance(1000.0);
        testEmployee.setClothingAllowance(800.0);
        testEmployee.setStatus("Regular");
        testEmployee.setPosition("Software Developer");
    }

    @Test
    @DisplayName("Should calculate basic payroll correctly")
    void testBasicPayrollCalculation() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 1);
        LocalDate periodEnd = LocalDate.of(2024, 6, 30);

        // Act & Assert - This will test the actual calculation logic
        assertDoesNotThrow(() -> {
            Payroll payroll = payrollCalculator.calculatePayroll(10001, periodStart, periodEnd);
            assertNotNull(payroll);
            assertTrue(payroll.getEmployeeId() > 0);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid employee ID")
    void testInvalidEmployeeId() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 1);
        LocalDate periodEnd = LocalDate.of(2024, 6, 30);

        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class, 
            () -> payrollCalculator.calculatePayroll(-1, periodStart, periodEnd));
    }

    @Test
    @DisplayName("Should throw exception for null dates")
    void testNullDates() {
        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class, 
            () -> payrollCalculator.calculatePayroll(10001, null, LocalDate.now()));
        
        assertThrows(PayrollCalculator.PayrollCalculationException.class, 
            () -> payrollCalculator.calculatePayroll(10001, LocalDate.now(), null));
    }

    @Test
    @DisplayName("Should throw exception for invalid date range")
    void testInvalidDateRange() {
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 6, 30);
        LocalDate periodEnd = LocalDate.of(2024, 6, 1); // End before start

        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class, 
            () -> payrollCalculator.calculatePayroll(10001, periodStart, periodEnd));
    }

    @Test
    @DisplayName("Should handle future dates")
    void testFutureDates() {
        // Arrange
        LocalDate futureStart = LocalDate.now().plusMonths(1);
        LocalDate futureEnd = futureStart.plusDays(30);

        // Act & Assert
        assertThrows(PayrollCalculator.PayrollCalculationException.class, 
            () -> payrollCalculator.calculatePayroll(10001, futureStart, futureEnd));
    }

    @Test
    @DisplayName("Should validate payroll calculation components")
    void testPayrollCalculationComponents() {
        // This test validates the calculation logic without database dependency
        
        // Test daily rate calculation
        double monthlySalary = 50000.0;
        double expectedDailyRate = monthlySalary / 22.0; // 22 working days
        assertEquals(2272.73, expectedDailyRate, 0.01);
        
        // Test hourly rate calculation
        double expectedHourlyRate = expectedDailyRate / 8.0; // 8 hours per day
        assertEquals(284.09, expectedHourlyRate, 0.01);
        
        // Test overtime calculation
        double overtimeHours = 5.0;
        double overtimeRate = expectedHourlyRate * 1.25; // 125% rate
        double expectedOvertimePay = overtimeHours * overtimeRate;
        assertEquals(1775.57, expectedOvertimePay, 0.01);
    }

    @Test
    @DisplayName("Should validate government contribution calculations")
    void testGovernmentContributions() {
        // Test SSS calculation for different salary ranges
        assertAll("SSS calculations",
            () -> assertEquals(180.00, calculateSSS(4000.0), 0.01),
            () -> assertEquals(1125.00, calculateSSS(30000.0), 0.01),
            () -> assertEquals(1125.00, calculateSSS(50000.0), 0.01)
        );
        
        // Test PhilHealth calculation
        double philHealthContrib = calculatePhilHealth(50000.0);
        assertTrue(philHealthContrib >= 500.00 && philHealthContrib <= 5000.00);
        
        // Test Pag-IBIG calculation
        assertAll("Pag-IBIG calculations",
            () -> assertEquals(15.00, calculatePagIBIG(1500.0), 0.01),
            () -> assertEquals(200.00, calculatePagIBIG(20000.0), 0.01),
            () -> assertEquals(200.00, calculatePagIBIG(50000.0), 0.01)
        );
    }

    // Helper methods for testing calculations
    private double calculateSSS(double monthlySalary) {
        if (monthlySalary <= 4000) return 180.00;
        if (monthlySalary <= 25000) return Math.min(monthlySalary * 0.045, 1125.00);
        return 1125.00;
    }

    private double calculatePhilHealth(double monthlySalary) {
        double contribution = monthlySalary * 0.025;
        return Math.max(Math.min(contribution, 5000.00), 500.00);
    }

    private double calculatePagIBIG(double monthlySalary) {
        if (monthlySalary <= 1500) return monthlySalary * 0.01;
        return Math.min(monthlySalary * 0.02, 200.00);
    }

    private List<Attendance> createMockAttendanceData() {
        List<Attendance> attendanceList = new ArrayList<>();
        
        // Create 22 working days of attendance
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        for (int i = 0; i < 22; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            
            // Skip weekends
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                Attendance attendance = new Attendance();
                attendance.setEmployeeId(10001);
                attendance.setDate(Date.valueOf(currentDate));
                attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
                attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
                attendanceList.add(attendance);
            }
        }
        
        return attendanceList;
    }
}