package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced CompensationPackage class demonstrating INHERITANCE and COMPOSITION
 * This addresses mentor feedback about lack of OOP principles in models
 */
public class CompensationPackage {
    private int packageId;
    private int employeeId;
    private String packageName;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private boolean isActive;
    
    // COMPOSITION: Package contains multiple allowances
    private List<Allowance> allowances;
    private List<Deduction> deductions;
    
    // Package components
    private double baseSalary;
    private double totalAllowances;
    private double totalDeductions;
    private double netCompensation;
    
    // Constructors
    public CompensationPackage() {
        this.allowances = new ArrayList<>();
        this.deductions = new ArrayList<>();
        this.effectiveDate = LocalDate.now();
        this.isActive = true;
    }
    
    public CompensationPackage(int employeeId, String packageName, double baseSalary) {
        this();
        this.employeeId = employeeId;
        this.packageName = packageName;
        this.baseSalary = baseSalary;
    }
    
    // COMPOSITION: Add allowances to the package
    public void addAllowance(Allowance allowance) {
        if (allowance != null && !allowances.contains(allowance)) {
            allowances.add(allowance);
            recalculatePackage();
        }
    }
    
    public void removeAllowance(Allowance allowance) {
        if (allowances.remove(allowance)) {
            recalculatePackage();
        }
    }
    
    // COMPOSITION: Add deductions to the package
    public void addDeduction(Deduction deduction) {
        if (deduction != null && !deductions.contains(deduction)) {
            deductions.add(deduction);
            recalculatePackage();
        }
    }
    
    public void removeDeduction(Deduction deduction) {
        if (deductions.remove(deduction)) {
            recalculatePackage();
        }
    }
    
    // Calculate total package value
    public void recalculatePackage() {
        // Calculate total allowances
        this.totalAllowances = allowances.stream()
                .mapToDouble(Allowance::getCalculatedAmount)
                .sum();
        
        // Calculate total deductions
        this.totalDeductions = deductions.stream()
                .mapToDouble(deduction -> {
                    deduction.calculateDeduction();
                    return deduction.getAmount();
                })
                .sum();
        
        // Calculate net compensation
        this.netCompensation = baseSalary + totalAllowances - totalDeductions;
    }
    
    // Business methods
    public boolean isPackageActive() {
        if (!isActive) return false;
        
        LocalDate now = LocalDate.now();
        if (effectiveDate != null && now.isBefore(effectiveDate)) return false;
        if (expiryDate != null && now.isAfter(expiryDate)) return false;
        
        return true;
    }
    
    public double getGrossCompensation() {
        return baseSalary + totalAllowances;
    }
    
    public double getCompensationRatio() {
        return baseSalary > 0 ? (totalAllowances / baseSalary) * 100 : 0;
    }
    
    public List<Allowance> getTaxableAllowances() {
        return allowances.stream()
                .filter(Allowance::isTaxable)
                .toList();
    }
    
    public List<Allowance> getNonTaxableAllowances() {
        return allowances.stream()
                .filter(allowance -> !allowance.isTaxable())
                .toList();
    }
    
    public String getPackageSummary() {
        return String.format("Package: %s | Base: ₱%.2f | Allowances: ₱%.2f | Deductions: ₱%.2f | Net: ₱%.2f",
                packageName, baseSalary, totalAllowances, totalDeductions, netCompensation);
    }
    
    // Getters and Setters
    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { 
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId; 
    }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { 
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        this.packageName = packageName.trim(); 
    }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { 
        if (effectiveDate != null && expiryDate != null && expiryDate.isBefore(effectiveDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before effective date");
        }
        this.expiryDate = expiryDate; 
    }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { 
        if (baseSalary < 0) {
            throw new IllegalArgumentException("Base salary cannot be negative");
        }
        this.baseSalary = baseSalary;
        recalculatePackage();
    }
    
    public double getTotalAllowances() { return totalAllowances; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetCompensation() { return netCompensation; }
    
    public List<Allowance> getAllowances() { return new ArrayList<>(allowances); }
    public List<Deduction> getDeductions() { return new ArrayList<>(deductions); }
    
    @Override
    public String toString() {
        return String.format("CompensationPackage{id=%d, employee=%d, name='%s', net=₱%.2f}", 
                packageId, employeeId, packageName, netCompensation);
    }
}