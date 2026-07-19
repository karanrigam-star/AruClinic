package com.aruclinic.view.appointment;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.DoctorService;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.UserService;
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
import com.vaadin.flow.router.RouteAlias;
import com.aruclinic.view.MainLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Appointment Booking view for scheduling new appointments.
 */
@PageTitle("Book Appointment | AruClinic")
@Route(value = "patient/appointments/add", layout = MainLayout.class)
@RouteAlias(value = "receptionist/appointments/add", layout = MainLayout.class)
@RouteAlias(value = "doctor/appointments/add", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
public class AppointmentBookingView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final UserService userService;

    private final ComboBox<String> doctorCombo = new ComboBox<>("Select Doctor");
    private final Div doctorInfoPanel = new Div();
    private final DatePicker datePicker = new DatePicker("Select Date");
    private final ComboBox<String> timeSlotCombo = new ComboBox<>("Select Time Slot");
    private final ComboBox<String> appointmentTypeCombo = new ComboBox<>("Appointment Type");
    private final TextArea reasonTextArea = new TextArea("Reason for Appointment");

    private final Button bookButton = new Button("Next: Select Date & Time");
    private final Button cancelButton = new Button("Cancel");

    // Staff Patient Booking fields
    private final RadioButtonGroup<String> patientChoice = new RadioButtonGroup<>("Patient Booking Type");
    private final ComboBox<com.aruclinic.dto.PatientDto> existingPatientCombo = new ComboBox<>("Select Patient");
    private final TextField newPatientFirstName = new TextField("Patient First Name");
    private final TextField newPatientLastName = new TextField("Patient Last Name");
    private final TextField newPatientEmail = new TextField("Patient Email");
    private final TextField newPatientMobile = new TextField("Patient Mobile");
    private final DatePicker newPatientDob = new DatePicker("Patient Date of Birth");
    private final Select<String> newPatientGender = new Select<>();

    private int currentStep = 1;
    private Div step1;
    private Div step2;
    private Div step3;
    private Div step1Content;
    private Div step2Content;
    private Div step3Content;

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
        "04:30 PM - 05:00 PM",
        "05:00 PM - 05:30 PM",
        "05:30 PM - 06:00 PM",
        "06:00 PM - 06:30 PM",
        "06:30 PM - 07:00 PM"
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

    public AppointmentBookingView(AppointmentService appointmentService, DoctorService doctorService, PatientService patientService, UserService userService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.userService = userService;

        configureComponents();
        add(createBookingForm());
        updateStepUI();
    }

    private void configureComponents() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Doctor combo
        doctorCombo.setRequired(true);
        doctorCombo.setRequiredIndicatorVisible(true);
        doctorCombo.setWidthFull();

        // Configure Patient Selection (if staff)
        String userRole = getUserRole();
        boolean isStaff = "RECEPTIONIST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole) || "DOCTOR".equalsIgnoreCase(userRole);

        if (isStaff) {
            patientChoice.setItems("Existing Patient", "Register New Patient");
            patientChoice.setValue("Existing Patient");
            
            existingPatientCombo.setItems(patientService.getAllPatients());
            existingPatientCombo.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")");
            existingPatientCombo.setRequired(true);
            existingPatientCombo.setWidthFull();

            newPatientFirstName.setRequired(true);
            newPatientFirstName.setWidthFull();

            newPatientLastName.setRequired(true);
            newPatientLastName.setWidthFull();

            newPatientEmail.setRequired(true);
            newPatientEmail.setWidthFull();

            newPatientMobile.setRequired(true);
            newPatientMobile.setWidthFull();
            newPatientMobile.setPattern("\\d{10}");

            newPatientDob.setRequired(true);
            newPatientDob.setWidthFull();

            newPatientGender.setLabel("Gender");
            newPatientGender.setItems("Male", "Female", "Other");
            newPatientGender.setValue("Male");
            newPatientGender.setWidthFull();
        }
        
        List<String> doctorsList = doctorService.getAllDoctors().stream()
            .map(d -> {
                String name = "Dr. " + d.getFirstName() + " " + d.getLastName();
                if (d.getSpecialization() != null && !d.getSpecialization().isEmpty()) {
                    name += " - " + d.getSpecialization();
                }
                return name;
            })
            .collect(java.util.stream.Collectors.toList());
            
        if (doctorsList.isEmpty()) {
            doctorCombo.setItems("No doctor available");
            doctorCombo.setValue("No doctor available");
            doctorCombo.setEnabled(false);
        } else {
            doctorCombo.setPlaceholder("Select a doctor");
            doctorCombo.setEnabled(true);
            doctorCombo.setItems(doctorsList);
        }
        
        doctorCombo.addValueChangeListener(e -> {
            String selectedName = e.getValue();
            doctorInfoPanel.removeAll();
            if (selectedName != null) {
                doctorService.getAllDoctors().stream()
                    .filter(d -> {
                        String formatted = "Dr. " + d.getFirstName() + " " + d.getLastName();
                        if (d.getSpecialization() != null && !d.getSpecialization().isEmpty()) {
                            formatted += " - " + d.getSpecialization();
                        }
                        return formatted.equals(selectedName);
                    })
                    .findFirst()
                    .ifPresent(d -> {
                        doctorInfoPanel.add(createDoctorDetailBadge(d));
                    });
            }
            updateAvailableSlots();
        });

        // Date picker
        datePicker.setRequired(true);
        datePicker.setRequiredIndicatorVisible(true);
        datePicker.setWidthFull();
        datePicker.setMin(LocalDate.now());
        datePicker.setValue(LocalDate.now()); // Set current date as default
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

        // Book / Next button
        bookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookButton.addClassName("aruclinic-booking-btn");
        bookButton.addClassName("primary");
        bookButton.addClickListener(e -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    currentStep = 2;
                    updateStepUI();
                }
            } else if (currentStep == 2) {
                if (validateStep2()) {
                    currentStep = 3;
                    updateStepUI();
                }
            } else if (currentStep == 3) {
                handleBookAppointment();
            }
        });

        // Cancel / Back button
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClassName("aruclinic-booking-btn");
        cancelButton.addClassName("secondary");
        cancelButton.addClickListener(e -> {
            if (currentStep == 1) {
                getUI().ifPresent(ui -> ui.navigate(determineDashboardRoute()));
            } else if (currentStep == 2) {
                currentStep = 1;
                updateStepUI();
            } else if (currentStep == 3) {
                currentStep = 2;
                updateStepUI();
            }
        });
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

        step1 = new Div();
        step1.addClassName("aruclinic-booking-step");
        Span step1Number = new Span("1");
        step1Number.addClassName("aruclinic-booking-step-number");
        Span step1Label = new Span("Select Doctor");
        step1Label.addClassName("aruclinic-booking-step-label");
        step1.add(step1Number, step1Label);

        step2 = new Div();
        step2.addClassName("aruclinic-booking-step");
        Span step2Number = new Span("2");
        step2Number.addClassName("aruclinic-booking-step-number");
        Span step2Label = new Span("Select Date & Time");
        step2Label.addClassName("aruclinic-booking-step-label");
        step2.add(step2Number, step2Label);

        step3 = new Div();
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
        step1Content = new Div();
        step1Content.addClassName("aruclinic-booking-step-content");

        FormLayout doctorLayout = new FormLayout();
        doctorLayout.setWidthFull();
        doctorLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1)
        );
        doctorLayout.add(doctorCombo);

        String userRole = getUserRole();
        boolean isStaff = "RECEPTIONIST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole) || "DOCTOR".equalsIgnoreCase(userRole);

        if (isStaff) {
            Div patientSection = new Div();
            patientSection.getStyle().set("margin-top", "20px");
            patientSection.add(patientChoice);

            FormLayout existingPatientLayout = new FormLayout();
            existingPatientLayout.setWidthFull();
            existingPatientLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
            );
            existingPatientLayout.add(existingPatientCombo);

            FormLayout newPatientLayout = new FormLayout();
            newPatientLayout.setWidthFull();
            newPatientLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
            );
            newPatientLayout.add(newPatientFirstName, newPatientLastName, newPatientEmail, newPatientMobile, newPatientDob, newPatientGender);
            newPatientLayout.setVisible(false);

            patientChoice.addValueChangeListener(e -> {
                boolean isExisting = "Existing Patient".equals(e.getValue());
                existingPatientLayout.setVisible(isExisting);
                newPatientLayout.setVisible(!isExisting);
            });

            step1Content.add(doctorLayout, patientSection, existingPatientLayout, newPatientLayout, doctorInfoPanel);
        } else {
            step1Content.add(doctorLayout, doctorInfoPanel);
        }

        // Step 2: Select Date & Time
        step2Content = new Div();
        step2Content.addClassName("aruclinic-booking-step-content");

        FormLayout dateTimeLayout = new FormLayout();
        dateTimeLayout.setWidthFull();
        dateTimeLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2),
            new FormLayout.ResponsiveStep("900px", 3)
        );

        dateTimeLayout.add(datePicker);
        dateTimeLayout.add(timeSlotCombo);
        dateTimeLayout.add(appointmentTypeCombo);

        step2Content.add(dateTimeLayout);

        // Step 3: Confirm
        step3Content = new Div();
        step3Content.addClassName("aruclinic-booking-step-content");

        FormLayout confirmLayout = new FormLayout();
        confirmLayout.setWidthFull();
        confirmLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1)
        );
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
        if (doctorCombo.getValue() != null && datePicker.getValue() != null) {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();
            
            // Resolve doctor ID
            String selectedDoctorDisplay = doctorCombo.getValue();
            Long doctorId = null;
            String doctorName = "";
            if (selectedDoctorDisplay != null) {
                com.aruclinic.dto.DoctorDto matchedDoctor = doctorService.getAllDoctors().stream()
                    .filter(d -> {
                        String displayName = "Dr. " + d.getFirstName() + " " + d.getLastName();
                        return selectedDoctorDisplay.startsWith(displayName);
                    })
                    .findFirst()
                    .orElse(null);
                if (matchedDoctor != null) {
                    doctorId = matchedDoctor.getId();
                    doctorName = matchedDoctor.getLastName();
                }
            }

            // Get doctor appointments on this day
            List<com.aruclinic.entity.Appointment> doctorAppts = new java.util.ArrayList<>();
            if (doctorId != null) {
                try {
                    doctorAppts = appointmentService.findByDoctorId(doctorId).stream()
                        .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().isEqual(selectedDate))
                        .filter(a -> a.getStatus() != com.aruclinic.entity.AppointmentStatus.CANCELLED)
                        .collect(java.util.stream.Collectors.toList());
                } catch (Exception ex) {
                    // Ignore
                }
            }

            final List<com.aruclinic.entity.Appointment> finalAppts = doctorAppts;
            final String finalDocName = doctorName;
            
            List<String> listToDisplay = timeSlots.stream()
                .map(slot -> {
                    try {
                        LocalTime slotStart = parseTimeSlot(slot);
                        // Filter out if today and slot time is in the past
                        if (selectedDate.isEqual(today) && !slotStart.isAfter(LocalTime.now())) {
                            return null;
                        }
                        // Check if doctor is already booked for this slot
                        boolean isBooked = finalAppts.stream()
                            .anyMatch(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalTime().equals(slotStart));
                        if (isBooked) {
                            return slot + " (Dr. " + finalDocName + " is not available for this time slot)";
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    return slot;
                })
                .filter(slot -> slot != null)
                .collect(java.util.stream.Collectors.toList());

            timeSlotCombo.setItems(listToDisplay);
        }
    }

    private void updateStepUI() {
        step1.removeClassName("active");
        step1.removeClassName("completed");
        step2.removeClassName("active");
        step2.removeClassName("completed");
        step3.removeClassName("active");
        step3.removeClassName("completed");

        step1Content.removeClassName("active");
        step2Content.removeClassName("active");
        step3Content.removeClassName("active");

        if (currentStep == 1) {
            step1.addClassName("active");
            step1Content.addClassName("active");
            
            cancelButton.setText("Cancel");
            bookButton.setText("Next: Select Date & Time");
        } else if (currentStep == 2) {
            step1.addClassName("completed");
            step2.addClassName("active");
            step2Content.addClassName("active");
            
            cancelButton.setText("Back");
            bookButton.setText("Next: Confirm");
        } else if (currentStep == 3) {
            step1.addClassName("completed");
            step2.addClassName("completed");
            step3.addClassName("active");
            step3Content.addClassName("active");
            
            cancelButton.setText("Back");
            bookButton.setText("Book Appointment");
        }
    }

    private boolean validateStep1() {
        if (doctorCombo.getValue() == null || "No doctor available".equals(doctorCombo.getValue())) {
            doctorCombo.setErrorMessage("No doctors are currently available to book");
            doctorCombo.setInvalid(true);
            return false;
        }
        doctorCombo.setErrorMessage(null);
        doctorCombo.setInvalid(false);

        String userRole = getUserRole();
        boolean isStaff = "RECEPTIONIST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole) || "DOCTOR".equalsIgnoreCase(userRole);

        if (isStaff) {
            if ("Existing Patient".equals(patientChoice.getValue())) {
                if (existingPatientCombo.getValue() == null) {
                    existingPatientCombo.setErrorMessage("Please select an existing patient");
                    existingPatientCombo.setInvalid(true);
                    return false;
                }
                existingPatientCombo.setErrorMessage(null);
                existingPatientCombo.setInvalid(false);
            } else {
                boolean isValid = true;
                if (newPatientFirstName.getValue().trim().isEmpty()) {
                    newPatientFirstName.setErrorMessage("First name is required");
                    newPatientFirstName.setInvalid(true);
                    isValid = false;
                } else {
                    newPatientFirstName.setErrorMessage(null);
                    newPatientFirstName.setInvalid(false);
                }

                if (newPatientLastName.getValue().trim().isEmpty()) {
                    newPatientLastName.setErrorMessage("Last name is required");
                    newPatientLastName.setInvalid(true);
                    isValid = false;
                } else {
                    newPatientLastName.setErrorMessage(null);
                    newPatientLastName.setInvalid(false);
                }

                String emailVal = newPatientEmail.getValue().trim();
                if (emailVal.isEmpty() || !emailVal.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    newPatientEmail.setErrorMessage("Please enter a valid email");
                    newPatientEmail.setInvalid(true);
                    isValid = false;
                } else {
                    newPatientEmail.setErrorMessage(null);
                    newPatientEmail.setInvalid(false);
                }

                String mobileVal = newPatientMobile.getValue().trim();
                if (!mobileVal.matches("\\d{10}")) {
                    newPatientMobile.setErrorMessage("Mobile number must be 10 digits");
                    newPatientMobile.setInvalid(true);
                    isValid = false;
                } else {
                    newPatientMobile.setErrorMessage(null);
                    newPatientMobile.setInvalid(false);
                }

                if (newPatientDob.getValue() == null) {
                    newPatientDob.setErrorMessage("Date of birth is required");
                    newPatientDob.setInvalid(true);
                    isValid = false;
                } else {
                    newPatientDob.setErrorMessage(null);
                    newPatientDob.setInvalid(false);
                }

                return isValid;
            }
        }
        return true;
    }

    private boolean validateStep2() {
        boolean isValid = true;
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
        } else if (timeSlotCombo.getValue().contains("not available")) {
            timeSlotCombo.setErrorMessage("The selected doctor is not available for this time slot");
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

    private void handleBookAppointment() {
        AppointmentDto appointmentDto = new AppointmentDto();
        appointmentDto.setAppointmentDate(datePicker.getValue());
        appointmentDto.setAppointmentTime(parseTimeSlot(timeSlotCombo.getValue()));
        appointmentDto.setAppointmentType(appointmentTypeCombo.getValue());
        appointmentDto.setReason(reasonTextArea.getValue().trim());
        appointmentDto.setStatus(com.aruclinic.dto.AppointmentStatus.SCHEDULED);

        // Resolve doctor ID robustly
        String selectedDoctorDisplay = doctorCombo.getValue();
        Long doctorId = null;
        String doctorNameForDto = "";
        if (selectedDoctorDisplay != null) {
            com.aruclinic.dto.DoctorDto matchedDoctor = doctorService.getAllDoctors().stream()
                .filter(d -> {
                    String displayName = "Dr. " + d.getFirstName() + " " + d.getLastName();
                    return selectedDoctorDisplay.startsWith(displayName);
                })
                .findFirst()
                .orElse(null);
            if (matchedDoctor != null) {
                doctorId = matchedDoctor.getId();
                doctorNameForDto = "Dr. " + matchedDoctor.getFirstName() + " " + matchedDoctor.getLastName();
            } else {
                doctorNameForDto = selectedDoctorDisplay;
            }
        }
        appointmentDto.setDoctorId(doctorId);
        appointmentDto.setDoctorName(doctorNameForDto);

        // Resolve patient ID
        Long patientId = null;
        String userRole = getUserRole();
        boolean isStaff = "RECEPTIONIST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole) || "SUPER_ADMIN".equalsIgnoreCase(userRole) || "DOCTOR".equalsIgnoreCase(userRole);

        if (isStaff) {
            if ("Existing Patient".equals(patientChoice.getValue())) {
                com.aruclinic.dto.PatientDto selected = existingPatientCombo.getValue();
                if (selected != null) {
                    patientId = selected.getId();
                }
            } else {
                // Register a new patient first
                try {
                    String regEmail = newPatientEmail.getValue().trim();
                    com.aruclinic.dto.PatientDto newPatientDto = new com.aruclinic.dto.PatientDto();
                    newPatientDto.setFirstName(newPatientFirstName.getValue().trim());
                    newPatientDto.setLastName(newPatientLastName.getValue().trim());
                    newPatientDto.setEmail(regEmail);
                    newPatientDto.setMobileNumber(newPatientMobile.getValue().trim());
                    newPatientDto.setDateOfBirth(newPatientDob.getValue());
                    newPatientDto.setGender(newPatientGender.getValue());
                    newPatientDto.setBloodGroup("O+");
                    newPatientDto.setAddress("Registered by staff during appointment booking");
                    newPatientDto.setAge(java.time.Period.between(newPatientDob.getValue(), LocalDate.now()).getYears());

                    if (!userService.existsByEmail(regEmail)) {
                        com.aruclinic.dto.UserDto userDto = new com.aruclinic.dto.UserDto();
                        userDto.setFirstName(newPatientFirstName.getValue().trim());
                        userDto.setLastName(newPatientLastName.getValue().trim());
                        userDto.setEmail(regEmail);
                        userDto.setMobileNumber(newPatientMobile.getValue().trim());
                        userDto.setPassword("patient123!");
                        userDto.setConfirmPassword("patient123!");
                        userDto.setDateOfBirth(newPatientDob.getValue());
                        userDto.setGender(newPatientGender.getValue());
                        userDto.setBloodGroup("O+");
                        userDto.setAddress("Registered by staff during appointment booking");
                        userDto.setCity("City");
                        userDto.setState("State");
                        userDto.setDistrict("District");
                        userDto.setZipCode("791111");
                        userDto.setEmergencyContactName("Emergency");
                        userDto.setEmergencyPhone("1234567890");
                        userService.registerUser(userDto);
                    }

                    com.aruclinic.dto.PatientDto created;
                    try {
                        created = patientService.getPatientByEmail(regEmail);
                    } catch (Exception e) {
                        created = patientService.createPatient(newPatientDto);
                    }

                    if (created != null) {
                        patientId = created.getId();
                    }
                } catch (Exception ex) {
                    showError("Failed to register new patient: " + ex.getMessage());
                    return;
                }
            }
        } else {
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth == null) {
                    com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
                    if (session != null) {
                        auth = (org.springframework.security.core.Authentication) 
                                session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                    }
                }

                String email = null;
                if (auth != null && auth.isAuthenticated()) {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                        email = springUser.getUsername();
                    } else if (principal instanceof String principalStr) {
                        email = principalStr;
                    }
                }

                if (email != null) {
                    com.aruclinic.dto.PatientDto currentPatient = null;
                    try {
                        currentPatient = patientService.getPatientByEmail(email);
                    } catch (Exception e) {
                        com.aruclinic.dto.UserDto userDto = userService.getUserByEmail(email);
                        if (userDto != null) {
                            com.aruclinic.dto.PatientDto newPatientDto = new com.aruclinic.dto.PatientDto();
                            newPatientDto.setFirstName(userDto.getFirstName());
                            newPatientDto.setLastName(userDto.getLastName());
                            newPatientDto.setEmail(userDto.getEmail());
                            newPatientDto.setMobileNumber(userDto.getMobileNumber());
                            newPatientDto.setDateOfBirth(userDto.getDateOfBirth() != null ? userDto.getDateOfBirth() : LocalDate.of(1995, 1, 1));
                            newPatientDto.setAge(userDto.getDateOfBirth() != null ? java.time.Period.between(userDto.getDateOfBirth(), LocalDate.now()).getYears() : 31);
                            newPatientDto.setGender(userDto.getGender() != null ? userDto.getGender() : "Other");
                            newPatientDto.setBloodGroup(userDto.getBloodGroup() != null ? userDto.getBloodGroup() : "O+");
                            newPatientDto.setAddress(userDto.getAddress() != null ? userDto.getAddress() : "Auto-created during appointment booking");
                            newPatientDto.setCity(userDto.getCity());
                            newPatientDto.setState(userDto.getState());
                            newPatientDto.setZipCode(userDto.getZipCode());
                            newPatientDto.setEmergencyContact(userDto.getEmergencyContactName());
                            newPatientDto.setEmergencyPhone(userDto.getEmergencyPhone());
                            currentPatient = patientService.createPatient(newPatientDto);
                        }
                    }
                    if (currentPatient != null) {
                        patientId = currentPatient.getId();
                    }
                }
            } catch (Exception ex) {}
        }

        if (patientId == null) {
            patientId = patientService.getAllPatients().stream()
                .map(com.aruclinic.dto.PatientDto::getId)
                .findFirst()
                .orElse(1L);
        }
        appointmentDto.setPatientId(patientId);

        try {
            appointmentService.createAppointment(appointmentDto);

            Notification.show(
                "Appointment booked successfully! You will receive a confirmation shortly.",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate(determineDashboardRoute()));

        } catch (com.aruclinic.exception.AppointmentSlotConflictException conflictEx) {
            showError(conflictEx.getMessage());
            currentStep = 2;
            updateStepUI();
        } catch (Exception e) {
            showError("Failed to book appointment: " + e.getMessage());
        }
    }

    private LocalTime parseTimeSlot(String timeSlot) {
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

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private String determineDashboardRoute() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
                if (session != null) {
                    auth = (org.springframework.security.core.Authentication) 
                            session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                }
            }
            if (auth != null && auth.isAuthenticated()) {
                boolean isReceptionist = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST"));
                if (isReceptionist) {
                    return "receptionist";
                }
                boolean isDoctor = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
                if (isDoctor) {
                    return "doctor";
                }
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
                if (isAdmin) {
                    return "admin";
                }
            }
        } catch (Exception e) {
            // Ignore and fall back to patient
        }
        return "patient";
    }

    private Component createDoctorDetailBadge(com.aruclinic.dto.DoctorDto d) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("background-color", "rgba(0, 59, 92, 0.04)")
            .set("border", "1px solid rgba(0, 59, 92, 0.1)")
            .set("border-radius", "8px")
            .set("padding", "var(--aruclinic-spacing-md)")
            .set("margin-top", "var(--aruclinic-spacing-md)")
            .set("width", "100%");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon docIcon = new Icon(VaadinIcon.DOCTOR);
        docIcon.getStyle().set("color", "#003b5c");
        Span nameSpan = new Span("Dr. " + d.getFirstName() + " " + d.getLastName());
        nameSpan.getStyle().set("font-weight", "600").set("color", "#003b5c");
        header.add(docIcon, nameSpan);

        FormLayout details = new FormLayout();
        details.setWidthFull();
        details.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("350px", 2)
        );

        Span dept = new Span("Department: " + (d.getDepartment() != null ? d.getDepartment() : "General Outpatient"));
        Span spec = new Span("Specialization: " + (d.getSpecialization() != null ? d.getSpecialization() : "General Medicine"));
        Span qual = new Span("Qualification: " + (d.getQualification() != null ? d.getQualification() : "MBBS"));
        Span exp = new Span("Experience: " + (d.getExperience() != null ? d.getExperience() + " Years" : "2 Years"));

        dept.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        spec.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        qual.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        exp.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");

        details.add(dept, spec, qual, exp);
        card.add(header, details);
        return card;
    }

    private String getUserRole() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
                if (session != null) {
                    auth = (org.springframework.security.core.Authentication) 
                            session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                }
            }
            if (auth != null && auth.isAuthenticated()) {
                return auth.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .map(a -> a.replace("ROLE_", ""))
                    .findFirst()
                    .orElse("PATIENT");
            }
        } catch (Exception e) {}
        return "PATIENT";
    }
}
