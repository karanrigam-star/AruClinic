package com.aruclinic.mapper;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.entity.Prescription;
import org.mapstruct.Mapper;

/**
 * Mapper for Prescription entity and PrescriptionDto.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PrescriptionMapper {

    default Prescription toPrescription(PrescriptionDto dto) {
        if (dto == null) {
            return null;
        }
        Prescription prescription = new Prescription();
        prescription.setId(dto.getId());
        prescription.setPrescriptionDate(dto.getPrescriptionDate());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setAdvice(dto.getAdvice());
        prescription.setStatus(dto.getStatus());
        return prescription;
    }

    default PrescriptionDto toPrescriptionDto(Prescription entity) {
        if (entity == null) {
            return null;
        }
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(entity.getId());
        dto.setPrescriptionDate(entity.getPrescriptionDate());
        dto.setDiagnosis(entity.getDiagnosis());
        dto.setAdvice(entity.getAdvice());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
