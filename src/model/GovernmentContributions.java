/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Government Contributions model class
 * Represents SSS, PhilHealth, Pag-IBIG, and Tax contributions
 */
public class GovernmentContributions {
    private int contributionId;
    private int employeeId;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;

    // Constructors
    public GovernmentContributions() {}

    public GovernmentContributions(int employeeId, double sss, double philhealth, double pagibig, double tax) {
        this.employeeId = employeeId;
        this.sss = sss;
        this.philhealth = philhealth;
        this.pagibig = pagibig;
        this.tax = tax;
    }

    // Getters and Setters
    public int getContributionId() {
        return contributionId;
    }

    public void setContributionId(int contributionId) {
        this.contributionId = contributionId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public double getSss() {
        return sss;
    }

    public void setSss(double sss) {
        this.sss = sss;
    }

    public double getPhilhealth() {
        return philhealth;
    }

    public void setPhilhealth(double philhealth) {
        this.philhealth = philhealth;
    }

    public double getPagibig() {
        return pagibig;
    }

    public void setPagibig(double pagibig) {
        this.pagibig = pagibig;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    // Utility methods
    public double getTotalContributions() {
        return sss + philhealth + pagibig + tax;
    }

    @Override
    public String toString() {
        return "GovernmentContributions{" +
                "contributionId=" + contributionId +
                ", employeeId=" + employeeId +
                ", sss=" + sss +
                ", philhealth=" + philhealth +
                ", pagibig=" + pagibig +
                ", tax=" + tax +
                '}';
    }
}