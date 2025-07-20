package ui;

import dao.AttendanceDAO;
import dao.PayrollDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import ui.PayrollDetailsDialog;
import ui.LoginForm;
import ui.LeaveRequestDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced Employee Dashboard with improved OOP design and better UI
 * Addresses mentor feedback about GUI improvements and functionality bugs
 */
public class EnhancedEmployeeDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Modern Color Scheme
    private static final Color PRIMARY_BLUE = new Color(52, 152, 219);
    private static final Color SECONDARY_BLUE = new Color(41, 128, 185);
    private static final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private static final Color WARNING_ORANGE = new Color(230, 126, 34);
    private static final Color DANGER_RED = new Color(231, 76, 60);
    private static final Color LIGHT_GRAY = new Color(236, 240, 241);
    private static final Color DARK_GRAY = new Color(52, 73, 94);

    // Personal Info Tab Components
    private JLabel nameLabel, positionLabel, statusLabel, salaryLabel;
    private JLabel phoneLabel, addressLabel, sssLabel, philhealthLabel;
    private JLabel tinLabel, pagibigLabel, birthdayLabel, ageLabel;

    // Attendance Tab Components
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JLabel totalDaysLabel, averageHoursLabel, lateCountLabel;
    private JButton refreshAttendanceButton;

    // Payroll Tab Components
    private JTable payrollTable;
    private DefaultTableModel payrollTableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JButton calculatePayrollButton;
    private JButton viewPayslipButton;

    // Leave Tab Components
    private JTable leaveTable;
    private DefaultTableModel leaveTableModel;
    private JButton submitLeaveButton;
    private JButton refreshLeaveButton;

    // Services
    private AttendanceDAO attendanceDAO;
    private PayrollDAO payrollDAO;
    private PayrollCalculator payrollCalculator;

    public EnhancedEmployeeDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Initialize services
            this.attendanceDAO = new AttendanceDAO();
            this.payrollDAO = new PayrollDAO();
            this.payrollCalculator = new PayrollCalculator();

            // Initialize UI
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            loadInitialData();

            System.out.println("✅ Enhanced Employee Dashboard initialized for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ Enhanced Employee Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            createErrorInterface(e);
        }

        // Set window properties
        setTitle("MotorPH Employee Portal - " + user.getFullName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void createErrorInterface(Exception error) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_GRAY);

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        String errorMessage = "<html><center>" +
                "<h1 style='color: #e74c3c;'>⚠️ System Error</h1>" +
                "<p style='font-size: 16px; margin: 20px 0;'>Unable to initialize Employee Dashboard</p>" +
                "<p><b>Error:</b> " + error.getMessage() + "</p>" +
                "<p><i>Please contact IT support for assistance.</i></p>" +
                "</center></html>";

        JLabel messageLabel = new JLabel(errorMessage, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton retryButton = createStyledButton("🔄 Retry", SUCCESS_GREEN);
        JButton logoutButton = createStyledButton("🚪 Logout", SECONDARY_BLUE);

        retryButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new EnhancedEmployeeDashboard(currentUser).setVisible(true));
        });

        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(retryButton);
        buttonPanel.add(logoutButton);

        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(errorPanel, BorderLayout.CENTER);
    }

    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Personal Info Labels
        nameLabel = createInfoLabel();
        positionLabel = createInfoLabel();
        statusLabel = createInfoLabel();
        salaryLabel = createInfoLabel();
        phoneLabel = createInfoLabel();
        addressLabel = createInfoLabel();
        sssLabel = createInfoLabel();
        philhealthLabel = createInfoLabel();
        tinLabel = createInfoLabel();
        pagibigLabel = createInfoLabel();
        birthdayLabel = createInfoLabel();
        ageLabel = createInfoLabel();

        // Attendance Components
        initializeAttendanceComponents();

        // Payroll Components
        initializePayrollComponents();

        // Leave Components
        initializeLeaveComponents();
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(DARK_GRAY);
        return label;
    }

    private void initializeAttendanceComponents() {
        String[] attendanceColumns = {"Date", "Log In", "Log Out", "Work Hours", "Status", "Late (min)", "Undertime (min)"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        setupTableStyling(attendanceTable);

        totalDaysLabel = createMetricLabel("0");
        averageHoursLabel = createMetricLabel("0.00");
        lateCountLabel = createMetricLabel("0");

        refreshAttendanceButton = createStyledButton("🔄 Refresh", PRIMARY_BLUE);
    }

    private void initializePayrollComponents() {
        String[] payrollColumns = {"Period", "Days Worked", "Basic Pay", "Allowances", "Deductions", "Net Pay", "Status"};
        payrollTableModel = new DefaultTableModel(payrollColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        payrollTable = new JTable(payrollTableModel);
        setupTableStyling(payrollTable);

        // Month/Year selectors
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        String[] years = {"2023", "2024", "2025"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        calculatePayrollButton = createStyledButton("💰 Calculate Payroll", SUCCESS_GREEN);
        viewPayslipButton = createStyledButton("📄 View Payslip", WARNING_ORANGE);
        viewPayslipButton.setEnabled(false);
    }

    private void initializeLeaveComponents() {
        String[] leaveColumns = {"Leave ID", "Type", "Start Date", "End Date", "Days", "Status", "Submitted"};
        leaveTableModel = new DefaultTableModel(leaveColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveTable = new JTable(leaveTableModel);
        setupTableStyling(leaveTable);

        submitLeaveButton = createStyledButton("📝 Submit Leave Request", PRIMARY_BLUE);
        refreshLeaveButton = createStyledButton("🔄 Refresh", SECONDARY_BLUE);
    }

    private JLabel createMetricLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(PRIMARY_BLUE);
        return label;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 35));
        return button;
    }

    private void setupTableStyling(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219, 50));
        table.setSelectionForeground(DARK_GRAY);
        table.setGridColor(new Color(189, 195, 199));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setBackground(Color.WHITE);
        table.setForeground(DARK_GRAY);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 249, 250));
                    }
                }
                
                return c;
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_GRAY);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabs
        tabbedPane.addTab("👤 Personal Info", createPersonalInfoTab());
        tabbedPane.addTab("📅 My Attendance", createAttendanceTab());
        tabbedPane.addTab("💰 My Payroll", createPayrollTab());
        tabbedPane.addTab("🏖️ Leave Requests", createLeaveTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_GRAY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Title and welcome
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(DARK_GRAY);

        JLabel titleLabel = new JLabel("MotorPH Employee Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFirstName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(LIGHT_GRAY);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(welcomeLabel, BorderLayout.SOUTH);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(DARK_GRAY);

        JButton profileButton = createStyledButton("👤 Profile", PRIMARY_BLUE);
        JButton logoutButton = createStyledButton("🚪 Logout", DANGER_RED);

        profileButton.addActionListener(e -> showProfileDialog());
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(profileButton);
        buttonPanel.add(logoutButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createPersonalInfoTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Main info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel titleLabel = new JLabel("Personal Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(DARK_GRAY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 30, 0);
        infoPanel.add(titleLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Add info fields in two columns
        addInfoField(infoPanel, gbc, "Full Name:", nameLabel, 0, 1);
        addInfoField(infoPanel, gbc, "Employee ID:", new JLabel(String.valueOf(currentUser.getEmployeeId())), 2, 1);
        addInfoField(infoPanel, gbc, "Position:", positionLabel, 0, 2);
        addInfoField(infoPanel, gbc, "Status:", statusLabel, 2, 2);
        addInfoField(infoPanel, gbc, "Basic Salary:", salaryLabel, 0, 3);
        addInfoField(infoPanel, gbc, "Birthday:", birthdayLabel, 2, 3);
        addInfoField(infoPanel, gbc, "Age:", ageLabel, 0, 4);
        addInfoField(infoPanel, gbc, "Phone:", phoneLabel, 2, 4);
        addInfoField(infoPanel, gbc, "Address:", addressLabel, 0, 5);
        addInfoField(infoPanel, gbc, "SSS Number:", sssLabel, 2, 5);
        addInfoField(infoPanel, gbc, "PhilHealth:", philhealthLabel, 0, 6);
        addInfoField(infoPanel, gbc, "TIN Number:", tinLabel, 2, 6);
        addInfoField(infoPanel, gbc, "Pag-IBIG:", pagibigLabel, 0, 7);

        // Allowances panel
        JPanel allowancesPanel = createAllowancesPanel();

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(allowancesPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addInfoField(JPanel parent, GridBagConstraints gbc, String labelText, JLabel valueLabel, int col, int row) {
        gbc.gridx = col; gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(DARK_GRAY);
        parent.add(label, gbc);

        gbc.gridx = col + 1;
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(DARK_GRAY);
        parent.add(valueLabel, gbc);
    }

    private JPanel createAllowancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel allowanceGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        allowanceGrid.setBackground(LIGHT_GRAY);

        // Create allowance cards
        JPanel riceCard = createAllowanceCard("🍚 Rice Subsidy",
                String.format("₱%.2f", currentUser.getRiceSubsidy()),
                SUCCESS_GREEN);

        JPanel phoneCard = createAllowanceCard("📱 Phone Allowance",
                String.format("₱%.2f", currentUser.getPhoneAllowance()),
                PRIMARY_BLUE);

        JPanel clothingCard = createAllowanceCard("👔 Clothing Allowance",
                String.format("₱%.2f", currentUser.getClothingAllowance()),
                WARNING_ORANGE);

        double totalAllowances = currentUser.getRiceSubsidy() +
                currentUser.getPhoneAllowance() +
                currentUser.getClothingAllowance();
        JPanel totalCard = createAllowanceCard("💰 Total Allowances",
                String.format("₱%.2f", totalAllowances),
                SECONDARY_BLUE);

        allowanceGrid.add(riceCard);
        allowanceGrid.add(phoneCard);
        allowanceGrid.add(clothingCard);
        allowanceGrid.add(totalCard);

        panel.add(allowanceGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAllowanceCard(String title, String amount, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel amountLabel = new JLabel(amount, JLabel.CENTER);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Summary panel
        JPanel summaryPanel = createAttendanceSummaryPanel();

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel tableTitle = new JLabel("📅 Recent Attendance Records");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(DARK_GRAY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAttendanceSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setBackground(LIGHT_GRAY);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Total days card
        JPanel totalDaysCard = createMetricCard("📊 Total Days", totalDaysLabel, PRIMARY_BLUE);

        // Average hours card
        JPanel avgHoursCard = createMetricCard("⏰ Avg Hours/Day", averageHoursLabel, SUCCESS_GREEN);

        // Late count card
        JPanel lateCard = createMetricCard("⚠️ Late Count", lateCountLabel, WARNING_ORANGE);

        summaryPanel.add(totalDaysCard);
        summaryPanel.add(avgHoursCard);
        summaryPanel.add(lateCard);

        return summaryPanel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 100));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Control panel
        JPanel controlPanel = createPayrollControlPanel();

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel tableTitle = new JLabel("💰 Payroll History");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(DARK_GRAY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPayrollControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Period selection
        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        periodPanel.setBackground(Color.WHITE);

        JLabel periodLabel = new JLabel("Select Period:");
        periodLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        periodLabel.setForeground(DARK_GRAY);

        periodPanel.add(periodLabel);
        periodPanel.add(monthComboBox);
        periodPanel.add(yearComboBox);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(calculatePayrollButton);
        buttonPanel.add(viewPayslipButton);

        controlPanel.add(periodPanel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        controlPanel.add(submitLeaveButton);
        controlPanel.add(refreshLeaveButton);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel tableTitle = new JLabel("🏖️ My Leave Requests");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(DARK_GRAY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT_GRAY);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createVerticalStrut(15), BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        panel.add(mainPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setBackground(Color.WHITE);

        JLabel statusLabel = new JLabel("Ready | Employee ID: " + currentUser.getEmployeeId() + 
                " | Position: " + currentUser.getPosition());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(DARK_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel timeLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(DARK_GRAY);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void setupEventHandlers() {
        // Attendance events
        refreshAttendanceButton.addActionListener(e -> loadAttendanceData());

        // Payroll events
        monthComboBox.addActionListener(e -> loadPayrollData());
        yearComboBox.addActionListener(e -> loadPayrollData());
        calculatePayrollButton.addActionListener(e -> calculateCurrentPayroll());
        viewPayslipButton.addActionListener(e -> viewSelectedPayslip());

        // Leave events
        submitLeaveButton.addActionListener(e -> showLeaveRequestDialog());
        refreshLeaveButton.addActionListener(e -> loadLeaveData());

        // Table selection events
        payrollTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                viewPayslipButton.setEnabled(payrollTable.getSelectedRow() != -1);
            }
        });
    }

    private void loadInitialData() {
        loadPersonalInfo();
        loadAttendanceData();
        loadPayrollData();
        loadLeaveData();
    }

    private void loadPersonalInfo() {
        nameLabel.setText(currentUser.getFullName());
        positionLabel.setText(currentUser.getPosition() != null ? currentUser.getPosition() : "N/A");
        statusLabel.setText(currentUser.getStatus() != null ? currentUser.getStatus() : "N/A");
        salaryLabel.setText(String.format("₱%,.2f", currentUser.getBasicSalary()));
        phoneLabel.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "N/A");
        addressLabel.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "N/A");
        sssLabel.setText(currentUser.getSssNumber() != null ? currentUser.getSssNumber() : "N/A");
        philhealthLabel.setText(currentUser.getPhilhealthNumber() != null ? currentUser.getPhilhealthNumber() : "N/A");
        tinLabel.setText(currentUser.getTinNumber() != null ? currentUser.getTinNumber() : "N/A");
        pagibigLabel.setText(currentUser.getPagibigNumber() != null ? currentUser.getPagibigNumber() : "N/A");
        
        if (currentUser.getBirthday() != null) {
            birthdayLabel.setText(currentUser.getBirthday().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            ageLabel.setText(String.valueOf(currentUser.getAge()));
        } else {
            birthdayLabel.setText("N/A");
            ageLabel.setText("N/A");
        }
    }

    private void loadAttendanceData() {
        attendanceTableModel.setRowCount(0);

        try {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());

            double totalHours = 0;
            int totalDays = 0;
            int lateCount = 0;

            for (Attendance att : attendanceList) {
                if (att.getLogIn() != null) {
                    totalDays++;
                    double workHours = att.getWorkHours();
                    totalHours += workHours;

                    if (att.isLate()) {
                        lateCount++;
                    }

                    String status = determineAttendanceStatus(att);

                    Object[] row = {
                            att.getDate(),
                            att.getLogIn() != null ? att.getLogIn().toString() : "N/A",
                            att.getLogOut() != null ? att.getLogOut().toString() : "N/A",
                            String.format("%.2f hrs", workHours),
                            status,
                            att.isLate() ? String.format("%.0f", att.getLateMinutes()) : "0",
                            att.hasUndertime() ? String.format("%.0f", att.getUndertimeMinutes()) : "0"
                    };
                    attendanceTableModel.addRow(row);
                }
            }

            // Update summary labels
            totalDaysLabel.setText(String.valueOf(totalDays));
            if (totalDays > 0) {
                double avgHours = totalHours / totalDays;
                averageHoursLabel.setText(String.format("%.2f", avgHours));
            } else {
                averageHoursLabel.setText("0.00");
            }
            lateCountLabel.setText(String.valueOf(lateCount));

        } catch (Exception e) {
            showErrorMessage("Error loading attendance data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String determineAttendanceStatus(Attendance att) {
        if (att.getLogIn() == null) return "No Log In";
        if (att.getLogOut() == null) return "No Log Out";

        boolean isLate = att.isLate();
        boolean hasUndertime = att.hasUndertime();

        if (isLate && hasUndertime) {
            return "Late & Undertime";
        } else if (isLate) {
            return "Late";
        } else if (hasUndertime) {
            return "Undertime";
        } else if (att.isFullDay()) {
            return "Full Day";
        } else {
            return "Present";
        }
    }

    private void loadPayrollData() {
        payrollTableModel.setRowCount(0);

        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            // Try to get existing payroll first
            List<Payroll> existingPayrolls = payrollDAO.getPayrollByEmployeeIdAndDateRange(
                    currentUser.getEmployeeId(), periodStart, periodEnd);

            if (!existingPayrolls.isEmpty()) {
                for (Payroll payroll : existingPayrolls) {
                    addPayrollRow(payroll, "Calculated");
                }
            } else {
                // Show placeholder row
                Object[] row = {
                        periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        "Not calculated",
                        "Click Calculate",
                        "Click Calculate",
                        "Click Calculate",
                        "Click Calculate",
                        "Pending"
                };
                payrollTableModel.addRow(row);
            }

        } catch (Exception e) {
            showErrorMessage("Error loading payroll data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPayrollRow(Payroll payroll, String status) {
        double totalAllowances = payroll.getRiceSubsidy() + payroll.getPhoneAllowance() + payroll.getClothingAllowance();

        Object[] row = {
                payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                payroll.getDaysWorked(),
                String.format("₱%,.2f", payroll.getGrossEarnings()),
                String.format("₱%,.2f", totalAllowances),
                String.format("₱%,.2f", payroll.getTotalDeductions()),
                String.format("₱%,.2f", payroll.getNetPay()),
                status
        };
        payrollTableModel.addRow(row);
    }

    private void loadLeaveData() {
        leaveTableModel.setRowCount(0);
        // Note: Leave data loading would require LeaveRequestDAO
        // For now, show placeholder
        Object[] row = {"N/A", "No leave requests", "N/A", "N/A", "N/A", "N/A", "N/A"};
        leaveTableModel.addRow(row);
    }

    private void calculateCurrentPayroll() {
        try {
            calculatePayrollButton.setEnabled(false);
            calculatePayrollButton.setText("Calculating...");

            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            Payroll payroll = payrollCalculator.calculatePayroll(currentUser.getEmployeeId(), periodStart, periodEnd);

            // Refresh the table
            loadPayrollData();

            showSuccessMessage("Payroll calculated successfully for " + 
                    periodStart.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        } catch (Exception e) {
            showErrorMessage("Error calculating payroll: " + e.getMessage());
            e.printStackTrace();
        } finally {
            calculatePayrollButton.setEnabled(true);
            calculatePayrollButton.setText("💰 Calculate Payroll");
        }
    }

    private void viewSelectedPayslip() {
        int selectedRow = payrollTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Please select a payroll record to view.");
            return;
        }

        try {
            String period = (String) payrollTableModel.getValueAt(selectedRow, 0);
            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, currentUser, period);
            dialog.setVisible(true);

        } catch (Exception e) {
            showErrorMessage("Error opening payslip: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLeaveRequestDialog() {
        try {
            LeaveRequestDialog dialog = new LeaveRequestDialog(this, currentUser);
            dialog.setVisible(true);
            loadLeaveData(); // Refresh after potential submission
        } catch (Exception e) {
            showErrorMessage("Error opening leave request dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showProfileDialog() {
        JOptionPane.showMessageDialog(this,
                "Profile management feature coming soon!",
                "Feature Coming Soon",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }

    // Utility methods for showing messages
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create test user
            Employee testUser = new Employee();
            testUser.setEmployeeId(10001);
            testUser.setFirstName("John");
            testUser.setLastName("Doe");
            testUser.setPosition("Software Developer");
            testUser.setStatus("Regular");
            testUser.setBasicSalary(50000.0);
            testUser.setRiceSubsidy(1500.0);
            testUser.setPhoneAllowance(1000.0);
            testUser.setClothingAllowance(800.0);

            new EnhancedEmployeeDashboard(testUser).setVisible(true);
        });
    }
}