package com.aruclinic.frontend.views.admin;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.service.AdminService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.frontend.views.MainLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Appointment Management | AruClinic")
@Route(value = "admin/appointments", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminAppointmentListView extends VerticalLayout {

    private final AdminService adminService;
    private final com.aruclinic.repository.AppointmentRepository appointmentRepository;
    private final com.aruclinic.repository.DoctorRepository doctorRepository;
    private final Grid<Appointment> grid = new Grid<>();
    private final DatePicker dateFilter = new DatePicker();
    private final Select<String> statusFilter = new Select<>();

    public AdminAppointmentListView(AdminService adminService,
                                   com.aruclinic.repository.AppointmentRepository appointmentRepository,
                                   com.aruclinic.repository.DoctorRepository doctorRepository) {
        this.adminService = adminService;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-appointment-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-appointment-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(Appointment::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(a -> a.getPatient() != null ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "N/A")
            .setHeader("Patient").setAutoWidth(true);
        grid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A")
            .setHeader("Doctor").setAutoWidth(true);
        grid.addColumn(a -> a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "N/A")
            .setHeader("Date").setAutoWidth(true);
        grid.addColumn(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "N/A")
            .setHeader("Time").setAutoWidth(true);
        
        grid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "N/A")
            .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);
    }

    private Component createActions(Appointment appt) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Select<AppointmentStatus> statusSelect = new Select<>();
        statusSelect.setItems(AppointmentStatus.values());
        statusSelect.setValue(appt.getStatus());
        statusSelect.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() != appt.getStatus()) {
                appt.setStatus(e.getValue());
                adminService.updateAppointment(appt.getId(), appt);
                Notification.show("Appointment status changed to " + e.getValue().name(), 2000, Notification.Position.TOP_CENTER);
                refreshGrid();
            }
        });

        Button viewBtn = new Button("View", new Icon(VaadinIcon.INFO_CIRCLE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewBtn.addClickListener(e -> showAppointmentDetailsDialog(appt));

        Button acceptBtn = new Button("Accept", new Icon(VaadinIcon.CHECK));
        acceptBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        acceptBtn.addClickListener(e -> {
            appt.setStatus(AppointmentStatus.SCHEDULED);
            adminService.updateAppointment(appt.getId(), appt);
            Notification.show("Appointment accepted!", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        Button confirmBtn = new Button("Confirm", new Icon(VaadinIcon.CHECK_CIRCLE));
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        confirmBtn.addClickListener(e -> {
            appt.setStatus(AppointmentStatus.SCHEDULED);
            adminService.updateAppointment(appt.getId(), appt);
            Notification.show("Appointment confirmed!", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        Button rescheduleBtn = new Button("Reschedule", new Icon(VaadinIcon.CALENDAR));
        rescheduleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        rescheduleBtn.addClickListener(e -> openRescheduleDialog(appt));

        Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        cancelBtn.addClickListener(e -> openCancelDialog(appt));

        boolean active = appt.getStatus() != AppointmentStatus.COMPLETED && appt.getStatus() != AppointmentStatus.CANCELLED;
        acceptBtn.setEnabled(active);
        confirmBtn.setEnabled(active);
        rescheduleBtn.setEnabled(active);
        cancelBtn.setEnabled(active);

        actions.add(viewBtn, statusSelect, acceptBtn, confirmBtn, rescheduleBtn, cancelBtn);
        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Appointment Management");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Book Appointment", new Icon(VaadinIcon.PLUS));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openBookDialog());

        header.add(title, addBtn);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        dateFilter.setPlaceholder("Filter by Date");
        dateFilter.addValueChangeListener(e -> refreshGrid());

        statusFilter.setPlaceholder("Filter by Status");
        statusFilter.setItems("ALL", "SCHEDULED", "CHECKED_IN", "WAITING", "IN_CONSULTATION", "COMPLETED", "CANCELLED", "NO_SHOW");
        statusFilter.setValue("ALL");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        bar.add(dateFilter, statusFilter);
        return bar;
    }

    private void openBookDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Book New Appointment");
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        Select<Patient> patientSelect = new Select<>();
        patientSelect.setLabel("Patient");
        patientSelect.setItems(adminService.getAllPatients());
        patientSelect.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")");

        Select<Doctor> doctorSelect = new Select<>();
        doctorSelect.setLabel("Doctor");
        doctorSelect.setItems(adminService.getAllDoctors());
        doctorSelect.setItemLabelGenerator(d -> "Dr. " + d.getName() + " (" + d.getSpecialization() + ")");

        DatePicker date = new DatePicker("Appointment Date");
        date.setValue(LocalDate.now());

        TimePicker time = new TimePicker("Appointment Time");
        time.setValue(LocalTime.of(10, 0));

        form.add(patientSelect, doctorSelect, date, time);

        Button saveBtn = new Button("Book", e -> {
            if (patientSelect.getValue() == null || doctorSelect.getValue() == null || date.getValue() == null || time.getValue() == null) {
                Notification.show("Please fill in all details", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            Appointment a = new Appointment();
            a.setPatient(patientSelect.getValue());
            a.setDoctor(doctorSelect.getValue());
            a.setAppointmentDate(date.getValue());
            a.setAppointmentTime(time.getValue());
            a.setStatus(AppointmentStatus.SCHEDULED);

            adminService.createAppointment(a);
            Notification.show("Appointment booked successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshGrid();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private final List<String> timeSlots = java.util.Arrays.asList(
        "09:00 AM - 09:30 AM",
        "09:30 AM - 10:00 AM",
        "10:00 AM - 10:30 AM",
        "10:30 AM - 11:00 AM",
        "11:00 AM - 11:30 AM",
        "11:30 AM - 12:00 PM",
        "02:00 PM - 02:30 PM",
        "02:30 PM - 03:00 PM",
        "03:00 PM - 03:30 PM",
        "03:30 PM - 04:00 PM",
        "04:00 PM - 04:30 PM",
        "04:30 PM - 05:00 PM",
        "05:00 PM - 05:30 PM",
        "05:30 PM - 06:00 PM",
        "06:00 PM - 06:30 PM",
        "06:30 PM - 07:00 PM"
    );

    private LocalTime parseTimeSlot(String timeSlot) {
        String startTime = timeSlot.split(" - ")[0];
        String[] parts = startTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1].split(" ")[0]);

        if (startTime.contains("PM") && hour != 12) {
            hour += 12;
        }
        if (startTime.contains("AM") && hour == 12) {
            hour = 0;
        }

        return LocalTime.of(hour, minute);
    }

    private void updateRescheduleSlots(Appointment appt, DatePicker datePicker, ComboBox<String> timeSlotCombo) {
        if (datePicker.getValue() == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();

        if (appt.getDoctor() == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        com.aruclinic.entity.Doctor doctor = doctorRepository.findById(appt.getDoctor().getId()).orElse(null);
        if (doctor == null) {
            timeSlotCombo.setItems(java.util.Collections.emptyList());
            timeSlotCombo.setValue(null);
            return;
        }

        String spec = doctor.getSpecialization();
        final List<com.aruclinic.entity.Doctor> sameSpecialtyDocs;
        if (spec != null && !spec.trim().isEmpty()) {
            List<com.aruclinic.entity.Doctor> tempDocs = java.util.Collections.emptyList();
            try {
                tempDocs = doctorRepository.findBySpecialization(spec).stream()
                    .filter(d -> !d.getId().equals(doctor.getId()))
                    .collect(java.util.stream.Collectors.toList());
            } catch (Exception ex) {}
            sameSpecialtyDocs = tempDocs;
        } else {
            sameSpecialtyDocs = java.util.Collections.emptyList();
        }

        List<Appointment> allApptsOnDate = new java.util.ArrayList<>();
        try {
            allApptsOnDate = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDateTime() != null && a.getAppointmentDateTime().toLocalDate().isEqual(selectedDate))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> !a.getId().equals(appt.getId()))
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception ex) {}

        final List<Appointment> finalAppts = allApptsOnDate;

        List<String> listToDisplay = timeSlots.stream()
            .map(slot -> {
                try {
                    LocalTime slotStart = parseTimeSlot(slot);
                    if (selectedDate.isEqual(today) && !slotStart.isAfter(LocalTime.now())) {
                        return null;
                    }

                    boolean origDocStatusOk = true;
                    if (selectedDate.isEqual(today)) {
                        origDocStatusOk = "AVAILABLE".equalsIgnoreCase(doctor.getStatus());
                    }
                    boolean origDocBooked = finalAppts.stream()
                        .anyMatch(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctor.getId()) && a.getAppointmentDateTime().toLocalTime().equals(slotStart));

                    if (origDocStatusOk && !origDocBooked) {
                        return slot;
                    }

                    for (com.aruclinic.entity.Doctor otherDoc : sameSpecialtyDocs) {
                        final com.aruclinic.entity.Doctor finalOtherDoc = otherDoc;
                        boolean otherDocStatusOk = true;
                        if (selectedDate.isEqual(today)) {
                            otherDocStatusOk = "AVAILABLE".equalsIgnoreCase(finalOtherDoc.getStatus());
                        }
                        boolean otherDocBooked = finalAppts.stream()
                            .anyMatch(a -> a.getDoctor() != null && a.getDoctor().getId().equals(finalOtherDoc.getId()) && a.getAppointmentDateTime().toLocalTime().equals(slotStart));

                        if (otherDocStatusOk && !otherDocBooked) {
                            return slot + " (with Dr. " + finalOtherDoc.getName() + " - ID:" + finalOtherDoc.getId() + ")";
                        }
                    }

                    return slot + " (Dr. " + doctor.getName() + " is not available)";
                } catch (Exception ex) {
                    return slot;
                }
            })
            .filter(slot -> slot != null)
            .collect(java.util.stream.Collectors.toList());

        timeSlotCombo.setItems(listToDisplay);
        if (!listToDisplay.isEmpty()) {
            String currentSlotMatch = listToDisplay.stream()
                .filter(s -> !s.contains("not available"))
                .findFirst()
                .orElse(null);
            timeSlotCombo.setValue(currentSlotMatch);
        } else {
            timeSlotCombo.setValue(null);
        }
    }

    private void openRescheduleDialog(Appointment appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reschedule Appointment #" + appt.getId());
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        DatePicker date = new DatePicker("New Date");
        date.setMin(LocalDate.now());
        date.setValue(appt.getAppointmentDate() != null && !appt.getAppointmentDate().isBefore(LocalDate.now()) ? appt.getAppointmentDate() : LocalDate.now());
        date.setRequired(true);

        ComboBox<String> timeSlotCombo = new ComboBox<>("New Time Slot");
        timeSlotCombo.setRequired(true);

        TextArea reasonField = new TextArea("Reason for Rescheduling");
        reasonField.setPlaceholder("Please specify why the appointment is being rescheduled.");
        reasonField.setRequired(true);

        // Fetch initial slots
        updateRescheduleSlots(appt, date, timeSlotCombo);

        date.addValueChangeListener(e -> updateRescheduleSlots(appt, date, timeSlotCombo));

        form.add(date, timeSlotCombo, reasonField);

        Button saveBtn = new Button("Reschedule", e -> {
            if (date.getValue() == null || timeSlotCombo.getValue() == null) {
                Notification.show("Please select date and time slot", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            String selectedSlot = timeSlotCombo.getValue();
            if (selectedSlot.contains("not available") || selectedSlot.contains("Doctor is currently")) {
                Notification.show("Please select a valid time slot", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            String reason = reasonField.getValue().trim();
            if (reason.isEmpty()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Please enter a reason");
                return;
            }

            Long newDoctorId = null;
            if (selectedSlot.contains(" - ID:")) {
                try {
                    String idStr = selectedSlot.substring(selectedSlot.indexOf(" - ID:") + 6).replace(")", "").trim();
                    newDoctorId = Long.parseLong(idStr);
                } catch (Exception ex) {}
            }

            LocalTime selectedTime = parseTimeSlot(selectedSlot);
            adminService.rescheduleAppointment(appt.getId(), date.getValue(), selectedTime, reason, newDoctorId);
            Notification.show("Appointment rescheduled successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshGrid();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openCancelDialog(Appointment appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancel Appointment #" + appt.getId());
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextArea reasonField = new TextArea("Reason for Cancellation");
        reasonField.setPlaceholder("Please specify why the appointment is being cancelled.");
        reasonField.setRequired(true);

        form.add(reasonField);

        Button saveBtn = new Button("Cancel Appointment", e -> {
            String reason = reasonField.getValue().trim();
            if (reason.isEmpty()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Please enter a reason");
                return;
            }

            adminService.cancelAppointment(appt.getId(), reason);
            Notification.show("Appointment cancelled successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshGrid();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
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
        detailsBlock.add(createDetailItem("Time", appt.getAppointmentTime() != null ? appt.getAppointmentTime().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Patient Name", appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "Unknown"));
        detailsBlock.add(createDetailItem("Doctor Name", appt.getDoctor() != null ? "Dr. " + appt.getDoctor().getName() : "N/A"));
        detailsBlock.add(createDetailItem("Status", appt.getStatus() != null ? appt.getStatus().name() : "N/A"));
        detailsBlock.add(createDetailItem("Reason / Note", appt.getReason() != null ? appt.getReason() : "N/A"));

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
        l.getStyle().set("font-weight", "600").set("color", "#64748b");
        Span v = new Span(value != null ? value : "N/A");
        v.getStyle().set("font-weight", "500").set("color", "#0f172a");
        item.add(l, v);
        return item;
    }

    private void refreshGrid() {
        List<Appointment> appts = adminService.getAllAppointments();

        LocalDate dFilter = dateFilter.getValue();
        if (dFilter != null) {
            appts = appts.stream().filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().isEqual(dFilter)).collect(Collectors.toList());
        }

        String sFilter = statusFilter.getValue();
        if (sFilter != null && !"ALL".equalsIgnoreCase(sFilter)) {
            appts = appts.stream().filter(a -> a.getStatus() != null && a.getStatus().name().equalsIgnoreCase(sFilter)).collect(Collectors.toList());
        }

        grid.setItems(appts);
    }
}
