-- Database Views and Stored Procedures for MotorPH Payroll System
-- Addresses mentor feedback about not using views and stored procedures

USE aoopdatabase_payroll;

-- =============================================
-- VIEWS - For better data abstraction and security
-- =============================================

-- Employee Summary View (3NF compliant)
CREATE OR REPLACE VIEW v_employee_summary AS
SELECT 
    e.employee_id,
    CONCAT(e.last_name, ', ', e.first_name) AS full_name,
    e.first_name,
    e.last_name,
    e.position,
    e.status,
    e.basic_salary,
    e.immediate_supervisor,
    TIMESTAMPDIFF(YEAR, e.birthday, CURDATE()) AS age,
    e.phone_number,
    e.address,
    (e.rice_subsidy + e.phone_allowance + e.clothing_allowance) AS total_allowances,
    (e.basic_salary + e.rice_subsidy + e.phone_allowance + e.clothing_allowance) AS total_compensation
FROM employees e
WHERE e.employee_id IS NOT NULL;

-- Payroll Summary View
CREATE OR REPLACE VIEW v_payroll_summary AS
SELECT 
    p.payroll_id,
    p.employee_id,
    e.full_name,
    e.position,
    p.period_start,
    p.period_end,
    p.days_worked,
    p.gross_pay,
    p.total_deductions,
    p.net_pay,
    (p.sss + p.philhealth + p.pagibig) AS government_contributions,
    p.tax AS withholding_tax,
    DATE_FORMAT(p.period_start, '%Y-%m') AS pay_period
FROM payroll p
JOIN v_employee_summary e ON p.employee_id = e.employee_id;

-- Attendance Summary View
CREATE OR REPLACE VIEW v_attendance_summary AS
SELECT 
    a.employee_id,
    e.full_name,
    e.position,
    DATE_FORMAT(a.date, '%Y-%m') AS month_year,
    COUNT(*) AS days_present,
    AVG(TIMESTAMPDIFF(MINUTE, a.log_in, a.log_out) / 60.0) AS avg_hours_per_day,
    SUM(CASE WHEN TIME(a.log_in) > '08:15:00' THEN 1 ELSE 0 END) AS late_count,
    SUM(CASE WHEN TIME(a.log_out) < '17:00:00' THEN 1 ELSE 0 END) AS undertime_count
FROM attendance a
JOIN v_employee_summary e ON a.employee_id = e.employee_id
WHERE a.log_in IS NOT NULL
GROUP BY a.employee_id, e.full_name, e.position, DATE_FORMAT(a.date, '%Y-%m');

-- Leave Request Summary View
CREATE OR REPLACE VIEW v_leave_summary AS
SELECT 
    lr.employee_id,
    e.full_name,
    lr.leave_type,
    lr.start_date,
    lr.end_date,
    DATEDIFF(lr.end_date, lr.start_date) + 1 AS leave_days,
    lr.status,
    DATE_FORMAT(lr.created_at, '%Y-%m-%d') AS request_date
FROM leave_request lr
JOIN v_employee_summary e ON lr.employee_id = e.employee_id;

-- Government Contributions View
CREATE OR REPLACE VIEW v_government_contributions AS
SELECT 
    p.employee_id,
    e.full_name,
    e.position,
    DATE_FORMAT(p.period_start, '%Y-%m') AS contribution_period,
    p.sss,
    p.philhealth,
    p.pagibig,
    (p.sss + p.philhealth + p.pagibig) AS total_contributions,
    p.tax AS withholding_tax
FROM payroll p
JOIN v_employee_summary e ON p.employee_id = e.employee_id
WHERE p.sss > 0 OR p.philhealth > 0 OR p.pagibig > 0;

-- =============================================
-- STORED PROCEDURES - For complex business logic
-- =============================================

DELIMITER //

