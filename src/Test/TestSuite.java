package Test;

/**
 * Test Suite to run all tests
 * Simple implementation without JUnit dependencies
 */
public class TestSuite {
    
    public static void main(String[] args) {
        System.out.println("🧪 Running MotorPH Payroll System Test Suite");
        System.out.println("=" .repeat(60));
        
        try {
            // Run all test classes
            System.out.println("\n📋 Running Employee Model Tests...");
            EmployeeModelTest.main(args);
            
            System.out.println("\n📋 Running Attendance Model Tests...");
            AttendanceModelTest.main(args);
            
            System.out.println("\n📋 Running Employee DAO Tests...");
            EmployeeDAOTest.main(args);
            
            System.out.println("\n📋 Running Login Form Tests...");
            LoginFormTest.main(args);
            
            System.out.println("\n📋 Running Payroll Calculator Tests...");
            PayrollCalculatorTest.main(args);
            
            System.out.println("\n📋 Running MotorPH System Tests...");
            MotorPHPayrollSystemTest.main(args);
            
            System.out.println("\n" + "=" .repeat(60));
            System.out.println("🎉 ALL TESTS COMPLETED SUCCESSFULLY!");
            System.out.println("✅ MotorPH Payroll System is ready for external testing");
            
        } catch (Exception e) {
            System.err.println("\n❌ Test Suite Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}