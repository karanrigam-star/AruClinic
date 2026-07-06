package com.aruclinic.frontend.views.dashboard;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Prescription;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.BillRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.PrescriptionRepository;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.util.PdfHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.StreamResource;
import com.aruclinic.frontend.views.MainLayout;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Patient Dashboard | AruClinic")
@Route(value = "patient", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
public class PatientDashboard extends VerticalLayout implements BeforeEnterObserver {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BillRepository billRepository;
    private final AppointmentService appointmentService;
    private final com.aruclinic.repository.DoctorRepository doctorRepository;

    private Patient currentPatient = null;

    public PatientDashboard(PatientRepository patientRepository,
                            AppointmentRepository appointmentRepository,
                            PrescriptionRepository prescriptionRepository,
                            BillRepository billRepository,
                            AppointmentService appointmentService,
                            com.aruclinic.repository.DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.billRepository = billRepository;
        this.appointmentService = appointmentService;
        this.doctorRepository = doctorRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolveCurrentPatient();
        removeAll();
        add(createDashboardContent());
    }

    private void resolveCurrentPatient() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                VaadinSession session = VaadinSession.getCurrent();
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
                currentPatient = patientRepository.findByEmail(email).orElse(null);
            }

            // Fallback for blank setups during testing
            if (currentPatient == null) {
                List<Patient> patients = patientRepository.findAll();
                if (!patients.isEmpty()) {
                    currentPatient = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private Component createDashboardContent() {
        Div content = new Div();
        content.addClassName("aruclinic-dashboard-content");
        content.setSizeFull();

        content.add(createWelcomeSection());
        content.add(createStatsGrid());
        content.add(createQuickActions());
        content.add(createUpcomingAppointments());
        content.add(createRecentPrescriptions());

        return content;
    }

    private Component createWelcomeSection() {
        Div welcomeSection = new Div();
        welcomeSection.addClassName("aruclinic-welcome-section");

        Div header = new Div();
        header.addClassName("aruclinic-welcome-header");

        String name = currentPatient != null ? currentPatient.getFirstName() + " " + currentPatient.getLastName() : "Patient";
        H1 title = new H1("Welcome, " + name);
        title.addClassName("aruclinic-welcome-title");

        Span subtitle = new Span("Welcome back! Manage your appointments, prescriptions, and medical history");
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button bookAppointmentBtn = new Button("Book Appointment", new Icon(VaadinIcon.CALENDAR));
        bookAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookAppointmentBtn.addClassName("aruclinic-btn");
        bookAppointmentBtn.addClassName("aruclinic-btn-primary");
        bookAppointmentBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/appointments/add")));

        Button viewProfileBtn = new Button("My Profile", new Icon(VaadinIcon.USER));
        viewProfileBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewProfileBtn.addClassName("aruclinic-btn");
        viewProfileBtn.addClassName("aruclinic-btn-secondary");
        viewProfileBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/profile")));

        actions.add(bookAppointmentBtn, viewProfileBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
    }

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");

        long upcomingCount = 0;
        long activePrescriptions = 0;
        long medicalRecords = 0;
        long pendingPayments = 0;

        if (currentPatient != null) {
            // 1. Upcoming Appointments
            upcomingCount = appointmentRepository.findByPatientId(currentPatient.getId()).stream()
                    .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isAfter(java.time.LocalDateTime.now()) && a.getStatus() != AppointmentStatus.CANCELLED)
                    .count();

            // 2. Active Prescriptions (non-draft)
            List<Prescription> prescriptions = prescriptionRepository.findByPatientId(currentPatient.getId());
            activePrescriptions = prescriptions.stream()
                    .filter(p -> !"DRAFT".equalsIgnoreCase(p.getStatus()))
                    .count();

            // 3. Medical Records (Completed Appointments + Prescriptions)
            long completedAppts = appointmentRepository.findByPatientId(currentPatient.getId()).stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();
            medicalRecords = completedAppts + prescriptions.size();

            // 4. Pending Payments
            pendingPayments = billRepository.findByPatientId(currentPatient.getId()).stream()
                    .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()) || "UNPAID".equalsIgnoreCase(b.getStatus()))
                    .count();
        }

        statsGrid.add(createStatCard("Upcoming Appointments", String.valueOf(upcomingCount), upcomingCount > 0 ? "+" + upcomingCount : "0", "primary", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Active Prescriptions", String.valueOf(activePrescriptions), activePrescriptions > 0 ? "+" + activePrescriptions : "0", "success", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Medical Records", String.valueOf(medicalRecords), medicalRecords > 0 ? "+" + medicalRecords : "0", "warning", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Pending Payments", String.valueOf(pendingPayments), pendingPayments > 0 ? "+" + pendingPayments : "0", "danger", VaadinIcon.MONEY));

        return statsGrid;
    }

    private Component createStatCard(String label, String value, String trend, String type, VaadinIcon icon) {
        Div statCard = new Div();
        statCard.addClassName("aruclinic-dashboard-stat-card");

        Div header = new Div();
        header.addClassName("stat-header");

        Div iconDiv = new Div();
        iconDiv.addClassName("stat-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(icon));

        Div trendDiv = new Div();
        trendDiv.addClassName("stat-trend");
        trendDiv.addClassName(trend.startsWith("-") ? "negative" : "positive");
        trendDiv.setText(trend);

        header.add(iconDiv, trendDiv);

        Div valueDiv = new Div();
        valueDiv.addClassName("stat-value");
        valueDiv.setText(value);

        Div labelDiv = new Div();
        labelDiv.addClassName("stat-label");
        labelDiv.setText(label);

        statCard.add(header, valueDiv, labelDiv);
        return statCard;
    }

    private Component createQuickActions() {
        Div quickActions = new Div();
        quickActions.addClassName("aruclinic-quick-actions");

        quickActions.add(createQuickAction(
            "Book Appointment",
            "Schedule a new appointment",
            "patient/appointments/add",
            VaadinIcon.CALENDAR
        ));

        quickActions.add(createQuickAction(
            "My Appointments",
            "View all appointments",
            "patient/appointments",
            VaadinIcon.CLOCK
        ));

        quickActions.add(createQuickAction(
            "Prescriptions",
            "View my prescriptions",
            "patient/prescriptions",
            VaadinIcon.FILE_TEXT
        ));

        quickActions.add(createQuickAction(
            "Medical History",
            "Access my medical records",
            "patient/medical-history",
            VaadinIcon.FILE_TEXT
        ));

        quickActions.add(createQuickAction(
            "Billing",
            "View and pay bills",
            "patient/billing",
            VaadinIcon.MONEY
        ));

        return quickActions;
    }

    private Component createQuickAction(String title, String description, String route, VaadinIcon icon) {
        Div action = new Div();
        action.addClassName("aruclinic-quick-action");
        action.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-quick-action-icon");
        iconDiv.add(new Icon(icon));

        Div titleDiv = new Div();
        titleDiv.addClassName("aruclinic-quick-action-title");
        titleDiv.setText(title);

        Div descDiv = new Div();
        descDiv.addClassName("aruclinic-quick-action-description");
        descDiv.setText(description);

        action.add(iconDiv, titleDiv, descDiv);
        return action;
    }

    private Component createUpcomingAppointments() {
        Div section = new Div();
        section.addClassName("aruclinic-recent-activity");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Upcoming Appointments");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/appointments")));

        header.add(title, viewAllBtn);

        Div appointmentList = new Div();
        appointmentList.addClassName("aruclinic-activity-list");

        List<Appointment> upcomingAppts = new ArrayList<>();
        if (currentPatient != null) {
            upcomingAppts = appointmentRepository.findByPatientId(currentPatient.getId()).stream()
                    .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().isAfter(java.time.LocalDateTime.now()))
                    .sorted((a1, a2) -> a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()))
                    .limit(3)
                    .collect(Collectors.toList());
        }

        if (upcomingAppts.isEmpty()) {
            Div emptyMessage = new Div();
            emptyMessage.setText("No upcoming appointments scheduled.");
            emptyMessage.getStyle().set("padding", "var(--aruclinic-spacing-md)")
                    .set("color", "var(--aruclinic-text-secondary)")
                    .set("text-align", "center")
                    .set("font-style", "italic");
            appointmentList.add(emptyMessage);
        } else {
            for (Appointment appt : upcomingAppts) {
                appointmentList.add(createPatientAppointmentItem(appt));
            }
        }

        section.add(header, appointmentList);
        return section;
    }

    private Component createPatientAppointmentItem(Appointment appt) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");
        item.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("width", "100%")
                .set("gap", "var(--aruclinic-spacing-md)");

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        left.setSpacing(true);

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");

        String statusType = "success";
        if (appt.getStatus() == AppointmentStatus.SCHEDULED) {
            statusType = "warning";
        } else if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            statusType = "danger";
        }
        iconDiv.addClassName(statusType);
        iconDiv.add(new Icon(VaadinIcon.CLOCK));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div timeDiv = new Div();
        timeDiv.addClassName("aruclinic-activity-title");
        String timeStr = appt.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("MMMM d, yyyy - hh:mm a"));
        timeDiv.setText(timeStr);

        Div doctorDiv = new Div();
        doctorDiv.addClassName("aruclinic-activity-description");
        String docName = appt.getDoctor() != null ? "Dr. " + appt.getDoctor().getName() : "General Practitioner";
        String specialization = (appt.getDoctor() != null && appt.getDoctor().getSpecialization() != null) ? appt.getDoctor().getSpecialization() : "Consultation";
        doctorDiv.setText(docName + " - " + specialization);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(appt.getStatus() != null ? appt.getStatus().name() : "SCHEDULED");

        content.add(timeDiv, doctorDiv, statusDiv);
        left.add(iconDiv, content);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button rescheduleBtn = new Button("Reschedule", new Icon(VaadinIcon.CALENDAR));
        rescheduleBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        rescheduleBtn.addClickListener(e -> openRescheduleDialog(appt));

        Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        cancelBtn.addClickListener(e -> openCancelDialog(appt));

        boolean active = appt.getStatus() != AppointmentStatus.COMPLETED && appt.getStatus() != AppointmentStatus.CANCELLED;
        rescheduleBtn.setEnabled(active);
        cancelBtn.setEnabled(active);

        if (active) {
            actions.add(rescheduleBtn, cancelBtn);
        } else if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            Span label = new Span("Cancelled");
            label.getStyle().set("color", "var(--aruclinic-danger)").set("font-weight", "600");
            actions.add(label);
        } else {
            Span label = new Span("Completed");
            label.getStyle().set("color", "var(--aruclinic-success)").set("font-weight", "600");
            actions.add(label);
        }

        item.add(left, actions);
        return item;
    }

    private final List<String> timeSlots = java.util.Arrays.asList(
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

    private void updateRescheduleSlots(Appointment appt, DatePicker datePicker, ComboBox<String> timeSlotCombo) {
        if (datePicker.getValue() == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();

        if (appt.getDoctor() == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        com.aruclinic.entity.Doctor doctor = doctorRepository.findById(appt.getDoctor().getId()).orElse(null);
        if (doctor == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        String spec = doctor.getSpecialization();
        final List<com.aruclinic.entity.Doctor> sameSpecialtyDocs;
        if (spec != null && !spec.trim().isEmpty()) {
            List<com.aruclinic.entity.Doctor> tempDocs = java.util.Collections.emptyList();
            try {
                tempDocs = doctorRepository.findBySpecialization(spec).stream()
                    .filter(d -> !d.getId().equals(doctor.getId()))
                    .collect(java.util.stream.Collectors.toList());
            } catch (Exception ex) {}
            sameSpecialtyDocs = tempDocs;
        } else {
            sameSpecialtyDocs = java.util.Collections.emptyList();
        }

        List<Appointment> allApptsOnDate = new java.util.ArrayList<>();
        try {
            allApptsOnDate = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().isEqual(selectedDate))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> !a.getId().equals(appt.getId()))
                .collect(java.util.stream.Collectors.toList());
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

                    for (com.aruclinic.entity.Doctor otherDoc : sameSpecialtyDocs) {
                        final com.aruclinic.entity.Doctor finalOtherDoc = otherDoc;
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
            .collect(java.util.stream.Collectors.toList());

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

    private void openRescheduleDialog(Appointment appt) {
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
        reasonField.setPlaceholder("Please specify why you are rescheduling this appointment.");
        reasonField.setRequired(true);

        // Fetch initial slots
        updateRescheduleSlots(appt, date, timeSlotCombo);

        date.addValueChangeListener(e -> updateRescheduleSlots(appt, date, timeSlotCombo));

        form.add(date, timeSlotCombo, reasonField);

        Button saveBtn = new Button("Request Reschedule", e -> {
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

            LocalTime selectedTime = parseTimeSlot(selectedSlot);
            appointmentService.patientRescheduleAppointment(appt.getId(), date.getValue(), selectedTime, reason, newDoctorId);
            Notification.show("Appointment rescheduled successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openCancelDialog(Appointment appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancel Appointment #" + appt.getId());
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextArea reasonField = new TextArea("Reason for Cancellation");
        reasonField.setPlaceholder("Please specify why you are cancelling this appointment.");
        reasonField.setRequired(true);

        form.add(reasonField);

        Button saveBtn = new Button("Cancel Appointment", e -> {
            String reason = reasonField.getValue().trim();
            if (reason.isEmpty()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Please enter a reason");
                return;
            }

            appointmentService.patientCancelAppointment(appt.getId(), reason);
            Notification.show("Appointment cancelled successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private Component createRecentPrescriptions() {
        Div section = new Div();
        section.addClassName("aruclinic-recent-activity");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Recent Prescriptions");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/prescriptions")));

        header.add(title, viewAllBtn);

        Div prescriptionList = new Div();
        prescriptionList.addClassName("aruclinic-activity-list");

        List<Prescription> recentPrescriptions = new ArrayList<>();
        if (currentPatient != null) {
            recentPrescriptions = prescriptionRepository.findByPatientId(currentPatient.getId()).stream()
                    .sorted((p1, p2) -> {
                        java.time.LocalDateTime d1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : java.time.LocalDateTime.MIN;
                        java.time.LocalDateTime d2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : java.time.LocalDateTime.MIN;
                        return d2.compareTo(d1);
                    })
                    .limit(3)
                    .collect(Collectors.toList());
        }

        if (recentPrescriptions.isEmpty()) {
            Div emptyMessage = new Div();
            emptyMessage.setText("No recent prescriptions found.");
            emptyMessage.getStyle().set("padding", "var(--aruclinic-spacing-md)")
                    .set("color", "var(--aruclinic-text-secondary)")
                    .set("text-align", "center")
                    .set("font-style", "italic");
            prescriptionList.add(emptyMessage);
        } else {
            for (Prescription p : recentPrescriptions) {
                prescriptionList.add(createPatientPrescriptionItem(p));
            }
        }

        section.add(header, prescriptionList);
        return section;
    }

    private Component createPatientPrescriptionItem(Prescription p) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");
        item.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("width", "100%")
                .set("gap", "var(--aruclinic-spacing-md)");

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        left.setSpacing(true);

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName("success");
        iconDiv.add(new Icon(VaadinIcon.FILE_TEXT));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div titleDiv = new Div();
        titleDiv.addClassName("aruclinic-activity-title");
        String medName = !p.getItems().isEmpty() ? p.getItems().get(0).getMedicineName() : "Prescription Details";
        if (p.getItems().size() > 1) {
            medName += " (" + p.getItems().size() + " items)";
        }
        titleDiv.setText(medName);

        Div descDiv = new Div();
        descDiv.addClassName("aruclinic-activity-description");
        String docName = p.getDoctor() != null ? p.getDoctor().getName() : "General Practitioner";
        String dateStr = p.getPrescriptionDate() != null ? p.getPrescriptionDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) : "Unknown Date";
        descDiv.setText("Prescribed by: Dr. " + docName + " on " + dateStr);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(p.getStatus() != null ? p.getStatus() : "ACTIVE");

        content.add(titleDiv, descDiv, statusDiv);
        left.add(iconDiv, content);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button viewBtn = new Button("View", new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prescriptions/view/" + p.getId())));

        Anchor downloadAnchor = new Anchor(new StreamResource("Prescription_" + p.getId() + ".pdf", () -> {
            String medsList = p.getItems().stream()
                    .map(it -> it.getMedicineName() + " (" + it.getDosage() + ")")
                    .collect(Collectors.joining(", "));
            return PdfHelper.generatePrescriptionPdf(
                "RX-" + p.getId(),
                currentPatient != null ? currentPatient.getFirstName() + " " + currentPatient.getLastName() : "Patient",
                p.getDoctor() != null ? p.getDoctor().getName() : "Doctor",
                p.getPrescriptionDate() != null ? p.getPrescriptionDate().toString() : "",
                p.getDiagnosis() != null ? p.getDiagnosis() : "Routine Checkup",
                medsList
            );
        }), "");
        downloadAnchor.getElement().setAttribute("download", true);
        
        Button downloadBtn = new Button("Download PDF", new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        downloadAnchor.add(downloadBtn);

        actions.add(viewBtn, downloadAnchor);
        item.add(left, actions);

        return item;
    }
}