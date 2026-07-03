package com.aruclinic.frontend.views.dashboard;

import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.UserService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Admin Dashboard | AruClinic")
@Route("admin")
@CssImport("./themes/aruclinic/dashboard.css")
public class AdminDashboard extends VerticalLayout {

    private final UserService userService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public AdminDashboard(UserService userService, PatientService patientService, AppointmentService appointmentService) {
        this.userService = userService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;

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
        content.add(createRecentActivitySection());

        return content;
    }

    private Component createWelcomeSection() {
        Div welcomeSection = new Div();
        welcomeSection.addClassName("aruclinic-welcome-section");

        Div header = new Div();
        header.addClassName("aruclinic-welcome-header");

        H1 title = new H1("Admin Dashboard");
        title.addClassName("aruclinic-welcome-title");

        Span subtitle = new Span("Manage users, patients, appointments, and system settings");
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button addUserBtn = new Button("Add User", new Icon(VaadinIcon.USER));
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserBtn.addClassName("aruclinic-btn");
        addUserBtn.addClassName("aruclinic-btn-primary");
        addUserBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/users/add")));

        Button settingsBtn = new Button("Settings", new Icon(VaadinIcon.COGS));
        settingsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        settingsBtn.addClassName("aruclinic-btn");
        settingsBtn.addClassName("aruclinic-btn-secondary");
        settingsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/settings")));

        actions.add(addUserBtn, settingsBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
    }

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");

        statsGrid.add(createStatCard("Total Users", "125", "12%", "primary", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Active Patients", "89", "8%", "success", VaadinIcon.HOSPITAL));
        statsGrid.add(createStatCard("Today's Appointments", "24", "5%", "warning", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Total Revenue", "$12,540", "15%", "primary", VaadinIcon.MONEY));

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

        Div registrationSection = new Div();
        registrationSection.addClassName("dashboard-section");
        H2 registrationTitle = new H2("User Activity");
        registrationSection.add(registrationTitle);

        Div statsContainer = new Div();
        statsContainer.addClassName("dashboard-stats");

        Div regStat = new Div();
        regStat.addClassName("summary-item");
        regStat.add(new Span("1,243 Registrations"));

        Div actStat = new Div();
        actStat.addClassName("summary-item");
        actStat.add(new Span("892 Appointments"));

        statsContainer.add(regStat, actStat);
        registrationSection.add(registrationTitle, statsContainer);

        chartsSection.add(registrationSection);

        return chartsSection;
    }

    private void refreshCharts() {
        Notification.show("Dashboard data refreshed");
    }

    private Component createRecentActivitySection() {
        Div activitySection = new Div();
        activitySection.addClassName("aruclinic-recent-activity");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Recent Activity");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/audit")));

        header.add(title, viewAllBtn);

        Div activityList = new Div();
        activityList.addClassName("aruclinic-activity-list");

        activityList.add(createActivityItem(
            "New user registered",
            "admin@example.com created a new account",
            "5 min ago",
            "primary"
        ));

        activityList.add(createActivityItem(
            "Appointment booked",
            "Patient John Doe booked an appointment with Dr. Smith",
            "15 min ago",
            "success"
        ));

        activityList.add(createActivityItem(
            "Payment received",
            "Payment of $150 received from Jane Doe",
            "1 hour ago",
            "success"
        ));

        activityList.add(createActivityItem(
            "Prescription created",
            "Dr. Smith created a new prescription for John Doe",
            "2 hours ago",
            "warning"
        ));

        activitySection.add(header, activityList);
        return activitySection;
    }

    private Component createActivityItem(String title, String description, String time, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(VaadinIcon.CLOCK));

        Div content = new Div();
        content.addClassName("aruclinic-activity-content");

        Div titleDiv = new Div();
        titleDiv.addClassName("aruclinic-activity-title");
        titleDiv.setText(title);

        Div descDiv = new Div();
        descDiv.addClassName("aruclinic-activity-description");
        descDiv.setText(description);

        Div timeDiv = new Div();
        timeDiv.addClassName("aruclinic-activity-time");
        timeDiv.setText(time);

        content.add(titleDiv, descDiv, timeDiv);
        item.add(iconDiv, content);

        return item;
    }
}