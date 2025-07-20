package model;

/**
 * Concrete Rice Allowance implementation demonstrating INHERITANCE
 * FIXED: Corrected enum reference issue
 */
public class RiceAllowance extends Allowance {
    private static final double STANDARD_RICE_ALLOWANCE = 1500.0;
    
    public RiceAllowance(int employeeId) {
        super(employeeId, "Rice Subsidy", STANDARD_RICE_ALLOWANCE, false);
    }
    
    @Override
    public void calculateAllowance() {
        // Rice allowance is typically fixed
        setAmount(STANDARD_RICE_ALLOWANCE);
    }
    
    @Override
    public boolean isEligible(Object employee) {
        if (employee instanceof Employee) {
            Employee emp = (Employee) employee;
            // FIXED: Use string comparison instead of enum
            return emp.isRegularEmployee() || "Probationary".equals(emp.getStatus());
        }
        return false;
    }
}