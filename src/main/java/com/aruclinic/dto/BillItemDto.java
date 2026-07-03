package com.aruclinic.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for BillItem entity.
 */
public class BillItemDto {

    private Long id;
    private Long billId;
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private BigDecimal unitPrice;

    public BillItemDto() {}

    public BillItemDto(Long id, Long billId, String description, BigDecimal amount) {
        this.id = id;
        this.billId = billId;
        this.description = description;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}