package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Abstract base class for all people in the system
 * Demonstrates ABSTRACTION - defines common behavior for all person types
 * FIXED: All abstract methods properly defined
 */
public abstract class Person {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected LocalDate birthDate;
    protected String address;
    protected String phoneNumber;
    protected String email;
    
    // Abstract methods that must be implemented by subclasses (ABSTRACTION)
    public abstract String getDisplayName();
    public abstract String getRole();
    public abstract boolean isActive();
    public abstract PersonType getPersonType();
    
    // Enum for person types (supports POLYMORPHISM)
    public enum PersonType {
        EMPLOYEE("Employee"),
        MANAGER("Manager"), 
        HR_PERSONNEL("HR Personnel"),
        CONTRACTOR("Contractor");
        
        private final String displayName;
        
        PersonType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    // Constructor
    public Person() {}
    
    public Person(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }
    
    // Template method pattern (demonstrates ABSTRACTION)
    public final String getFormattedInfo() {
        return String.format("%s: %s (%s)", 
            getRole(), getDisplayName(), isActive() ? "Active" : "Inactive");
    }
    
    // Common behavior for all people
    public String getFullName() {
        if (firstName == null || lastName == null) return "Unknown";
        return firstName.trim() + " " + lastName.trim();
    }
    
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    // Validation method (can be overridden)
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty();
    }
    
    // Getters and setters with validation
    public int getId() { return id; }
    public void setId(int id) { 
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative");
        this.id = id; 
    }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim(); 
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim(); 
    }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { 
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        this.birthDate = birthDate; 
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return id == person.id && 
               Objects.equals(firstName, person.firstName) && 
               Objects.equals(lastName, person.lastName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }
    
    @Override
    public String toString() {
        return getFormattedInfo();
    }
}