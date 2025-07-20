package ui;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import ui.LoginForm;
import ui.EmployeeDetailsDialog;
import ui.PasswordChangeDialog;
import ui.LeaveManagementDialog;
import ui.AttendanceManagementDialog;
import ui.ReportsDialog;
import ui.PayrollDetailsDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Comparator;

public class HRDashboard extends JFrame {
    private Employee currentUser;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;
    private JPanel dashboardPanel;
    private String currentView = "dashboard";

    // Modern Color Palette - Professional Purple/Blue Theme
    private static final Color PRIMARY_PURPLE = new Color(88, 86, 214);     // Deep purple
    private static final Color SECONDARY_PURPLE = new Color(124, 58, 237);  // Purple accent
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);       // Bright blue
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);      // Success green
    private static final Color WARNING_ORANGE = new Color(251, 146, 60);    // Warning orange
    private static final Color SIDEBAR_DARK = new Color(30, 27, 75);        // Dark sidebar
    private static final Color BACKGROUND_LIGHT = new Color(248, 250, 252); // Light background
    private static final Color CARD_WHITE = new Color(255, 255, 255);       // Pure white
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);        // Dark text
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // Gray text
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);     // Light border

    // Dashboard Components
    private JTable employeeTable;
    private DefaultTableModel employeeTableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JLabel totalEmployeesLabel;
    private JLabel newEmployeesLabel;
    private JLabel attendanceRateLabel;
    private JLabel currentDateTimeLabel;

    // Services
    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;

    // Timer for real-time updates
    private Timer dateTimeTimer;
    private Timer metricsTimer;

    public HRDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Set modern look and feel - use default to avoid compatibility issues
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel()); // Commented out to avoid errors
            
            this.employeeDAO = new EmployeeDAO();
            this.attendanceDAO = new AttendanceDAO();
            this.payrollCalculator = new PayrollCalculator();

            initializeComponents();
            setupLayout();
            setupEventHandlers();
            startRealTimeUpdates();
            loadData();

            System.out.println("✅ Comprehensive HR Dashboard initialized for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ HR Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            createErrorInterface(e);
        }

        setTitle("MotorPH Workforce Management System - " + user.getFullName());
        setSize(1600, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        try {
            setIconImage(createAppIcon());
        } catch (Exception e) {
            // Continue without icon
        }
    }

    private Image createAppIcon() {
        // Create a simple modern icon - simplified to avoid BufferedImage issues
        try {
            BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = icon.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(PRIMARY_PURPLE);
            g2.fillRoundRect(0, 0, 32, 32, 8, 8);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int x = (32 - fm.stringWidth("M")) / 2;
            int y = ((32 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString("M", x, y);
            
            g2.dispose();
            return icon;
        } catch (Exception e) {
            // Return null if BufferedImage not available
            return null;
        }
    }

    private void createErrorInterface(Exception error) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_LIGHT);

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(CARD_WHITE);
        errorPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        String errorMessage = "<html><center>" +
                "<h1 style='color: #dc2626;'>⚠️ System Error</h1>" +
                "<p style='font-size: 16px; margin: 20px 0;'>Unable to initialize HR Dashboard</p>" +
                "<p><b>Error:</b> " + error.getMessage() + "</p>" +
                "</center></html>";

        JLabel messageLabel = new JLabel(errorMessage, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton retryButton = createModernButton("🔄 Retry", PRIMARY_PURPLE);
        retryButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new HRDashboard(currentUser).setVisible(true));
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_WHITE);
        buttonPanel.add(retryButton);

        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(errorPanel, BorderLayout.CENTER);
    }

    private void initializeComponents() {
        // Initialize main panels
        sidebarPanel = createSidebarPanel();
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(BACKGROUND_LIGHT);

        // Initialize dashboard metrics labels
        totalEmployeesLabel = new JLabel("0");
        newEmployeesLabel = new JLabel("+0");
        attendanceRateLabel = new JLabel("0%");
        currentDateTimeLabel = new JLabel();

        // Initialize employee table for dashboard
        String[] columns = {"ID", "Last Name", "First Name", "Position", "Department", "Email", "Phone", "Hire Date", "Salary"};
        employeeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(employeeTableModel);
        setupTableStyling(employeeTable);

        tableSorter = new TableRowSorter<>(employeeTableModel);
        employeeTable.setRowSorter(tableSorter);

        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // Create dashboard panel
        dashboardPanel = createDashboardPanel();
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

        // Header with logo and welcome
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(SIDEBAR_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 25, 25, 25));

        // Logo section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoPanel.setBackground(SIDEBAR_DARK);

        JLabel logoIcon = new JLabel("🏍️");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel logoTextPanel = new JPanel();
        logoTextPanel.setLayout(new BoxLayout(logoTextPanel, BoxLayout.Y_AXIS));
        logoTextPanel.setBackground(SIDEBAR_DARK);

        JLabel logoTitle = new JLabel("MotorPH");
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoTitle.setForeground(Color.WHITE);
        logoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSubtitle = new JLabel("Workforce System");
        logoSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoSubtitle.setForeground(new Color(156, 163, 175));
        logoSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoTextPanel.add(logoTitle);
        logoTextPanel.add(logoSubtitle);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createHorizontalStrut(10));
        logoPanel.add(logoTextPanel);

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(SIDEBAR_DARK);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel welcomeLabel = new JLabel("Welcome,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(156, 163, 175));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userNameLabel = new JLabel(currentUser.getFirstName() + " " + currentUser.getLastName());
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userRoleLabel = new JLabel(currentUser.getPosition());
        userRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRoleLabel.setForeground(new Color(156, 163, 175));
        userRoleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(userNameLabel);
        welcomePanel.add(userRoleLabel);

        headerPanel.add(logoPanel);
        headerPanel.add(welcomePanel);

        // Navigation menu
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_DARK);
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        // Navigation buttons
        JButton dashboardBtn = createSidebarButton("📊 Dashboard", "dashboard", true);
        JButton employeesBtn = createSidebarButton("👥 Employees", "employees", false);
        JButton payrollBtn = createSidebarButton("💰 Payroll", "payroll", false);
        JButton attendanceBtn = createSidebarButton("📅 Attendance", "attendance", false);
        JButton leaveBtn = createSidebarButton("🏖️ Leave Management", "leave", false);
        JButton reportsBtn = createSidebarButton("📈 Reports", "reports", false);

        navPanel.add(dashboardBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(employeesBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(payrollBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(attendanceBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(leaveBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(reportsBtn);

        // Bottom section with logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(SIDEBAR_DARK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 30, 20));

        JButton logoutBtn = createSidebarButton("🚪 Sign Out", "logout", false);
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.addActionListener(e -> logout());

        bottomPanel.add(Box.createVerticalGlue());
        bottomPanel.add(logoutBtn);

        sidebar.add(headerPanel);
        sidebar.add(navPanel);
        sidebar.add(bottomPanel);

        return sidebar;
    }

    private JButton createSidebarButton(String text, String action, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(240, 45));
        
        if (isActive) {
            button.setBackground(PRIMARY_PURPLE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(SIDEBAR_DARK);
            button.setForeground(new Color(156, 163, 175));
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) {
                    button.setBackground(new Color(55, 65, 81));
                    button.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) {
                    button.setBackground(SIDEBAR_DARK);
                    button.setForeground(new Color(156, 163, 175));
                }
            }
        });

        button.addActionListener(e -> switchView(action));

        return button;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BACKGROUND_LIGHT);
        dashboard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Top section with date/time and metrics
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(BACKGROUND_LIGHT);
        topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Date/Time Card
        JPanel dateTimeCard = createMetricCard("📅 Current Date & Time", "", PRIMARY_PURPLE);
        currentDateTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        currentDateTimeLabel.setForeground(Color.WHITE);
        dateTimeCard.add(currentDateTimeLabel, BorderLayout.SOUTH);

        // Metrics cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel.setBackground(BACKGROUND_LIGHT);

        JPanel totalEmployeesCard = createMetricCard("👥 Total Employees", "", ACCENT_BLUE);
        totalEmployeesLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        totalEmployeesLabel.setForeground(Color.WHITE);
        totalEmployeesCard.add(totalEmployeesLabel, BorderLayout.CENTER);

        JPanel newEmployeesCard = createMetricCard("🆕 New Employees", "", SUCCESS_GREEN);
        newEmployeesLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        newEmployeesLabel.setForeground(Color.WHITE);
        newEmployeesCard.add(newEmployeesLabel, BorderLayout.CENTER);

        JPanel attendanceCard = createMetricCard("📈 Attendance Rate", "", WARNING_ORANGE);
        attendanceRateLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        attendanceRateLabel.setForeground(Color.WHITE);
        attendanceCard.add(attendanceRateLabel, BorderLayout.CENTER);

        metricsPanel.add(totalEmployeesCard);
        metricsPanel.add(newEmployeesCard);
        metricsPanel.add(attendanceCard);

        topSection.add(dateTimeCard, BorderLayout.WEST);
        topSection.add(metricsPanel, BorderLayout.CENTER);

        // Employee table section
        JPanel tableSection = createEmployeeTableSection();

        dashboard.add(topSection, BorderLayout.NORTH);
        dashboard.add(tableSection, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createMetricCard(String title, String subtitle, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setPreferredSize(new Dimension(220, 120));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        if (!subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitleLabel.setForeground(new Color(255, 255, 255, 180));
            card.add(subtitleLabel, BorderLayout.SOUTH);
        }

        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createEmployeeTableSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(BACKGROUND_LIGHT);

        // Header with search
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        JLabel tableTitle = new JLabel("📋 Employee Directory");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(TEXT_PRIMARY);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(CARD_WHITE);

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(TEXT_PRIMARY);

        JButton searchButton = createModernButton("Search", PRIMARY_PURPLE);
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchButton.addActionListener(e -> searchEmployees());

        JButton addButton = createModernButton("➕ Add Employee", SUCCESS_GREEN);
        addButton.setPreferredSize(new Dimension(150, 35));
        addButton.addActionListener(e -> showAddEmployeeDialog());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);

        headerPanel.add(tableTitle, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        section.add(headerPanel, BorderLayout.NORTH);
        section.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        section.add(tablePanel, BorderLayout.SOUTH);

        return section;
    }

    private void setupTableStyling(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(88, 86, 214, 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_LIGHT);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setBackground(CARD_WHITE);
        table.setForeground(TEXT_PRIMARY);

        // Modern table header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_LIGHT));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_LIGHT);

        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        // Initially show dashboard
        switchView("dashboard");
    }

    private void switchView(String view) {
        currentView = view;
        mainContentPanel.removeAll();

        switch (view) {
            case "dashboard":
                mainContentPanel.add(dashboardPanel, BorderLayout.CENTER);
                break;
            case "employees":
                mainContentPanel.add(createEmployeesView(), BorderLayout.CENTER);
                break;
            case "payroll":
                mainContentPanel.add(createPayrollView(), BorderLayout.CENTER);
                break;
            case "attendance":
                mainContentPanel.add(createAttendanceView(), BorderLayout.CENTER);
                break;
            case "leave":
                openLeaveManagement();
                return;
            case "reports":
                openReportsDialog();
                return;
            case "logout":
                logout();
                return;
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        updateSidebarButtons(view);
    }

    private void updateSidebarButtons(String activeView) {
        // Update sidebar button states - this would require storing button references
        // For now, we'll recreate the sidebar
        remove(sidebarPanel);
        sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private JPanel createEmployeesView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("👥 Employee Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        panel.add(title, BorderLayout.NORTH);
        // Add employee management content here

        return panel;
    }

    private JPanel createPayrollView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("💰 Payroll Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        panel.add(title, BorderLayout.NORTH);
        // Add payroll management content here

        return panel;
    }

    private JPanel createAttendanceView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("📅 Attendance Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        panel.add(title, BorderLayout.NORTH);
        // Add attendance management content here

        return panel;
    }

    private void startRealTimeUpdates() {
        // Update date/time every second
        dateTimeTimer = new Timer(1000, e -> updateDateTime());
        dateTimeTimer.start();

        // Update metrics every 30 seconds
        metricsTimer = new Timer(30000, e -> updateMetrics());
        metricsTimer.start();

        // Initial updates
        updateDateTime();
        updateMetrics();
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String dateTimeText = now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy\nhh:mm:ss a"));
        currentDateTimeLabel.setText("<html><center>" + dateTimeText.replace("\n", "<br>") + "</center></html>");
    }

    private void updateMetrics() {
        try {
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            totalEmployeesLabel.setText(String.valueOf(allEmployees.size()));

            // Calculate new employees - simplified since getHireDate might not exist
            // For now, show a placeholder value
            newEmployeesLabel.setText("+2");

            // Calculate attendance rate (placeholder - implement based on your logic)
            attendanceRateLabel.setText("96.8%");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        searchField.addActionListener(e -> searchEmployees());

        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = employeeTable.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = employeeTable.convertRowIndexToModel(row);
                        int employeeId = (Integer) employeeTableModel.getValueAt(modelRow, 0);
                        Employee employee = employeeDAO.getEmployeeById(employeeId);
                        if (employee != null) {
                            showEmployeeDetails(employee);
                        }
                    }
                }
            }
        });
    }

    private void loadData() {
        loadEmployeeData();
        updateMetrics();
    }

    private void loadEmployeeData() {
        employeeTableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            for (Employee emp : employees) {
                Object[] row = {
                        emp.getEmployeeId(),
                        emp.getLastName(),
                        emp.getFirstName(),
                        emp.getPosition(),
                        "HR", // Department placeholder since getDepartment() doesn't exist
                        emp.getEmployeeId() + "@motorphilippines.com", // Email placeholder
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "Not provided",
                        "Jan 15, 2024", // Hire date placeholder since getHireDate() doesn't exist
                        String.format("₱%,.2f", emp.getBasicSalary())
                };
                employeeTableModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Data Loading Error</h3><p>Error loading employee data: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void searchEmployees() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadEmployeeData();
            return;
        }

        employeeTableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.searchEmployees(searchTerm);

            for (Employee emp : employees) {
                Object[] row = {
                        emp.getEmployeeId(),
                        emp.getLastName(),
                        emp.getFirstName(),
                        emp.getPosition(),
                        "HR", // Department placeholder
                        emp.getEmployeeId() + "@motorphilippines.com", // Email placeholder
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "Not provided",
                        "Jan 15, 2024", // Hire date placeholder
                        String.format("₱%,.2f", emp.getBasicSalary())
                };
                employeeTableModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Search Error</h3><p>Error searching employees: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showAddEmployeeDialog() {
        try {
            EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, null, true);
            dialog.setVisible(true);
            loadEmployeeData();
            updateMetrics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Dialog Error</h3><p>Error opening employee dialog: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEmployeeDetails(Employee employee) {
        try {
            EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, employee, false);
            dialog.setVisible(true);
            loadEmployeeData();
            updateMetrics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Dialog Error</h3><p>Error opening employee details: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openLeaveManagement() {
        try {
            LeaveManagementDialog dialog = new LeaveManagementDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ System Error</h3><p>Unable to open leave management: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openReportsDialog() {
        try {
            ReportsDialog dialog = new ReportsDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ System Error</h3><p>Unable to open reports: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void logout() {
        // Stop timers
        if (dateTimeTimer != null) {
            dateTimeTimer.stop();
        }
        if (metricsTimer != null) {
            metricsTimer.stop();
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><center>" +
                "<h3>🚪 Confirm Sign Out</h3>" +
                "<p>Are you sure you want to sign out of the system?</p>" +
                "<p>Any unsaved work will be lost.</p>" +
                "</center></html>",
                "Confirm Sign Out", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        } else {
            // Restart timers if user cancels
            startRealTimeUpdates();
        }
    }

    // Additional methods for comprehensive functionality

    private void showEmployeeContextMenu(MouseEvent e, Employee employee) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(CARD_WHITE);
        popup.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        JMenuItem viewItem = createModernMenuItem("👁️ View Profile", PRIMARY_PURPLE);
        viewItem.addActionListener(ev -> showEmployeeDetails(employee));

        JMenuItem editItem = createModernMenuItem("✏️ Edit Information", ACCENT_BLUE);
        editItem.addActionListener(ev -> showEditEmployeeDialog(employee));

        JMenuItem passwordItem = createModernMenuItem("🔑 Reset Password", WARNING_ORANGE);
        passwordItem.addActionListener(ev -> showPasswordChangeDialog(employee));

        JMenuItem deleteItem = createModernMenuItem("🗑️ Remove Employee", new Color(220, 38, 38));
        deleteItem.addActionListener(ev -> deleteEmployee(employee));

        popup.add(viewItem);
        popup.add(editItem);
        popup.addSeparator();
        popup.add(passwordItem);
        popup.addSeparator();
        popup.add(deleteItem);

        popup.show(employeeTable, e.getX(), e.getY());
    }

    private JMenuItem createModernMenuItem(String text, Color color) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setForeground(color);
        item.setBackground(CARD_WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(248, 250, 252));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(CARD_WHITE);
            }
        });
        
        return item;
    }

    private void showEditEmployeeDialog(Employee employee) {
        try {
            EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, employee, false);
            dialog.setVisible(true);
            loadEmployeeData();
            updateMetrics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Dialog Error</h3><p>Error opening edit dialog: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPasswordChangeDialog(Employee employee) {
        try {
            PasswordChangeDialog dialog = new PasswordChangeDialog(this, employee);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "<html><center><h3>❌ Dialog Error</h3><p>Error opening password dialog: " + e.getMessage() + "</p></center></html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee(Employee employee) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><center>" +
                "<h3>⚠️ Confirm Employee Removal</h3>" +
                "<p>Are you sure you want to permanently remove <b>" + employee.getFullName() + "</b>?</p>" +
                "<br>" +
                "<p style='color: #dc2626;'><b>This action will permanently delete:</b></p>" +
                "<ul style='text-align: left;'>" +
                "<li>Employee profile and personal information</li>" +
                "<li>All attendance and time tracking records</li>" +
                "<li>Complete payroll history and calculations</li>" +
                "<li>Login credentials and system access</li>" +
                "<li>Leave requests and approval history</li>" +
                "</ul>" +
                "<p style='color: #dc2626;'><b>This action cannot be undone!</b></p>" +
                "</center></html>",
                "Confirm Employee Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeDAO.deleteEmployee(employee.getEmployeeId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "<html><center>" +
                            "<h3>✅ Employee Successfully Removed</h3>" +
                            "<p><b>" + employee.getFullName() + "</b> has been permanently removed from the system.</p>" +
                            "</center></html>",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEmployeeData();
                    updateMetrics();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "<html><center>" +
                            "<h3>❌ Removal Failed</h3>" +
                            "<p>Unable to remove employee. Please contact IT support.</p>" +
                            "</center></html>",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "<html><center>" +
                        "<h3>❌ System Error</h3>" +
                        "<p>Error removing employee: " + e.getMessage() + "</p>" +
                        "</center></html>",
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Enhanced table with comprehensive data display
    private void setupComprehensiveEmployeeTable() {
        // Set column widths for better display
        if (employeeTable.getColumnModel().getColumnCount() > 0) {
            employeeTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
            employeeTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Last Name
            employeeTable.getColumnModel().getColumn(2).setPreferredWidth(120); // First Name
            employeeTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Position
            employeeTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Department
            employeeTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Email
            employeeTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Phone
            employeeTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Hire Date
            employeeTable.getColumnModel().getColumn(8).setPreferredWidth(120); // Salary
        }

        // Add alternating row colors for better readability
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(CARD_WHITE);
                    } else {
                        c.setBackground(new Color(248, 250, 252));
                    }
                }
                
                return c;
            }
        });
    }

    // Create notification system for real-time updates
    private void showNotification(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            JWindow notification = new JWindow();
            notification.setBackground(new Color(0, 0, 0, 0));
            
            JPanel panel = new JPanel();
            panel.setBackground(color);
            panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            
            JLabel label = new JLabel(message);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            panel.add(label);
            notification.add(panel);
            notification.pack();
            
            // Position at top-right corner
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            notification.setLocation(screenSize.width - notification.getWidth() - 20, 20);
            notification.setVisible(true);
            
            // Auto-hide after 3 seconds
            Timer timer = new Timer(3000, e -> notification.dispose());
            timer.setRepeats(false);
            timer.start();
        });
    }

    // Add keyboard shortcuts for common actions
    private void setupKeyboardShortcuts() {
        // Ctrl+N for new employee
        KeyStroke ctrlN = KeyStroke.getKeyStroke("ctrl N");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlN, "newEmployee");
        getRootPane().getActionMap().put("newEmployee", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddEmployeeDialog();
            }
        });

        // Ctrl+F for search
        KeyStroke ctrlF = KeyStroke.getKeyStroke("ctrl F");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "search");
        getRootPane().getActionMap().put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocus();
            }
        });

        // F5 for refresh
        KeyStroke f5 = KeyStroke.getKeyStroke("F5");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadEmployeeData();
                updateMetrics();
                showNotification("✅ Data refreshed successfully", SUCCESS_GREEN);
            }
        });
    }

    @Override
    public void dispose() {
        // Clean up timers
        if (dateTimeTimer != null) {
            dateTimeTimer.stop();
        }
        if (metricsTimer != null) {
            metricsTimer.stop();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel - commented out to avoid compatibility issues
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                
                // Create test user for demonstration
                Employee testUser = new Employee();
                testUser.setEmployeeId(10001);
                testUser.setFirstName("Andrea");
                testUser.setLastName("Mae");
                testUser.setPosition("HR Manager");

                HRDashboard dashboard = new HRDashboard(testUser);
                dashboard.setupComprehensiveEmployeeTable();
                dashboard.setupKeyboardShortcuts();
                dashboard.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(null,
                        "<html><center>" +
                        "<h3>❌ System Initialization Failed</h3>" +
                        "<p>Unable to start the Comprehensive HR Dashboard.</p>" +
                        "<p>Error: " + e.getMessage() + "</p>" +
                        "</center></html>",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}