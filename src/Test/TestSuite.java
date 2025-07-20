package Test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    EmployeeModelTest.class,
    AttendanceModelTest.class,
    PayrollCalculatorTest.class,
    EmployeeDAOTest.class,
    LoginFormTest.class,
    MotorPHPayrollSystemTest.class
})
public class TestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}