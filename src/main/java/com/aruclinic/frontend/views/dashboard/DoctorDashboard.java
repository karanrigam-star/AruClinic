package com.aruclinic.frontend.views.dashboard;

import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.PrescriptionService;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Doctor Dashboard | AruClinic")
@Route("doctor")
@CssImport("./themes/aruclinic/dashboard.css")
public class DoctorDashboard extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;

    public DoctorDashboard(AppointmentService appointmentService, PrescriptionService prescriptionService) {
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;

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
        content.add(createChartsSection());
        content.add(createQuickActions());
        content.add(createTodaysAppointments());

        return content;
    }

    private Component createWelcomeSection() {
        Div welcomeSection = new Div();
        welcomeSection.addClassName("aruclinic-welcome-section");

        Div header = new Div();
        header.addClassName("aruclinic-welcome-header");

        H1 title = new H1("Doctor Dashboard");
        title.addClassName("aruclinic-welcome-title");

        Span subtitle = new Span("Manage your appointments, patients, and prescriptions");
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button newAppointmentBtn = new Button("New Appointment", new Icon(VaadinIcon.CALENDAR));
        newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newAppointmentBtn.addClassName("aruclinic-btn");
        newAppointmentBtn.addClassName("aruclinic-btn-primary");
        newAppointmentBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/appointments/add")));

        Button viewScheduleBtn = new Button("My Schedule", new Icon(VaadinIcon.CALENDAR));
        viewScheduleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewScheduleBtn.addClassName("aruclinic-btn");
        viewScheduleBtn.addClassName("aruclinic-btn-secondary");
        viewScheduleBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/schedule")));

        actions.add(newAppointmentBtn, viewScheduleBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
    }

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");

        statsGrid.add(createStatCard("Today's Appointments", "12", "3", "primary", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Total Patients", "89", "5", "success", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Prescriptions", "45", "8", "warning", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Earnings (Today)", "$450", "20%", "success", VaadinIcon.MONEY));

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

    private Component createChartsSection() {
        Div chartsSection = new Div();
        chartsSection.addClassName("aruclinic-dashboard-charts");

        Div summarySection = new Div();
        summarySection.addClassName("dashboard-summary");

        H2 summaryTitle = new H2("Appointment Summary");
        summarySection.add(summaryTitle);

        Div statsContainer = new Div();
        statsContainer.addClassName("dashboard-stats");

        Div statusStat = new Div();
        statusStat.addClassName("summary-item");
        statusStat.add(new Span("Status: Confirmed (65%), Pending (20%), Completed (10%), Cancelled (5%)"));

        Div weeklyStat = new Div();
        weeklyStat.addClassName("summary-item");
        weeklyStat.add(new Span("Weekly Appointments: Mon(8), Tue(12), Wed(10), Thu(15), Fri(18)"));

        statsContainer.add(statusStat, weeklyStat);
        summarySection.add(summaryTitle, statsContainer);

        chartsSection.add(summarySection);

        return chartsSection;
    }

    private Component createQuickActions() {
        Div quickActions = new Div();
        quickActions.addClassName("aruclinic-quick-actions");

        quickActions.add(createQuickAction(
            "View Patients",
            "Browse all your patients",
            "doctor/patients",
            VaadinIcon.USERS
        ));

        quickActions.add(createQuickAction(
            "My Appointments",
            "View and manage appointments",
            "doctor/appointments",
            VaadinIcon.CLOCK
        ));

        quickActions.add(createQuickAction(
            "Create Prescription",
            "Write new prescriptions",
            "doctor/prescriptions/add",
            VaadinIcon.FILE_TEXT
        ));

        quickActions.add(createQuickAction(
            "Medical Records",
            "Access patient records",
            "doctor/medical-records",
            VaadinIcon.FILE_TEXT
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
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/appointments")));

        header.add(title, viewAllBtn);

        Div appointmentList = new Div();
        appointmentList.addClassName("aruclinic-activity-list");

        appointmentList.add(createAppointmentItem(
            "09:00 AM",
            "John Doe",
            "Follow-up consultation",
            "Confirmed",
            "success"
        ));

        appointmentList.add(createAppointmentItem(
            "10:30 AM",
            "Jane Smith",
            "Annual checkup",
            "Confirmed",
            "success"
        ));

        appointmentList.add(createAppointmentItem(
            "02:00 PM",
            "Robert Johnson",
            "New patient visit",
            "Pending",
            "warning"
        ));

        appointmentList.add(createAppointmentItem(
            "04:00 PM",
            "Alice Brown",
            "Blood test results",
            "Confirmed",
            "success"
        ));

        section.add(header, appointmentList);
        return section;
    }

    private Component createAppointmentItem(String time, String patient, String reason, String status, String type) {
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
        timeDiv.setText(time);

        Div patientDiv = new Div();
        patientDiv.addClassName("aruclinic-activity-description");
        patientDiv.setText(patient + " - " + reason);

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(status);

        content.add(timeDiv, patientDiv, statusDiv);
        item.add(iconDiv, content);

        return item;
    }
}