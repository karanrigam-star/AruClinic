package com.aruclinic.frontend.views.dashboard;

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

@PageTitle("Patient Dashboard | AruClinic")
@Route("patient")
@CssImport("./themes/aruclinic/dashboard.css")
public class PatientDashboard extends VerticalLayout {

    public PatientDashboard() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createDashboardContent());
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

        H1 title = new H1("Patient Dashboard");
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

        statsGrid.add(createStatCard("Upcoming Appointments", "3", "1", "primary", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Active Prescriptions", "5", "0", "success", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Medical Records", "12", "2", "warning", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Pending Payments", "1", "0", "danger", VaadinIcon.MONEY));

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

        appointmentList.add(createAppointmentItem(
            "Tomorrow, 09:00 AM",
            "Dr. Smith",
            "Follow-up consultation",
            "Confirmed",
            "success"
        ));

        appointmentList.add(createAppointmentItem(
            "June 15, 2025 - 02:00 PM",
            "Dr. Johnson",
            "Annual checkup",
            "Confirmed",
            "success"
        ));

        appointmentList.add(createAppointmentItem(
            "June 20, 2025 - 10:30 AM",
            "Dr. Williams",
            "Blood test results",
            "Pending",
            "warning"
        ));

        section.add(header, appointmentList);
        return section;
    }

    private Component createAppointmentItem(String dateTime, String doctor, String reason, String status, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(VaadinIcon.CLOCK));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div dateDiv = new Div();
        dateDiv.addClassName("aruclinic-activity-title");
        dateDiv.setText(dateTime);

        Div doctorDiv = new Div();
        doctorDiv.addClassName("aruclinic-activity-description");
        doctorDiv.setText(doctor + " - " + reason);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(status);

        content.add(dateDiv, doctorDiv, statusDiv);
        item.add(iconDiv, content);

        return item;
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

        prescriptionList.add(createPrescriptionItem(
            "Amoxicillin",
            "Dr. Smith",
            "June 1, 2025",
            "Active",
            "success"
        ));

        prescriptionList.add(createPrescriptionItem(
            "Lisinopril",
            "Dr. Johnson",
            "May 15, 2025",
            "Active",
            "success"
        ));

        prescriptionList.add(createPrescriptionItem(
            "Ibuprofen",
            "Dr. Smith",
            "April 20, 2025",
            "Completed",
            "primary"
        ));

        section.add(header, prescriptionList);
        return section;
    }

    private Component createPrescriptionItem(String medication, String doctor, String date, String status, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(VaadinIcon.FILE_TEXT));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div medDiv = new Div();
        medDiv.addClassName("aruclinic-activity-title");
        medDiv.setText(medication);

        Div doctorDiv = new Div();
        doctorDiv.addClassName("aruclinic-activity-description");
        doctorDiv.setText("Prescribed by: " + doctor + " on " + date);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(status);

        content.add(medDiv, doctorDiv, statusDiv);
        item.add(iconDiv, content);

        return item;
    }
}