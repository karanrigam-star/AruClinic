package com.aruclinic.view.dashboard;

import com.aruclinic.entity.*;
import com.aruclinic.service.AdminService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.time.LocalDate;

@PageTitle("Admin Dashboard | AruClinic")
@Route(value = "admin", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
public class AdminDashboard extends VerticalLayout {

    private final AdminService adminService;
    private final com.aruclinic.service.NotificationService notificationService;
    private Div dashboardContentDiv;
    private Component statsGrid;
    private Component recentActivitySection;

    public AdminDashboard(AdminService adminService, com.aruclinic.service.NotificationService notificationService) {
        this.adminService = adminService;
        this.notificationService = notificationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createDashboardContent());
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Enable polling every 3 seconds for real-time updates
        attachEvent.getUI().setPollInterval(3000);
        
        // Add a poll listener to refresh stats, activity logs, and display new notifications
        attachEvent.getUI().addPollListener(e -> {
            refreshStats();
        });
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        // Disable polling when leaving the page to save resources
        detachEvent.getUI().setPollInterval(-1);
        super.onDetach(detachEvent);
    }

    private void refreshStats() {
        if (dashboardContentDiv == null) return;
        
        // Rebuild and replace stats grid
        Component newStatsGrid = createStatsGrid();
        dashboardContentDiv.replace(statsGrid, newStatsGrid);
        statsGrid = newStatsGrid;
        
        // Rebuild and replace recent activity section
        Component newRecentActivity = createRecentActivitySection();
        dashboardContentDiv.replace(recentActivitySection, newRecentActivity);
        recentActivitySection = newRecentActivity;

        // Check for new real-time payment/system notifications for current admin
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = adminService.getUserByEmail(auth.getName());
                if (user != null) {
                    java.util.List<com.aruclinic.entity.Notification> unread = notificationService.findByUserId(user.getId()).stream()
                            .filter(n -> !n.isRead())
                            .toList();
                    if (!unread.isEmpty()) {
                        for (com.aruclinic.entity.Notification n : unread) {
                            Notification.show(n.getTitle() + ": " + n.getMessage(), 5000, Notification.Position.TOP_CENTER);
                            notificationService.markAsRead(n.getId());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Ignore context/concurrency exceptions
        }
    }

    private Component createDashboardContent() {
        dashboardContentDiv = new Div();
        dashboardContentDiv.addClassName("aruclinic-dashboard-content");
        dashboardContentDiv.setSizeFull();

        dashboardContentDiv.add(createWelcomeSection());
        dashboardContentDiv.add(createQuickActionsBar());
        
        statsGrid = createStatsGrid();
        dashboardContentDiv.add(statsGrid);
        
        dashboardContentDiv.add(createChartsSection());
        
        recentActivitySection = createRecentActivitySection();
        dashboardContentDiv.add(recentActivitySection);

        return dashboardContentDiv;
    }

    private Component createWelcomeSection() {
        String adminName = "Admin";
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                User user = adminService.getUserByEmail(email);
                if (user != null) {
                    adminName = user.getFirstName() + " " + user.getLastName();
                }
            }
        } catch (Exception e) {
            // Ignore context issues in test environments
        }

        Div welcomeSection = new Div();
        welcomeSection.addClassName("aruclinic-welcome-section");

        Div header = new Div();
        header.addClassName("aruclinic-welcome-header");

        H1 title = new H1("Welcome Admin, " + adminName + "!");
        title.addClassName("aruclinic-welcome-title");

        Span subtitle = new Span("Manage users, patients, appointments, billing, and system settings");
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button logoutBtn = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT));
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutBtn.addClassName("aruclinic-btn");
        logoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("auth/logout")));
        actions.add(logoutBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
    }

    private Component createQuickActionsBar() {
        com.vaadin.flow.component.orderedlayout.FlexLayout bar = new com.vaadin.flow.component.orderedlayout.FlexLayout();
        bar.setWidthFull();
        bar.addClassName("aruclinic-quick-actions-bar");

        Button addUserBtn = new Button("Add User", new Icon(VaadinIcon.USER));
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addUserBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/users")));

        Button addDocBtn = new Button("Add Doctor", new Icon(VaadinIcon.DOCTOR));
        addDocBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addDocBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/doctors")));

        Button addPatientBtn = new Button("Register Patient", new Icon(VaadinIcon.HOSPITAL));
        addPatientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addPatientBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/patients")));

        Button bookApptBtn = new Button("Book Appointment", new Icon(VaadinIcon.CALENDAR));
        bookApptBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookApptBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/appointments")));

        Button reportsBtn = new Button("Reports", new Icon(VaadinIcon.CHART));
        reportsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        reportsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/reports")));

        bar.add(addUserBtn, addDocBtn, addPatientBtn, bookApptBtn, reportsBtn);
        return bar;
    }

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");
        statsGrid.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))")
                .set("gap", "16px")
                .set("padding", "16px");

        statsGrid.add(createStatCard("Total Users", String.valueOf(adminService.getTotalUsers()), "Users", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Active Patients", String.valueOf(adminService.getTotalPatients()), "Patients", VaadinIcon.HOSPITAL));
        statsGrid.add(createStatCard("Total Doctors", String.valueOf(adminService.getTotalDoctors()), "Doctors", VaadinIcon.DOCTOR));
        statsGrid.add(createStatCard("Total Receptionists", String.valueOf(adminService.getTotalReceptionists()), "Staff", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Today's Appointments", String.valueOf(adminService.getTodaysAppointments()), "Schedule", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Waiting Patients", String.valueOf(adminService.getWaitingPatients()), "Queue", VaadinIcon.HOURGLASS));
        statsGrid.add(createStatCard("Completed Consults", String.valueOf(adminService.getCompletedConsultations()), "Completed", VaadinIcon.CHECK_CIRCLE));
        statsGrid.add(createStatCard("Revenue Today", "₹" + String.format("%.2f", adminService.getRevenueToday()), "Paid", VaadinIcon.MONEY));
        statsGrid.add(createStatCard("Revenue Month", "₹" + String.format("%.2f", adminService.getRevenueThisMonth()), "Paid", VaadinIcon.MONEY));
        statsGrid.add(createStatCard("Pending Bills", String.valueOf(adminService.getPendingBillsCount()), "Unpaid", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("New Registrations", String.valueOf(adminService.getNewRegistrationsCount()), "Last 7 Days", VaadinIcon.SIGN_IN));

        return statsGrid;
    }

    private Component createStatCard(String label, String value, String subtitle, VaadinIcon icon) {
        Div statCard = new Div();
        statCard.addClassName("aruclinic-dashboard-stat-card");
        statCard.getStyle().set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 4px 6px -1px rgb(0 0 0 / 0.1)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div iconDiv = new Div(new Icon(icon));
        iconDiv.getStyle().set("color", "var(--aruclinic-primary)").set("font-size", "24px");

        Span subSpan = new Span(subtitle);
        subSpan.getStyle().set("font-size", "12px").set("color", "#64748B");

        header.add(iconDiv, subSpan);

        Div valueDiv = new Div();
        valueDiv.addClassName("stat-value");
        valueDiv.setText(value);
        valueDiv.getStyle().set("font-size", "28px").set("font-weight", "700").set("margin", "8px 0");

        Div labelDiv = new Div();
        labelDiv.addClassName("stat-label");
        labelDiv.setText(label);
        labelDiv.getStyle().set("font-size", "14px").set("color", "#475569");

        statCard.add(header, valueDiv, labelDiv);
        return statCard;
    }

    private Component createChartsSection() {
        Div chartsSection = new Div();
        chartsSection.addClassName("aruclinic-dashboard-charts");

        Div registrationSection = new Div();
        registrationSection.addClassName("dashboard-section");
        registrationSection.getStyle().set("background", "white").set("border-radius", "12px").set("padding", "20px");
        
        H2 registrationTitle = new H2("Overview Charts & Statistics");
        registrationSection.add(registrationTitle);

        Div statsContainer = new Div();
        statsContainer.addClassName("dashboard-stats");

        long totalUsers = adminService.getTotalUsers();
        long totalAppts = adminService.getTotalPatients(); // total patients

        Div regStat = new Div();
        regStat.addClassName("summary-item");
        regStat.add(new Span(totalUsers + " Registrations"));

        Div actStat = new Div();
        actStat.addClassName("summary-item");
        actStat.add(new Span(totalAppts + " Patients Recorded"));

        statsContainer.add(regStat, actStat);
        registrationSection.add(registrationTitle, statsContainer);

        chartsSection.add(registrationSection);

        return chartsSection;
    }

    private Component createRecentActivitySection() {
        Div activitySection = new Div();
        activitySection.addClassName("aruclinic-recent-activity");
        activitySection.getStyle().set("background", "white").set("border-radius", "12px").set("padding", "20px").set("margin-top", "24px");

        Div header = new Div();
        header.addClassName("aruclinic-activity-header");

        H2 title = new H2("Recent System Activity");
        title.addClassName("aruclinic-activity-title");

        Button viewAllBtn = new Button("View All", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/audit")));

        header.add(title, viewAllBtn);

        Div activityList = new Div();
        activityList.addClassName("aruclinic-activity-list");

        java.util.List<AuditLog> recentLogs = adminService.getAuditLogs();
        recentLogs.sort((a, b) -> b.getPerformedAt().compareTo(a.getPerformedAt()));

        if (recentLogs.isEmpty()) {
            Div emptyMsg = new Div();
            emptyMsg.setText("No recent system activity recorded.");
            emptyMsg.getStyle().set("padding", "16px").set("color", "#64748B");
            activityList.add(emptyMsg);
        } else {
            int limit = Math.min(5, recentLogs.size());
            for (int i = 0; i < limit; i++) {
                AuditLog log = recentLogs.get(i);
                activityList.add(createActivityItem(
                    log.getAction(),
                    log.getDetails() != null ? log.getDetails() : (log.getEntityType() + " #" + log.getEntityId() + " modified"),
                    log.getPerformedAt().toString().replace("T", " "),
                    "primary"
                ));
            }
        }

        activitySection.add(header, activityList);
        return activitySection;
    }

    private Component createActivityItem(String title, String description, String time, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-activity-item");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-activity-icon");
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