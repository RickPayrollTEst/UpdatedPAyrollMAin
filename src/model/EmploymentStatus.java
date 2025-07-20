/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * EmploymentStatus model class - cleaned up version
 * Removed nested Position class to fix compilation errors
 */
public class EmploymentStatus {
    private int statusId;
    private String statusName;
    private String description;
    private boolean isActive;
    private boolean eligibleForBenefits;
    private int maxLeaveDays;

    // Constructors
    public EmploymentStatus() {
        this.isActive = true;
        this.eligibleForBenefits = false;
        this.maxLeaveDays = 0;
    }

    public EmploymentStatus(int statusId, String statusName) {
        this();
        this.statusId = statusId;
        this.statusName = statusName;
    }

    public EmploymentStatus(int statusId, String statusName, String description) {
        this(statusId, statusName);
        this.description = description;
    }

    // Getters and Setters
    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isEligibleForBenefits() {
        return eligibleForBenefits;
    }

    public void setEligibleForBenefits(boolean eligibleForBenefits) {
        this.eligibleForBenefits = eligibleForBenefits;
    }

    public int getMaxLeaveDays() {
        return maxLeaveDays;
    }

    public void setMaxLeaveDays(int maxLeaveDays) {
        this.maxLeaveDays = maxLeaveDays;
    }

    // Utility methods
    public boolean isRegularStatus() {
        return "Regular".equalsIgnoreCase(statusName);
    }

    public boolean isProbationaryStatus() {
        return "Probationary".equalsIgnoreCase(statusName);
    }

    public boolean isContractualStatus() {
        return "Contractual".equalsIgnoreCase(statusName);
    }

    @Override
    public String toString() {
        return statusName != null ? statusName : "Unknown Status";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmploymentStatus that = (EmploymentStatus) obj;
        return statusId == that.statusId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(statusId);
    }
}