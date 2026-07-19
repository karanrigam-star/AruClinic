package com.aruclinic.view.dashboard;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Bill;
import com.aruclinic.entity.Doctor;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.BillRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PrescriptionRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Doctor Dashboard | AruClinic")
@Route(value = "doctor", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
public class DoctorDashboard extends VerticalLayout implements com.vaadin.flow.router.BeforeEnterObserver {

    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BillRepository billRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private Doctor currentDoctor = null;

    public DoctorDashboard(AppointmentRepository appointmentRepository,
                           PrescriptionRepository prescriptionRepository,
                           BillRepository billRepository,
                           DoctorRepository doctorRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.billRepository = billRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(com.vaadin.flow.router.BeforeEnterEvent event) {
        resolveCurrentDoctor();
        removeAll();
        add(createDashboardContent());
    }

    private void resolveCurrentDoctor() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
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
                currentDoctor = doctorRepository.findByEmail(email).orElse(null);
            }

            // Fallback for SUPER_ADMIN or blank setups during testing
            if (currentDoctor == null) {
                List<Doctor> doctors = doctorRepository.findAll();
                if (!doctors.isEmpty()) {
                    currentDoctor = doctors.get(0);
                }
            }
        } catch (Exception e) {
            // Log or ignore
        }
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

        String doctorName = currentDoctor != null ? currentDoctor.getName() : "Doctor";
        H1 title = new H1("Welcome, Dr. " + doctorName);
        title.addClassName("aruclinic-welcome-title");

        String specializationInfo = currentDoctor != null ? 
                currentDoctor.getSpecialization() + " | Department: " + currentDoctor.getDepartment() : 
                "Clinic Practitioner";
        Span subtitle = new Span(specializationInfo);
        subtitle.addClassName("aruclinic-welcome-subtitle");

        Div actions = new Div();
        actions.addClassName("aruclinic-welcome-actions");

        Button newAppointmentBtn = new Button("New Appointment", new Icon(VaadinIcon.CALENDAR));
        newAppointmentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newAppointmentBtn.addClassName("aruclinic-btn");
        newAppointmentBtn.addClassName("aruclinic-btn-primary");
        newAppointmentBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/appointments/add")));
        newAppointmentBtn.setVisible(false); // Hide if receptional scheduling only, else set visible

        Button viewScheduleBtn = new Button("My Schedule", new Icon(VaadinIcon.CALENDAR));
        viewScheduleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewScheduleBtn.addClassName("aruclinic-btn");
        viewScheduleBtn.addClassName("aruclinic-btn-secondary");
        viewScheduleBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/schedule")));

        actions.add(viewScheduleBtn);

        header.add(title, subtitle, actions);
        welcomeSection.add(header);

        return welcomeSection;
    }

    private Component createStatsGrid() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-dashboard-stats");

        long todayApptsCount = 0;
        long totalPatients = 0;
        long totalPrescriptions = 0;
        BigDecimal todayEarnings = BigDecimal.ZERO;

        long pendingToday = 0;

        if (currentDoctor != null) {
            List<Appointment> allAppts = appointmentRepository.findByDoctorId(currentDoctor.getId());
            
            // Today's appointments
            List<Appointment> todayAppts = allAppts.stream()
                    .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().isEqual(LocalDate.now()))
                    .collect(Collectors.toList());
            todayApptsCount = todayAppts.size();
            pendingToday = todayAppts.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                    .count();

            // Total unique patients
            totalPatients = allAppts.stream()
                    .map(a -> a.getPatient().getId())
                    .distinct()
                    .count();

            // Total prescriptions
            totalPrescriptions = prescriptionRepository.findByDoctorId(currentDoctor.getId()).size();

            // Earnings (Today)
            List<Bill> doctorBills = billRepository.findByDoctorId(currentDoctor.getId());
            todayEarnings = doctorBills.stream()
                    .filter(b -> b.getPaidDate() != null && b.getPaidDate().isEqual(LocalDate.now()))
                    .map(Bill::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        statsGrid.add(createStatCard("Today's Appointments", String.valueOf(todayApptsCount), pendingToday + " pending", "primary", VaadinIcon.CLOCK));
        statsGrid.add(createStatCard("Total Patients", String.valueOf(totalPatients), "active records", "success", VaadinIcon.USERS));
        statsGrid.add(createStatCard("Prescriptions", String.valueOf(totalPrescriptions), "written Rx", "warning", VaadinIcon.FILE_TEXT));
        statsGrid.add(createStatCard("Earnings (Today)", "₹" + todayEarnings.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), "collected", "success", VaadinIcon.MONEY));

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
        trendDiv.addClassName("positive");
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
        chartsSection.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
            .set("gap", "var(--aruclinic-spacing-lg)")
            .set("margin-bottom", "var(--aruclinic-spacing-lg)");

        // 1. Status Breakdown Card Group
        Div summarySection = new Div();
        summarySection.addClassName("dashboard-summary");
        summarySection.getStyle()
            .set("background", "var(--aruclinic-card-bg)")
            .set("border", "1px solid var(--aruclinic-border)")
            .set("border-radius", "var(--aruclinic-radius-md)")
            .set("padding", "var(--aruclinic-spacing-lg)")
            .set("box-shadow", "var(--aruclinic-shadow-md)");

        H2 summaryTitle = new H2("Appointment Summary");
        summaryTitle.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("font-weight", "600")
            .set("margin-bottom", "var(--aruclinic-spacing-md)")
            .set("color", "var(--aruclinic-text-primary)");
        summarySection.add(summaryTitle);

        long scheduled = 0;
        long completed = 0;
        long cancelled = 0;

        if (currentDoctor != null) {
            List<Appointment> appts = appointmentRepository.findByDoctorId(currentDoctor.getId());
            scheduled = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).count();
            completed = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            cancelled = appts.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        }

        Div statusCards = new Div();
        statusCards.addClassName("aruclinic-summary-cards");

        statusCards.add(createSummaryCard("Scheduled", String.valueOf(scheduled), "var(--aruclinic-primary)", VaadinIcon.CLOCK));
        statusCards.add(createSummaryCard("Completed", String.valueOf(completed), "var(--aruclinic-success)", VaadinIcon.CHECK_CIRCLE));
        statusCards.add(createSummaryCard("Cancelled", String.valueOf(cancelled), "var(--aruclinic-danger)", VaadinIcon.CLOSE_CIRCLE));

        summarySection.add(statusCards);

        // 2. Weekly Load Distribution Chart Card
        Div chartCard = new Div();
        chartCard.getStyle()
            .set("background", "var(--aruclinic-card-bg)")
            .set("border", "1px solid var(--aruclinic-border)")
            .set("border-radius", "var(--aruclinic-radius-md)")
            .set("padding", "var(--aruclinic-spacing-lg)")
            .set("box-shadow", "var(--aruclinic-shadow-md)");

        H2 chartTitle = new H2("Weekly Load Distribution");
        chartTitle.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("font-weight", "600")
            .set("margin-bottom", "var(--aruclinic-spacing-md)")
            .set("color", "var(--aruclinic-text-primary)");
        chartCard.add(chartTitle);

        List<Appointment> monAppts = new ArrayList<>();
        List<Appointment> tueAppts = new ArrayList<>();
        List<Appointment> wedAppts = new ArrayList<>();
        List<Appointment> thuAppts = new ArrayList<>();
        List<Appointment> friAppts = new ArrayList<>();
        List<Appointment> satAppts = new ArrayList<>();
        List<Appointment> sunAppts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
        LocalDate endOfWeek = startOfWeek.plusDays(6); // Sunday

        if (currentDoctor != null) {
            List<Appointment> appts = appointmentRepository.findByDoctorId(currentDoctor.getId());
            for (Appointment a : appts) {
                if (a.getAppointmentDate() != null) {
                    LocalDate apptDate = a.getAppointmentDate();
                    if (!apptDate.isBefore(startOfWeek) && !apptDate.isAfter(endOfWeek)) {
                        if (apptDate.isEqual(startOfWeek)) monAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(1))) tueAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(2))) wedAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(3))) thuAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(4))) friAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(5))) satAppts.add(a);
                        else if (apptDate.isEqual(startOfWeek.plusDays(6))) sunAppts.add(a);
                    }
                }
            }
        }

        Div chartContainer = new Div();
        chartContainer.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("align-items", "flex-end")
            .set("height", "110px")
            .set("padding", "var(--aruclinic-spacing-sm) var(--aruclinic-spacing-md)")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--aruclinic-radius-sm)");

        long monCount = monAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long tueCount = tueAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long wedCount = wedAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long thuCount = thuAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long friCount = friAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long satCount = satAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();
        long sunCount = sunAppts.stream().filter(a -> a.getStatus() != AppointmentStatus.CANCELLED).count();

        long maxVal = Math.max(1, Math.max(monCount, Math.max(tueCount, Math.max(wedCount, Math.max(thuCount, Math.max(friCount, Math.max(satCount, sunCount)))))));

        chartContainer.add(createBarColumn("Mon " + startOfWeek.getDayOfMonth(), monCount, maxVal, buildWeeklyTooltip("Monday", startOfWeek, monAppts)));
        chartContainer.add(createBarColumn("Tue " + startOfWeek.plusDays(1).getDayOfMonth(), tueCount, maxVal, buildWeeklyTooltip("Tuesday", startOfWeek.plusDays(1), tueAppts)));
        chartContainer.add(createBarColumn("Wed " + startOfWeek.plusDays(2).getDayOfMonth(), wedCount, maxVal, buildWeeklyTooltip("Wednesday", startOfWeek.plusDays(2), wedAppts)));
        chartContainer.add(createBarColumn("Thu " + startOfWeek.plusDays(3).getDayOfMonth(), thuCount, maxVal, buildWeeklyTooltip("Thursday", startOfWeek.plusDays(3), thuAppts)));
        chartContainer.add(createBarColumn("Fri " + startOfWeek.plusDays(4).getDayOfMonth(), friCount, maxVal, buildWeeklyTooltip("Friday", startOfWeek.plusDays(4), friAppts)));
        chartContainer.add(createBarColumn("Sat " + startOfWeek.plusDays(5).getDayOfMonth(), satCount, maxVal, buildWeeklyTooltip("Saturday", startOfWeek.plusDays(5), satAppts)));
        chartContainer.add(createBarColumn("Sun " + startOfWeek.plusDays(6).getDayOfMonth(), sunCount, maxVal, buildWeeklyTooltip("Sunday", startOfWeek.plusDays(6), sunAppts)));

        chartCard.add(chartContainer);

        chartsSection.add(summarySection, chartCard);
        return chartsSection;
    }

    private String buildWeeklyTooltip(String dayName, LocalDate date, List<Appointment> dayAppts) {
        StringBuilder sb = new StringBuilder();
        sb.append(dayName).append(" (").append(date.format(DateTimeFormatter.ofPattern("MMM dd"))).append("):\n");
        
        List<Appointment> activeAppts = dayAppts.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        if (activeAppts.isEmpty()) {
            sb.append("No patient this date");
        } else {
            for (Appointment appt : activeAppts) {
                String timeStr = appt.getAppointmentTime() != null ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";
                String patientName = appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "Unknown";
                sb.append("- ").append(timeStr).append(": ").append(patientName).append("\n");
            }
            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.setLength(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    private Component createSummaryCard(String label, String value, String color, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "var(--aruclinic-spacing-sm)")
            .set("padding", "var(--aruclinic-spacing-sm) var(--aruclinic-spacing-md)")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--aruclinic-radius-sm)");

        Icon iconComponent = new Icon(icon);
        iconComponent.getStyle().set("color", color);

        Div textContainer = new Div();
        textContainer.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column");

        Span valSpan = new Span(value);
        valSpan.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("font-weight", "700")
            .set("color", "var(--aruclinic-text-primary)");

        Span lblSpan = new Span(label);
        lblSpan.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "var(--aruclinic-text-muted)")
            .set("font-weight", "500");

        textContainer.add(valSpan, lblSpan);
        card.add(iconComponent, textContainer);
        return card;
    }

    private Component createBarColumn(String day, long value, long maxVal, String tooltipText) {
        Div column = new Div();
        column.getElement().setAttribute("title", tooltipText);
        column.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("width", "12%")
            .set("height", "100%")
            .set("justify-content", "flex-end")
            .set("gap", "4px")
            .set("cursor", "pointer");

        Span valueSpan = new Span(String.valueOf(value));
        valueSpan.getStyle()
            .set("font-size", "9px")
            .set("font-weight", "600")
            .set("color", "var(--aruclinic-text-primary)");

        Div bar = new Div();
        double percent = (double) value / maxVal * 100;
        double heightPercent = value == 0 ? 8 : percent * 0.7 + 8;
        
        bar.getStyle()
            .set("width", "100%")
            .set("height", heightPercent + "%")
            .set("background", value == 0 ? "var(--lumo-contrast-15pct)" : "var(--aruclinic-primary)")
            .set("border-radius", "3px 3px 0 0")
            .set("transition", "height 0.3s ease");

        Span labelSpan = new Span(day);
        labelSpan.getStyle()
            .set("font-size", "10px")
            .set("color", "var(--aruclinic-text-secondary)")
            .set("font-weight", "500");

        column.add(valueSpan, bar, labelSpan);
        return column;
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
            "My Schedule",
            "View and manage availability",
            "doctor/schedule",
            VaadinIcon.CLOCK
        ));

        quickActions.add(createQuickAction(
            "Create Prescription",
            "Write new prescriptions",
            "doctor/prescriptions/form",
            VaadinIcon.FILE_TEXT
        ));

        quickActions.add(createQuickAction(
            "Prescriptions Audit",
            "Access prescription histories",
            "doctor/prescriptions",
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

        Button viewAllBtn = new Button("View Schedule", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllBtn.addClassName("aruclinic-btn");
        viewAllBtn.addClassName("aruclinic-btn-outline");
        viewAllBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/schedule")));

        header.add(title, viewAllBtn);

        Div appointmentList = new Div();
        appointmentList.addClassName("aruclinic-activity-list");

        List<Appointment> todayAppts = new ArrayList<>();
        if (currentDoctor != null) {
            todayAppts = appointmentRepository.findByDoctorId(currentDoctor.getId()).stream()
                    .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().isEqual(LocalDate.now()))
                    .sorted((a1, a2) -> a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()))
                    .collect(Collectors.toList());
        }

        if (todayAppts.isEmpty()) {
            Div emptyMessage = new Div();
            emptyMessage.setText("No appointments scheduled for today.");
            emptyMessage.getStyle().set("padding", "var(--aruclinic-spacing-md)")
                    .set("color", "var(--aruclinic-text-secondary)")
                    .set("text-align", "center")
                    .set("font-style", "italic");
            appointmentList.add(emptyMessage);
        } else {
            for (Appointment appt : todayAppts) {
                appointmentList.add(createAppointmentItem(appt));
            }
        }

        section.add(header, appointmentList);
        return section;
    }

    private Component createAppointmentItem(Appointment appt) {
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
        String timeStr = appt.getAppointmentDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        timeDiv.setText(timeStr);

        Div patientDiv = new Div();
        patientDiv.addClassName("aruclinic-activity-description");
        String patientName = appt.getPatient() != null ? 
                appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : 
                "Unknown Patient";
        patientDiv.setText(patientName + " - General Consultation");

        Div statusDiv = new Div();
        statusDiv.addClassName("aruclinic-activity-time");
        statusDiv.setText(appt.getStatus() != null ? appt.getStatus().name() : "SCHEDULED");

        content.add(timeDiv, patientDiv, statusDiv);
        left.add(iconDiv, content);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button startBtn = new Button("Consult", new Icon(VaadinIcon.FILE_TEXT));
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        startBtn.addClickListener(e -> getUI().ifPresent(ui -> 
                ui.navigate("doctor/prescriptions/form/patient-" + appt.getPatient().getId())));

        Button completeBtn = new Button("Complete", new Icon(VaadinIcon.CHECK));
        completeBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        completeBtn.addClickListener(e -> {
            appt.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appt);
            
            if (appt.getDoctor() != null) {
                try {
                    Doctor doc = appt.getDoctor();
                    doc.setStatus("AVAILABLE");
                    doctorRepository.save(doc);
                } catch (Exception ex) {
                    // Ignore
                }
            }
            
            Notification.show("Appointment marked as completed!", 2000, Notification.Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.getPage().reload());
        });

        Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        cancelBtn.addClickListener(e -> {
            Dialog cancelDialog = new Dialog();
            cancelDialog.setHeaderTitle("Cancel Appointment");
            cancelDialog.setWidth("400px");

            TextArea reasonField = new TextArea("Reason for Cancellation");
            reasonField.setWidthFull();
            reasonField.setRequired(true);
            reasonField.setPlaceholder("Please specify why the appointment is being cancelled.");

            Button confirmCancelBtn = new Button("Confirm Cancellation", new Icon(VaadinIcon.CHECK));
            confirmCancelBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            confirmCancelBtn.addClickListener(clickEvent -> {
                String reason = reasonField.getValue().trim();
                if (reason.isEmpty()) {
                    reasonField.setInvalid(true);
                    reasonField.setErrorMessage("Please enter a reason for cancellation");
                    return;
                }

                // Update appointment status to CANCELLED in DB
                appt.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appt);

                // Send notification to the patient
                try {
                    if (appt.getPatient() != null && appt.getPatient().getEmail() != null) {
                        String patientEmail = appt.getPatient().getEmail();
                        com.aruclinic.entity.User patientUser = userRepository.findByEmail(patientEmail).orElse(null);
                        if (patientUser != null) {
                            com.aruclinic.entity.Notification notification = new com.aruclinic.entity.Notification();
                            notification.setUser(patientUser);
                            notification.setTitle("Appointment Cancelled");
                            String docName = currentDoctor != null ? currentDoctor.getName() : "Doctor";
                            notification.setMessage("Your appointment with Dr. " + docName + " has been cancelled. Reason: " + reason);
                            notification.setRead(false);
                            notification.setCreatedAt(java.time.LocalDateTime.now());
                            notificationRepository.save(notification);
                        }
                    }
                } catch (Exception ex) {
                    // Ignore notification failure to ensure status update persists
                }

                cancelDialog.close();
                Notification.show("Appointment cancelled successfully!", 2000, Notification.Position.TOP_CENTER);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });

            Button closeDialogBtn = new Button("Close", e2 -> cancelDialog.close());
            closeDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            cancelDialog.getFooter().add(closeDialogBtn, confirmCancelBtn);
            cancelDialog.add(new Div(new Span("Please enter the cancellation reason for the patient:")), reasonField);
            cancelDialog.open();
        });

        if (appt.getStatus() != AppointmentStatus.COMPLETED && appt.getStatus() != AppointmentStatus.CANCELLED) {
            actions.add(startBtn, completeBtn, cancelBtn);
        } else if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            Span cancelledLabel = new Span("Appointment Cancelled");
            cancelledLabel.getStyle().set("color", "var(--aruclinic-danger)").set("font-weight", "600");
            actions.add(cancelledLabel);
        } else {
            Span doneLabel = new Span("Consultation Completed");
            doneLabel.getStyle().set("color", "var(--aruclinic-success)").set("font-weight", "600");
            actions.add(doneLabel);
        }

        item.add(left, actions);
        return item;
    }
}