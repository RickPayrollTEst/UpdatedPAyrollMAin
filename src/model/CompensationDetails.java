package model;

/**
 * CompensationDetails model class
 * FIXED: Added missing default constructor and enhanced functionality
 */
public class CompensationDetails {
    private int compId;
    private int employeeId;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    
    // ENHANCED: Add composition with allowance objects
    private RiceAllowance riceAllowanceObj;
    private PhoneAllowance phoneAllowanceObj;
    private ClothingAllowance clothingAllowanceObj;

    // FIXED: Add default constructor
    public CompensationDetails() {
        // Initialize with default values
        this.riceSubsidy = 0.0;
        this.phoneAllowance = 0.0;
        this.clothingAllowance = 0.0;
    }

    public CompensationDetails(int employeeId) {
        this.employeeId = employeeId;
        // Initialize allowance objects (COMPOSITION)
        this.riceAllowanceObj = new RiceAllowance(employeeId);
        this.phoneAllowanceObj = new PhoneAllowance(employeeId, 800.0);
        this.clothingAllowanceObj = new ClothingAllowance(employeeId);
        
        // Calculate amounts
        updateAllowances();
    }
    
    private void updateAllowances() {
        if (riceAllowanceObj != null) {
            this.riceSubsidy = riceAllowanceObj.getCalculatedAmount();
        }
        if (phoneAllowanceObj != null) {
            this.phoneAllowance = phoneAllowanceObj.getCalculatedAmount();
        }
        if (clothingAllowanceObj != null) {
            this.clothingAllowance = clothingAllowanceObj.getCalculatedAmount();
        }
    }
    
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
    // ENHANCED: Calculate allowances based on eligibility
    public void calculateAllowancesForEmployee(Employee employee) {
        if (employee != null) {
            if (riceAllowanceObj != null && riceAllowanceObj.isEligible(employee)) {
                riceAllowanceObj.calculateAllowance();
                this.riceSubsidy = riceAllowanceObj.getAmount();
            }
            if (phoneAllowanceObj != null && phoneAllowanceObj.isEligible(employee)) {
                phoneAllowanceObj.calculateAllowance();
                this.phoneAllowance = phoneAllowanceObj.getAmount();
            }
            if (clothingAllowanceObj != null && clothingAllowanceObj.isEligible(employee)) {
                clothingAllowanceObj.calculateAllowance();
                this.clothingAllowance = clothingAllowanceObj.getAmount();
            }
        }
    }
    
    // Existing getters and setters...
    public int getCompId() { return compId; }
    public void setCompId(int compId) { this.compId = compId; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { 
        this.riceSubsidy = Math.max(0, riceSubsidy);
        if (riceAllowanceObj != null) {
            riceAllowanceObj.setAmount(riceSubsidy);
        }
    }
    
    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { 
        this.phoneAllowance = Math.max(0, phoneAllowance);
        if (phoneAllowanceObj != null) {
            phoneAllowanceObj.setAmount(phoneAllowance);
        }
    }
    
    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { 
        this.clothingAllowance = Math.max(0, clothingAllowance);
        if (clothingAllowanceObj != null) {
            clothingAllowanceObj.setAmount(clothingAllowance);
        }
    }
    
    // ENHANCED: Additional utility methods
    public boolean hasAllowances() {
        return getTotalAllowances() > 0;
    }
    
    public String getAllowancesSummary() {
        return String.format("Rice: ₱%.2f, Phone: ₱%.2f, Clothing: ₱%.2f (Total: ₱%.2f)", 
                riceSubsidy, phoneAllowance, clothingAllowance, getTotalAllowances());
    }
    
    @Override
    public String toString() {
        return String.format("CompensationDetails{compId=%d, employeeId=%d, total=₱%.2f}", 
                compId, employeeId, getTotalAllowances());
    }
}