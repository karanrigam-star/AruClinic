package com.aruclinic.service;

import com.aruclinic.dto.BillDto;
import java.util.List;

/**
 * Service interface for billing management operations.
 */
public interface BillingService {

    BillDto createBill(BillDto billDto);

    BillDto getBillById(Long id);

    BillDto updateBill(Long id, BillDto billDto);

    void deleteBill(Long id);

    List<BillDto> getAllBills();

    List<BillDto> getBillsByPatientId(Long patientId);

    List<BillDto> getUnpaidBillsByPatientId(Long patientId);

    List<BillDto> getPaidBillsByPatientId(Long patientId);

    List<BillDto> getBillsByStatus(String status);

    BillDto markBillAsPaid(Long billId);

    double getTotalRevenue();

    double getPendingPayments();

    void processPayment(String invoiceId, java.math.BigDecimal amount);

    List<com.aruclinic.entity.Bill> getBillEntitiesByPatientId(Long patientId);

    com.aruclinic.entity.Bill getBillEntityById(Long id);

    List<com.aruclinic.entity.Bill> getAllBillEntities();

    List<com.aruclinic.entity.Bill> getBillEntitiesByDoctorId(Long doctorId);
}
