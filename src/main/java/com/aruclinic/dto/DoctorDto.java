package com.aruclinic.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object for Doctor entity.
 */
public class DoctorDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String qualification;
    private String specialization;
    private String department;
    private String mobileNumber;
    private String email;
    private Integer experience;
    private String about;
    private LocalTime workingHoursStart;
    private LocalTime workingHoursEnd;
    private String status = "AVAILABLE";

    // Constructors
    public DoctorDto() {}

    public DoctorDto(Long id, String firstName, String lastName, String qualification,
                     String specialization, String department, String mobileNumber,
                     String email, String about, LocalTime workingHoursStart,
                     LocalTime workingHoursEnd) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.qualification = qualification;
        this.specialization = specialization;
        this.department = department;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.about = about;
        this.workingHoursStart = workingHoursStart;
        this.workingHoursEnd = workingHoursEnd;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public LocalTime getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(LocalTime workingHoursStart) { this.workingHoursStart = workingHoursStart; }

    public LocalTime getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(LocalTime workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}