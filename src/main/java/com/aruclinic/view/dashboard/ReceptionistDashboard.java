package com.aruclinic.view.dashboard;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Patient;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.BillingService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receptionist Dashboard view for managing patient registration, appointments, and billing with real-time data.
 */
@PageTitle("Receptionist Dashboard | AruClinic")
@Route(value = "receptionist", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
public class ReceptionistDashboard extends VerticalLayout implements com.vaadin.flow.router.BeforeEnterObserver {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    private final Div statsGrid = new Div();
    private final Div appointmentList = new Div();
    private final Div registrationList = new Div();

    public ReceptionistDashboard(PatientService patientService,
                                 AppointmentService appointmentService,
                                 BillingService billingService) {
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(com.vaadin.flow.router.BeforeEnterEvent event) {
        removeAll();
        add(createDashboardContent());
        refreshData();
    }

    private Component createDashboardContent() {
        Div content = new Div();
        content.addClassName("aruclinic-dashboard-content");
        content.setSizeFull();

        // Welcome section
        content.add(createWelcomeSection());

        // Stats grid
        statsGrid.addClassName("aruclinic-dashboard-stats");
        content.add(statsGrid);

        // Quick actions
        content.add(createQuickActions());

        // Today's appointments
        content.add(createTodaysAppointmentsSection());

        // Recent registrations
        content.add(createRecentRegistrationsSection());

        return content;
    }

    private Component createWelcomeSection() {
        Div welcomeSection = new Div();
        welcomeSection.addClassName("aruclinic-welcome-section");

        Div header = new Div();
        header.addClassName("aruclinic-welcome-header");

        H1 title = new H1("Receptionist Dashboard");
        title.addClassName("aruclinic-welcome-title");

        Span subtitle = new Span("Manage patient registration, appointments, and billing");
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button registerPatientBtn = new Button("Register Patient", new Icon(VaadinIcon.USER));
        registerPatientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerPatientBtn.addClassName("aruclinic-btn");
        registerPatientBtn.addClassName("aruclinic-btn-primary");
        registerPatientBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("receptionist/patient-registration")));

        Button bookAppointmentBtn = new Button("Book Appointment", new Icon(VaadinIcon.CALENDAR));
        bookAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bookAppointmentBtn.addClassName("aruclinic-btn");
        bookAppointmentBtn.addClassName("aruclinic-btn-secondary");
        bookAppointmentBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("receptionist/appointments/add")));

        actions.add(registerPatientBtn, bookAppointmentBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
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
            "Patient Registration",
            "Register new patients",
            "receptionist/patient-registration",
            VaadinIcon.USER
        ));

        quickActions.add(createQuickAction(
            "Book Appointment",
            "Schedule new appointments",
            "receptionist/appointments/add",
            VaadinIcon.CALENDAR
        ));

        quickActions.add(createQuickAction(
            "Check In",
            "Check in arriving patients",
            "receptionist/checkin",
            VaadinIcon.CHECK
        ));

        quickActions.add(createQuickAction(
            "Billing",
            "Process payments and invoices",
            "receptionist/billing",
            VaadinIcon.MONEY
        ));

        quickActions.add(createQuickAction(
            "Reports",
            "Generate daily reports",
            "receptionist/reports",
            VaadinIcon.CHART
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

    private Component createTodaysAppointmentsSection() {
        Div section = new Div();
        section.addClassName("aruclinic-recent-activity");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Today's Appointments");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("receptionist/appointments")));

        header.add(title, viewAllBtn);

        appointmentList.addClassName("aruclinic-activity-list");

        section.add(header, appointmentList);
        return section;
    }

    private Component createAppointmentItem(String time, String patient, String doctor, String status, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(VaadinIcon.CLOCK));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div timeDiv = new Div();
        timeDiv.addClassName("aruclinic-activity-title");
        timeDiv.setText(time + " - " + patient);

        Div doctorDiv = new Div();
        doctorDiv.addClassName("aruclinic-activity-description");
        doctorDiv.setText("Doctor: " + doctor);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(status);

        content.add(timeDiv, doctorDiv, statusDiv);
        item.add(iconDiv, content);

        return item;
    }

    private Component createRecentRegistrationsSection() {
        Div section = new Div();
        section.addClassName("aruclinic-recent-activity");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Recent Patient Registrations");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("receptionist/patients")));

        header.add(title, viewAllBtn);

        registrationList.addClassName("aruclinic-activity-list");

        section.add(header, registrationList);
        return section;
    }

    private Component createRegistrationItem(String name, String email, String mobile, String time, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(VaadinIcon.USER));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div nameDiv = new Div();
        nameDiv.addClassName("aruclinic-activity-title");
        nameDiv.setText(name);

        Div emailDiv = new Div();
        emailDiv.addClassName("aruclinic-activity-description");
        emailDiv.setText(email + " | " + mobile);

        Div timeDiv = new Div();
        timeDiv.addClassName("aruclinic-activity-time");
        timeDiv.setText(time);

        content.add(nameDiv, emailDiv, timeDiv);
        item.add(iconDiv, content);

        return item;
    }

    private void refreshData() {
        statsGrid.removeAll();
        appointmentList.removeAll();
        registrationList.removeAll();

        // 1. Stats from DB
        long totalPatients = patientService.getAllPatientEntities().size();
        
        long todayRegistrations = patientService.getAllPatientEntities().stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().isEqual(LocalDate.now()))
                .count();

        long todayAppointments = appointmentService.findAll().stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().isEqual(LocalDate.now()))
                .count();

        long pendingPayments = billingService.getAllBillEntities().stream()
                .filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                .count();

        statsGrid.add(createStatCard("Total Patients", String.valueOf(totalPatients), "+" + totalPatients, "primary", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Today's Registrations", String.valueOf(todayRegistrations), "+" + todayRegistrations, "success", VaadinIcon.USER));
        statsGrid.add(createStatCard("Today's Appointments", String.valueOf(todayAppointments), "+" + todayAppointments, "warning", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Pending Payments", String.valueOf(pendingPayments), "+" + pendingPayments, "danger", VaadinIcon.MONEY));

        // 2. Today's Appointments from DB
        List<Appointment> todayAppts = appointmentService.findAll().stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().isEqual(LocalDate.now()))
                .sorted((a1, a2) -> {
                    if (a1.getAppointmentTime() != null && a2.getAppointmentTime() != null) {
                        return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
                    }
                    return 0;
                })
                .limit(5)
                .collect(Collectors.toList());

        if (todayAppts.isEmpty()) {
            Div empty = new Div();
            empty.setText("No appointments scheduled for today.");
            empty.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-style", "italic").set("padding", "var(--aruclinic-spacing-md)");
            appointmentList.add(empty);
        } else {
            for (Appointment a : todayAppts) {
                String timeStr = a.getAppointmentTime() != null ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";
                String patientName = a.getPatient() != null ? (a.getPatient().getFirstName() + " " + a.getPatient().getLastName()) : "N/A";
                String doctorName = a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A";
                String statusStr = a.getStatus() != null ? a.getStatus().name() : "SCHEDULED";
                
                String type = "primary";
                if (AppointmentStatus.COMPLETED.equals(a.getStatus())) type = "success";
                else if (AppointmentStatus.CANCELLED.equals(a.getStatus())) type = "danger";
                else if (AppointmentStatus.SCHEDULED.equals(a.getStatus())) type = "warning";

                appointmentList.add(createAppointmentItem(timeStr, patientName, doctorName, statusStr, type));
            }
        }

        // 3. Recent Patient Registrations from DB
        List<Patient> recentPatients = patientService.getAllPatientEntities().stream()
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) {
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt()); // newest first
                    }
                    return p2.getId().compareTo(p1.getId());
                })
                .limit(5)
                .collect(Collectors.toList());

        if (recentPatients.isEmpty()) {
            Div empty = new Div();
            empty.setText("No patients registered yet.");
            empty.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-style", "italic").set("padding", "var(--aruclinic-spacing-md)");
            registrationList.add(empty);
        } else {
            for (Patient p : recentPatients) {
                String name = p.getFirstName() + " " + p.getLastName();
                String emailStr = p.getEmail() != null ? p.getEmail() : "N/A";
                String mobileStr = p.getMobileNumber() != null ? p.getMobileNumber() : "N/A";
                
                String timeAgo = "Just registered";
                if (p.getCreatedAt() != null) {
                    java.time.Duration duration = java.time.Duration.between(p.getCreatedAt(), LocalDateTime.now());
                    long mins = duration.toMinutes();
                    if (mins < 1) {
                        timeAgo = "Just now";
                    } else if (mins < 60) {
                        timeAgo = mins + " min ago";
                    } else {
                        long hours = duration.toHours();
                        if (hours < 24) {
                            timeAgo = hours + " hours ago";
                        } else {
                            timeAgo = p.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                        }
                    }
                }
                
                registrationList.add(createRegistrationItem(name, emailStr, mobileStr, timeAgo, "success"));
            }
        }
    }
}
