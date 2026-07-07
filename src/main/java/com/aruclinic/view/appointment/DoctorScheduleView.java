package com.aruclinic.view.appointment;

import com.aruclinic.dto.DoctorDto;
import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.Doctor;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.service.DoctorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.repository.PatientRepository;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Doctor Schedule view for displaying and managing doctor availability from the database.
 */
@PageTitle("Doctor Schedule | AruClinic")
@Route(value = "doctor/schedule", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
public class DoctorScheduleView extends VerticalLayout implements BeforeEnterObserver {

    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    private final ComboBox<DoctorDto> doctorSelector = new ComboBox<>("Select Doctor");
    private DoctorDto selectedDoctor = null;
    private Doctor currentDoctor = null;
    private final Div gridContainer = new Div();

    private String currentView = "current_week"; // current_week, past_month
    private final Button toggleViewBtn = new Button("View Past 1 Month Calendar", new Icon(VaadinIcon.CALENDAR_CLOCK));
    private final Div dateHeaderContainer = new Div();

    public DoctorScheduleView(DoctorService doctorService,
                              AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.doctorService = doctorService;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        initializeDateHeader();
        add(createScheduleContent());
    }

    private void initializeDateHeader() {
        dateHeaderContainer.addClassName("aruclinic-current-date-card");
        dateHeaderContainer.getStyle()
                .set("background", "var(--aruclinic-card-bg, #ffffff)")
                .set("border", "1px solid var(--aruclinic-border, #e2e8f0)")
                .set("border-radius", "var(--aruclinic-radius-md, 8px)")
                .set("padding", "var(--aruclinic-spacing-md, 16px)")
                .set("margin-bottom", "var(--aruclinic-spacing-md, 16px)")
                .set("box-shadow", "var(--aruclinic-shadow-sm, 0 1px 3px rgba(0,0,0,0.05))")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("width", "100%");

        Div dateInfo = new Div();
        Span todayLabel = new Span("TODAY'S DATE");
        todayLabel.getStyle()
                .set("font-size", "var(--aruclinic-font-size-xs, 12px)")
                .set("font-weight", "700")
                .set("color", "var(--aruclinic-primary, #0062ff)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "1px")
                .set("display", "block")
                .set("margin-bottom", "4px");
        H2 dateText = new H2(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateText.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--aruclinic-font-size-xl, 20px)")
                .set("color", "var(--aruclinic-text-primary, #111827)");
        dateInfo.add(todayLabel, dateText);

        toggleViewBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleViewBtn.getStyle().set("cursor", "pointer");
        toggleViewBtn.addClickListener(e -> toggleView());

        dateHeaderContainer.add(dateInfo, toggleViewBtn);
    }

    private void toggleView() {
        if ("current_week".equals(currentView)) {
            currentView = "past_month";
            toggleViewBtn.setText("View Current Week Schedule");
            toggleViewBtn.setIcon(new Icon(VaadinIcon.CALENDAR));
            toggleViewBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        } else {
            currentView = "current_week";
            toggleViewBtn.setText("View Past 1 Month Calendar");
            toggleViewBtn.setIcon(new Icon(VaadinIcon.CALENDAR_CLOCK));
            toggleViewBtn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            toggleViewBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        refreshSchedule();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolveCurrentDoctor();
        configureComponents();
        refreshSchedule();
    }

    private void resolveCurrentDoctor() {
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
                currentDoctor = doctorRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void configureComponents() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        doctorSelector.setItems(doctors);
        doctorSelector.setItemLabelGenerator(d -> "Dr. " + d.getFirstName() + " " + d.getLastName());
        doctorSelector.setWidth("280px");
        doctorSelector.addValueChangeListener(e -> {
            selectedDoctor = e.getValue();
            refreshSchedule();
        });

        // Set default selection (prioritize logged-in doctor, otherwise first doctor in DB)
        if (selectedDoctor == null) {
            if (currentDoctor != null) {
                DoctorDto currentDto = doctors.stream()
                        .filter(d -> d.getId().equals(currentDoctor.getId()))
                        .findFirst()
                        .orElse(null);
                if (currentDto != null) {
                    selectedDoctor = currentDto;
                    doctorSelector.setValue(currentDto);
                }
            }
            if (selectedDoctor == null && !doctors.isEmpty()) {
                selectedDoctor = doctors.get(0);
                doctorSelector.setValue(selectedDoctor);
            }
        }

        // Only let non-doctors (e.g. receptionist/admin) change the selected doctor view
        if (currentDoctor != null) {
            doctorSelector.setVisible(false);
        } else {
            doctorSelector.setVisible(true);
        }
    }

    private Component createScheduleContent() {
        Div content = new Div();
        content.addClassName("aruclinic-doctor-schedule");
        content.setWidthFull();

        // Display current date at first of UI
        content.add(dateHeaderContainer);

        // Header
        content.add(createScheduleHeader());

        // Grid container with locked 3-column layout on desktop viewports
        gridContainer.addClassName("aruclinic-schedule-grid");
        gridContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "var(--aruclinic-spacing-lg)");

        content.add(gridContainer);
        return content;
    }

    private Component createScheduleHeader() {
        Div header = new Div();
        header.addClassName("aruclinic-schedule-header");

        Div leftSection = new Div();
        H1 title = new H1("Doctor Schedule");
        title.addClassName("aruclinic-schedule-title");
        leftSection.add(title);

        Div rightSection = new Div();
        rightSection.addClassName("aruclinic-schedule-doctor-selector");
        doctorSelector.addClassName("aruclinic-schedule-doctor-select");
        rightSection.add(doctorSelector);

        header.add(leftSection, rightSection);
        return header;
    }

    private void populateScheduleGrid() {
        gridContainer.removeAll();

        if (selectedDoctor == null) {
            return;
        }

        // Fetch appointments for selected doctor directly from DB
        List<Appointment> appointments = appointmentRepository.findByDoctorId(selectedDoctor.getId());

        if ("current_week".equals(currentView)) {
            // Display current week starting from today (remove past dates)
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.plusDays(i);
                gridContainer.add(createDayCard(date, appointments));
            }
        } else {
            // Display past 1 month calendar (30 days ago to yesterday)
            LocalDate today = LocalDate.now();
            for (int i = 30; i >= 1; i--) {
                LocalDate date = today.minusDays(i);
                gridContainer.add(createPastDayCard(date, appointments));
            }
        }
    }

    private Component createPastDayCard(LocalDate date, List<Appointment> appointments) {
        Div dayCard = new Div();
        dayCard.addClassName("aruclinic-schedule-day-card");
        dayCard.getStyle().set("background-color", "rgba(240, 244, 248, 0.45)");

        Div header = new Div();
        header.addClassName("aruclinic-schedule-day-header");

        Span dayName = new Span(date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()));
        dayName.addClassName("aruclinic-schedule-day-name");

        Span dayDate = new Span(date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) + " " + date.getDayOfMonth());
        dayDate.addClassName("aruclinic-schedule-day-date");

        header.add(dayName, dayDate);

        Div slots = new Div();
        slots.addClassName("aruclinic-schedule-slots");

        // Filter active appointments for this past date
        List<Appointment> dayAppts = new java.util.ArrayList<>();
        for (Appointment appt : appointments) {
            if (appt.getAppointmentDate() != null && appt.getAppointmentDate().isEqual(date)) {
                if (appt.getStatus() != com.aruclinic.entity.AppointmentStatus.CANCELLED) {
                    dayAppts.add(appt);
                }
            }
        }

        if (dayAppts.isEmpty()) {
            Span noBooking = new Span("No bookings");
            noBooking.getStyle()
                    .set("color", "var(--aruclinic-text-muted)")
                    .set("font-size", "var(--aruclinic-font-size-sm)")
                    .set("font-style", "italic")
                    .set("padding", "var(--aruclinic-spacing-md)")
                    .set("display", "block")
                    .set("text-align", "center");
            slots.add(noBooking);
        } else {
            for (Appointment appt : dayAppts) {
                Div slot = new Div();
                slot.addClassName("aruclinic-schedule-slot");
                slot.addClassName("booked");

                String time = appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                Span timeSpan = new Span(time);
                timeSpan.addClassName("aruclinic-schedule-slot-time");

                Span statusSpan = new Span();
                statusSpan.addClassName("aruclinic-schedule-slot-status");
                statusSpan.addClassName("status");
                statusSpan.addClassName("booked");
                if (appt.getPatient() != null) {
                    statusSpan.setText("Booked - " + appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName());
                } else {
                    statusSpan.setText("Booked");
                }

                slot.add(timeSpan, statusSpan);
                slots.add(slot);
            }
        }

        dayCard.add(header, slots);
        return dayCard;
    }

    private Component createDayCard(LocalDate date, List<Appointment> appointments) {
        Div dayCard = new Div();
        dayCard.addClassName("aruclinic-schedule-day-card");

        Div header = new Div();
        header.addClassName("aruclinic-schedule-day-header");

        Span dayName = new Span(date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()));
        dayName.addClassName("aruclinic-schedule-day-name");

        Span dayDate = new Span(date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) + " " + date.getDayOfMonth());
        dayDate.addClassName("aruclinic-schedule-day-date");

        header.add(dayName, dayDate);

        Div slots = new Div();
        slots.addClassName("aruclinic-schedule-slots");

        // Add time slots for this day
        List<String> timeSlots = getTimeSlotsForDay(date);
        for (String slot : timeSlots) {
            slots.add(createTimeSlot(slot, date, appointments));
        }

        dayCard.add(header, slots);
        return dayCard;
    }

    private Component createTimeSlot(String time, LocalDate date, List<Appointment> appointments) {
        Div slot = new Div();
        slot.addClassName("aruclinic-schedule-slot");

        Span timeSpan = new Span(time);
        timeSpan.addClassName("aruclinic-schedule-slot-time");

        Span statusSpan = new Span();
        statusSpan.addClassName("aruclinic-schedule-slot-status");

        // Determine status based on the slot & database appointments
        String status = getSlotStatus(time, date, appointments);
        statusSpan.addClassName("status");
        statusSpan.addClassName(status.toLowerCase());

        if (status.equals("Booked")) {
            slot.addClassName("booked");
            Appointment appt = findMatchingAppointment(time, date, appointments);
            if (appt != null && appt.getPatient() != null) {
                statusSpan.setText("Booked - " + appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName());
            } else {
                statusSpan.setText("Booked");
            }
        } else {
            statusSpan.setText(status);
        }

        slot.add(timeSpan, statusSpan);

        // Make slot clickable if available
        if (status.equals("Available")) {
            slot.addClickListener(e -> bookSlot(time, date));
        }

        return slot;
    }

    private List<String> getTimeSlotsForDay(LocalDate date) {
        return Arrays.asList(
            "09:00 AM",
            "09:30 AM",
            "10:00 AM",
            "10:30 AM",
            "11:00 AM",
            "11:30 AM",
            "02:00 PM",
            "02:30 PM",
            "03:00 PM",
            "03:30 PM",
            "04:00 PM",
            "04:30 PM"
        );
    }

    private Appointment findMatchingAppointment(String timeStr, LocalDate date, List<Appointment> appointments) {
        try {
            DateTimeFormatter slotFormatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
            java.time.LocalTime slotTime = java.time.LocalTime.parse(timeStr, slotFormatter);

            Appointment bestMatch = null;
            for (Appointment appt : appointments) {
                if (appt.getAppointmentDate() != null && appt.getAppointmentTime() != null) {
                    if (appt.getAppointmentDate().isEqual(date) && 
                        appt.getAppointmentTime().getHour() == slotTime.getHour() && 
                        appt.getAppointmentTime().getMinute() == slotTime.getMinute()) {
                        if (appt.getStatus() != com.aruclinic.entity.AppointmentStatus.CANCELLED) {
                            return appt; // Prioritize active/completed appointments
                        }
                        bestMatch = appt; // Keep cancelled one as fallback
                    }
                }
            }
            return bestMatch;
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String getSlotStatus(String timeStr, LocalDate date, List<Appointment> appointments) {
        Appointment appt = findMatchingAppointment(timeStr, date, appointments);
        if (appt != null && appt.getStatus() != com.aruclinic.entity.AppointmentStatus.CANCELLED) {
            return "Booked";
        }

        // If date is in the past, mark as unavailable
        if (date.isBefore(LocalDate.now())) {
            return "Unavailable";
        }

        // If date is today, check if time slot has already passed
        if (date.isEqual(LocalDate.now())) {
            try {
                DateTimeFormatter slotFormatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
                java.time.LocalTime slotTime = java.time.LocalTime.parse(timeStr, slotFormatter);
                if (java.time.LocalTime.now().isAfter(slotTime)) {
                    return "Unavailable";
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        return "Available";
    }

    private void bookSlot(String time, LocalDate date) {
        if (selectedDoctor == null) {
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Book Appointment - " + date.toString() + " at " + time);
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        ComboBox<Patient> patientSelect = new ComboBox<>("Select Patient");
        patientSelect.setItems(patientRepository.findAll());
        patientSelect.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")");
        patientSelect.setRequired(true);
        patientSelect.setWidthFull();

        form.add(patientSelect);

        Button confirmBtn = new Button("Confirm Booking", new Icon(VaadinIcon.CHECK));
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmBtn.addClickListener(e -> {
            Patient patient = patientSelect.getValue();
            if (patient == null) {
                patientSelect.setInvalid(true);
                patientSelect.setErrorMessage("Please select a patient");
                return;
            }

            try {
                DateTimeFormatter slotFormatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
                java.time.LocalTime slotTime = java.time.LocalTime.parse(time, slotFormatter);

                Doctor doc = doctorRepository.findById(selectedDoctor.getId()).orElse(null);
                if (doc == null) {
                    Notification.show("Error: Doctor not found", 3000, Notification.Position.TOP_CENTER);
                    dialog.close();
                    return;
                }

                Appointment appt = new Appointment();
                appt.setPatient(patient);
                appt.setDoctor(doc);
                appt.setAppointmentDate(date);
                appt.setAppointmentTime(slotTime);
                appt.setStatus(AppointmentStatus.SCHEDULED);
                
                appointmentRepository.save(appt);

                Notification.show("Appointment booked successfully!", 2000, Notification.Position.TOP_CENTER);
                dialog.close();
                refreshSchedule();
            } catch (Exception ex) {
                Notification.show("Error booking appointment: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.add(form);
        dialog.open();
    }

    private void refreshSchedule() {
        populateScheduleGrid();
    }
}
