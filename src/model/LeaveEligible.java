package model;

/**
 * Interface for entities eligible for leave
 * Demonstrates ABSTRACTION through interface definition
 */
public interface LeaveEligible {
    boolean isEligibleForLeave(String leaveType);
    int getMaxLeavesDays(String leaveType);
    double getLeaveAllowance();
    boolean requiresApproval(String leaveType);
}