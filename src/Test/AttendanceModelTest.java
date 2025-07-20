package Test;

// Simple test class without JUnit dependencies for now
import model.Attendance;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceModelTest {

    private Attendance attendance;

    public void setUp() {
        attendance = new Attendance();
    }

    public void testCreateValidAttendance() {
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
        assert attendance.getEmployeeId() == employeeId : "Employee ID should match";
        assert attendance.getDate().equals(date) : "Date should match";
        assert attendance.getLogIn().equals(logIn) : "Log in time should match";
        assert attendance.getLogOut().equals(logOut) : "Log out time should match";
        assert attendance.isPresent() : "Should be present";
        assert Math.abs(attendance.getWorkHours() - 9.0) < 0.01 : "Work hours should be 9.0";
        
        System.out.println("✅ testCreateValidAttendance passed");
    }

    public void testInvalidEmployeeId() {
        try {
            attendance.setEmployeeId(-1);
            assert false : "Should throw exception for invalid employee ID";
        } catch (IllegalArgumentException e) {
            System.out.println("✅ testInvalidEmployeeId passed");
        }
    }

    public void testLateArrival() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 30))); // 30 minutes late
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));

        // Act & Assert
        assert attendance.isLate() : "Should detect late arrival";
        assert Math.abs(attendance.getLateMinutes() - 30.0) < 0.01 : "Late minutes should be 30";
        
        System.out.println("✅ testLateArrival passed");
    }

    public void testUndertime() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(16, 30))); // 30 minutes early

        // Act & Assert
        assert attendance.hasUndertime() : "Should detect undertime";
        assert Math.abs(attendance.getUndertimeMinutes() - 30.0) < 0.01 : "Undertime minutes should be 30";
        
        System.out.println("✅ testUndertime passed");
    }

    public void testWorkDurationCalculation() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 30)));

        // Act
        double workHours = attendance.getWorkHours();

        // Assert
        assert Math.abs(workHours - 9.5) < 0.01 : "Work hours should be 9.5";
        
        System.out.println("✅ testWorkDurationCalculation passed");
    }

    public void testNullLogOut() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(null);

        // Act & Assert
        assert attendance.isPresent() : "Should be present even with null log out";
        assert Math.abs(attendance.getWorkHours() - 0.0) < 0.01 : "Work hours should be 0 with null log out";
        
        System.out.println("✅ testNullLogOut passed");
    }
    
    // Main method to run all tests
    public static void main(String[] args) {
        System.out.println("🧪 Running Attendance Model Tests...");
        
        AttendanceModelTest test = new AttendanceModelTest();
        
        try {
            test.setUp();
            test.testCreateValidAttendance();
            
            test.setUp();
            test.testInvalidEmployeeId();
            
            test.setUp();
            test.testLateArrival();
            
            test.setUp();
            test.testUndertime();
            
            test.setUp();
            test.testWorkDurationCalculation();
            
            test.setUp();
            test.testNullLogOut();
            
            System.out.println("🎉 All Attendance Model Tests Passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}