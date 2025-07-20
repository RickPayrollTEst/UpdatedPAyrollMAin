/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Position model class - moved from EmploymentStatus.java to separate file
 * This fixes the compilation error where Position was nested inside EmploymentStatus
 */
public class Position {
    private int positionId;
    private String positionName;
    private String department;
    private double minimumSalary;
    private double maximumSalary;
    private String description;

    // Constructors
    public Position() {}

    public Position(int positionId, String positionName) {
        this.positionId = positionId;
        this.positionName = positionName;
    }

    public Position(int positionId, String positionName, String department) {
        this(positionId, positionName);
        this.department = department;
    }

    // Getters and Setters
    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getMinimumSalary() {
        return minimumSalary;
    }

    public void setMinimumSalary(double minimumSalary) {
        this.minimumSalary = minimumSalary;
    }

    public double getMaximumSalary() {
        return maximumSalary;
    }

    public void setMaximumSalary(double maximumSalary) {
        this.maximumSalary = maximumSalary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Utility methods
    public boolean isValidSalaryRange(double salary) {
        return salary >= minimumSalary && salary <= maximumSalary;
    }

    public double getSalaryMidpoint() {
        return (minimumSalary + maximumSalary) / 2.0;
    }

    @Override
    public String toString() {
        return positionName != null ? positionName : "Unknown Position";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return positionId == position.positionId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(positionId);
    }
}