package com.aruclinic.view.doctor;

import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Doctor;
import com.aruclinic.dto.MedicalHistoryItemDto;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.DoctorService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Doctor-facing view for managing patient medical records and histories.
 */
@PageTitle("Medical History Management | AruClinic")
@Route(value = "doctor/medical-history", layout = MainLayout.class)
@CssImport("./themes/aruclinic/dashboard.css")
@CssImport("./themes/aruclinic/patient.css")
public class DoctorMedicalHistoryView extends VerticalLayout {

    private final PatientService patientService;
    private final DoctorService doctorService;

    private Doctor currentDoctor = null;
    private Patient selectedPatient = null;

    private final ComboBox<Patient> patientSelector = new ComboBox<>("Search Patient");
    private final Div patientInfoContainer = new Div();
    private final Div historyTimelineContainer = new Div();
    private final Grid<MedicalHistoryItemDto> historyGrid = new Grid<>();

    public DoctorMedicalHistoryView(PatientService patientService, DoctorService doctorService) {
        this.patientService = patientService;
        this.doctorService = doctorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        resolveCurrentDoctor();

        // Title
        H1 title = new H1("Medical History Management");
        title.addClassName("aruclinic-welcome-title");
        add(title);

        // Selector row
        configureSelector();
        
        // Patient details & medical history timeline
        configureLayout();
    }

    private void resolveCurrentDoctor() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                VaadinSession session = VaadinSession.getCurrent();
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
                currentDoctor = doctorService.getDoctorEntityByEmail(email);
            }
            if (currentDoctor == null) {
                List<Doctor> doctors = doctorService.getAllDoctorEntities();
                if (!doctors.isEmpty()) {
                    currentDoctor = doctors.get(0);
                }
            }
        } catch (Exception e) {}
    }

    private void configureSelector() {
        HorizontalLayout selectorRow = new HorizontalLayout();
        selectorRow.setWidthFull();
        selectorRow.setAlignItems(FlexComponent.Alignment.END);

        patientSelector.setWidth("400px");
        patientSelector.setPlaceholder("Search by first name, last name, or email...");
        patientSelector.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")");
        
        // Load all patients
        List<Patient> patients = patientService.getAllPatientEntities();
        patientSelector.setItems(patients);
        patientSelector.setClearButtonVisible(true);

        patientSelector.addValueChangeListener(e -> {
            selectedPatient = e.getValue();
            updatePatientDetails();
        });

        selectorRow.add(patientSelector);
        add(selectorRow);
    }

    private void configureLayout() {
        // Patient Info Panel
        patientInfoContainer.addClassName("aruclinic-patient-profile-card");
        patientInfoContainer.setVisible(false);
        patientInfoContainer.setWidthFull();

        // Medical History Grid
        historyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        historyGrid.setHeight("400px");
        historyGrid.addColumn(MedicalHistoryItemDto::getDate).setHeader("Date").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getTitle).setHeader("Title").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getDoctor).setHeader("Provider").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getDetails).setHeader("Details").setAutoWidth(true);
        historyGrid.addColumn(MedicalHistoryItemDto::getType).setHeader("Type").setAutoWidth(true);

        historyTimelineContainer.setVisible(false);
        historyTimelineContainer.setWidthFull();

        add(patientInfoContainer, historyTimelineContainer);
    }

    private void updatePatientDetails() {
        if (selectedPatient == null) {
            patientInfoContainer.setVisible(false);
            historyTimelineContainer.setVisible(false);
            return;
        }

        // Fetch fresh copy to avoid stale state
        selectedPatient = patientService.getPatientEntityById(selectedPatient.getId());

        patientInfoContainer.removeAll();
        patientInfoContainer.setVisible(true);

        // Render profile card info
        Div header = new Div();
        header.addClassName("profile-card-header");
        H2 name = new H2(selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
        Span role = new Span("Patient Profile");
        role.addClassName("profile-card-role");
        header.add(name, role);

        Div body = new Div();
        body.addClassName("profile-card-body");
        body.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))").set("gap", "var(--aruclinic-spacing-md)");

        body.add(createField("Age", selectedPatient.getAge() + " years"));
        body.add(createField("Gender", selectedPatient.getGender()));
        body.add(createField("Blood Group", selectedPatient.getBloodGroup() != null ? selectedPatient.getBloodGroup() : "N/A"));
        body.add(createField("Mobile", selectedPatient.getMobileNumber()));
        body.add(createField("Email", selectedPatient.getEmail()));
        body.add(createField("Allergies", selectedPatient.getAllergies() != null && !selectedPatient.getAllergies().isEmpty() ? selectedPatient.getAllergies() : "None"));

        patientInfoContainer.add(header, body);

        // Timeline layout
        historyTimelineContainer.removeAll();
        historyTimelineContainer.setVisible(true);

        Span sectionTitle = new Span("Medical Records Timeline");
        sectionTitle.getStyle().set("font-weight", "600").set("font-size", "var(--aruclinic-font-size-lg)").set("color", "var(--aruclinic-primary)");

        Button addRecordBtn = new Button("Add Medical Record", new Icon(VaadinIcon.PLUS));
        addRecordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRecordBtn.addClickListener(e -> openAddRecordDialog());

        HorizontalLayout sectionHeader = new HorizontalLayout(sectionTitle, addRecordBtn);
        sectionHeader.setWidthFull();
        sectionHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        sectionHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        historyTimelineContainer.add(sectionHeader, historyGrid);

        // Load grid items
        ObjectMapper mapper = new ObjectMapper();
        List<MedicalHistoryItemDto> records = new ArrayList<>();
        String json = selectedPatient.getMedicalHistory();
        if (json != null && !json.trim().isEmpty()) {
            try {
                records = mapper.readValue(json, new TypeReference<List<MedicalHistoryItemDto>>() {});
            } catch (Exception ex) {}
        }
        historyGrid.setItems(records);
    }

    private Component createField(String label, String val) {
        Div field = new Div();
        field.getStyle().set("display", "flex").set("flex-direction", "column");
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "var(--aruclinic-font-size-xs)").set("color", "var(--aruclinic-text-muted)");
        Span value = new Span(val);
        value.getStyle().set("font-weight", "500").set("color", "var(--aruclinic-text-primary)");
        field.add(lbl, value);
        return field;
    }

    private void openAddRecordDialog() {
        if (selectedPatient == null) return;

        Dialog addDialog = new Dialog();
        addDialog.setHeaderTitle("Add Medical Record: " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName());
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
                // Fetch fresh copy to avoid optimistic locking / stale data
                Patient freshPatient = patientService.getPatientEntityById(selectedPatient.getId());
                
                ObjectMapper mapper = new ObjectMapper();
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

                // Update selectedPatient reference
                selectedPatient.setMedicalHistory(newJson);

                // Update UI list
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
    }
}
