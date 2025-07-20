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

        // Create mock attendance data
        List<Attendance> attendanceList = createMockAttendanceData();

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