-- Procedure to calculate employee payroll
CREATE PROCEDURE sp_calculate_employee_payroll(
    IN p_employee_id INT,
    IN p_period_start DATE,
    IN p_period_end DATE,
    OUT p_gross_pay DECIMAL(10,2),
    OUT p_total_deductions DECIMAL(10,2),
    OUT p_net_pay DECIMAL(10,2)
)
BEGIN
    DECLARE v_basic_salary DECIMAL(10,2);
    DECLARE v_daily_rate DECIMAL(10,2);
    DECLARE v_days_worked INT;
    DECLARE v_rice_subsidy DECIMAL(8,2);
    DECLARE v_phone_allowance DECIMAL(8,2);
    DECLARE v_clothing_allowance DECIMAL(8,2);
    DECLARE v_sss DECIMAL(8,2);
    DECLARE v_philhealth DECIMAL(8,2);
    DECLARE v_pagibig DECIMAL(8,2);
    DECLARE v_tax DECIMAL(8,2);
    
    -- Get employee basic info
    SELECT basic_salary, rice_subsidy, phone_allowance, clothing_allowance
    INTO v_basic_salary, v_rice_subsidy, v_phone_allowance, v_clothing_allowance
    FROM employees 
    WHERE employee_id = p_employee_id;
    
    -- Calculate daily rate
    SET v_daily_rate = v_basic_salary / 22;
    
    -- Count working days
    SELECT COUNT(*)
    INTO v_days_worked
    FROM attendance
    WHERE employee_id = p_employee_id 
    AND date BETWEEN p_period_start AND p_period_end
    AND log_in IS NOT NULL;
    
    -- Calculate government contributions
    CALL sp_calculate_government_contributions(v_basic_salary, v_sss, v_philhealth, v_pagibig, v_tax);
    
    -- Calculate totals
    SET p_gross_pay = (v_days_worked * v_daily_rate) + v_rice_subsidy + v_phone_allowance + v_clothing_allowance;
    SET p_total_deductions = v_sss + v_philhealth + v_pagibig + v_tax;
    SET p_net_pay = p_gross_pay - p_total_deductions;
    
END //

-- Procedure to calculate government contributions
CREATE PROCEDURE sp_calculate_government_contributions(
    IN p_basic_salary DECIMAL(10,2),
    OUT p_sss DECIMAL(8,2),
    OUT p_philhealth DECIMAL(8,2),
    OUT p_pagibig DECIMAL(8,2),
    OUT p_tax DECIMAL(8,2)
)
BEGIN
    -- SSS Calculation
    CASE 
        WHEN p_basic_salary <= 4000 THEN SET p_sss = 180.00;
        WHEN p_basic_salary <= 4750 THEN SET p_sss = 202.50;
        WHEN p_basic_salary <= 5500 THEN SET p_sss = 225.00;
        WHEN p_basic_salary <= 6250 THEN SET p_sss = 247.50;
        WHEN p_basic_salary <= 7000 THEN SET p_sss = 270.00;
        WHEN p_basic_salary <= 25000 THEN SET p_sss = LEAST(p_basic_salary * 0.045, 1125.00);
        ELSE SET p_sss = 1125.00;
    END CASE;
    
    -- PhilHealth Calculation (5% total, 2.5% employee share)
    SET p_philhealth = GREATEST(LEAST((p_basic_salary * 0.05) / 2, 5000.00), 500.00);
    
    -- Pag-IBIG Calculation
    IF p_basic_salary <= 1500 THEN
        SET p_pagibig = p_basic_salary * 0.01;
    ELSE
        SET p_pagibig = LEAST(p_basic_salary * 0.02, 200.00);
    END IF;
    
    -- Tax Calculation (simplified)
    CASE 
        WHEN (p_basic_salary * 12) <= 250000 THEN SET p_tax = 0.00;
        WHEN (p_basic_salary * 12) <= 400000 THEN SET p_tax = ((p_basic_salary * 12) - 250000) * 0.15 / 12;
        WHEN (p_basic_salary * 12) <= 800000 THEN SET p_tax = (22500 + ((p_basic_salary * 12) - 400000) * 0.20) / 12;
        ELSE SET p_tax = (102500 + ((p_basic_salary * 12) - 800000) * 0.25) / 12;
    END CASE;
    
END //

