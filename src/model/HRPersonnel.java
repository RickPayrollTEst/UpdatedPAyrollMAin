package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * HRPersonnel class demonstrating INHERITANCE and POLYMORPHISM
 * Extends Employee with HR-specific functionality
 * FIXED: Corrected enum usage and method references
 */
public class HRPersonnel extends Employee {
    private String hrSpecialization;
    private boolean canAccessConfidentialData;
    private List<String> certifications;
    
    // Constructor
    public HRPersonnel() {
        super();
        this.certifications = new ArrayList<>();
        this.canAccessConfidentialData = true;
        // FIXED: Use string instead of enum
        setStatus("Regular");
    }
    
    public HRPersonnel(String firstName, String lastName, int employeeId, String hrSpecialization) {
        super(firstName, lastName, employeeId);
        this.hrSpecialization = hrSpecialization;
        this.certifications = new ArrayList<>();
        this.canAccessConfidentialData = true;
        setStatus("Regular");
    }
    
    // Override methods to demonstrate POLYMORPHISM
    @Override
    public String getRole() {
        return hrSpecialization != null ? "HR " + hrSpecialization : "HR Personnel";
    }
    
    @Override
    public PersonType getPersonType() {
        return PersonType.HR_PERSONNEL;
    }
    
    // Override leave eligibility (POLYMORPHISM)
    @Override
    public boolean isEligibleForLeave(String leaveType) {
        // HR personnel have enhanced leave benefits
        if ("professional development".equalsIgnoreCase(leaveType)) {
            return true; // Special leave type for HR
        }
        return super.isEligibleForLeave(leaveType);
    }
    
    // Override maximum leave days (POLYMORPHISM)
    @Override
    public int getMaxLeavesDays(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "sick": return 15; // Enhanced sick leave
            case "vacation": return 25; // Enhanced vacation
            case "personal": return 10; // Enhanced personal leave
            case "professional development": return 5; // Special HR leave
            default: return super.getMaxLeavesDays(leaveType);
        }
    }
    
    // Override allowances calculation (POLYMORPHISM)
    @Override
    public double calculateAllowances() {
        double baseAllowances = super.calculateAllowances();
        double certificationAllowance = certifications.size() * 1000.0; // ₱1000 per certification
        double confidentialDataAllowance = canAccessConfidentialData ? 2000.0 : 0.0;
        return baseAllowances + certificationAllowance + confidentialDataAllowance;
    }
    
    // Override bonus eligibility (POLYMORPHISM)
    @Override
    public boolean canReceiveBonus() {
        return super.canReceiveBonus() && certifications.size() >= 2;
    }
    
    // HR-specific methods
    public boolean canAccessEmployeeRecords() {
        return isRegularEmployee() && canAccessConfidentialData;
    }
    
    public boolean canProcessPayroll() {
        return canAccessConfidentialData && 
               (hrSpecialization != null && hrSpecialization.toLowerCase().contains("payroll"));
    }
    
    public boolean canConductInterviews() {
        return isRegularEmployee() && 
               (hrSpecialization == null || hrSpecialization.toLowerCase().contains("recruitment"));
    }
    
    public boolean canApproveLeaveRequests() {
        return canAccessConfidentialData && getYearsOfService() >= 2;
    }
    
    public void addCertification(String certification) {
        if (certification != null && !certification.trim().isEmpty() && 
            !certifications.contains(certification.trim())) {
            certifications.add(certification.trim());
        }
    }
    
    public void removeCertification(String certification) {
        certifications.remove(certification);
    }
    
    // Getters and setters
    public String getHrSpecialization() { return hrSpecialization; }
    public void setHrSpecialization(String hrSpecialization) { 
        this.hrSpecialization = hrSpecialization != null ? hrSpecialization.trim() : null; 
    }
    
    public boolean canAccessConfidentialData() { return canAccessConfidentialData; }
    public void setCanAccessConfidentialData(boolean canAccessConfidentialData) { 
        this.canAccessConfidentialData = canAccessConfidentialData; 
    }
    
    public List<String> getCertifications() { return new ArrayList<>(certifications); }
    public void setCertifications(List<String> certifications) { 
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>(); 
    }
}