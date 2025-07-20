/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.Payroll;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PayrollReport model class
 */
public class PayrollReport {
    private String reportTitle;
    private ReportType reportType;
    private String generatedBy;
    private LocalDate generatedDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private List<Payroll> payrollData;
    private ReportSummary summary;

    public enum ReportType {
        MONTHLY_PAYROLL,
        EMPLOYEE_PAYROLL,
        GOVERNMENT_CONTRIBUTIONS
    }

    // Constructors
    public PayrollReport() {
        this.generatedDate = LocalDate.now();
    }

    public PayrollReport(String reportTitle, ReportType reportType, String generatedBy) {
        this();
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.generatedBy = generatedBy;
    }

    // Getters and Setters
    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public List<Payroll> getPayrollData() { return payrollData; }
    public void setPayrollData(List<Payroll> payrollData) { this.payrollData = payrollData; }

    public ReportSummary getSummary() { return summary; }
    public void setSummary(ReportSummary summary) { this.summary = summary; }

    // Utility methods
    public String getFormattedPeriod() {
        if (periodStart != null && periodEnd != null) {
            return periodStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + 
                   " - " + periodEnd.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        return "";
    }

    public int getTotalEmployees() {
        return payrollData != null ? payrollData.size() : 0;
    }

    public double getTotalGrossPay() {
        return payrollData != null ? 
            payrollData.stream().mapToDouble(Payroll::getGrossPay).sum() : 0.0;
    }

    public double getTotalDeductions() {
        return payrollData != null ? 
            payrollData.stream().mapToDouble(Payroll::getTotalDeductions).sum() : 0.0;
    }

    public double getTotalNetPay() {
        return payrollData != null ? 
            payrollData.stream().mapToDouble(Payroll::getNetPay).sum() : 0.0;
    }

    /**
     * Report Summary inner class
     */
    public static class ReportSummary {
        private int totalEmployees;
        private double totalGrossPay;
        private double totalDeductions;
        private double totalNetPay;
        private double totalSSSContributions;
        private double totalPhilHealthContributions;
        private double totalPagIBIGContributions;
        private double totalTax;

        // Getters and Setters
        public int getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

        public double getTotalGrossPay() { return totalGrossPay; }
        public void setTotalGrossPay(double totalGrossPay) { this.totalGrossPay = totalGrossPay; }

        public double getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

        public double getTotalNetPay() { return totalNetPay; }
        public void setTotalNetPay(double totalNetPay) { this.totalNetPay = totalNetPay; }

        public double getTotalSSSContributions() { return totalSSSContributions; }
        public void setTotalSSSContributions(double totalSSSContributions) { this.totalSSSContributions = totalSSSContributions; }

        public double getTotalPhilHealthContributions() { return totalPhilHealthContributions; }
        public void setTotalPhilHealthContributions(double totalPhilHealthContributions) { this.totalPhilHealthContributions = totalPhilHealthContributions; }

        public double getTotalPagIBIGContributions() { return totalPagIBIGContributions; }
        public void setTotalPagIBIGContributions(double totalPagIBIGContributions) { this.totalPagIBIGContributions = totalPagIBIGContributions; }

        public double getTotalTax() { return totalTax; }
        public void setTotalTax(double totalTax) { this.totalTax = totalTax; }
    }
}

/**
 * AttendanceReport model class
 */
class AttendanceReport {
    private String reportTitle;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String generatedBy;
    private LocalDate generatedDate;
    private Map<Integer, List<model.Attendance>> attendanceData;

    // Constructors
    public AttendanceReport() {
        this.generatedDate = LocalDate.now();
    }

    // Getters and Setters
    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public Map<Integer, List<model.Attendance>> getAttendanceData() { return attendanceData; }
    public void setAttendanceData(Map<Integer, List<model.Attendance>> attendanceData) { this.attendanceData = attendanceData; }

    // Calculate summary statistics
    public void calculateSummaryStatistics() {
        // Implementation for calculating attendance statistics
        // This would include total days, average hours, etc.
    }
}