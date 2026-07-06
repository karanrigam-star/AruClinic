package com.aruclinic.repository;

import com.aruclinic.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientId(Long patientId);
    List<Bill> findByPatientIdAndStatus(Long patientId, String status);
    List<Bill> findByStatus(String status);
    Optional<Bill> findByInvoiceNumber(String invoiceNumber);
    List<Bill> findByDoctorId(Long doctorId);

    List<Bill> findTop5ByOrderByInvoiceDateDesc();
}