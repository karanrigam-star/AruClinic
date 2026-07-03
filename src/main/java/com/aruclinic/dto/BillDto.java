package com.aruclinic.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for Bill entity.
 */
public class BillDto {

    private Long id;
    private String invoiceNumber;
    private Long patientId;
    private Long doctorId;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal total;
    private LocalDate invoiceDate;

    private List<BillItemDto> items;

    private String patientName;
    private String doctorName;

    private Long createdBy;
    private String status;
    private String description;
    private LocalDate dueDate;

    private BigDecimal discount;
    private BigDecimal subtotal;

    private Long createdByUserId;
    private String paymentMethod;

    public BillDto() {}

    public BillDto(Long id, String invoiceNumber, Long patientId, Long doctorId,
                   BigDecimal amount, BigDecimal tax, BigDecimal total, LocalDate invoiceDate,
                   List<BillItemDto> items, String patientName, String doctorName) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.amount = amount;
        this.tax = tax;
        this.total = total;
        this.invoiceDate = invoiceDate;
        this.items = items;
        this.patientName = patientName;
        this.doctorName = doctorName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    // Alias for invoiceNumber (used by BillingView)
    public String getInvoiceId() { return invoiceNumber; }
    public void setInvoiceId(String invoiceId) { this.invoiceNumber = invoiceId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    // Override for String patient ID (used by InvoiceView)
    public void setPatientId(String patientId) {
        this.patientId = patientId != null ? Long.parseLong(patientId.replace("PAT-", "")) : null;
    }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public List<BillItemDto> getItems() { return items; }
    public void setItems(List<BillItemDto> items) { this.items = items; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}