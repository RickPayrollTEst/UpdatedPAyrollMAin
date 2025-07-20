package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Manager class demonstrating INHERITANCE and POLYMORPHISM
 * Extends Employee with additional management-specific functionality
 * FIXED: Removed enum conflicts, uses proper string status
 */
public class Manager extends Employee {
    private double managementAllowance;
    private int teamSize;
    private List<Integer> subordinateIds;
    private double bonusMultiplier;
    
    // Constructor
    public Manager() {
        super();
        this.subordinateIds = new ArrayList<>();
        this.bonusMultiplier = 1.5; // Managers get 1.5x bonus multiplier
        this.managementAllowance = 5000.0; // Default management allowance
    }
    
    public Manager(String firstName, String lastName, int employeeId, double managementAllowance) {
        super(firstName, lastName, employeeId);
        this.managementAllowance = managementAllowance;
        this.subordinateIds = new ArrayList<>();
        this.bonusMultiplier = 1.5;
        // FIXED: Use string instead of enum
        setStatus("Regular");
    }
    
    // Override methods to demonstrate POLYMORPHISM
    @Override
    public String getRole() {
        return "Manager";
    }
    
    @Override
    public PersonType getPersonType() {
        return PersonType.MANAGER;
    }
    
    // Override allowance calculation (POLYMORPHISM)
    @Override
    public double calculateAllowances() {
        double baseAllowances = super.calculateAllowances();
        return baseAllowances + managementAllowance + calculateTeamAllowance();
    }
    
    // Override bonus eligibility (POLYMORPHISM)
    @Override
    public boolean canReceiveBonus() {
        return super.canReceiveBonus() && teamSize > 0;
    }
    
    // Override leave approval requirement (POLYMORPHISM)
    @Override
    public boolean requiresApproval(String leaveType) {
        // Managers need higher-level approval for certain leave types
        if ("annual".equalsIgnoreCase(leaveType) && getYearsOfService() > 5) {
            return false; // Senior managers can self-approve annual leave
        }
        return super.requiresApproval(leaveType);
    }
    
    // Manager-specific methods
    public double calculateTeamAllowance() {
        // Additional allowance based on team size
        return teamSize * 500.0; // ₱500 per team member
    }
    
    public double calculateManagerialBonus() {
        return calculateBasePay() * bonusMultiplier * 0.1; // 10% bonus with multiplier
    }
    
    public boolean canApproveLeave() {
        return isRegularEmployee() && teamSize > 0;
    }
    
    public boolean canApproveOvertime() {
        return isRegularEmployee();
    }
    
    public void addSubordinate(int employeeId) {
        if (employeeId > 0 && !subordinateIds.contains(employeeId)) {
            subordinateIds.add(employeeId);
            teamSize = subordinateIds.size();
        }
    }
    
    public void removeSubordinate(int employeeId) {
        subordinateIds.remove(Integer.valueOf(employeeId));
        teamSize = subordinateIds.size();
    }
    
    // Getters and setters
    public double getManagementAllowance() { return managementAllowance; }
    public void setManagementAllowance(double managementAllowance) { 
        if (managementAllowance < 0) {
            throw new IllegalArgumentException("Management allowance cannot be negative");
        }
        this.managementAllowance = managementAllowance; 
    }
    
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { 
        if (teamSize < 0) {
            throw new IllegalArgumentException("Team size cannot be negative");
        }
        this.teamSize = teamSize; 
    }
    
    public List<Integer> getSubordinateIds() { return new ArrayList<>(subordinateIds); }
    
    public double getBonusMultiplier() { return bonusMultiplier; }
    public void setBonusMultiplier(double bonusMultiplier) { 
        if (bonusMultiplier < 0) {
            throw new IllegalArgumentException("Bonus multiplier cannot be negative");
        }
        this.bonusMultiplier = bonusMultiplier; 
    }
}