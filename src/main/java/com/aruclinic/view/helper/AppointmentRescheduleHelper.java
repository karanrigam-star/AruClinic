package com.aruclinic.view.helper;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.Doctor;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.DoctorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter/helper class as intermediate between UI views and Service layer for appointment rescheduling.
 */
public class AppointmentRescheduleHelper {

    private static final List<String> timeSlots = Arrays.asList(
        "09:00 AM - 09:30 AM",
        "09:30 AM - 10:00 AM",
        "10:00 AM - 10:30 AM",
        "10:30 AM - 11:00 AM",
        "11:00 AM - 11:30 AM",
        "11:30 AM - 12:00 PM",
        "02:00 PM - 02:30 PM",
        "02:30 PM - 03:00 PM",
        "03:00 PM - 03:30 PM",
        "03:30 PM - 04:00 PM",
        "04:00 PM - 04:30 PM",
        "04:30 PM - 05:00 PM",
        "05:00 PM - 05:30 PM",
        "05:30 PM - 06:00 PM",
        "06:00 PM - 06:30 PM",
        "06:30 PM - 07:00 PM"
    );

    private static LocalTime parseTimeSlot(String timeSlot) {
        String startTime = timeSlot.split(" - ")[0];
        String[] parts = startTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1].split(" ")[0]);

        if (startTime.contains("PM") && hour != 12) {
            hour += 12;
        }
        if (startTime.contains("AM") && hour == 12) {
            hour = 0;
        }

        return LocalTime.of(hour, minute);
    }

    public static void openRescheduleDialog(
            Appointment appt, 
            AppointmentService appointmentService, 
            DoctorService doctorService,
            Runnable refreshCallback) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reschedule Appointment #" + appt.getId());
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        DatePicker date = new DatePicker("New Date");
        date.setMin(LocalDate.now());
        date.setValue(appt.getAppointmentDate() != null && !appt.getAppointmentDate().isBefore(LocalDate.now()) ? appt.getAppointmentDate() : LocalDate.now());
        date.setRequired(true);

        ComboBox<String> timeSlotCombo = new ComboBox<>("New Time Slot");
        timeSlotCombo.setRequired(true);

        TextArea reasonField = new TextArea("Reason for Rescheduling");
        reasonField.setPlaceholder("Please specify why the appointment is being rescheduled.");
        reasonField.setRequired(true);

        // Fetch initial slots
        updateRescheduleSlots(appt, date, timeSlotCombo, appointmentService, doctorService);

        date.addValueChangeListener(e -> updateRescheduleSlots(appt, date, timeSlotCombo, appointmentService, doctorService));

        form.add(date, timeSlotCombo, reasonField);

        Button saveBtn = new Button("Reschedule", e -> {
            if (date.getValue() == null || timeSlotCombo.getValue() == null) {
                Notification.show("Please select date and time slot", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            String selectedSlot = timeSlotCombo.getValue();
            if (selectedSlot.contains("not available") || selectedSlot.contains("Doctor is currently")) {
                Notification.show("Please select a valid time slot", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            String reason = reasonField.getValue().trim();
            if (reason.isEmpty()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Please enter a reason");
                return;
            }

            Long newDoctorId = null;
            if (selectedSlot.contains(" - ID:")) {
                try {
                    String idStr = selectedSlot.substring(selectedSlot.indexOf(" - ID:") + 6).replace(")", "").trim();
                    newDoctorId = Long.parseLong(idStr);
                } catch (Exception ex) {}
            }

            try {
                LocalTime selectedTime = parseTimeSlot(selectedSlot);
                appointmentService.patientRescheduleAppointment(appt.getId(), date.getValue(), selectedTime, reason, newDoctorId);
                Notification.show("Appointment rescheduled successfully!", 2000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshCallback.run();
            } catch (com.aruclinic.exception.AppointmentSlotConflictException conflictEx) {
                Notification.show(conflictEx.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Failed to reschedule: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private static void updateRescheduleSlots(
            Appointment appt, 
            DatePicker datePicker, 
            ComboBox<String> timeSlotCombo,
            AppointmentService appointmentService,
            DoctorService doctorService) {

        if (datePicker.getValue() == null) {
            timeSlotCombo.setItems(Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();

        if (appt.getDoctor() == null) {
            timeSlotCombo.setItems(Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        Doctor doctor = doctorService.getDoctorEntityById(appt.getDoctor().getId());
        if (doctor == null) {
            timeSlotCombo.setItems(Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        String spec = doctor.getSpecialization();
        final List<Doctor> sameSpecialtyDocs;
        if (spec != null && !spec.trim().isEmpty()) {
            List<Doctor> tempDocs = Collections.emptyList();
            try {
                tempDocs = doctorService.getDoctorsBySpecializationEntity(spec).stream()
                    .filter(d -> !d.getId().equals(doctor.getId()))
                    .collect(Collectors.toList());
            } catch (Exception ex) {}
            sameSpecialtyDocs = tempDocs;
        } else {
            sameSpecialtyDocs = Collections.emptyList();
        }

        List<Appointment> allApptsOnDate = new ArrayList<>();
        try {
            allApptsOnDate = appointmentService.getActiveAppointmentsOnDate(selectedDate).stream()
                .filter(a -> !a.getId().equals(appt.getId()))
                .collect(Collectors.toList());
        } catch (Exception ex) {}

        final List<Appointment> finalAppts = allApptsOnDate;

        List<String> listToDisplay = timeSlots.stream()
            .map(slot -> {
                try {
                    LocalTime slotStart = parseTimeSlot(slot);
                    if (selectedDate.isEqual(today) && !slotStart.isAfter(LocalTime.now())) {
                        return null;
                    }

                    boolean origDocStatusOk = true;
                    if (selectedDate.isEqual(today)) {
                        origDocStatusOk = "AVAILABLE".equalsIgnoreCase(doctor.getStatus());
                    }
                    boolean origDocBooked = finalAppts.stream()
                        .anyMatch(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctor.getId()) && a.getAppointmentDateTime().toLocalTime().equals(slotStart));

                    if (origDocStatusOk && !origDocBooked) {
                        return slot;
                    }

                    for (Doctor otherDoc : sameSpecialtyDocs) {
                        final Doctor finalOtherDoc = otherDoc;
                        boolean otherDocStatusOk = true;
                        if (selectedDate.isEqual(today)) {
                            otherDocStatusOk = "AVAILABLE".equalsIgnoreCase(finalOtherDoc.getStatus());
                        }
                        boolean otherDocBooked = finalAppts.stream()
                            .anyMatch(a -> a.getDoctor() != null && a.getDoctor().getId().equals(finalOtherDoc.getId()) && a.getAppointmentDateTime().toLocalTime().equals(slotStart));

                        if (otherDocStatusOk && !otherDocBooked) {
                            return slot + " (with Dr. " + finalOtherDoc.getName() + " - ID:" + finalOtherDoc.getId() + ")";
                        }
                    }

                    return slot + " (Dr. " + doctor.getName() + " is not available)";
                } catch (Exception ex) {
                    return slot;
                }
            })
            .filter(slot -> slot != null)
            .collect(Collectors.toList());

        timeSlotCombo.setItems(listToDisplay);
        if (!listToDisplay.isEmpty()) {
            String currentSlotMatch = listToDisplay.stream()
                .filter(s -> !s.contains("not available"))
                .findFirst()
                .orElse(null);
            timeSlotCombo.setValue(currentSlotMatch);
        } else {
            timeSlotCombo.setValue(null);
        }
    }
}
