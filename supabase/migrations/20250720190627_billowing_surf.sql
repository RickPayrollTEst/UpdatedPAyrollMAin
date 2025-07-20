-- Database Normalization to 3NF for MotorPH Payroll System
-- Addresses mentor feedback: "Database is not yet 3NF"

USE aoopdatabase_payroll;

-- Disable foreign key checks for restructuring
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 3NF NORMALIZATION IMPLEMENTATION
-- =============================================

-- 1. Create separate tables for better normalization

-- Departments table (eliminates redundancy in position/department)
CREATE TABLE IF NOT EXISTS departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    department_head VARCHAR(100),
    budget_allocation DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Positions table (separates position from employee)
CREATE TABLE IF NOT EXISTS positions (
    position_id INT AUTO_INCREMENT PRIMARY KEY,
    position_title VARCHAR(100) NOT NULL,
    department_id INT,
    min_salary DECIMAL(10,2),
    max_salary DECIMAL(10,2),
    job_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- Employee Status table (normalizes status)
CREATE TABLE IF NOT EXISTS employee_status (
    status_id INT AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    benefits_eligible BOOLEAN DEFAULT FALSE,
    overtime_eligible BOOLEAN DEFAULT FALSE,
    max_leave_days INT DEFAULT 0
);

-- Government ID Types table
CREATE TABLE IF NOT EXISTS government_id_types (
    id_type_id INT AUTO_INCREMENT PRIMARY KEY,
    id_type_name VARCHAR(50) NOT NULL UNIQUE,
    id_format_pattern VARCHAR(100),
    issuing_agency VARCHAR(100)
);

-- Employee Government IDs table (normalized)
CREATE TABLE IF NOT EXISTS employee_government_ids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    id_type_id INT NOT NULL,
    id_number VARCHAR(50) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    FOREIGN KEY (id_type_id) REFERENCES government_id_types(id_type_id),
    UNIQUE KEY unique_employee_id_type (employee_id, id_type_id)
);

-- =============================================
-- Insert reference data
-- =============================================

-- Insert departments
INSERT IGNORE INTO departments (department_name, department_head, budget_allocation) VALUES
('Executive', 'Garcia, Manuel III', 500000.00),
('Human Resources', 'Villanueva, Andrea Mae', 200000.00),
('Finance & Accounting', 'Aquino, Bianca Sofia', 300000.00),
('Information Technology', 'Hernandez, Eduard', 250000.00),
('Sales & Marketing', 'Reyes, Isabella', 400000.00),
('Operations', 'Lim, Antonio', 350000.00);

-- Insert positions
INSERT IGNORE INTO positions (position_title, department_id, min_salary, max_salary, job_description) VALUES
('Chief Executive Officer', 1, 80000, 120000, 'Overall company leadership and strategic direction'),
('Chief Operating Officer', 1, 55000, 80000, 'Operations oversight and management'),
('Chief Finance Officer', 1, 55000, 80000, 'Financial planning and management'),
('Chief Marketing Officer', 1, 55000, 80000, 'Marketing strategy and brand management'),
('HR Manager', 2, 45000, 60000, 'Human resources management and policy implementation'),
('HR Team Leader', 2, 35000, 50000, 'HR team coordination and employee relations'),
('HR Rank and File', 2, 20000, 30000, 'HR administrative tasks and support'),
('IT Operations and Systems', 4, 45000, 60000, 'IT infrastructure and systems management'),
('Accounting Head', 3, 45000, 60000, 'Accounting operations and financial reporting'),
('Payroll Manager', 3, 45000, 55000, 'Payroll processing and management'),
('Payroll Team Leader', 3, 35000, 45000, 'Payroll team coordination'),
('Payroll Rank and File', 3, 22000, 28000, 'Payroll processing support'),
('Account Manager', 5, 45000, 60000, 'Client account management'),
('Account Team Leader', 5, 35000, 50000, 'Account team coordination'),
('Account Rank and File', 5, 20000, 28000, 'Account support and administration'),
('Sales & Marketing', 5, 45000, 60000, 'Sales and marketing activities'),
('Supply Chain and Logistics', 6, 45000, 60000, 'Supply chain and logistics management'),
('Customer Service and Relations', 6, 45000, 60000, 'Customer service and relationship management');

-- Insert employee status types
INSERT IGNORE INTO employee_status (status_name, description, benefits_eligible, overtime_eligible, max_leave_days) VALUES
('Regular', 'Full-time regular employee with full benefits', TRUE, TRUE, 15),
('Probationary', 'Employee under probationary period', TRUE, TRUE, 5),
('Contractual', 'Contract-based employee', FALSE, FALSE, 0),
('Part-time', 'Part-time employee', FALSE, FALSE, 0);

