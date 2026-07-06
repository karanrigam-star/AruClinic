package com.aruclinic.frontend.views.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.dto.PrescriptionItemDto;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.frontend.views.MainLayout;
import java.util.stream.Collectors;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Prescription Form | AruClinic")
@Route(value = "doctor/prescriptions/form", layout = MainLayout.class)
@CssImport("./themes/aruclinic/prescription.css")
public class PrescriptionFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final PrescriptionService prescriptionService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private Long editPrescriptionId = null;
    private Doctor currentDoctor = null;

    // Form fields
    private final ComboBox<Patient> patientSelect = new ComboBox<>("Select Patient");
    private final TextField doctorField = new TextField("Doctor (Author)");
    private final DatePicker visitDate = new DatePicker("Visit Date");
    private final TextField diagnosis = new TextField("Diagnosis");
    private final TextArea symptoms = new TextArea("Symptoms");
    private final TextArea advice = new TextArea("Doctor Notes / Instructions");
    private final DatePicker followUpDate = new DatePicker("Follow-up Date");

    // Medicine entry fields
    private final TextField medName = new TextField("Medicine Name");
    private final TextField medStrength = new TextField("Strength (e.g. 500mg)");
    private final Checkbox medMorning = new Checkbox("Morning");
    private final Checkbox medAfternoon = new Checkbox("Afternoon");
    private final Checkbox medNight = new Checkbox("Night");
    private final RadioButtonGroup<String> medFood = new RadioButtonGroup<>("Food Relation");
    private final IntegerField medDuration = new IntegerField("Duration (Days)");
    private final TextField medRemarks = new TextField("Remarks");

    private final Grid<PrescriptionItemDto> itemsGrid = new Grid<>();
    private final List<PrescriptionItemDto> itemsList = new ArrayList<>();

    private final H1 viewTitle = new H1("Create Prescription");
    private String status = "DRAFT";

    public PrescriptionFormView(PrescriptionService prescriptionService,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository,
                                UserRepository userRepository,
                                NotificationRepository notificationRepository) {
        this.prescriptionService = prescriptionService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-prescription-form-view");

        resolveDoctor();
        configureFormFields();
        configureGrid();

        add(createHeader(), createFormContent());
    }

    private void resolveDoctor() {
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

        if (currentDoctor != null) {
            doctorField.setValue(currentDoctor.getName());
            doctorField.setReadOnly(true);
            doctorField.getStyle().set("font-weight", "bold");
        } else {
            doctorField.setValue("Doctor Entity Not Found");
            doctorField.setReadOnly(true);
        }
    }

    private void configureFormFields() {
        patientSelect.setItems(patientRepository.findAll());
        patientSelect.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getMobileNumber() + ")");
        patientSelect.setRequired(true);

        visitDate.setValue(LocalDate.now());
        visitDate.setRequired(true);

        diagnosis.setRequired(true);
        symptoms.setRequired(true);

        medFood.setItems("Before Food", "After Food");
        medFood.setValue("After Food");
        medFood.getStyle().set("display", "inline-flex").set("flex-direction", "row");
        medDuration.setValue(5);
        medDuration.setMin(1);
    }

    private void configureGrid() {
        itemsGrid.addClassName("aruclinic-prescription-form-grid");
        itemsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        itemsGrid.setItems(itemsList);

        itemsGrid.addColumn(PrescriptionItemDto::getMedicineName).setHeader("Medicine");
        itemsGrid.addColumn(this::parseDosageDisplay).setHeader("Instructions / Dosage");
        itemsGrid.addColumn(PrescriptionItemDto::getDuration).setHeader("Days").setWidth("80px").setFlexGrow(0);
        itemsGrid.addColumn(item -> {
            Button removeBtn = new Button(new Icon(VaadinIcon.TRASH));
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            removeBtn.addClickListener(e -> {
                itemsList.remove(item);
                itemsGrid.getDataProvider().refreshAll();
            });
            return removeBtn;
        }).setHeader("Action").setWidth("100px").setFlexGrow(0);
    }

    private String parseDosageDisplay(PrescriptionItemDto item) {
        if (item.getDosage() == null) return "";
        return item.getDosage()
                .replace("Strength: ", "")
                .replace("Dose: ", "")
                .replace("Food: ", "")
                .replace("Remarks: ", "")
                .replace(" | ", ", ");
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        viewTitle.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button backBtn = new Button("Back to List", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions")));

        header.add(viewTitle, backBtn);
        return header;
    }

    private Component createFormContent() {
        Div container = new Div();
        container.addClassName("aruclinic-prescription-form-container");
        container.setWidthFull();

        FormLayout baseForm = new FormLayout();
        baseForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        baseForm.add(patientSelect, doctorField, visitDate, followUpDate, diagnosis, symptoms);

        // Symptoms full width
        baseForm.setColspan(symptoms, 2);

        // Medicines card
        Div medicinesCard = new Div();
        medicinesCard.addClassName("aruclinic-medicines-card");

        H3 medTitle = new H3("Add Medicines");
        medTitle.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        FormLayout medForm = new FormLayout();
        medForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 4)
        );

        HorizontalLayout checkboxes = new HorizontalLayout(medMorning, medAfternoon, medNight);
        checkboxes.setAlignItems(FlexComponent.Alignment.CENTER);
        checkboxes.getStyle().set("margin-top", "24px");

        medForm.add(medName, medStrength, checkboxes, medFood, medDuration, medRemarks);
        medForm.setColspan(medFood, 2);
        medForm.setColspan(medRemarks, 2);

        Button addMedBtn = new Button("Add to Prescription", new Icon(VaadinIcon.PLUS));
        addMedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addMedBtn.addClickListener(e -> addMedicine());

        HorizontalLayout actionRow = new HorizontalLayout(addMedBtn);
        actionRow.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        medicinesCard.add(medTitle, medForm, actionRow, itemsGrid);

        // Notes and Advice
        FormLayout notesForm = new FormLayout();
        notesForm.add(advice);
        notesForm.setColspan(advice, 2);

        // Footer buttons
        Button saveDraftBtn = new Button("Save Draft", new Icon(VaadinIcon.FILE_TEXT_O));
        saveDraftBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        saveDraftBtn.addClickListener(e -> savePrescription("DRAFT"));

        Button issueBtn = new Button("Issue Prescription", new Icon(VaadinIcon.CHECK_CIRCLE));
        issueBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        issueBtn.getStyle().set("background-color", "var(--aruclinic-success)");
        issueBtn.addClickListener(e -> savePrescription("ISSUED"));

        HorizontalLayout footer = new HorizontalLayout(saveDraftBtn, issueBtn);
        footer.getStyle().set("margin-top", "var(--aruclinic-spacing-xl)");

        container.add(baseForm, medicinesCard, notesForm, footer);
        return container;
    }

    private void addMedicine() {
        String name = medName.getValue().trim();
        if (name.isEmpty()) {
            Notification.show("Please enter a medicine name", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        String strength = medStrength.getValue().trim();
        boolean morn = medMorning.getValue();
        boolean aft = medAfternoon.getValue();
        boolean night = medNight.getValue();
        String food = medFood.getValue();
        Integer duration = medDuration.getValue();

        if (duration == null || duration < 1) {
            Notification.show("Please enter a valid duration", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        // Format dosage details to store in single dosage DB column
        String mVal = morn ? "1" : "0";
        String aVal = aft ? "1" : "0";
        String nVal = night ? "1" : "0";
        String doseString = mVal + "-" + aVal + "-" + nVal;

        String formattedDosage = String.format("Strength: %s | Dose: %s | Food: %s | Remarks: %s",
                strength.isEmpty() ? "N/A" : strength,
                doseString,
                food != null ? food : "After Food",
                medRemarks.getValue().trim().isEmpty() ? "None" : medRemarks.getValue().trim()
        );

        PrescriptionItemDto item = new PrescriptionItemDto(null, null, name, formattedDosage, duration);
        itemsList.add(item);
        itemsGrid.getDataProvider().refreshAll();

        // Clear medicine entry fields
        medName.clear();
        medStrength.clear();
        medMorning.clear();
        medAfternoon.clear();
        medNight.clear();
        medFood.setValue("After Food");
        medDuration.setValue(5);
        medRemarks.clear();
    }

    private void savePrescription(String targetStatus) {
        if (patientSelect.getValue() == null) {
            Notification.show("Please select a Patient", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        if (diagnosis.getValue().trim().isEmpty()) {
            Notification.show("Please enter a Diagnosis", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        if (symptoms.getValue().trim().isEmpty()) {
            Notification.show("Please enter Symptoms", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        if (itemsList.isEmpty()) {
            Notification.show("Please add at least one medicine", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        if (currentDoctor == null) {
            Notification.show("Doctor entity not resolved. Cannot save.", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        try {
            PrescriptionDto dto = new PrescriptionDto();
            dto.setPatientId(patientSelect.getValue().getId());
            dto.setDoctorId(currentDoctor.getId());
            dto.setPrescriptionDate(visitDate.getValue());
            dto.setFollowUpDate(followUpDate.getValue());
            dto.setDiagnosis(diagnosis.getValue().trim());
            dto.setSymptoms(symptoms.getValue().trim());
            dto.setAdvice(advice.getValue().trim());
            dto.setStatus(targetStatus);
            dto.setItems(itemsList);

            if (editPrescriptionId == null) {
                prescriptionService.createPrescription(dto);
                Notification.show("Prescription created successfully!", 2000, Notification.Position.TOP_CENTER);
            } else {
                prescriptionService.updatePrescription(editPrescriptionId, dto);
                Notification.show("Prescription updated successfully!", 2000, Notification.Position.TOP_CENTER);
            }

            getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions"));
        } catch (Exception ex) {
            Notification.show("Error saving prescription: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            try {
                if (parameter.startsWith("patient-")) {
                    Long patId = Long.parseLong(parameter.replace("patient-", ""));
                    patientRepository.findById(patId).ifPresent(patientSelect::setValue);
                    return;
                }

                // Check if duplicating
                boolean isDuplicate = false;
                Long targetId = null;
                if (parameter.startsWith("duplicate-")) {
                    targetId = Long.parseLong(parameter.replace("duplicate-", ""));
                    isDuplicate = true;
                } else {
                    targetId = Long.parseLong(parameter);
                }

                PrescriptionDto existing = prescriptionService.getPrescriptionById(targetId);
                if (existing != null) {
                    diagnosis.setValue(existing.getDiagnosis());
                    symptoms.setValue(existing.getSymptoms());
                    advice.setValue(existing.getAdvice() != null ? existing.getAdvice() : "");
                    followUpDate.setValue(existing.getFollowUpDate());

                    // Find corresponding Patient in select
                    patientRepository.findById(existing.getPatientId()).ifPresent(patientSelect::setValue);

                    // Load medicines
                    itemsList.clear();
                    if (existing.getItems() != null) {
                        for (PrescriptionItemDto it : existing.getItems()) {
                            itemsList.add(new PrescriptionItemDto(null, null, it.getMedicineName(), it.getDosage(), it.getDuration()));
                        }
                    }
                    itemsGrid.getDataProvider().refreshAll();

                    if (isDuplicate) {
                        editPrescriptionId = null;
                        viewTitle.setText("Duplicate Prescription");
                    } else {
                        editPrescriptionId = targetId;
                        status = existing.getStatus();
                        viewTitle.setText("Edit Prescription - " + existing.getPrescriptionId());
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid ID
            }
        } else {
            // Check session for duplicate shortcuts
            Long dupId = (Long) VaadinSession.getCurrent().getAttribute("duplicate_prescription_id");
            if (dupId != null) {
                VaadinSession.getCurrent().setAttribute("duplicate_prescription_id", null);
                getUI().ifPresent(ui -> ui.getPage().getHistory().replaceState(null, "doctor/prescriptions/form/duplicate-" + dupId));
            }
        }
    }
}
