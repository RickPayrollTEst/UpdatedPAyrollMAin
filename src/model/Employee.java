package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Enhanced Employee class demonstrating INHERITANCE, ABSTRACTION, and POLYMORPHISM
 * Extends Person and implements multiple interfaces
 * FIXED: All method reference errors resolved
 */
public class Employee extends Person implements Payable, AttendanceTrackable, LeaveEligible, Comparable<Employee> {
    protected int employeeId;
    protected String position;
    protected String status; // Changed from EmploymentStatus enum to String for simplicity
    protected String immediateSupervisor;
    protected double basicSalary;
    protected LocalDate hireDate;
    protected String department;
    
    // Government IDs
    protected String sssNumber;
    protected String philhealthNumber;
    protected String tinNumber;
    protected String pagibigNumber;
    
    // Allowances
    protected double riceSubsidy;
    protected double phoneAllowance;
    protected double clothingAllowance;
    protected double grossSemiMonthlyRate;
    protected double hourlyRate;
    
    // Status constants for better type safety
    public static final String STATUS_REGULAR = "Regular";
    public static final String STATUS_PROBATIONARY = "Probationary";
    public static final String STATUS_CONTRACTUAL = "Contractual";
    public static final String STATUS_PART_TIME = "Part-time";
    
    // Employment status enum - keeping it simple
    public enum EmploymentStatus {
        REGULAR("Regular", true, true, 15),
        PROBATIONARY("Probationary", true, false, 5),
        CONTRACTUAL("Contractual", false, false, 0),
        PART_TIME("Part-time", false, false, 0);
        
        private final String displayName;
        private final boolean eligibleForBenefits;
        private final boolean eligibleForOvertime;
        private final int maxLeaveDays;
        
        EmploymentStatus(String displayName, boolean eligibleForBenefits, 
                        boolean eligibleForOvertime, int maxLeaveDays) {
            this.displayName = displayName;
            this.eligibleForBenefits = eligibleForBenefits;
            this.eligibleForOvertime = eligibleForOvertime;
            this.maxLeaveDays = maxLeaveDays;
        }
        
        public String getDisplayName() { return displayName; }
        public boolean isEligibleForBenefits() { return eligibleForBenefits; }
        public boolean isEligibleForOvertime() { return eligibleForOvertime; }
        public int getMaxLeaveDays() { return maxLeaveDays; }
    }
    
    // Constructors
    public Employee() {
        super();
        this.status = STATUS_PROBATIONARY;
        this.hireDate = LocalDate.now();
        this.riceSubsidy = 0.0;
        this.phoneAllowance = 0.0;
        this.clothingAllowance = 0.0;
    }
    
    public Employee(String firstName, String lastName, int employeeId) {
        super(firstName, lastName);
        setEmployeeId(employeeId);
        this.status = STATUS_PROBATIONARY;
        this.hireDate = LocalDate.now();
        this.riceSubsidy = 0.0;
        this.phoneAllowance = 0.0;
        this.clothingAllowance = 0.0;
    }
    
    // Implementing abstract methods from Person (POLYMORPHISM)
    @Override
    public String getDisplayName() {
        return String.format("%s (ID: %d)", getFullName(), employeeId);
    }
    
    @Override
    public String getRole() {
        return position != null ? position : "Employee";
    }
    
    @Override
    public boolean isActive() {
        return status != null && !STATUS_CONTRACTUAL.equals(status);
    }
    
    @Override
    public PersonType getPersonType() {
        return PersonType.EMPLOYEE;
    }
    
    // Implementing Payable interface (POLYMORPHISM)
    @Override
    public double calculateBasePay() {
        return basicSalary;
    }
    
    @Override
    public double calculateAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
    @Override
    public double calculateOvertimePay() {
        // Default implementation - calculate based on overtime hours
        return 0.0; // Will be calculated by payroll processor
    }
    
    @Override
    public double calculateDeductions() {
        // Default implementation - basic government deductions
        return calculateGovernmentDeductions();
    }
    
    @Override
    public double calculateGrossPay() {
        return calculateBasePay() + calculateAllowances() + calculateOvertimePay();
    }
    
    @Override
    public double calculateNetPay() {
        return calculateGrossPay() - calculateDeductions();
    }
    
    // Implementing AttendanceTrackable interface (POLYMORPHISM)
    @Override
    public boolean canTrackAttendance() {
        return true; // All employees can have attendance tracked
    }
    
    @Override
    public boolean requiresTimeTracking() {
        return STATUS_REGULAR.equals(status) || STATUS_PROBATIONARY.equals(status);
    }
    
    @Override
    public int getRequiredWorkingHoursPerDay() {
        return STATUS_PART_TIME.equals(status) ? 4 : 8;
    }
    
    @Override
    public boolean isEligibleForOvertime() {
        return STATUS_REGULAR.equals(status) || STATUS_PROBATIONARY.equals(status);
    }
    
    // Implementing LeaveEligible interface (POLYMORPHISM)
    @Override
    public boolean isEligibleForLeave(String leaveType) {
        if (!isEligibleForBenefits()) return false;
        
        // Different leave types have different eligibility rules
        switch (leaveType.toLowerCase()) {
            case "sick":
                return true; // All benefit-eligible employees can take sick leave
            case "annual":
            case "vacation":
                return STATUS_REGULAR.equals(status);
            case "maternity":
            case "paternity":
                return STATUS_REGULAR.equals(status);
            default:
                return false;
        }
    }
    