-- Insert government ID types
INSERT IGNORE INTO government_id_types (id_type_name, id_format_pattern, issuing_agency) VALUES
('SSS', '##-#######-#', 'Social Security System'),
('PhilHealth', '############', 'Philippine Health Insurance Corporation'),
('TIN', '###-###-###-###', 'Bureau of Internal Revenue'),
('Pag-IBIG', '############', 'Home Development Mutual Fund');

-- =============================================
-- Migrate existing employee data to normalized structure
-- =============================================

-- Insert government IDs from existing employee data
INSERT IGNORE INTO employee_government_ids (employee_id, id_type_id, id_number)
SELECT e.employee_id, 1, e.sss_number 
FROM employees e 
WHERE e.sss_number IS NOT NULL AND e.sss_number != '';

INSERT IGNORE INTO employee_government_ids (employee_id, id_type_id, id_number)
SELECT e.employee_id, 2, e.philhealth_number 
FROM employees e 
WHERE e.philhealth_number IS NOT NULL AND e.philhealth_number != '';

INSERT IGNORE INTO employee_government_ids (employee_id, id_type_id, id_number)
SELECT e.employee_id, 3, e.tin_number 
FROM employees e 
WHERE e.tin_number IS NOT NULL AND e.tin_number != '';

INSERT IGNORE INTO employee_government_ids (employee_id, id_type_id, id_number)
SELECT e.employee_id, 4, e.pagibig_number 
FROM employees e 
WHERE e.pagibig_number IS NOT NULL AND e.pagibig_number != '';

-- =============================================
-- Create normalized employee table structure
-- =============================================

-- Create new normalized employees table
CREATE TABLE IF NOT EXISTS employees_normalized (
    employee_id INT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birthday DATE,
    address TEXT,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    hire_date DATE DEFAULT (CURRENT_DATE),
    position_id INT,
    status_id INT,
    immediate_supervisor_id INT,
    basic_salary DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    FOREIGN KEY (status_id) REFERENCES employee_status(status_id),
    FOREIGN KEY (immediate_supervisor_id) REFERENCES employees_normalized(employee_id)
);

-- Separate allowances table (3NF compliance)
CREATE TABLE IF NOT EXISTS employee_allowances (
    allowance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    allowance_type ENUM('Rice Subsidy', 'Phone Allowance', 'Clothing Allowance', 'Transportation', 'Meal', 'Other') NOT NULL,
    amount DECIMAL(8,2) NOT NULL DEFAULT 0,
    is_taxable BOOLEAN DEFAULT TRUE,
    effective_date DATE DEFAULT (CURRENT_DATE),
    expiry_date DATE NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    UNIQUE KEY unique_employee_allowance_type (employee_id, allowance_type, effective_date)
);

-- Insert allowances from existing employee data
INSERT IGNORE INTO employee_allowances (employee_id, allowance_type, amount, is_taxable)
SELECT employee_id, 'Rice Subsidy', rice_subsidy, FALSE FROM employees WHERE rice_subsidy > 0;

INSERT IGNORE INTO employee_allowances (employee_id, allowance_type, amount, is_taxable)
SELECT employee_id, 'Phone Allowance', phone_allowance, TRUE FROM employees WHERE phone_allowance > 0;

INSERT IGNORE INTO employee_allowances (employee_id, allowance_type, amount, is_taxable)
SELECT employee_id, 'Clothing Allowance', clothing_allowance, TRUE FROM employees WHERE clothing_allowance > 0;

-- =============================================
-- Create views for backward compatibility
-- =============================================

-- Employee view that maintains compatibility with existing code
CREATE OR REPLACE VIEW v_employees_compatible AS
SELECT 
    e.employee_id,
    e.first_name,
    e.last_name,
    CONCAT(e.last_name, ', ', e.first_name) AS full_name,
    e.birthday,
    e.address,
    e.phone_number,
    e.email,
    e.hire_date,
    p.position_title AS position,
    s.status_name AS status,
    e.basic_salary,
    e.immediate_supervisor_id,
    sup.full_name AS immediate_supervisor,
    COALESCE(rice.amount, 0) AS rice_subsidy,
    COALESCE(phone.amount, 0) AS phone_allowance,
    COALESCE(clothing.amount, 0) AS clothing_allowance,
    (e.basic_salary / 2) AS gross_semi_monthly_rate,
    (e.basic_salary / 22 / 8) AS hourly_rate,
    sss_id.id_number AS sss_number,
    phil_id.id_number AS philhealth_number,
    tin_id.id_number AS tin_number,
    pagibig_id.id_number AS pagibig_number,
    d.department_name AS department,
    TIMESTAMPDIFF(YEAR, e.birthday, CURDATE()) AS age,
    e.created_at,
    e.updated_at