-- Procedure to get employee attendance summary
CREATE PROCEDURE sp_get_attendance_summary(
    IN p_employee_id INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        COUNT(*) AS total_days,
        AVG(TIMESTAMPDIFF(MINUTE, log_in, log_out) / 60.0) AS avg_hours,
        SUM(CASE WHEN TIME(log_in) > '08:15:00' THEN 1 ELSE 0 END) AS late_count,
        SUM(CASE WHEN TIME(log_out) < '17:00:00' THEN 1 ELSE 0 END) AS undertime_count,
        SUM(TIMESTAMPDIFF(MINUTE, '08:00:00', log_in)) / 60.0 AS total_late_hours,
        SUM(TIMESTAMPDIFF(MINUTE, log_out, '17:00:00')) / 60.0 AS total_undertime_hours
    FROM attendance
    WHERE employee_id = p_employee_id
    AND date BETWEEN p_start_date AND p_end_date
    AND log_in IS NOT NULL;
END //

-- Procedure to get monthly payroll report
CREATE PROCEDURE sp_monthly_payroll_report(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT 
        e.employee_id,
        e.full_name,
        e.position,
        p.days_worked,
        p.gross_pay,
        p.total_deductions,
        p.net_pay,
        p.sss,
        p.philhealth,
        p.pagibig,
        p.tax
    FROM v_employee_summary e
    LEFT JOIN payroll p ON e.employee_id = p.employee_id
        AND YEAR(p.period_start) = p_year
        AND MONTH(p.period_start) = p_month
    ORDER BY e.last_name, e.first_name;
END //

-- Procedure to validate employee data
CREATE PROCEDURE sp_validate_employee(
    IN p_employee_id INT,
    IN p_first_name VARCHAR(50),
    IN p_last_name VARCHAR(50),
    IN p_basic_salary DECIMAL(10,2),
    OUT p_is_valid BOOLEAN,
    OUT p_error_message VARCHAR(255)
)
BEGIN
    DECLARE v_existing_count INT DEFAULT 0;
    
    SET p_is_valid = TRUE;
    SET p_error_message = '';
    
    -- Check if employee ID already exists (for new employees)
    SELECT COUNT(*) INTO v_existing_count
    FROM employees 
    WHERE employee_id = p_employee_id;
    
    IF v_existing_count > 0 THEN
        SET p_is_valid = FALSE;
        SET p_error_message = 'Employee ID already exists';
    END IF;
    
    -- Validate required fields
    IF p_first_name IS NULL OR TRIM(p_first_name) = '' THEN
        SET p_is_valid = FALSE;
        SET p_error_message = CONCAT(p_error_message, '; First name is required');
    END IF;
    
    IF p_last_name IS NULL OR TRIM(p_last_name) = '' THEN
        SET p_is_valid = FALSE;
        SET p_error_message = CONCAT(p_error_message, '; Last name is required');
    END IF;
    
    IF p_basic_salary < 0 THEN
        SET p_is_valid = FALSE;
        SET p_error_message = CONCAT(p_error_message, '; Basic salary cannot be negative');
    END IF;
    
    -- Clean up error message
    SET p_error_message = TRIM(LEADING '; ' FROM p_error_message);
    
END //

DELIMITER ;

-- =============================================
-- INDEXES for better performance (3NF optimization)
-- =============================================

-- Additional indexes for better query performance
CREATE INDEX idx_employees_name ON employees(last_name, first_name);
CREATE INDEX idx_employees_salary_range ON employees(basic_salary);
CREATE INDEX idx_payroll_period_employee ON payroll(period_start, period_end, employee_id);
CREATE INDEX idx_attendance_month ON attendance(employee_id, date);
CREATE INDEX idx_leave_request_period ON leave_request(employee_id, start_date, end_date);

-- =============================================
-- Test the views and procedures
-- =============================================

-- Test employee summary view
SELECT * FROM v_employee_summary LIMIT 5;

-- Test payroll summary view
SELECT * FROM v_payroll_summary LIMIT 5;

-- Test attendance summary view
SELECT * FROM v_attendance_summary LIMIT 5;

-- Test stored procedure
CALL sp_get_attendance_summary(10001, '2024-06-01', '2024-06-30');

SELECT 'Database views and stored procedures created successfully!' AS Status;