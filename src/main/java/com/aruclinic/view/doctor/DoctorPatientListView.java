package com.aruclinic.view.doctor;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Prescription;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.service.DoctorService;
import com.aruclinic.view.helper.AppointmentRescheduleHelper;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;
import com.aruclinic.dto.MedicalHistoryItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("My Patients | AruClinic")
@Route(value = "doctor/patients", layout = MainLayout.class)
@CssImport("./themes/aruclinic/patient.css")
public class DoctorPatientListView extends VerticalLayout {

    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    private Doctor currentDoctor = null;
    private final Grid<Patient> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public DoctorPatientListView(PatientService patientService,
                                 PrescriptionService prescriptionService,
                                 AppointmentService appointmentService,
                                 DoctorService doctorService) {
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-doctor-patient-list-view");

        resolveCurrentDoctor();
        configureGrid();

        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-patient-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(p -> "PAT-" + p.getId()).setHeader("Patient ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(p -> p.getFirstName() + " " + p.getLastName()).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Patient::getGender).setHeader("Gender").setAutoWidth(true);
        grid.addColumn(p -> p.getDateOfBirth() != null ? calculateAge(p.getDateOfBirth()) + " yrs" : "N/A").setHeader("Age").setAutoWidth(true);
        grid.addColumn(Patient::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        grid.addColumn(Patient::getEmail).setHeader("Email").setAutoWidth(true);

        // Display latest appointment status dynamically
        grid.addColumn(p -> {
            if (currentDoctor == null) return "N/A";
            List<Appointment> appts = appointmentService.findByPatientId(p.getId()).stream()
                    .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(currentDoctor.getId()))
                    .collect(Collectors.toList());
            if (appts.isEmpty()) {
                return "No Bookings";
            }
            Appointment latest = null;
            for (Appointment a : appts) {
                if (a.getAppointmentDate() != null) {
                    if (latest == null || latest.getAppointmentDate() == null || a.getAppointmentDate().isAfter(latest.getAppointmentDate())) {
                        latest = a;
                    }
                }
            }
            if (latest != null && latest.getStatus() != null) {
                return latest.getStatus().name();
            }
            return "N/A";
        }).setHeader("Appointment Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActionsComponent).setHeader("Actions").setAutoWidth(true);
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
                currentDoctor = doctorService.getDoctorEntityByEmail(email);
            }

            // Fallback for SUPER_ADMIN or missing records
            if (currentDoctor == null) {
                List<Doctor> doctors = doctorService.getAllDoctorEntities();
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

        H1 title = new H1("My Patients");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search by name or phone...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("300px");

        bar.add(searchField);
        return bar;
    }

    private int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private Component createActionsComponent(Patient patient) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        Button consultBtn = new Button("Consult", new Icon(VaadinIcon.FILE_TEXT));
        consultBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        consultBtn.addClickListener(e -> getUI().ifPresent(ui -> 
                ui.navigate("doctor/prescriptions/form/patient-" + patient.getId())));

        Button rescheduleBtn = new Button("Reschedule", new Icon(VaadinIcon.CALENDAR));
        rescheduleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        rescheduleBtn.addClickListener(e -> {
            List<Appointment> activeAppts = appointmentService.findByPatientId(patient.getId()).stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                    .collect(Collectors.toList());
            if (activeAppts.isEmpty()) {
                Notification.show("No active scheduled appointment found for this patient.", 3000, Notification.Position.TOP_CENTER);
            } else {
                Appointment latestActive = activeAppts.get(0);
                AppointmentRescheduleHelper.openRescheduleDialog(latestActive, appointmentService, doctorService, this::refreshGrid);
            }
        });

        Button historyBtn = new Button("View Records", new Icon(VaadinIcon.DOCTOR));
        historyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        historyBtn.addClassName("aruclinic-btn-outline");
        historyBtn.addClickListener(e -> openMedicalRecordsDialog(patient));

        layout.add(consultBtn, rescheduleBtn, historyBtn);
        return layout;
    }

    private void openMedicalRecordsDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("680px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Medical Records: " + patient.getFirstName() + " " + patient.getLastName());
        layout.add(title);

        // 1. Appointment History
        Span appointmentsHeader = new Span("Appointment History");
        appointmentsHeader.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)");
        layout.add(appointmentsHeader);

