/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.util.List;

public interface TeamLeader {
    void addTeamMember(Employee employee);
    void removeTeamMember(Employee employee);
    List<Employee> getTeamMembers();
    int getTeamSize();
    double calculateTeamPerformance();
}