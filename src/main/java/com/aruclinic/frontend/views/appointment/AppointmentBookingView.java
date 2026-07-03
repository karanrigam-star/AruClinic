package com.aruclinic.frontend.views.appointment;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.DoctorService;
import com.aruclinic.service.PatientService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Appointment Booking view for scheduling new appointments.
 */
@PageTitle("Book Appointment | AruClinic")
@Route("patient/appointments/add")
@CssImport("./themes/aruclinic/appointment.css")
public class AppointmentBookingView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    private final ComboBox<String> doctorCombo = new ComboBox<>("Select Doctor");
    private final DatePicker datePicker = new DatePicker("Select Date");
    private final ComboBox<String> timeSlotCombo = new ComboBox<>("Select Time Slot");
    private final ComboBox<String> appointmentTypeCombo = new ComboBox<>("Appointment Type");
    private final TextArea reasonTextArea = new TextArea("Reason for Appointment");

    private final Button bookButton = new Button("Book Appointment");
    private final Button cancelButton = new Button("Cancel");

    private final List<String> timeSlots = Arrays.asList(
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
        "04:30 PM - 05:00 PM"
    );

    private final List<String> appointmentTypes = Arrays.asList(
        "General Checkup",
        "Follow-up Visit",
        "New Patient Visit",
        "Annual Physical",
        "Blood Test",
        "X-Ray",
        "Vaccination",
        "Consultation",
        "Emergency"
    );

    public AppointmentBookingView(AppointmentService appointmentService, DoctorService doctorService, PatientService patientService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;

        configureComponents();
        add(createBookingForm());
    }

    private void configureComponents() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Doctor combo
        doctorCombo.setItems("Dr. Smith", "Dr. Johnson", "Dr. Williams", "Dr. Brown");
        doctorCombo.setRequired(true);
        doctorCombo.setRequiredIndicatorVisible(true);
        doctorCombo.setWidthFull();
        doctorCombo.setItems(doctorService.getAllDoctors().stream()
            .map(d -> "Dr. " + d.getFirstName() + " " + d.getLastName())
            .collect(java.util.stream.Collectors.toList()));
        doctorCombo.addValueChangeListener(e -> updateAvailableSlots());

        // Date picker
        datePicker.setRequired(true);
        datePicker.setRequiredIndicatorVisible(true);
        datePicker.setWidthFull();
        datePicker.setMin(LocalDate.now());
        datePicker.addValueChangeListener(e -> updateAvailableSlots());

        // Time slot combo
        timeSlotCombo.setItems(timeSlots);
        timeSlotCombo.setRequired(true);
        timeSlotCombo.setRequiredIndicatorVisible(true);
        timeSlotCombo.setWidthFull();

        // Appointment type combo
        appointmentTypeCombo.setItems(appointmentTypes);
        appointmentTypeCombo.setRequired(true);
        appointmentTypeCombo.setRequiredIndicatorVisible(true);
        appointmentTypeCombo.setWidthFull();

        // Reason text area
        reasonTextArea.setPlaceholder("Briefly describe the reason for your appointment");
        reasonTextArea.setWidthFull();
        reasonTextArea.setMinHeight("100px");
        reasonTextArea.setMaxHeight("200px");

        // Book button
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookButton.addClassName("aruclinic-booking-btn");
        bookButton.addClassName("primary");
        bookButton.addClickListener(e -> handleBookAppointment());

        // Cancel button
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClassName("aruclinic-booking-btn");
        cancelButton.addClassName("secondary");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/appointments")));
    }

    private Component createBookingForm() {
        Div formContainer = new Div();
        formContainer.addClassName("aruclinic-appointment-booking");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-booking-header");

        H1 title = new H1("Book Appointment");
        title.addClassName("aruclinic-booking-title");

        Paragraph subtitle = new Paragraph("Schedule an appointment with one of our doctors");
        subtitle.addClassName("aruclinic-booking-subtitle");

        header.add(title, subtitle);

        // Step indicator
        Div stepIndicator = new Div();
        stepIndicator.addClassName("aruclinic-booking-step-indicator");

        Div step1 = new Div();
        step1.addClassName("aruclinic-booking-step");
        step1.addClassName("active");
        Span step1Number = new Span("1");
        step1Number.addClassName("aruclinic-booking-step-number");
        Span step1Label = new Span("Select Doctor");
        step1Label.addClassName("aruclinic-booking-step-label");
        step1.add(step1Number, step1Label);

        Div step2 = new Div();
        step2.addClassName("aruclinic-booking-step");
        Span step2Number = new Span("2");
        step2Number.addClassName("aruclinic-booking-step-number");
        Span step2Label = new Span("Select Date & Time");
        step2Label.addClassName("aruclinic-booking-step-label");
        step2.add(step2Number, step2Label);

        Div step3 = new Div();
        step3.addClassName("aruclinic-booking-step");
        Span step3Number = new Span("3");
        step3Number.addClassName("aruclinic-booking-step-number");
        Span step3Label = new Span("Confirm");
        step3Label.addClassName("aruclinic-booking-step-label");
        step3.add(step3Number, step3Label);

        stepIndicator.add(step1, step2, step3);

        // Form
        Div form = new Div();
        form.addClassName("aruclinic-booking-form");

        // Step 1: Select Doctor
        Div step1Content = new Div();
        step1Content.addClassName("aruclinic-booking-step-content");
        step1Content.addClassName("active");

        FormLayout doctorLayout = new FormLayout();
        doctorLayout.setWidthFull();
        doctorLayout.add(doctorCombo);

        step1Content.add(doctorLayout);

        // Step 2: Select Date & Time
        Div step2Content = new Div();
        step2Content.addClassName("aruclinic-booking-step-content");

        FormLayout dateTimeLayout = new FormLayout();
        dateTimeLayout.setWidthFull();

        dateTimeLayout.add(datePicker);
        dateTimeLayout.add(timeSlotCombo);
        dateTimeLayout.add(appointmentTypeCombo);

        step2Content.add(dateTimeLayout);

        // Step 3: Confirm
        Div step3Content = new Div();
        step3Content.addClassName("aruclinic-booking-step-content");

        FormLayout confirmLayout = new FormLayout();
        confirmLayout.setWidthFull();
        confirmLayout.add(reasonTextArea);

        step3Content.add(confirmLayout);

        form.add(step1Content, step2Content, step3Content);

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-booking-actions");
        actions.add(cancelButton, bookButton);

        formContainer.add(header, stepIndicator, form, actions);
        return formContainer;
    }

    private void updateAvailableSlots() {
        // In a real application, this would fetch available slots from the backend
        // based on the selected doctor and date
        if (doctorCombo.getValue() != null && datePicker.getValue() != null) {
            // Simulate fetching available slots
            timeSlotCombo.setItems(timeSlots);
        }
    }

    private void handleBookAppointment() {
        // Validate all fields
        if (!validateFields()) {
            return;
        }

        AppointmentDto appointmentDto = new AppointmentDto();
        appointmentDto.setDoctorName(doctorCombo.getValue());
        appointmentDto.setAppointmentDate(datePicker.getValue());
        appointmentDto.setAppointmentTime(parseTimeSlot(timeSlotCombo.getValue()));
        appointmentDto.setAppointmentType(appointmentTypeCombo.getValue());
        appointmentDto.setReason(reasonTextArea.getValue().trim());
        appointmentDto.setStatus(com.aruclinic.dto.AppointmentStatus.SCHEDULED);

        // Resolve doctor ID
        String selectedDoctorName = doctorCombo.getValue();
        Long doctorId = doctorService.getAllDoctors().stream()
            .filter(d -> ("Dr. " + d.getFirstName() + " " + d.getLastName()).equals(selectedDoctorName))
            .map(com.aruclinic.dto.DoctorDto::getId)
            .findFirst()
            .orElse(null);
        appointmentDto.setDoctorId(doctorId);

        // Resolve patient ID
        Long patientId = patientService.getAllPatients().stream()
            .map(com.aruclinic.dto.PatientDto::getId)
            .findFirst()
            .orElse(1L);
        appointmentDto.setPatientId(patientId);

        try {
            appointmentService.createAppointment(appointmentDto);

            Notification.show(
                "Appointment booked successfully! You will receive a confirmation shortly.",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navigate to appointments list
            getUI().ifPresent(ui -> ui.navigate("patient/appointments"));

        } catch (Exception e) {
            showError("Failed to book appointment: " + e.getMessage());
        }
    }

    private LocalTime parseTimeSlot(String timeSlot) {
        // Parse time slot like "09:00 AM - 09:30 AM" to LocalTime
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

    private boolean validateFields() {
        boolean isValid = true;

        if (doctorCombo.getValue() == null) {
            doctorCombo.setErrorMessage("Please select a doctor");
            doctorCombo.setInvalid(true);
            isValid = false;
        } else {
            doctorCombo.setErrorMessage(null);
            doctorCombo.setInvalid(false);
        }

        if (datePicker.getValue() == null) {
            datePicker.setErrorMessage("Please select a date");
            datePicker.setInvalid(true);
            isValid = false;
        } else {
            datePicker.setErrorMessage(null);
            datePicker.setInvalid(false);
        }

        if (timeSlotCombo.getValue() == null) {
            timeSlotCombo.setErrorMessage("Please select a time slot");
            timeSlotCombo.setInvalid(true);
            isValid = false;
        } else {
            timeSlotCombo.setErrorMessage(null);
            timeSlotCombo.setInvalid(false);
        }

        if (appointmentTypeCombo.getValue() == null) {
            appointmentTypeCombo.setErrorMessage("Please select an appointment type");
            appointmentTypeCombo.setInvalid(true);
            isValid = false;
        } else {
            appointmentTypeCombo.setErrorMessage(null);
            appointmentTypeCombo.setInvalid(false);
        }

        return isValid;
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
