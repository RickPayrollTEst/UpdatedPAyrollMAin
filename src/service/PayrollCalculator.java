package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import dao.OvertimeDAO;
import dao.DeductionDAO;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.Overtime;
import model.Payroll;
import model.Deduction;

import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced PayrollCalculator with better error handling and AOOP compliance
 * Addresses mentor feedback about functionality bugs and proper OOP implementation
 */
public class PayrollCalculator {

    private static final Logger LOGGER = Logger.getLogger(PayrollCalculator.class.getName());

    // Constants for payroll calculations - moved to interface for better design
    public interface PayrollConstants {
        int STANDARD_WORKING_DAYS_PER_MONTH = 22;
        int STANDARD_WORKING_HOURS_PER_DAY = 8;
        double OVERTIME_RATE_MULTIPLIER = 1.25;
        LocalTime STANDARD_LOGIN_TIME = LocalTime.of(8, 0);
        LocalTime LATE_THRESHOLD_TIME = LocalTime.of(8, 15);
        LocalTime STANDARD_LOGOUT_TIME = LocalTime.of(17, 0);
    }

    // DAO instances with proper initialization
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;
    private LeaveRequestDAO leaveDAO;
    private OvertimeDAO overtimeDAO;
    private DeductionDAO deductionDAO;

    // Constructor with better error handling
    public PayrollCalculator() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();
        
