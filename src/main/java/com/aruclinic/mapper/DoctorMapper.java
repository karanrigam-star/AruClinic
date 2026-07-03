package com.aruclinic.mapper;

import com.aruclinic.dto.DoctorDto;
import com.aruclinic.entity.Doctor;
import org.mapstruct.Mapper;

/**
 * Mapper for Doctor entity and DoctorDto.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface DoctorMapper {

    default Doctor toDoctor(DoctorDto dto) {
        if (dto == null) {
            return null;
        }
        Doctor doctor = new Doctor();
        doctor.setName(dto.getFirstName() + " " + (dto.getLastName() != null ? dto.getLastName() : ""));
        doctor.setQualification(dto.getQualification());
        doctor.setExperience(dto.getExperience());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setDepartment(dto.getDepartment());
        doctor.setMobileNumber(dto.getMobileNumber());
        doctor.setEmail(dto.getEmail());
        return doctor;
    }

    default DoctorDto toDoctorDto(Doctor entity) {
        if (entity == null) {
            return null;
        }
        DoctorDto dto = new DoctorDto();
        dto.setId(entity.getId());
        String[] nameParts = entity.getName() != null ? entity.getName().split(" ", 2) : new String[0];
        dto.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
        dto.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        dto.setQualification(entity.getQualification());
        dto.setSpecialization(entity.getSpecialization());
        dto.setDepartment(entity.getDepartment());
        dto.setMobileNumber(entity.getMobileNumber());
        dto.setEmail(entity.getEmail());
        dto.setExperience(entity.getExperience());
        return dto;
    }
}