        Grid<Appointment> apptGrid = new Grid<>();
        apptGrid.setHeight("130px");
        apptGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        
        List<Appointment> appointments = appointmentService.findByPatientId(patient.getId());
        apptGrid.setItems(appointments);
        
        apptGrid.addColumn(a -> {
            if (a.getAppointmentDate() != null && a.getAppointmentTime() != null) {
                return a.getAppointmentDate().toString() + " " + a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
            } else if (a.getAppointmentDate() != null) {
                return a.getAppointmentDate().toString();
            } else if (a.getAppointmentDateTime() != null) {
                return a.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
            }
            return "N/A";
        }).setHeader("Date/Time").setAutoWidth(true);

        apptGrid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A")
                .setHeader("Doctor").setAutoWidth(true);
                
        apptGrid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "SCHEDULED").setHeader("Status").setAutoWidth(true);

        layout.add(apptGrid);

        // 2. Prescription History
        Span rxHeader = new Span("Prescription History");
        rxHeader.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)");
        layout.add(rxHeader);

        Grid<Prescription> rxGrid = new Grid<>();
        rxGrid.setHeight("130px");
        rxGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        List<Prescription> prescriptions = prescriptionService.getPrescriptionEntitiesByPatientId(patient.getId());
        rxGrid.setItems(prescriptions);
        rxGrid.addColumn(p -> "RX-" + p.getId()).setHeader("Rx ID").setAutoWidth(true);
        rxGrid.addColumn(p -> p.getPrescriptionDate() != null ? p.getPrescriptionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A").setHeader("Date").setAutoWidth(true);
        rxGrid.addColumn(Prescription::getDiagnosis).setHeader("Diagnosis").setAutoWidth(true);
        rxGrid.addColumn(Prescription::getStatus).setHeader("Status").setAutoWidth(true);

        layout.add(rxGrid);

        // 3. Medical History Records
        Span historyHeader = new Span("Medical History Records");
        historyHeader.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)");

        Button addHistoryBtn = new Button("Add Medical Record", new Icon(VaadinIcon.PLUS));
        addHistoryBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout historyHeaderRow = new HorizontalLayout(historyHeader, addHistoryBtn);
        historyHeaderRow.setWidthFull();
        historyHeaderRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        historyHeaderRow.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(historyHeaderRow);

        Grid<MedicalHistoryItemDto> historyGrid = new Grid<>();
        historyGrid.setHeight("130px");
        historyGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        ObjectMapper mapper = new ObjectMapper();
        List<MedicalHistoryItemDto> historyRecords = new ArrayList<>();
        String currentHistoryJson = patient.getMedicalHistory();
        if (currentHistoryJson != null && !currentHistoryJson.trim().isEmpty()) {
            try {
                historyRecords = mapper.readValue(currentHistoryJson, new TypeReference<List<MedicalHistoryItemDto>>() {});
            } catch (Exception ex) {}
        }
        historyGrid.setItems(historyRecords);

        historyGrid.addColumn(MedicalHistoryItemDto::getDate).setHeader("Date").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getTitle).setHeader("Title").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getDoctor).setHeader("Provider").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getDetails).setHeader("Details").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getType).setHeader("Type").setAutoWidth(true);

        layout.add(historyGrid);

        addHistoryBtn.addClickListener(e -> {
            Dialog addDialog = new Dialog();
            addDialog.setHeaderTitle("Add Medical Record: " + patient.getFirstName() + " " + patient.getLastName());
            addDialog.setWidth("450px");

            DatePicker recordDate = new DatePicker("Record Date", LocalDate.now());
            recordDate.setWidthFull();

            TextField recordTitle = new TextField("Record Title / Event");
            recordTitle.setPlaceholder("e.g. Follow-up Exam, Vaccination - Flu Shot");
            recordTitle.setWidthFull();
            recordTitle.setRequiredIndicatorVisible(true);

            TextArea recordDetails = new TextArea("Record Details / Notes");
            recordDetails.setPlaceholder("Enter consultation notes, measurements, or details...");
            recordDetails.setWidthFull();
            recordDetails.setRequiredIndicatorVisible(true);

            Select<String> severitySelect = new Select<>();
            severitySelect.setLabel("Record Type (Severity)");
            severitySelect.setItems("Info", "Success", "Warning", "Danger");
            severitySelect.setValue("Info");
            severitySelect.setWidthFull();

            Button saveBtn = new Button("Save Record", new Icon(VaadinIcon.CHECK));
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            Button cancelDialogBtn = new Button("Cancel", click -> addDialog.close());
            cancelDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            saveBtn.addClickListener(clickEvent -> {
                String titleVal = recordTitle.getValue().trim();
                String detailsVal = recordDetails.getValue().trim();
                LocalDate dateVal = recordDate.getValue();

                if (titleVal.isEmpty()) {
                    recordTitle.setInvalid(true);
                    return;
                }
                if (detailsVal.isEmpty()) {
                    recordDetails.setInvalid(true);
                    return;
                }
                if (dateVal == null) {
                    recordDate.setInvalid(true);
                    return;
                }

                // Map severity to color type
                String typeVal = "primary";
                if ("Success".equals(severitySelect.getValue())) typeVal = "success";
                else if ("Warning".equals(severitySelect.getValue())) typeVal = "warning";
                else if ("Danger".equals(severitySelect.getValue())) typeVal = "danger";

                String providerName = currentDoctor != null ? currentDoctor.getName() : "Doctor";

                MedicalHistoryItemDto newRecord = new MedicalHistoryItemDto(
                    dateVal.toString(),
                    titleVal,
                    providerName,
                    detailsVal,
                    typeVal
                );

                try {
                    // Fetch latest patient entity to avoid optimistic locking / stale data
                    Patient freshPatient = patientService.getPatientEntityById(patient.getId());
                    
                    List<MedicalHistoryItemDto> records = new ArrayList<>();
                    String json = freshPatient.getMedicalHistory();
                    if (json != null && !json.trim().isEmpty()) {
                        records = mapper.readValue(json, new TypeReference<List<MedicalHistoryItemDto>>() {});
                    }

                    // Add new record at the beginning of the list
                    records.add(0, newRecord);

                    // Serialize back
                    String newJson = mapper.writeValueAsString(records);
                    freshPatient.setMedicalHistory(newJson);

                    // Save
                    patientService.savePatient(freshPatient);

                    // Update dialog reference
                    patient.setMedicalHistory(newJson);

                    // Update UI list in parent dialog
                    historyGrid.setItems(records);

                    addDialog.close();
                    Notification.show("Medical record added successfully!", 2000, Notification.Position.TOP_CENTER);
                } catch (Exception ex) {
                    Notification.show("Failed to save record: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                }
            });

            addDialog.getFooter().add(cancelDialogBtn, saveBtn);
            addDialog.add(new VerticalLayout(recordDate, recordTitle, recordDetails, severitySelect));
            addDialog.open();
        });

        Button closeBtn = new Button("Close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.addClickListener(e -> dialog.close());

        HorizontalLayout footer = new HorizontalLayout(closeBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        layout.add(footer);
        dialog.add(layout);
        dialog.open();
    }

    private void refreshGrid() {
        String filter = searchField.getValue().trim();
        List<Patient> patients;

        if (currentDoctor != null) {
            // Find patient IDs directly to avoid LazyInitializationException outside transactional context
            java.util.Set<Long> patientIds = new java.util.HashSet<>(
                appointmentService.findPatientIdsByDoctorId(currentDoctor.getId())
            );
            patientIds.addAll(
                prescriptionService.findPatientIdsByDoctorId(currentDoctor.getId())
            );

            if (patientIds.isEmpty()) {
                patients = new java.util.ArrayList<>();
            } else {
                patients = patientService.getPatientEntitiesByIds(new java.util.ArrayList<>(patientIds));
            }
        } else {
            patients = patientService.getAllPatientEntities();
        }

        if (!filter.isEmpty()) {
            patients = patients.stream()
                    .filter(p -> (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(filter.toLowerCase())
                            || (p.getMobileNumber() != null && p.getMobileNumber().contains(filter)))
                    .collect(Collectors.toList());
        }

        grid.setItems(patients);
    }
}
