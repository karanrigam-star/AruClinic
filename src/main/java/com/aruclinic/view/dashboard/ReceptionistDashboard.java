package com.aruclinic.view.dashboard;

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
import com.aruclinic.view.MainLayout;

/**
 * Receptionist Dashboard view for managing patient registration and appointments.
 */
@PageTitle("Receptionist Dashboard | AruClinic")
@Route(value = "receptionist", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
public class ReceptionistDashboard extends VerticalLayout {

    public ReceptionistDashboard() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createDashboardContent());
    }

    private Component createDashboardContent() {
        Div content = new Div();
        content.addClassName("aruclinic-dashboard-content");
        content.setSizeFull();

        // Welcome section
        content.add(createWelcomeSection());

        // Stats grid
        content.add(createStatsGrid());

        // Quick actions
        content.add(createQuickActions());

        // Today's appointments
        content.add(createTodaysAppointments());

        // Recent registrations
        content.add(createRecentRegistrations());

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

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");

        // Total Patients
        statsGrid.add(createStatCard("Total Patients", "245", "12", "primary", VaadinIcon.USERS));

        // Today's Registrations
        statsGrid.add(createStatCard("Today's Registrations", "8", "3", "success", VaadinIcon.USER));

        // Today's Appointments
        statsGrid.add(createStatCard("Today's Appointments", "24", "5", "warning", VaadinIcon.CLOCK));

        // Pending Payments
        statsGrid.add(createStatCard("Pending Payments", "15", "2", "danger", VaadinIcon.MONEY));

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

    private Component createTodaysAppointments() {
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

        Div appointmentList = new Div();
        appointmentList.addClassName("aruclinic-activity-list");

        // Sample appointments
        appointmentList.add(createAppointmentItem(
            "09:00 AM",
            "John Doe",
            "Dr. Smith",
            "Confirmed",
            "success"
        ));

        appointmentList.add(createAppointmentItem(
            "10:30 AM",
            "Jane Smith",
            "Dr. Johnson",
            "Checked In",
            "primary"
        ));

        appointmentList.add(createAppointmentItem(
            "02:00 PM",
            "Robert Johnson",
            "Dr. Williams",
            "Pending",
            "warning"
        ));

        appointmentList.add(createAppointmentItem(
            "04:00 PM",
            "Alice Brown",
            "Dr. Smith",
            "Confirmed",
            "success"
        ));

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

    private Component createRecentRegistrations() {
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

        Div registrationList = new Div();
        registrationList.addClassName("aruclinic-activity-list");

        // Sample registrations
        registrationList.add(createRegistrationItem(
            "John Doe",
            "john.doe@email.com",
            "1234567890",
            "5 min ago",
            "primary"
        ));

        registrationList.add(createRegistrationItem(
            "Jane Smith",
            "jane.smith@email.com",
            "0987654321",
            "15 min ago",
            "success"
        ));

        registrationList.add(createRegistrationItem(
            "Robert Johnson",
            "robert.j@email.com",
            "1122334455",
            "1 hour ago",
            "warning"
        ));

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
}
