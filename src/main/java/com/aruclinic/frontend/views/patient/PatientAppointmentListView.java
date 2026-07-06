package com.aruclinic.frontend.views.patient;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.entity.Patient;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.frontend.views.MainLayout;
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
    private final PatientRepository patientRepository;
    private final com.aruclinic.repository.AppointmentRepository appointmentRepository;
    private final com.aruclinic.repository.DoctorRepository doctorRepository;

    private Patient currentPatient = null;
    private final Grid<Appointment> grid = new Grid<>();
    private final List<Appointment> allAppointments = new ArrayList<>();
    private final DatePicker dateFilter = new DatePicker();
    private final Select<String> statusFilter = new Select<>();

    public PatientAppointmentListView(AppointmentService appointmentService,
                                     PatientRepository patientRepository,
                                     com.aruclinic.repository.AppointmentRepository appointmentRepository,
                                     com.aruclinic.repository.DoctorRepository doctorRepository) {
        this.appointmentService = appointmentService;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;

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
                currentPatient = patientRepository.findByEmail(email).orElse(null);
            }

            // Fallback for blank setups during testing
            if (currentPatient == null) {
                List<Patient> patients = patientRepository.findAll();
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
            rescheduleBtn.addClickListener(e -> openRescheduleDialog(appt));

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
        reasonField.setPlaceholder("Please specify why you are rescheduling this appointment.");
        reasonField.setRequired(true);

        // Fetch initial slots
        updateRescheduleSlots(appt, date, timeSlotCombo);

        date.addValueChangeListener(e -> updateRescheduleSlots(appt, date, timeSlotCombo));

        form.add(date, timeSlotCombo, reasonField);

        Button saveBtn = new Button("Request Reschedule", e -> {
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
            appointmentService.patientRescheduleAppointment(appt.getId(), date.getValue(), selectedTime, reason, newDoctorId);
            Notification.show("Appointment reschedule request submitted successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshData();
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
