package com.aruclinic.mapper;

import com.aruclinic.dto.BillDto;
import com.aruclinic.entity.Bill;
import org.mapstruct.Mapper;

/**
 * Mapper for Bill entity and BillDto.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BillingMapper {

    default Bill toBill(BillDto dto) {
        if (dto == null) {
            return null;
        }
        Bill bill = new Bill();
        bill.setId(dto.getId());
        bill.setInvoiceDate(dto.getInvoiceDate());
        bill.setDueDate(dto.getDueDate());
        bill.setAmount(dto.getAmount());
        bill.setTax(dto.getTax());
        bill.setDiscount(dto.getDiscount());
        bill.setTotal(dto.getTotal());
        bill.setStatus(dto.getStatus());
        bill.setPaymentMethod(dto.getPaymentMethod());
        bill.setDescription(dto.getDescription());
        return bill;
    }

    default BillDto toBillDto(Bill entity) {
        if (entity == null) {
            return null;
        }
        BillDto dto = new BillDto();
        dto.setId(entity.getId());
        dto.setInvoiceDate(entity.getInvoiceDate());
        dto.setDueDate(entity.getDueDate());
        dto.setAmount(entity.getAmount());
        dto.setTax(entity.getTax());
        dto.setDiscount(entity.getDiscount());
        dto.setTotal(entity.getTotal());
        dto.setStatus(entity.getStatus());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