        // Initialize optional DAOs with proper error handling
        initializeOptionalDAOs();
    }

    private void initializeOptionalDAOs() {
        try {
            this.leaveDAO = new LeaveRequestDAO();
            LOGGER.info("✅ LeaveRequestDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("⚠️ LeaveRequestDAO not available - leave calculations will be skipped: " + e.getMessage());
            this.leaveDAO = null;
        }

        try {
            this.overtimeDAO = new OvertimeDAO();
            LOGGER.info("✅ OvertimeDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("⚠️ OvertimeDAO not available - overtime calculations will be skipped: " + e.getMessage());
            this.overtimeDAO = null;
        }

        try {
            this.deductionDAO = new DeductionDAO();
            LOGGER.info("✅ DeductionDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("⚠️ DeductionDAO not available - deduction records will not be saved: " + e.getMessage());
            this.deductionDAO = null;
        }
    }

    /**
     * Main payroll calculation method with enhanced error handling
     * FIXED: Better error messages and null checks
     */
    public Payroll calculatePayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {

        try {
            // Enhanced input validation
            validateCalculationInputs(employeeId, periodStart, periodEnd);

            // Get employee with better error handling
            Employee employee = getEmployeeWithValidation(employeeId);

            // Initialize payroll object
            Payroll payroll = createPayrollObject(employeeId, periodStart, periodEnd, employee);

            // Calculate all payroll components with error handling
            calculateAllPayrollComponents(payroll, employee, periodStart, periodEnd);

            // Final calculations and validation
            finalizePayrollCalculation(payroll);

            logPayrollSummary(payroll, employee, periodStart, periodEnd);

            return payroll;

        } catch (PayrollCalculationException e) {
            throw e; // Re-throw custom exceptions
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("❌ Unexpected error calculating payroll for employee %d", employeeId), e);
            throw new PayrollCalculationException("Unexpected error during payroll calculation: " + e.getMessage(), e);
        }
    }

    private void validateCalculationInputs(int employeeId, LocalDate periodStart, LocalDate periodEnd) 
            throws PayrollCalculationException {
        if (employeeId <= 0) {
            throw new PayrollCalculationException("Invalid employee ID: " + employeeId + ". Employee ID must be positive.");
        }
        if (periodStart == null || periodEnd == null) {
            throw new PayrollCalculationException("Period dates cannot be null. Please provide valid start and end dates.");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new PayrollCalculationException("Invalid date range: Period end (" + periodEnd + ") cannot be before period start (" + periodStart + ")");
        }
        if (periodStart.isAfter(LocalDate.now())) {
            throw new PayrollCalculationException("Cannot calculate payroll for future periods. Period start: " + periodStart);
        }
    }

    private Employee getEmployeeWithValidation(int employeeId) throws PayrollCalculationException {
        try {
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                throw new PayrollCalculationException("Employee not found with ID: " + employeeId + ". Please verify the employee exists in the system.");
            }

            if (employee.getBasicSalary() <= 0) {
                throw new PayrollCalculationException("Invalid employee data: Employee " + employeeId + " has invalid basic salary (" + employee.getBasicSalary() + ")");
            }

            return employee;
        } catch (Exception e) {
            if (e instanceof PayrollCalculationException) {
                throw e;
            }
            throw new PayrollCalculationException("Error retrieving employee data for ID " + employeeId + ": " + e.getMessage(), e);
        }
    }

    private Payroll createPayrollObject(int employeeId, LocalDate periodStart, LocalDate periodEnd, Employee employee) {
        Payroll payroll = new Payroll(employeeId, Date.valueOf(periodStart), Date.valueOf(periodEnd));
        
        double monthlySalary = employee.getBasicSalary();
        double dailyRate = monthlySalary / PayrollConstants.STANDARD_WORKING_DAYS_PER_MONTH;

        payroll.setMonthlyRate(monthlySalary);
        payroll.setDailyRate(dailyRate);

        return payroll;
    }

    private void calculateAllPayrollComponents(Payroll payroll, Employee employee, LocalDate periodStart, LocalDate periodEnd) 
            throws PayrollCalculationException {
        try {
            double dailyRate = payroll.getDailyRate();
            
            // Calculate attendance-based earnings
            calculateAttendanceBasedEarnings(payroll, employee.getEmployeeId(), periodStart, periodEnd, dailyRate);

            // Calculate overtime earnings
            calculateOvertimeEarnings(payroll, employee.getEmployeeId(), periodStart, periodEnd, dailyRate);

            // Calculate allowances and benefits
            calculateAllowancesAndBenefits(payroll, employee);

            // Calculate time-based deductions
            calculateTimeBasedDeductions(payroll, employee.getEmployeeId(), periodStart, periodEnd, dailyRate);

            // Calculate government contributions and tax
            calculateGovernmentContributionsAndTax(payroll, employee.getBasicSalary());

        } catch (Exception e) {
            throw new PayrollCalculationException("Error calculating payroll components: " + e.getMessage(), e);
        }
    }

    private void finalizePayrollCalculation(Payroll payroll) throws PayrollCalculationException {
        try {
            payroll.calculateGrossPay();
            payroll.calculateTotalDeductions();
            payroll.calculateNetPay();

            // Validate final payroll
            validateFinalPayroll(payroll);
        } catch (Exception e) {
            throw new PayrollCalculationException("Error finalizing payroll calculations: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Better attendance calculation with proper error handling
     */
    private void calculateAttendanceBasedEarnings(Payroll payroll, int employeeId,
                                                   LocalDate periodStart, LocalDate periodEnd, double dailyRate) 
            throws PayrollCalculationException {

        LOGGER.info(String.format("🔍 Calculating attendance for employee %d (period: %s to %s)", 
                employeeId, periodStart, periodEnd));

        try {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                    employeeId, periodStart, periodEnd);

            if (attendanceList == null) {
                LOGGER.warning("⚠️ Attendance list is null for employee " + employeeId);
                payroll.setDaysWorked(0);
                payroll.setGrossEarnings(0.0);
                return;
            }

            LOGGER.info(String.format("📊 Found %d attendance records for employee %d", attendanceList.size(), employeeId));

            int validAttendanceDays = 0;
            double totalValidHours = 0.0;

            for (Attendance attendance : attendanceList) {
                if (attendance == null) {
                    LOGGER.warning("⚠️ Null attendance record found, skipping");
                    continue;
                }

                if (attendance.getLogIn() != null) {
                    validAttendanceDays++;
                    double workHours = attendance.getWorkHours();
                    totalValidHours += workHours;

                    LOGGER.fine(String.format("✓ Valid attendance: Date=%s, Hours=%.2f", 
                            attendance.getDate(), workHours));
                } else {
                    LOGGER.warning(String.format("⚠️ Invalid attendance (no log in): Date=%s", 
                            attendance.getDate()));
                }
            }

            // Set calculated values
            payroll.setDaysWorked(validAttendanceDays);
            double basicPay = validAttendanceDays * dailyRate;
            payroll.setGrossEarnings(basicPay);

            LOGGER.info(String.format("✅ Attendance calculation complete: %d days worked, %.2f basic pay", 
                    validAttendanceDays, basicPay));

            // Warn if no valid attendance found
            if (validAttendanceDays == 0) {
                LOGGER.warning(String.format("⚠️ No valid attendance found for employee %d in period %s to %s", 
                        employeeId, periodStart, periodEnd));
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error calculating attendance-based earnings", e);
            throw new PayrollCalculationException("Failed to calculate attendance-based earnings: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Better overtime calculation with proper null checks
     */
    private void calculateOvertimeEarnings(Payroll payroll, int employeeId,
                                           LocalDate periodStart, LocalDate periodEnd, double dailyRate) {
        if (overtimeDAO == null) {
            LOGGER.info("ℹ️ Overtime calculation skipped - OvertimeDAO not available");
            payroll.setTotalOvertimeHours(0.0);
            payroll.setOvertimePay(0.0);
            return;
        }

        try {
            List<Overtime> overtimeList = overtimeDAO.getOvertimeByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            if (overtimeList == null) {
                LOGGER.warning("⚠️ Overtime list is null for employee " + employeeId);
                payroll.setTotalOvertimeHours(0.0);
                payroll.setOvertimePay(0.0);
                return;
            }

            double totalOvertimeHours = overtimeList.stream()
                    .filter(overtime -> overtime != null && overtime.isApproved())
                    .mapToDouble(Overtime::getHours)
                    .sum();

            double hourlyRate = dailyRate / PayrollConstants.STANDARD_WORKING_HOURS_PER_DAY;
            double overtimePay = totalOvertimeHours * hourlyRate * PayrollConstants.OVERTIME_RATE_MULTIPLIER;

            payroll.setTotalOvertimeHours(totalOvertimeHours);
            payroll.setOvertimePay(overtimePay);

            LOGGER.info(String.format("✅ Overtime calculation: %.2f hours, ₱%.2f pay for employee %d", 
                    totalOvertimeHours, overtimePay, employeeId));

        } catch (Exception e) {
            LOGGER.warning("⚠️ Error calculating overtime, setting to 0: " + e.getMessage());
            payroll.setTotalOvertimeHours(0.0);
            payroll.setOvertimePay(0.0);
        }
    }

    /**
     * FIXED: Better allowances calculation with null safety
     */
    private void calculateAllowancesAndBenefits(Payroll payroll, Employee employee) {
        try {
            double riceSubsidy = employee.getRiceSubsidy();
            double phoneAllowance = employee.getPhoneAllowance();
            double clothingAllowance = employee.getClothingAllowance();

            payroll.setRiceSubsidy(riceSubsidy);
            payroll.setPhoneAllowance(phoneAllowance);
            payroll.setClothingAllowance(clothingAllowance);

            LOGGER.info(String.format("✅ Allowances for employee %d - Rice: ₱%.2f, Phone: ₱%.2f, Clothing: ₱%.2f",
                    employee.getEmployeeId(), riceSubsidy, phoneAllowance, clothingAllowance));

        } catch (Exception e) {
            LOGGER.warning("⚠️ Error calculating allowances, setting to 0: " + e.getMessage());
            payroll.setRiceSubsidy(0.0);
            payroll.setPhoneAllowance(0.0);
            payroll.setClothingAllowance(0.0);
        }
    }

    /**
     * FIXED: Enhanced time-based deductions with better error handling
     */
    private void calculateTimeBasedDeductions(Payroll payroll, int employeeId,
                                              LocalDate periodStart, LocalDate periodEnd, double dailyRate) {
        try {
            // Get attendance records safely
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                    employeeId, periodStart, periodEnd);

            if (attendanceList == null) {
                LOGGER.warning("⚠️ No attendance data for deduction calculation");
                setZeroDeductions(payroll);
                return;
            }

            // Calculate time-based deductions
            double lateDeduction = calculateLateDeduction(attendanceList, dailyRate);
            double undertimeDeduction = calculateUndertimeDeduction(attendanceList, dailyRate);

            payroll.setLateDeduction(lateDeduction);
            payroll.setUndertimeDeduction(undertimeDeduction);

            // Calculate unpaid leave deduction
            calculateUnpaidLeaveDeduction(payroll, employeeId, periodStart, periodEnd, dailyRate);

            LOGGER.info(String.format("✅ Time deductions for employee %d - Late: ₱%.2f, Undertime: ₱%.2f, Unpaid Leave: ₱%.2f",
                    employeeId, lateDeduction, undertimeDeduction, payroll.getUnpaidLeaveDeduction()));

        } catch (Exception e) {
            LOGGER.warning("⚠️ Error calculating time-based deductions: " + e.getMessage());
            setZeroDeductions(payroll);
        }
    }

    private void setZeroDeductions(Payroll payroll) {
        payroll.setLateDeduction(0.0);
        payroll.setUndertimeDeduction(0.0);
        payroll.setUnpaidLeaveDeduction(0.0);
        payroll.setUnpaidLeaveCount(0);
    }

    private void calculateUnpaidLeaveDeduction(Payroll payroll, int employeeId, 
                                               LocalDate periodStart, LocalDate periodEnd, double dailyRate) {
        if (leaveDAO == null) {
            LOGGER.info("ℹ️ Unpaid leave calculation skipped - LeaveRequestDAO not available");
            payroll.setUnpaidLeaveDeduction(0.0);
            payroll.setUnpaidLeaveCount(0);
            return;
        }

        try {
            List<LeaveRequest> approvedLeaves = leaveDAO.getApprovedLeavesByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            if (approvedLeaves == null) {
                payroll.setUnpaidLeaveDeduction(0.0);
                payroll.setUnpaidLeaveCount(0);
                return;
            }

            int unpaidLeaveCount = (int) approvedLeaves.stream()
                    .filter(leave -> leave != null && "Unpaid".equalsIgnoreCase(leave.getLeaveType()))
                    .mapToLong(LeaveRequest::getLeaveDays)
                    .sum();

            double unpaidLeaveDeduction = unpaidLeaveCount * dailyRate;

            payroll.setUnpaidLeaveCount(unpaidLeaveCount);
            payroll.setUnpaidLeaveDeduction(unpaidLeaveDeduction);

            if (unpaidLeaveCount > 0) {
                LOGGER.info(String.format("✅ Unpaid leave for employee %d: %d days, ₱%.2f deduction",
                        employeeId, unpaidLeaveCount, unpaidLeaveDeduction));
            }

        } catch (Exception e) {
            LOGGER.warning("⚠️ Error calculating unpaid leave deduction: " + e.getMessage());
            payroll.setUnpaidLeaveDeduction(0.0);
            payroll.setUnpaidLeaveCount(0);
        }
    }

    /**
     * FIXED: Enhanced late deduction calculation
     */
    private double calculateLateDeduction(List<Attendance> attendanceList, double dailyRate) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return 0.0;
        }

        double totalLateDeduction = 0.0;
        double hourlyRate = dailyRate / PayrollConstants.STANDARD_WORKING_HOURS_PER_DAY;

        for (Attendance attendance : attendanceList) {
            if (attendance == null || attendance.getLogIn() == null) {
                continue;
            }

            try {
                LocalTime loginTime = attendance.getLogIn().toLocalTime();

                if (loginTime.isAfter(PayrollConstants.LATE_THRESHOLD_TIME)) {
                    long minutesLate = ChronoUnit.MINUTES.between(PayrollConstants.STANDARD_LOGIN_TIME, loginTime);
                    double hoursLate = minutesLate / 60.0;
                    totalLateDeduction += hoursLate * hourlyRate;
                }
            } catch (Exception e) {
                LOGGER.warning("⚠️ Error processing attendance record for late calculation: " + e.getMessage());
            }
        }

        return totalLateDeduction;
    }

    /**
     * FIXED: Enhanced undertime deduction calculation
     */
    private double calculateUndertimeDeduction(List<Attendance> attendanceList, double dailyRate) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return 0.0;
        }

        double totalUndertimeDeduction = 0.0;
        double hourlyRate = dailyRate / PayrollConstants.STANDARD_WORKING_HOURS_PER_DAY;

        for (Attendance attendance : attendanceList) {
            if (attendance == null || attendance.getLogOut() == null) {
                continue;
            }

            try {
                LocalTime logoutTime = attendance.getLogOut().toLocalTime();

                if (logoutTime.isBefore(PayrollConstants.STANDARD_LOGOUT_TIME)) {
                    long minutesShort = ChronoUnit.MINUTES.between(logoutTime, PayrollConstants.STANDARD_LOGOUT_TIME);
                    double hoursShort = minutesShort / 60.0;
                    totalUndertimeDeduction += hoursShort * hourlyRate;
                }
            } catch (Exception e) {
                LOGGER.warning("⚠️ Error processing attendance record for undertime calculation: " + e.getMessage());
            }
        }

        return totalUndertimeDeduction;
    }

    /**
     * FIXED: Better government contributions calculation
     */
    private void calculateGovernmentContributionsAndTax(Payroll payroll, double monthlySalary) {
        try {
            double sss = calculateSSSContribution(monthlySalary);
            double philhealth = calculatePhilHealthContribution(monthlySalary);
            double pagibig = calculatePagIBIGContribution(monthlySalary);
            double tax = calculateIncomeTax(monthlySalary);

            payroll.setSss(sss);
            payroll.setPhilhealth(philhealth);
            payroll.setPagibig(pagibig);
            payroll.setTax(tax);

            LOGGER.info(String.format("✅ Government contributions - SSS: ₱%.2f, PhilHealth: ₱%.2f, Pag-IBIG: ₱%.2f, Tax: ₱%.2f",
                    sss, philhealth, pagibig, tax));

        } catch (Exception e) {
            LOGGER.warning("⚠️ Error calculating government contributions: " + e.getMessage());
            payroll.setSss(0.0);
            payroll.setPhilhealth(0.0);
            payroll.setPagibig(0.0);
            payroll.setTax(0.0);
        }
    }

    // Government contribution calculation methods (unchanged but with better error handling)
    private double calculateSSSContribution(double monthlySalary) {
        try {
            if (monthlySalary <= 4000) return 180.00;
            if (monthlySalary <= 4750) return 202.50;
            if (monthlySalary <= 5500) return 225.00;
            if (monthlySalary <= 6250) return 247.50;
            if (monthlySalary <= 7000) return 270.00;
            if (monthlySalary <= 7750) return 292.50;
            if (monthlySalary <= 8500) return 315.00;
            if (monthlySalary <= 9250) return 337.50;
            if (monthlySalary <= 10000) return 360.00;
            if (monthlySalary <= 15000) return 540.00;
            if (monthlySalary <= 20000) return 720.00;
            if (monthlySalary <= 25000) return 900.00;
            return 1125.00;
        } catch (Exception e) {
            LOGGER.warning("Error calculating SSS contribution: " + e.getMessage());
            return 0.0;
        }
    }

    private double calculatePhilHealthContribution(double monthlySalary) {
        try {
            double rate = 0.05;
            double employeeShare = (monthlySalary * rate) / 2;
            double minContribution = 500.00;
            double maxContribution = 5000.00;

            if (employeeShare < minContribution) return minContribution;
            if (employeeShare > maxContribution) return maxContribution;
            return employeeShare;
        } catch (Exception e) {
            LOGGER.warning("Error calculating PhilHealth contribution: " + e.getMessage());
            return 0.0;
        }
    }

    private double calculatePagIBIGContribution(double monthlySalary) {
        try {
            if (monthlySalary <= 1500) {
                return monthlySalary * 0.01;
            } else {
                double contribution = monthlySalary * 0.02;
                return Math.min(contribution, 200.00);
            }
        } catch (Exception e) {
            LOGGER.warning("Error calculating Pag-IBIG contribution: " + e.getMessage());
            return 0.0;
        }
    }

    private double calculateIncomeTax(double monthlySalary) {
        try {
            double annualSalary = monthlySalary * 12;
            double annualTax = 0.0;

            if (annualSalary <= 250000) {
                annualTax = 0.0;
            } else if (annualSalary <= 400000) {
                annualTax = (annualSalary - 250000) * 0.15;
            } else if (annualSalary <= 800000) {
                annualTax = 22500 + (annualSalary - 400000) * 0.20;
            } else if (annualSalary <= 2000000) {
                annualTax = 102500 + (annualSalary - 800000) * 0.25;
            } else if (annualSalary <= 8000000) {
                annualTax = 402500 + (annualSalary - 2000000) * 0.30;
            } else {
                annualTax = 2202500 + (annualSalary - 8000000) * 0.35;
            }

            return annualTax / 12;
        } catch (Exception e) {
            LOGGER.warning("Error calculating income tax: " + e.getMessage());
            return 0.0;
        }
    }

    private void validateFinalPayroll(Payroll payroll) throws PayrollCalculationException {
        if (!payroll.isValid()) {
            throw new PayrollCalculationException("Invalid payroll calculation result - basic validation failed");
        }
        
        if (payroll.getGrossPay() < 0) {
            throw new PayrollCalculationException("Invalid calculation: Gross pay cannot be negative (" + payroll.getGrossPay() + ")");
        }
        
        if (payroll.getTotalDeductions() < 0) {
            throw new PayrollCalculationException("Invalid calculation: Total deductions cannot be negative (" + payroll.getTotalDeductions() + ")");
        }

        if (payroll.getNetPay() < 0) {
            LOGGER.warning(String.format("⚠️ Negative net pay detected for employee %d: ₱%.2f (Gross: ₱%.2f, Deductions: ₱%.2f)",
                    payroll.getEmployeeId(), payroll.getNetPay(), payroll.getGrossPay(), payroll.getTotalDeductions()));
        }
    }

    private void logPayrollSummary(Payroll payroll, Employee employee, LocalDate periodStart, LocalDate periodEnd) {
        LOGGER.info(String.format("=== PAYROLL CALCULATION SUMMARY ==="));
        LOGGER.info(String.format("Employee: %s (ID: %d)", employee.getFullName(), employee.getEmployeeId()));
        LOGGER.info(String.format("Period: %s to %s", periodStart, periodEnd));
        LOGGER.info(String.format("Days Worked: %d", payroll.getDaysWorked()));
        LOGGER.info(String.format("Daily Rate: ₱%.2f", payroll.getDailyRate()));
        LOGGER.info(String.format("Basic Pay: ₱%.2f", payroll.getGrossEarnings()));
        LOGGER.info(String.format("Overtime Pay: ₱%.2f", payroll.getOvertimePay()));
        LOGGER.info(String.format("Total Allowances: ₱%.2f", 
                payroll.getRiceSubsidy() + payroll.getPhoneAllowance() + payroll.getClothingAllowance()));
        LOGGER.info(String.format("Gross Pay: ₱%.2f", payroll.getGrossPay()));
        LOGGER.info(String.format("Total Deductions: ₱%.2f", payroll.getTotalDeductions()));
        LOGGER.info(String.format("Net Pay: ₱%.2f", payroll.getNetPay()));
        LOGGER.info(String.format("====================================="));
    }

    /**
     * Enhanced custom exception for payroll calculation errors
     */
    public static class PayrollCalculationException extends Exception {
        public PayrollCalculationException(String message) {
            super(message);
        }

        public PayrollCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}