FROM employees e
LEFT JOIN positions p ON e.position_id = p.position_id
LEFT JOIN employee_status s ON e.status_id = s.status_id
LEFT JOIN departments d ON p.department_id = d.department_id
LEFT JOIN v_employees_compatible sup ON e.immediate_supervisor_id = sup.employee_id
LEFT JOIN employee_allowances rice ON e.employee_id = rice.employee_id AND rice.allowance_type = 'Rice Subsidy' AND rice.is_active = TRUE
LEFT JOIN employee_allowances phone ON e.employee_id = phone.employee_id AND phone.allowance_type = 'Phone Allowance' AND phone.is_active = TRUE
LEFT JOIN employee_allowances clothing ON e.employee_id = clothing.employee_id AND clothing.allowance_type = 'Clothing Allowance' AND clothing.is_active = TRUE
LEFT JOIN employee_government_ids sss_id ON e.employee_id = sss_id.employee_id AND sss_id.id_type_id = 1
LEFT JOIN employee_government_ids phil_id ON e.employee_id = phil_id.employee_id AND phil_id.id_type_id = 2
LEFT JOIN employee_government_ids tin_id ON e.employee_id = tin_id.employee_id AND tin_id.id_type_id = 3
LEFT JOIN employee_government_ids pagibig_id ON e.employee_id = pagibig_id.employee_id AND pagibig_id.id_type_id = 4;

-- =============================================
-- Create triggers for data consistency
-- =============================================

DELIMITER //

-- Trigger to automatically calculate derived fields
CREATE TRIGGER tr_employee_salary_update
    BEFORE UPDATE ON employees
    FOR EACH ROW
BEGIN
    SET NEW.gross_semi_monthly_rate = NEW.basic_salary / 2;
    SET NEW.hourly_rate = NEW.basic_salary / 22 / 8;
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

-- Trigger to validate salary ranges based on position
CREATE TRIGGER tr_validate_employee_salary
    BEFORE INSERT ON employees
    FOR EACH ROW
BEGIN
    DECLARE v_min_salary DECIMAL(10,2);
    DECLARE v_max_salary DECIMAL(10,2);
    
    SELECT min_salary, max_salary 
    INTO v_min_salary, v_max_salary
    FROM positions p
    WHERE p.position_title = NEW.position;
    
    IF v_min_salary IS NOT NULL AND NEW.basic_salary < v_min_salary THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Salary below minimum for position';
    END IF;
    
    IF v_max_salary IS NOT NULL AND NEW.basic_salary > v_max_salary THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Salary above maximum for position';
    END IF;
END //

DELIMITER ;

-- =============================================
-- Create functions for business logic
-- =============================================

DELIMITER //

-- Function to calculate total compensation
CREATE FUNCTION fn_calculate_total_compensation(p_employee_id INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    
    SELECT 
        e.basic_salary + COALESCE(SUM(ea.amount), 0)
    INTO v_total
    FROM employees e
    LEFT JOIN employee_allowances ea ON e.employee_id = ea.employee_id AND ea.is_active = TRUE
    WHERE e.employee_id = p_employee_id
    GROUP BY e.employee_id, e.basic_salary;
    
    RETURN COALESCE(v_total, 0);
END //

-- Function to get employee department
CREATE FUNCTION fn_get_employee_department(p_employee_id INT)
RETURNS VARCHAR(100)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_department VARCHAR(100);
    
    SELECT d.department_name
    INTO v_department
    FROM employees e
    JOIN positions p ON e.position_id = p.position_id
    JOIN departments d ON p.department_id = d.department_id
    WHERE e.employee_id = p_employee_id;
    
    RETURN COALESCE(v_department, 'Unknown');
END //

DELIMITER ;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- Verification queries
-- =============================================

-- Test the normalized structure
SELECT 'Departments' AS table_name, COUNT(*) AS count FROM departments
UNION ALL
SELECT 'Positions' AS table_name, COUNT(*) AS count FROM positions
UNION ALL
SELECT 'Employee Status' AS table_name, COUNT(*) AS count FROM employee_status
UNION ALL
SELECT 'Government ID Types' AS table_name, COUNT(*) AS count FROM government_id_types
UNION ALL
SELECT 'Employee Government IDs' AS table_name, COUNT(*) AS count FROM employee_government_ids
UNION ALL
SELECT 'Employee Allowances' AS table_name, COUNT(*) AS count FROM employee_allowances;

-- Test the compatible view
SELECT employee_id, full_name, position, department, total_compensation 
FROM v_employees_compatible 
LIMIT 5;

-- Test the function
SELECT 
    employee_id, 
    full_name, 
    fn_calculate_total_compensation(employee_id) AS calculated_compensation,
    fn_get_employee_department(employee_id) AS department
FROM v_employees_compatible 
LIMIT 5;

SELECT '3NF Database normalization completed successfully!' AS Status;