    @Override
    public int getMaxLeavesDays(String leaveType) {
        if (!isEligibleForLeave(leaveType)) return 0;
        
        switch (leaveType.toLowerCase()) {
            case "sick":
                return 5;
            case "annual":
            case "vacation":
                return STATUS_REGULAR.equals(status) ? 15 : 5;
            case "maternity":
                return 105; // 15 weeks
            case "paternity":
                return 7;
            default:
                return 0;
        }
    }
    
    @Override
    public double getLeaveAllowance() {
        return isEligibleForLeave("annual") ? basicSalary / 22.0 : 0.0; // Daily rate
    }
    
    @Override
    public boolean requiresApproval(String leaveType) {
        return true; // All leave types require approval
    }
    
    // Implementing Comparable for natural ordering (POLYMORPHISM)
    @Override
    public int compareTo(Employee other) {
        // Sort by last name, then first name
        int lastNameCompare = this.lastName.compareToIgnoreCase(other.lastName);
        if (lastNameCompare != 0) return lastNameCompare;
        return this.firstName.compareToIgnoreCase(other.firstName);
    }
    
    // Business methods (can be overridden by subclasses)
    public boolean isRegularEmployee() {
        return STATUS_REGULAR.equals(status);
    }
    
    public boolean isEligibleForBenefits() {
        return STATUS_REGULAR.equals(status) || STATUS_PROBATIONARY.equals(status);
    }
    
    public boolean canReceiveBonus() {
        return isRegularEmployee() && basicSalary > 0;
    }
    
    public double getDailyRate() {
        return basicSalary / 22.0; // 22 working days per month
    }
    
    public double getHourlyRate() {
        return getDailyRate() / getRequiredWorkingHoursPerDay();
    }
    
    public int getYearsOfService() {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }
    
    // Helper method for government deductions
    protected double calculateGovernmentDeductions() {
        if (basicSalary <= 0) return 0.0;
        
        double sss = calculateSSS();
        double philhealth = calculatePhilHealth();
        double pagibig = calculatePagIBIG();
        
        return sss + philhealth + pagibig;
    }
    
    private double calculateSSS() {
        // Simplified SSS calculation
        if (basicSalary <= 4000) return 180.00;
        if (basicSalary <= 25000) return Math.min(basicSalary * 0.045, 1125.00);
        return 1125.00;
    }
    
    private double calculatePhilHealth() {
        // Simplified PhilHealth calculation
        double contribution = basicSalary * 0.025;
        return Math.max(Math.min(contribution, 5000.00), 500.00);
    }
    
    private double calculatePagIBIG() {
        // Simplified Pag-IBIG calculation
        if (basicSalary <= 1500) return basicSalary * 0.01;
        return Math.min(basicSalary * 0.02, 200.00);
    }
    
    // Enhanced validation
    @Override
    public boolean isValid() {
        return super.isValid() && 
               employeeId > 0 && 
               position != null && !position.trim().isEmpty() &&
               basicSalary >= 0;
    }
    
    // Getters and setters with validation
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { 
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId; 
    }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { 
        if (position != null && position.trim().isEmpty()) {
            throw new IllegalArgumentException("Position cannot be empty");
        }
        this.position = position != null ? position.trim() : null; 
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status != null ? status.trim() : STATUS_PROBATIONARY; 
    }
    
    // Support for EmploymentStatus enum
    public void setStatus(EmploymentStatus status) { 
        this.status = status != null ? status.getDisplayName() : STATUS_PROBATIONARY; 
    }
    
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { 
        this.immediateSupervisor = immediateSupervisor; 
    }
    
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { 
        if (basicSalary < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }
        this.basicSalary = basicSalary;
        // Auto-calculate derived fields
        this.grossSemiMonthlyRate = basicSalary / 2.0;
        this.hourlyRate = getDailyRate() / getRequiredWorkingHoursPerDay();
    }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    // Government ID getters/setters
    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    
    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    
    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    
    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }
    
    // Allowances getters/setters
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { 
        this.riceSubsidy = Math.max(0, riceSubsidy); 
    }
    
    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { 
        this.phoneAllowance = Math.max(0, phoneAllowance); 
    }
    
    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { 
        this.clothingAllowance = Math.max(0, clothingAllowance); 
    }
    
    public double getTotalAllowances() { 
        return riceSubsidy + phoneAllowance + clothingAllowance; 
    }
    
    // Backward compatibility methods
    public LocalDate getBirthday() { return getBirthDate(); }
    public void setBirthday(LocalDate birthday) { setBirthDate(birthday); }
    
    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double rate) { this.grossSemiMonthlyRate = rate; }
    
    public void setHourlyRate(double rate) { this.hourlyRate = rate; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Employee employee = (Employee) obj;
        return employeeId == employee.employeeId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeId);
    }
    
    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', position='%s', status='%s'}", 
                employeeId, getFullName(), position, status);
    }
}