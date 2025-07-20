-- JasperReports Template Setup for MotorPH Payroll System
-- Addresses mentor feedback: "Report generation did not follow the requirement to use JasperReport"

-- Create reports metadata table
CREATE TABLE IF NOT EXISTS report_templates (
    template_id INT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL UNIQUE,
    template_file_path VARCHAR(255) NOT NULL,
    template_type ENUM('Payslip', 'Payroll Report', 'Attendance Report', 'Leave Report') NOT NULL,
    description TEXT,
    parameters JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert MotorPH report templates
INSERT INTO report_templates (template_name, template_file_path, template_type, description, parameters) VALUES
('MotorPH Employee Payslip', 'reports/templates/motorph_payslip.jrxml', 'Payslip', 'Official MotorPH employee payslip with company branding', 
 '{"company_name": "MotorPH", "company_address": "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City", "company_phone": "(028) 911-5071"}'),

('MotorPH Monthly Payroll Report', 'reports/templates/motorph_monthly_payroll.jrxml', 'Payroll Report', 'Monthly payroll summary report for all employees',
 '{"report_title": "Monthly Payroll Report", "include_summary": true, "include_government_contributions": true}'),

('MotorPH Attendance Report', 'reports/templates/motorph_attendance_report.jrxml', 'Attendance Report', 'Employee attendance summary report',
 '{"report_title": "Attendance Summary Report", "include_late_analysis": true, "include_overtime": true}'),

('MotorPH Leave Report', 'reports/templates/motorph_leave_report.jrxml', 'Leave Report', 'Employee leave requests and balances report',
 '{"report_title": "Leave Management Report", "include_balances": true, "include_pending_requests": true}');

-- Create report generation log table
CREATE TABLE IF NOT EXISTS report_generation_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    template_id INT NOT NULL,
    generated_by INT NOT NULL,
    report_parameters JSON,
    output_file_path VARCHAR(255),
    generation_status ENUM('Success', 'Failed', 'In Progress') DEFAULT 'In Progress',
    error_message TEXT,
    generation_time_ms INT,
    file_size_bytes BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES report_templates(template_id),
    FOREIGN KEY (generated_by) REFERENCES employees(employee_id)
);

-- Create stored procedure for report generation
DELIMITER //

CREATE PROCEDURE sp_generate_jasper_report(
    IN p_template_name VARCHAR(100),
    IN p_employee_id INT,
    IN p_parameters JSON,
    OUT p_report_id INT,
    OUT p_status VARCHAR(50),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_template_id INT;
    DECLARE v_template_path VARCHAR(255);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'Failed';
        SET p_message = 'Database error during report generation';
    END;
    
    START TRANSACTION;
    
    -- Get template information
    SELECT template_id, template_file_path
    INTO v_template_id, v_template_path
    FROM report_templates
    WHERE template_name = p_template_name AND is_active = TRUE;
    
    IF v_template_id IS NULL THEN
        SET p_status = 'Failed';
        SET p_message = 'Report template not found';
        ROLLBACK;
    ELSE
        -- Insert generation log
        INSERT INTO report_generation_log (template_id, generated_by, report_parameters)
        VALUES (v_template_id, p_employee_id, p_parameters);
        
        SET p_report_id = LAST_INSERT_ID();
        SET p_status = 'In Progress';
        SET p_message = CONCAT('Report generation started with ID: ', p_report_id);
        
        COMMIT;
    END IF;
    
END //

-- Procedure to update report generation status
CREATE PROCEDURE sp_update_report_status(
    IN p_report_id INT,
    IN p_status VARCHAR(50),
    IN p_output_path VARCHAR(255),
    IN p_error_message TEXT,
    IN p_generation_time_ms INT,
    IN p_file_size_bytes BIGINT
)
BEGIN
    UPDATE report_generation_log
    SET 
        generation_status = p_status,
        output_file_path = p_output_path,
        error_message = p_error_message,
        generation_time_ms = p_generation_time_ms,
        file_size_bytes = p_file_size_bytes
    WHERE log_id = p_report_id;
END //

DELIMITER ;

-- Create view for report analytics
CREATE OR REPLACE VIEW v_report_analytics AS
SELECT 
    rt.template_name,
    rt.template_type,
    COUNT(rgl.log_id) AS total_generations,
    SUM(CASE WHEN rgl.generation_status = 'Success' THEN 1 ELSE 0 END) AS successful_generations,
    SUM(CASE WHEN rgl.generation_status = 'Failed' THEN 1 ELSE 0 END) AS failed_generations,
    AVG(rgl.generation_time_ms) AS avg_generation_time_ms,
    AVG(rgl.file_size_bytes) AS avg_file_size_bytes,
    MAX(rgl.created_at) AS last_generated
FROM report_templates rt
LEFT JOIN report_generation_log rgl ON rt.template_id = rgl.template_id
GROUP BY rt.template_id, rt.template_name, rt.template_type;

-- Sample data for testing
INSERT INTO report_generation_log (template_id, generated_by, report_parameters, generation_status, generation_time_ms, file_size_bytes)
VALUES 
(1, 10001, '{"employee_id": 10001, "period": "2024-06"}', 'Success', 1500, 245760),
(2, 10006, '{"month": 6, "year": 2024}', 'Success', 3200, 512000),
(1, 10002, '{"employee_id": 10002, "period": "2024-06"}', 'Success', 1200, 238940);

-- Test the setup
SELECT 'Report Templates' AS table_name, COUNT(*) AS count FROM report_templates
UNION ALL
SELECT 'Report Generation Log' AS table_name, COUNT(*) AS count FROM report_generation_log;

-- Test the analytics view
SELECT * FROM v_report_analytics;

SELECT 'JasperReports setup completed successfully!' AS Status;