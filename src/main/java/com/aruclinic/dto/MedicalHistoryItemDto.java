package com.aruclinic.dto;

public class MedicalHistoryItemDto {
    private String date;
    private String title;
    private String doctor;
    private String details;
    private String type; // primary, success, warning, danger

    public MedicalHistoryItemDto() {}

    public MedicalHistoryItemDto(String date, String title, String doctor, String details, String type) {
        this.date = date;
        this.title = title;
        this.doctor = doctor;
        this.details = details;
        this.type = type;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
