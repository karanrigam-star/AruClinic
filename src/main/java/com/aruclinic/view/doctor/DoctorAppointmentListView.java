package com.aruclinic.view.doctor;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Doctor;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.vaadin.flow.component.textfield.TextArea;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("My Appointments | AruClinic")
@Route(value = "doctor/appointments", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
@CssImport("./themes/aruclinic/patient.css")
public class DoctorAppointmentListView extends VerticalLayout {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private Doctor currentDoctor = null;
    private final Grid<Appointment> grid = new Grid<>();
    private final DatePicker dateFilter = new DatePicker();
    private final Select<String> statusFilter = new Select<>();

    public DoctorAppointmentListView(AppointmentRepository appointmentRepository,
                                     DoctorRepository doctorRepository,
                                     UserRepository userRepository,
                                     NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-doctor-appointment-list-view");

        resolveCurrentDoctor();
        configureGrid();
        
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-appointment-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A")
                .setHeader("Time").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(a -> a.getPatient() != null ? "PAT-" + a.getPatient().getId() : "N/A")
                .setHeader("Patient ID").setAutoWidth(true);

        grid.addColumn(a -> a.getPatient() != null ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "Unknown")
                .setHeader("Patient Name").setSortable(true).setAutoWidth(true);

        grid.addColumn(a -> a.getPatient() != null ? a.getPatient().getMobileNumber() : "N/A")
                .setHeader("Mobile").setAutoWidth(true);

        grid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "SCHEDULED")
                .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActionsComponent).setHeader("Actions").setAutoWidth(true);
    }

    private void resolveCurrentDoctor() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = null;
            if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                email = springUser.getUsername();
            } else if (principal instanceof String principalStr) {
                email = principalStr;
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
            // Ignore
        }
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Appointments");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        dateFilter.setPlaceholder("Filter by Date");
        dateFilter.setValue(LocalDate.now());
        dateFilter.setWidth("200px");
        dateFilter.addValueChangeListener(e -> refreshGrid());

        statusFilter.setItems("ALL", "SCHEDULED", "COMPLETED", "CANCELLED");
        statusFilter.setValue("ALL");
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        Button findBtn = new Button("Find", new Icon(VaadinIcon.SEARCH));
        findBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        findBtn.addClickListener(e -> refreshGrid());

        bar.add(dateFilter, statusFilter, findBtn);
        return bar;
    }

    private Component createActionsComponent(Appointment appt) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button viewDetailsBtn = new Button("Details", new Icon(VaadinIcon.INFO_CIRCLE));
        viewDetailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewDetailsBtn.addClickListener(e -> showAppointmentDetailsDialog(appt));
        layout.add(viewDetailsBtn);

        if (appt.getStatus() != AppointmentStatus.COMPLETED && appt.getStatus() != AppointmentStatus.CANCELLED) {
            Button consultBtn = new Button("Consult", new Icon(VaadinIcon.FILE_TEXT));
            consultBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            consultBtn.addClickListener(e -> {
                com.vaadin.flow.server.VaadinSession.getCurrent().setAttribute("consult_navigation_active", true);
                getUI().ifPresent(ui -> 
                        ui.navigate("doctor/prescriptions/form/patient-" + appt.getPatient().getId()));
            });

            Button completeBtn = new Button("Complete", new Icon(VaadinIcon.CHECK));
            completeBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            completeBtn.addClickListener(e -> {
                appt.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appt);
                Notification.show("Appointment marked as completed!", 2000, Notification.Position.TOP_CENTER);
                refreshGrid();
            });

            Button acceptBtn = new Button("Accept", new Icon(VaadinIcon.CHECK));
            acceptBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            acceptBtn.addClickListener(e -> {
                appt.setStatus(AppointmentStatus.SCHEDULED);
                appointmentRepository.save(appt);
                Notification.show("Appointment accepted!", 2000, Notification.Position.TOP_CENTER);
                refreshGrid();
            });

            Button confirmBtn = new Button("Confirm", new Icon(VaadinIcon.CHECK_CIRCLE));
            confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            confirmBtn.addClickListener(e -> {
                appt.setStatus(AppointmentStatus.SCHEDULED);
                appointmentRepository.save(appt);
                Notification.show("Appointment confirmed!", 2000, Notification.Position.TOP_CENTER);
                refreshGrid();
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
                    refreshGrid();
                });

                Button closeDialogBtn = new Button("Close", e2 -> cancelDialog.close());
                closeDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                cancelDialog.getFooter().add(closeDialogBtn, confirmCancelBtn);
                cancelDialog.add(new Div(new Span("Please enter the cancellation reason for the patient:")), reasonField);
                cancelDialog.open();
            });

            layout.add(consultBtn, completeBtn, acceptBtn, confirmBtn, cancelBtn);
        } else {
            Span statusLabel = new Span(appt.getStatus() == AppointmentStatus.COMPLETED ? "Completed" : "Cancelled");
            statusLabel.getStyle()
                    .set("color", appt.getStatus() == AppointmentStatus.COMPLETED ? "var(--aruclinic-success)" : "var(--aruclinic-danger)")
                    .set("font-weight", "600");
            layout.add(statusLabel);
        }

        return layout;
    }

    private void showAppointmentDetailsDialog(Appointment appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Appointment Details");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Div detailsBlock = new Div();
        detailsBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px").set("width", "100%");

        detailsBlock.add(createDetailItem("Appointment ID", "APT-" + appt.getId()));
        detailsBlock.add(createDetailItem("Date", appt.getAppointmentDate() != null ? appt.getAppointmentDate().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Time", appt.getAppointmentTime() != null ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A"));
        detailsBlock.add(createDetailItem("Patient ID", appt.getPatient() != null ? "PAT-" + appt.getPatient().getId() : "N/A"));
        detailsBlock.add(createDetailItem("Patient Name", appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "Unknown"));
        detailsBlock.add(createDetailItem("Mobile Number", appt.getPatient() != null ? appt.getPatient().getMobileNumber() : "N/A"));
        detailsBlock.add(createDetailItem("Email", appt.getPatient() != null ? appt.getPatient().getEmail() : "N/A"));
        detailsBlock.add(createDetailItem("Status", appt.getStatus() != null ? appt.getStatus().name() : "N/A"));

        content.add(detailsBlock);
        dialog.add(content);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private Component createDetailItem(String label, String value) {
        Div item = new Div();
        item.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("border-bottom", "1px solid #f1f5f9")
            .set("padding-bottom", "8px")
            .set("width", "100%");
        Span l = new Span(label);
        l.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-text-secondary, #64748b)");
        Span v = new Span(value != null ? value : "N/A");
        v.getStyle().set("font-weight", "500").set("color", "var(--aruclinic-text-primary, #0f172a)");
        item.add(l, v);
        return item;
    }

    private void refreshGrid() {
        if (currentDoctor == null) {
            grid.setItems(new ArrayList<>());
            return;
        }

        List<Appointment> appts;
        LocalDate selectedDate = dateFilter.getValue();
        if (selectedDate != null) {
            appts = appointmentRepository.findByDoctorIdAndAppointmentDateWithDetails(currentDoctor.getId(), selectedDate);
        } else {
            appts = appointmentRepository.findByDoctorIdWithDetails(currentDoctor.getId());
        }

        // Filter by status
        String status = statusFilter.getValue();
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            appts = appts.stream()
                    .filter(a -> a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Sort by time
        appts.sort((a1, a2) -> {
            if (a1.getAppointmentTime() == null && a2.getAppointmentTime() == null) return 0;
            if (a1.getAppointmentTime() == null) return 1;
            if (a2.getAppointmentTime() == null) return -1;
            return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
        });

        grid.setItems(appts);

        // Pop up warning notification if no records are found
        if (appts.isEmpty()) {
            Notification.show("No appointments found for the selected date and status.", 3000, Notification.Position.MIDDLE);
        }
    }
}
