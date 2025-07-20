package model;

/**
 * Interface for entities that can have their attendance tracked
 * Demonstrates ABSTRACTION through interface definition
 */
public interface AttendanceTrackable {
    boolean canTrackAttendance();
    boolean requiresTimeTracking();
    int getRequiredWorkingHoursPerDay();
    boolean isEligibleForOvertime();
}