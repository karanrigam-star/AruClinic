package com.aruclinic.mapper;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.entity.Appointment;
import org.mapstruct.Mapper;

/**
 * Mapper for Appointment entity and AppointmentDto.
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AppointmentMapper {

    default Appointment toAppointment(AppointmentDto dto) {
        if (dto == null) {
            return null;
        }
        Appointment appointment = new Appointment();
        appointment.setId(dto.getId());
        appointment.setAppointmentDateTime(dto.getAppointmentDateTime());
        appointment.setStatus(dto.getStatus() != null ? com.aruclinic.entity.AppointmentStatus.valueOf(dto.getStatus().name()) : null);
        return appointment;
    }

    default AppointmentDto toAppointmentDto(Appointment entity) {
        if (entity == null) {
            return null;
        }
        AppointmentDto dto = new AppointmentDto();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setDoctorId(entity.getDoctor() != null ? entity.getDoctor().getId() : null);
        dto.setAppointmentDateTime(entity.getAppointmentDateTime());
        dto.setStatus(entity.getStatus() != null ? com.aruclinic.dto.AppointmentStatus.valueOf(entity.getStatus().name()) : null);
        return dto;
    }
}
