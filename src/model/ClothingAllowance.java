package model;

/**
 * Concrete Clothing Allowance implementation demonstrating INHERITANCE
 */
public class ClothingAllowance extends Allowance {
    private static final double STANDARD_CLOTHING_ALLOWANCE = 1000.0;
    
    public ClothingAllowance(int employeeId) {
        super(employeeId, "Clothing Allowance", STANDARD_CLOTHING_ALLOWANCE, true);
    }
    
    @Override
    public void calculateAllowance() {
        setAmount(STANDARD_CLOTHING_ALLOWANCE);
    }
    
    @Override
    public boolean isEligible(Object employee) {
        if (employee instanceof Employee) {
            Employee emp = (Employee) employee;
            return emp.isRegularEmployee();
        }
        return false;
    }
}