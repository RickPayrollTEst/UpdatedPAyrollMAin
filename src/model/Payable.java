package model;

/**
 * Payable interface for all entities that can receive pay
 * Demonstrates ABSTRACTION through interface definition
 */
public interface Payable {
    double calculateBasePay();
    double calculateAllowances();
    double calculateOvertimePay();
    double calculateDeductions();
    double calculateGrossPay();
    double calculateNetPay();
    
    // Default method for common pay calculation logic
    default double getTotalEarnings() {
        return calculateBasePay() + calculateAllowances() + calculateOvertimePay();
    }
    
    default double getTotalDeductions() {
        return calculateDeductions();
    }
}