package test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    EmployeeModelTest.class,
    AttendanceModelTest.class,
    PayrollCalculatorTest.class,
    EmployeeDAOTest.class,
    LoginFormTest.class
})
public class TestSuite {
}