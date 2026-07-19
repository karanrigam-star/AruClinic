package com.aruclinic.view.patient;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Patient;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.DoctorService;
import com.aruclinic.view.helper.AppointmentRescheduleHelper;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated patient appointment list view, styled identically to the doctor's appointment list view.
 */
@PageTitle("My Appointments | AruClinic")
@Route(value = "patient/appointments", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
@CssImport("./themes/aruclinic/patient.css")
public class PatientAppointmentListView extends VerticalLayout implements BeforeEnterObserver {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    private Patient currentPatient = null;
    private final Grid<Appointment> grid = new Grid<>();
    private final List<Appointment> allAppointments = new ArrayList<>();
    private final DatePicker dateFilter = new DatePicker();
    private final Select<String> statusFilter = new Select<>();

    public PatientAppointmentListView(AppointmentService appointmentService,
                                     PatientService patientService,
                                     DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-patient-appointment-list-view");

        resolvePatient();
        configureGrid();

        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshData();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolvePatient();
        refreshData();
    }

    private void resolvePatient() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    auth = (org.springframework.security.core.Authentication)
                            session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                }
            }

            if (auth != null) {
                org.springframework.security.core.context.SecurityContext context =
                        org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
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
                currentPatient = patientService.getPatientEntityByEmail(email);
            }

            // Fallback for blank setups during testing (strictly disallowed for PATIENT role)
            boolean isPatient = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (currentPatient == null && !isPatient) {
                List<Patient> patients = patientService.getAllPatientEntities();
                if (!patients.isEmpty()) {
                    currentPatient = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-appointment-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(a -> a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "N/A")
                .setHeader("Date").setSortable(true).setAutoWidth(true);

        grid.addColumn(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A")
                .setHeader("Time").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(a -> a.getDoctor() != null ? "DOC-" + a.getDoctor().getId() : "N/A")
                .setHeader("Doctor ID").setAutoWidth(true);

        grid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "Unknown")
                .setHeader("Doctor Name").setSortable(true).setAutoWidth(true);

        grid.addColumn(a -> a.getDoctor() != null ? a.getDoctor().getSpecialization() : "General Practitioner")
                .setHeader("Specialization").setAutoWidth(true);

        grid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "SCHEDULED")
                .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActionsComponent).setHeader("Actions").setAutoWidth(true);
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
            Button rescheduleBtn = new Button("Reschedule", new Icon(VaadinIcon.CALENDAR));
            rescheduleBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            rescheduleBtn.addClickListener(e -> AppointmentRescheduleHelper.openRescheduleDialog(appt, appointmentService, doctorService, this::refreshData));

            Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            cancelBtn.addClickListener(e -> openCancelDialog(appt));

            layout.add(rescheduleBtn, cancelBtn);
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
        detailsBlock.add(createDetailItem("Doctor ID", appt.getDoctor() != null ? "DOC-" + appt.getDoctor().getId() : "N/A"));
        detailsBlock.add(createDetailItem("Doctor Name", appt.getDoctor() != null ? "Dr. " + appt.getDoctor().getName() : "Unknown"));
        detailsBlock.add(createDetailItem("Specialization", appt.getDoctor() != null ? appt.getDoctor().getSpecialization() : "N/A"));
        detailsBlock.add(createDetailItem("Reason", appt.getReason() != null ? appt.getReason() : "N/A"));
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

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Appointments");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button bookBtn = new Button("Book Appointment", new Icon(VaadinIcon.PLUS));
        bookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bookBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/appointments/add")));

        header.add(title, bookBtn);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        dateFilter.setPlaceholder("Filter by Date");
        dateFilter.setWidth("200px");
        dateFilter.addValueChangeListener(e -> updateGridList());

        statusFilter.setPlaceholder("Filter by Status");
        statusFilter.setItems("ALL", "SCHEDULED", "COMPLETED", "CANCELLED");
        statusFilter.setValue("ALL");
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> updateGridList());

        Button findBtn = new Button("Find", new Icon(VaadinIcon.SEARCH));
        findBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        findBtn.addClickListener(e -> updateGridList());

        bar.add(dateFilter, statusFilter, findBtn);
        return bar;
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
            refreshData();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void refreshData() {
        if (currentPatient == null) {
            allAppointments.clear();
            updateGridList();
            return;
        }

        List<Appointment> list = appointmentService.findByPatientId(currentPatient.getId());
        allAppointments.clear();
        allAppointments.addAll(list);

        updateGridList();
    }

    private void updateGridList() {
        LocalDate dFilter = dateFilter.getValue();
        String sFilter = statusFilter.getValue();

        List<Appointment> filtered = allAppointments.stream()
                .filter(a -> {
                    if (dFilter == null) return true;
                    return a.getAppointmentDate() != null && a.getAppointmentDate().isEqual(dFilter);
                })
                .filter(a -> {
                    if (sFilter == null || "ALL".equalsIgnoreCase(sFilter)) return true;
                    return a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(sFilter);
                })
                .collect(Collectors.toList());

        // Sort by date and time
        filtered.sort((a1, a2) -> {
            if (a1.getAppointmentDate() == null && a2.getAppointmentDate() == null) return 0;
            if (a1.getAppointmentDate() == null) return 1;
            if (a2.getAppointmentDate() == null) return -1;
            int dateComp = a1.getAppointmentDate().compareTo(a2.getAppointmentDate());
            if (dateComp != 0) return dateComp;
            
            if (a1.getAppointmentTime() == null && a2.getAppointmentTime() == null) return 0;
            if (a1.getAppointmentTime() == null) return 1;
            if (a2.getAppointmentTime() == null) return -1;
            return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
        });

        grid.setItems(filtered);

        if (filtered.isEmpty()) {
            Notification.show("No appointments found for the selected date and status.", 3000, Notification.Position.MIDDLE);
        }
    }
}
