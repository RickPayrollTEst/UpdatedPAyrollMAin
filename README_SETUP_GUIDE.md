# MotorPH Payroll System - Complete Setup Guide

## 🚀 Step-by-Step Setup Instructions

### Prerequisites
1. **Java Development Kit (JDK) 8 or higher**
2. **MySQL Server 8.0+**
3. **MySQL Workbench** (recommended)
4. **NetBeans IDE** (or any Java IDE)

### Required JAR Files
Download and add these JAR files to your project's `lib` folder:

1. **mysql-connector-j-8.4.0.jar** - MySQL JDBC Driver
   - Download from: https://dev.mysql.com/downloads/connector/j/
   
2. **junit-jupiter-api-5.9.2.jar** - JUnit 5 for testing
   - Download from: https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api/5.9.2
   
3. **mockito-core-5.1.1.jar** - Mockito for mocking in tests
   - Download from: https://mvnrepository.com/artifact/org.mockito/mockito-core/5.1.1
   
4. **jasperreports-6.20.6.jar** - JasperReports for PDF generation
   - Download from: https://sourceforge.net/projects/jasperreports/files/jasperreports/
   
5. **commons-collections4-4.4.jar** - Apache Commons Collections
   - Download from: https://mvnrepository.com/artifact/org.apache.commons/commons-collections4/4.4
   
6. **commons-logging-1.2.jar** - Apache Commons Logging
   - Download from: https://mvnrepository.com/artifact/commons-logging/commons-logging/1.2

### Database Setup

#### Step 1: Create Database
1. Open MySQL Workbench
2. Connect to your MySQL server
3. Execute the main database script:
   ```sql
   -- Run: src/util/aoopdatabase_payroll.sql
   ```

#### Step 2: Apply 3NF Normalization
1. Execute the 3NF normalization script:
   ```sql
   -- Run: src/util/Database3NF.sql
   ```

#### Step 3: Create Views and Stored Procedures
1. Execute the views and procedures script:
   ```sql
   -- Run: src/util/DatabaseViews.sql
   ```

#### Step 4: Setup JasperReports
1. Execute the JasperReports setup script:
   ```sql
   -- Run: src/util/JasperReportsSetup.sql
   ```

### Project Configuration

#### Step 1: Add JAR Files to Project
1. In NetBeans, right-click your project
2. Go to Properties → Libraries
3. Click "Add JAR/Folder"
4. Add all the downloaded JAR files from the `lib` folder

#### Step 2: Configure Database Connection
1. Verify database settings in `src/util/DBConnection.java`:
   ```java
   private static final String DB_HOST = "localhost";
   private static final String DB_PORT = "3306";
   private static final String DB_NAME = "aoopdatabase_payroll";
   private static final String DB_USERNAME = "root";
   private static final String DB_PASSWORD = "admin";
   ```

#### Step 3: Test Database Connection
1. Run the test class:
   ```bash
   java -cp ".:lib/*" src/Test.java
   ```

### Running the Application

#### Step 1: Compile the Project
```bash
javac -cp ".:lib/*" src/**/*.java
```

#### Step 2: Run the Application
```bash
java -cp ".:lib/*:src" ui.MainApplication
```

#### Step 3: Login Credentials
Use any of these test accounts:
- **Employee ID**: 10001 to 10034
- **Password**: password1234

### Running Unit Tests

#### Step 1: Compile Tests
```bash
javac -cp ".:lib/*:src" src/Test/*.java
```

#### Step 2: Run JUnit Tests
```bash
java -cp ".:lib/*:src" org.junit.platform.console.ConsoleLauncher --scan-classpath
```

### Features Implemented

#### ✅ AOOP Principles Fixed
1. **Inheritance**: Employee → Manager, HRPersonnel, Contractor
2. **Abstraction**: Person (abstract), Allowance (abstract), Deduction (abstract)
3. **Polymorphism**: Payable interface, LeaveEligible interface
4. **Encapsulation**: Proper getters/setters with validation

#### ✅ Database Improvements
1. **3NF Normalization**: Separated departments, positions, allowances
2. **Views**: Employee summary, payroll summary, attendance summary
3. **Stored Procedures**: Payroll calculation, government contributions
4. **Indexes**: Optimized query performance

#### ✅ GUI Enhancements
1. **Enhanced Employee Dashboard**: Modern UI with better usability
2. **Fixed Functionality Bugs**: Proper error handling and validation
3. **Improved Navigation**: Tabbed interface with clear sections

#### ✅ JasperReports Integration
1. **PDF Generation**: Professional payslip generation
2. **MotorPH Template**: Company-branded reports
3. **Report Management**: Template system with logging

#### ✅ Proper Unit Testing
1. **JUnit 5**: Proper test framework implementation
2. **Assert Functions**: Comprehensive test assertions
3. **Test Coverage**: Model, DAO, Service, and UI tests
4. **Mockito Integration**: Proper mocking for isolated tests

### Troubleshooting

#### Common Issues

1. **ClassNotFoundException: MySQL Driver**
   - Ensure mysql-connector-j-8.4.0.jar is in classpath
   - Verify JAR file is not corrupted

2. **Database Connection Failed**
   - Check MySQL server is running
   - Verify credentials in DBConnection.java
   - Ensure database exists

3. **JUnit Tests Not Running**
   - Verify JUnit JAR files are in classpath
   - Check test class naming (must end with Test)
   - Ensure proper annotations (@Test, @BeforeEach, etc.)

4. **JasperReports PDF Generation Failed**
   - Verify jasperreports-6.20.6.jar is in classpath
   - Check commons-collections4 and commons-logging JARs
   - Ensure reports directory exists

#### Performance Optimization
1. Use connection pooling for better database performance
2. Enable query caching in MySQL
3. Regular database maintenance and optimization

### Project Structure
```
src/
├── dao/           # Data Access Objects
├── model/         # Entity classes with OOP principles
├── service/       # Business logic services
├── ui/            # User interface classes
├── util/          # Utility classes and database scripts
├── Test/          # JUnit test classes
└── reports/       # JasperReports templates
```

### Next Steps
1. Test all functionality thoroughly
2. Generate sample reports using JasperReports
3. Run comprehensive unit tests
4. Verify database normalization
5. Test GUI improvements

## 🎉 Success Criteria
- ✅ All JAR files properly added
- ✅ Database successfully created and normalized
- ✅ Application runs without errors
- ✅ Unit tests pass with proper assertions
- ✅ JasperReports generates PDF files
- ✅ GUI improvements implemented
- ✅ OOP principles properly demonstrated

For any issues, refer to the troubleshooting section or check the application logs.