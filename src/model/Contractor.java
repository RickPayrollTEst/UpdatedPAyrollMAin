package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Contractor class demonstrating INHERITANCE and POLYMORPHISM
 * Extends Employee with contract-specific functionality  
 * FIXED: Removed enum conflicts, uses proper string status
 */
public class Contractor extends Employee {
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private double hourlyRate;
    private int contractedHours;
    private String contractType;
    private boolean isRenewable;
    
    // Constructor
    public Contractor() {
        super();
        this.contractStartDate = LocalDate.now();
        this.isRenewable = false;
        // FIXED: Use string instead of enum
        setStatus("Contractual");
    }
    
    public Contractor(String firstName, String lastName, int employeeId, 
                     double hourlyRate, int contractedHours, LocalDate contractEndDate) {
        super(firstName, lastName, employeeId);
        this.hourlyRate = hourlyRate;
        this.contractedHours = contractedHours;
        this.contractStartDate = LocalDate.now();
        this.contractEndDate = contractEndDate;
        this.isRenewable = false;
        setStatus("Contractual");
    }
    
    // Override methods to demonstrate POLYMORPHISM
    @Override
    public String getRole() {
        return contractType != null ? contractType + " Contractor" : "Contractor";
    }
    
    @Override
    public PersonType getPersonType() {
        return PersonType.CONTRACTOR;
    }
    
    // Override pay calculations for hourly-based work (POLYMORPHISM)
    @Override
    public double calculateBasePay() {
        return hourlyRate * contractedHours;
    }
    
    @Override
    public double calculateAllowances() {
        // Contractors typically don't get standard allowances
        return 0.0;
    }
    
    @Override
    public double calculateOvertimePay() {
        // Contractors don't get overtime pay
        return 0.0;
    }
    
    @Override
    public double calculateDeductions() {
        // Different tax structure for contractors
        return calculateBasePay() * 0.08; // 8% withholding tax
    }
    
    // Override leave eligibility (POLYMORPHISM)
    @Override
    public boolean isEligibleForLeave(String leaveType) {
        // Contractors have limited leave benefits
        return "emergency".equalsIgnoreCase(leaveType);
    }
    
    @Override
    public int getMaxLeavesDays(String leaveType) {
        return "emergency".equalsIgnoreCase(leaveType) ? 3 : 0;
    }
    
    @Override
    public boolean requiresApproval(String leaveType) {
        return true; // All contractor leave requires approval
    }
    
    // Override attendance tracking (POLYMORPHISM)
    @Override
    public boolean requiresTimeTracking() {
        return true; // Contractors must track hours closely
    }
    
    @Override
    public boolean isEligibleForOvertime() {
        return false; // Contractors work fixed hours
    }
    
    // Override bonus eligibility (POLYMORPHISM)
    @Override
    public boolean canReceiveBonus() {
        return false; // Contractors typically don't get bonuses
    }
    
    // Contractor-specific methods
    public boolean isContractActive() {
        LocalDate now = LocalDate.now();
        return contractEndDate != null && 
               !now.isBefore(contractStartDate) && 
               !now.isAfter(contractEndDate);
    }
    
    public boolean isContractExpiring(int daysFromNow) {
        if (contractEndDate == null) return false;
        LocalDate checkDate = LocalDate.now().plusDays(daysFromNow);
        return contractEndDate.isBefore(checkDate) || contractEndDate.isEqual(checkDate);
    }
    
    public long getDaysUntilContractEnd() {
        if (contractEndDate == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), contractEndDate);
    }
    
    public boolean canRenewContract() {
        return isRenewable && isContractExpiring(30);
    }
    
    // Getters and setters
    public LocalDate getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(LocalDate contractStartDate) { 
        this.contractStartDate = contractStartDate; 
    }
    
    public LocalDate getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(LocalDate contractEndDate) { 
        if (contractEndDate != null && contractStartDate != null && 
            contractEndDate.isBefore(contractStartDate)) {
            throw new IllegalArgumentException("Contract end date cannot be before start date");
        }
        this.contractEndDate = contractEndDate; 
    }
    
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { 
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.hourlyRate = hourlyRate; 
    }
    
    public int getContractedHours() { return contractedHours; }
    public void setContractedHours(int contractedHours) { 
        if (contractedHours < 0) {
            throw new IllegalArgumentException("Contracted hours cannot be negative");
        }
        this.contractedHours = contractedHours; 
    }
    
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { 
        this.contractType = contractType != null ? contractType.trim() : null; 
    }
    
    public boolean isRenewable() { return isRenewable; }
    public void setRenewable(boolean renewable) { this.isRenewable = renewable; }
}