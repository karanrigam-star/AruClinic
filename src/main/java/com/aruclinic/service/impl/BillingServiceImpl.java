package com.aruclinic.service.impl;

import com.aruclinic.dto.BillDto;
import com.aruclinic.entity.Bill;
import com.aruclinic.exception.AruClinicException;
import com.aruclinic.repository.BillRepository;
import com.aruclinic.service.BillingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BillingService.
 */
@Service
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;

    public BillingServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public BillDto createBill(BillDto billDto) {
        Bill bill = new Bill();
        bill.setInvoiceNumber(billDto.getInvoiceId());
        bill.setInvoiceDate(billDto.getInvoiceDate());
        bill.setDueDate(billDto.getDueDate());
        bill.setAmount(billDto.getAmount());
        bill.setTax(billDto.getTax());
        bill.setTotal(billDto.getTotal());
        bill.setStatus(billDto.getStatus());
        bill.setDescription(billDto.getDescription());
        bill.setPaymentMethod(billDto.getPaymentMethod());

        Bill savedBill = billRepository.save(bill);

        BillDto result = new BillDto();
        result.setId(savedBill.getId());
        result.setInvoiceId(savedBill.getInvoiceNumber());
        result.setInvoiceDate(savedBill.getInvoiceDate());
        result.setDueDate(savedBill.getDueDate());
        result.setAmount(savedBill.getAmount());
        result.setTax(savedBill.getTax());
        result.setTotal(savedBill.getTotal());
        result.setStatus(savedBill.getStatus());
        result.setDescription(savedBill.getDescription());
        result.setPaymentMethod(savedBill.getPaymentMethod());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public BillDto getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Bill not found with id: " + id));

        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setInvoiceId(bill.getInvoiceNumber());
        dto.setInvoiceDate(bill.getInvoiceDate());
        dto.setDueDate(bill.getDueDate());
        dto.setAmount(bill.getAmount());
        dto.setTax(bill.getTax());
        dto.setTotal(bill.getTotal());
        dto.setStatus(bill.getStatus());
        dto.setDescription(bill.getDescription());
        dto.setPaymentMethod(bill.getPaymentMethod());

        return dto;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public BillDto updateBill(Long id, BillDto billDto) {
        Bill existingBill = billRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Bill not found with id: " + id));

        if (billDto.getInvoiceId() != null) {
            existingBill.setInvoiceNumber(billDto.getInvoiceId());
        }
        if (billDto.getInvoiceDate() != null) {
            existingBill.setInvoiceDate(billDto.getInvoiceDate());
        }
        if (billDto.getDueDate() != null) {
            existingBill.setDueDate(billDto.getDueDate());
        }
        if (billDto.getAmount() != null) {
            existingBill.setAmount(billDto.getAmount());
        }
        if (billDto.getTax() != null) {
            existingBill.setTax(billDto.getTax());
        }
        if (billDto.getTotal() != null) {
            existingBill.setTotal(billDto.getTotal());
        }
        if (billDto.getStatus() != null) {
            existingBill.setStatus(billDto.getStatus());
        }
        if (billDto.getDescription() != null) {
            existingBill.setDescription(billDto.getDescription());
        }
        if (billDto.getPaymentMethod() != null) {
            existingBill.setPaymentMethod(billDto.getPaymentMethod());
        }

        Bill savedBill = billRepository.save(existingBill);

        BillDto result = new BillDto();
        result.setId(savedBill.getId());
        result.setInvoiceId(savedBill.getInvoiceNumber());
        result.setInvoiceDate(savedBill.getInvoiceDate());
        result.setDueDate(savedBill.getDueDate());
        result.setAmount(savedBill.getAmount());
        result.setTax(savedBill.getTax());
        result.setTotal(savedBill.getTotal());
        result.setStatus(savedBill.getStatus());
        result.setDescription(savedBill.getDescription());
        result.setPaymentMethod(savedBill.getPaymentMethod());

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public void deleteBill(Long id) {
        billRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public List<BillDto> getAllBills() {
        return billRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<BillDto> getBillsByPatientId(Long patientId) {
        return billRepository.findByPatientId(patientId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<BillDto> getUnpaidBillsByPatientId(Long patientId) {
        return billRepository.findByPatientIdAndStatus(patientId, "PENDING").stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<BillDto> getPaidBillsByPatientId(Long patientId) {
        return billRepository.findByPatientIdAndStatus(patientId, "PAID").stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public List<BillDto> getBillsByStatus(String status) {
        return billRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public BillDto markBillAsPaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new AruClinicException("Bill not found with id: " + billId));
        bill.setStatus("PAID");
        Bill savedBill = billRepository.save(bill);
        return convertToDto(savedBill);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public double getTotalRevenue() {
        return billRepository.findAll().stream()
                .filter(bill -> "PAID".equals(bill.getStatus()))
                .mapToDouble(bill -> bill.getTotal().doubleValue())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public double getPendingPayments() {
        return billRepository.findByStatus("PENDING").stream()
                .mapToDouble(bill -> bill.getTotal().doubleValue())
                .sum();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public void processPayment(String invoiceId, BigDecimal amount) {
        Bill bill = billRepository.findByInvoiceNumber(invoiceId)
                .orElseThrow(() -> new AruClinicException("Invoice not found: " + invoiceId));

        if (!"PENDING".equals(bill.getStatus())) {
            throw new AruClinicException("Invoice is not in PENDING status");
        }

        if (bill.getTotal().compareTo(amount) != 0) {
            throw new AruClinicException("Payment amount does not match invoice total");
        }

        bill.setStatus("PAID");
        billRepository.save(bill);
    }

    private BillDto convertToDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setInvoiceId(bill.getInvoiceNumber());
        dto.setInvoiceDate(bill.getInvoiceDate());
        dto.setDueDate(bill.getDueDate());
        dto.setAmount(bill.getAmount());
        dto.setTax(bill.getTax());
        dto.setTotal(bill.getTotal());
        dto.setStatus(bill.getStatus());
        dto.setDescription(bill.getDescription());
        dto.setPaymentMethod(bill.getPaymentMethod());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<Bill> getBillEntitiesByPatientId(Long patientId) {
        return billRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public Bill getBillEntityById(Long id) {
        return billRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public List<Bill> getAllBillEntities() {
        return billRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RECEPTIONIST')")
    public List<Bill> getBillEntitiesByDoctorId(Long doctorId) {
        return billRepository.findByDoctorId(doctorId);
    }
}
