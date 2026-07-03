package com.aruclinic.dto;

/**
 * Data Transfer Object for PrescriptionItem entity.
 */
public class PrescriptionItemDto {

    private Long id;
    private Long prescriptionId;
    private String medicineName;
    private String dosage;
    private Integer duration; // days

    public PrescriptionItemDto() {}

    public PrescriptionItemDto(Long id, Long prescriptionId, String medicineName, String dosage, Integer duration) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.duration = duration;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}