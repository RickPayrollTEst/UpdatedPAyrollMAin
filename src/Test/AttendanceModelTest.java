package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.Attendance;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@DisplayName("Attendance Model Tests")
class AttendanceModelTest {

    private Attendance attendance;

    @BeforeEach
    void setUp() {
        attendance = new Attendance();
    }

    @Test
    @DisplayName("Should create valid attendance record")
    void testCreateValidAttendance() {
        // Arrange
        int employeeId = 10001;
        Date date = Date.valueOf(LocalDate.now());
        Time logIn = Time.valueOf(LocalTime.of(8, 0));
        Time logOut = Time.valueOf(LocalTime.of(17, 0));

        // Act
        attendance.setEmployeeId(employeeId);
        attendance.setDate(date);
        attendance.setLogIn(logIn);
        attendance.setLogOut(logOut);

        // Assert
        assertAll("Attendance validation",
            () -> assertEquals(employeeId, attendance.getEmployeeId()),
            () -> assertEquals(date, attendance.getDate()),
            () -> assertEquals(logIn, attendance.getLogIn()),
            () -> assertEquals(logOut, attendance.getLogOut()),
            () -> assertTrue(attendance.isPresent()),
            () -> assertEquals(9.0, attendance.getWorkHours(), 0.01)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid employee ID")
    void testInvalidEmployeeId() {
        assertThrows(IllegalArgumentException.class, 
            () -> attendance.setEmployeeId(-1));
    }

    @Test
    @DisplayName("Should detect late arrival")
    void testLateArrival() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 30))); // 30 minutes late
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));

        // Act & Assert
        assertTrue(attendance.isLate());
        assertEquals(30.0, attendance.getLateMinutes(), 0.01);
    }

    @Test
    @DisplayName("Should detect undertime")
    void testUndertime() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(16, 30))); // 30 minutes early

        // Act & Assert
        assertTrue(attendance.hasUndertime());
        assertEquals(30.0, attendance.getUndertimeMinutes(), 0.01);
    }

    @Test
    @DisplayName("Should calculate work duration correctly")
    void testWorkDurationCalculation() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 30)));

        // Act
        double workHours = attendance.getWorkHours();

        // Assert
        assertEquals(9.5, workHours, 0.01);
    }

    @Test
    @DisplayName("Should handle null log out time")
    void testNullLogOut() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(null);

        // Act & Assert
        assertTrue(attendance.isPresent());
        assertEquals(0.0, attendance.getWorkHours(), 0.01);
    }
}