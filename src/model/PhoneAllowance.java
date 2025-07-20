package model;

/**
 * Concrete Phone Allowance implementation demonstrating INHERITANCE
 */
public class PhoneAllowance extends Allowance {
    private double baseAmount;
    
    public PhoneAllowance(int employeeId, double baseAmount) {
        super(employeeId, "Phone Allowance", baseAmount, true); // Taxable
        this.baseAmount = baseAmount;
    }
    
    @Override
    public void calculateAllowance() {
        // Phone allowance may vary based on position
        setAmount(baseAmount);
    }
    
    @Override
    public boolean isEligible(Object employee) {
        if (employee instanceof Employee) {
            Employee emp = (Employee) employee;
            return emp.isRegularEmployee();
        }
        return false;
    }
    
    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }
}