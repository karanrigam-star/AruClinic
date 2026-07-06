package com.aruclinic.frontend.views.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.dto.PrescriptionItemDto;
import com.aruclinic.entity.Patient;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.frontend.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@PageTitle("View Prescription | AruClinic")
@Route(value = "prescriptions/view", layout = MainLayout.class)
@CssImport("./themes/aruclinic/prescription.css")
public class ViewPrescriptionView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PrescriptionService prescriptionService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private Long prescriptionId;
    private PrescriptionDto prescription;

    // View Components
    private final Span idSpan = new Span();
    private final Span dateSpan = new Span();
    private final Span statusBadge = new Span();

    private final Span patientName = new Span();
    private final Span patientAgeGender = new Span();
    private final Span patientMobile = new Span();

    private final Span doctorName = new Span();
    private final Span doctorQualSpec = new Span();
    private final Span doctorDept = new Span();
    private final Span doctorSignName = new Span();

    private final Span diagnosis = new Span();
    private final Span symptoms = new Span();
    private final Paragraph advice = new Paragraph();
    private final Span followUpDate = new Span();

    private final Grid<ParsedMedicine> medGrid = new Grid<>();
    private final List<ParsedMedicine> medList = new ArrayList<>();

    private final HorizontalLayout actionRow = new HorizontalLayout();

    public ViewPrescriptionView(PrescriptionService prescriptionService,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository) {
        this.prescriptionService = prescriptionService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-prescription-view-details");

        configureGrid();
        add(createHeader(), createPrescriptionSheet());
    }

    private void configureGrid() {
        medGrid.addClassName("aruclinic-prescription-view-grid");
        medGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        medGrid.setItems(medList);

        medGrid.addColumn(ParsedMedicine::getName).setHeader("Medicine Name");
        medGrid.addColumn(ParsedMedicine::getStrength).setHeader("Strength");
        medGrid.addColumn(ParsedMedicine::getDose).setHeader("Dosage (M-A-N)");
        medGrid.addColumn(ParsedMedicine::getFood).setHeader("Food Relation");
        medGrid.addColumn(ParsedMedicine::getDuration).setHeader("Duration (Days)");
        medGrid.addColumn(ParsedMedicine::getRemarks).setHeader("Remarks");

        medGrid.setHeight("auto");
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Prescription Details");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button backBtn = new Button("Back", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> goBack());

        header.add(title, backBtn);
        return header;
    }

    private Component createPrescriptionSheet() {
        Div sheet = new Div();
        sheet.addClassName("aruclinic-prescription-sheet");

        // Clinic Branding Section
        Div brandRow = new Div();
        brandRow.addClassName("prescription-brand-row");

        Div brandLeft = new Div();
        H2 clinicName = new H2("ARUCLINIC");
        clinicName.getStyle().set("color", "var(--aruclinic-primary)").set("margin", "0");
        Span clinicAddress = new Span("123 Healthcare Plaza, Medical District, City");
        clinicAddress.getStyle().set("font-size", "var(--aruclinic-font-size-xs)").set("color", "var(--aruclinic-text-muted)");
        brandLeft.add(clinicName, clinicAddress);

        Div brandRight = new Div();
        brandRight.getStyle().set("text-align", "right");
        H3 rxTitle = new H3("PRESCRIPTION");
        rxTitle.getStyle().set("margin", "0").set("color", "var(--aruclinic-primary)");
        brandRight.add(rxTitle);

        brandRow.add(brandLeft, brandRight);

        // Meta Info Block
        Div metaInfo = new Div();
        metaInfo.addClassName("prescription-meta-block");

        Div metaItem1 = new Div(new Span("Prescription ID: "), idSpan);
        Div metaItem2 = new Div(new Span("Date: "), dateSpan);
        Div metaItem3 = new Div(new Span("Status: "), statusBadge);
        metaInfo.add(metaItem1, metaItem2, metaItem3);

        // Doctor / Patient Info Grid
        Div infoGrid = new Div();
        infoGrid.addClassName("prescription-info-grid");

        Div patientCol = new Div();
        H4 pTitle = new H4("PATIENT DETAILS");
        pTitle.getStyle().set("color", "var(--aruclinic-primary)");
        Div pNameDiv = new Div(new Span("Name: "), patientName);
        Div pAgeDiv = new Div(new Span("Age/Gender: "), patientAgeGender);
        Div pMobDiv = new Div(new Span("Mobile: "), patientMobile);
        patientCol.add(pTitle, pNameDiv, pAgeDiv, pMobDiv);

        Div doctorCol = new Div();
        H4 dTitle = new H4("DOCTOR DETAILS");
        dTitle.getStyle().set("color", "var(--aruclinic-primary)");
        Div dNameDiv = new Div(new Span("Name: "), doctorName);
        Div dQualDiv = new Div(new Span("Qualification: "), doctorQualSpec);
        Div dDeptDiv = new Div(new Span("Department: "), doctorDept);
        doctorCol.add(dTitle, dNameDiv, dQualDiv, dDeptDiv);

        infoGrid.add(patientCol, doctorCol);

        // Clinical Details Block
        Div clinicalBlock = new Div();
        clinicalBlock.addClassName("prescription-clinical-block");
        H4 cTitle = new H4("CLINICAL EXAMINATION");
        cTitle.getStyle().set("color", "var(--aruclinic-primary)");
        Div symDiv = new Div(new Span("Symptoms: "), symptoms);
        Div diagDiv = new Div(new Span("Diagnosis: "), diagnosis);
        clinicalBlock.add(cTitle, symDiv, diagDiv);

        // Medicines Block
        Div medBlock = new Div();
        medBlock.addClassName("prescription-medicines-block");
        H4 mTitle = new H4("PRESCRIBED MEDICINES (Rx)");
        mTitle.getStyle().set("color", "var(--aruclinic-primary)");
        medBlock.add(mTitle, medGrid);

        // Advice and Instructions
        Div adviceBlock = new Div();
        adviceBlock.addClassName("prescription-advice-block");
        H4 aTitle = new H4("INSTRUCTIONS & ADVICE");
        aTitle.getStyle().set("color", "var(--aruclinic-primary)");
        adviceBlock.add(aTitle, advice);

        // Follow-up
        Div followUpBlock = new Div();
        followUpBlock.addClassName("prescription-followup-block");
        H4 fTitle = new H4("FOLLOW-UP");
        fTitle.getStyle().set("color", "var(--aruclinic-primary)");
        Div fDateDiv = new Div(new Span("Next Visit Date: "), followUpDate);
        followUpBlock.add(fTitle, fDateDiv);

        // Signature area
        Div signatureRow = new Div();
        signatureRow.addClassName("prescription-signature-row");
        doctorSignName.getStyle().set("font-weight", "bold").set("font-size", "1.1rem").set("color", "var(--aruclinic-primary)");
        Span sigLabel = new Span("Authorized Signature");
        sigLabel.getStyle().set("font-size", "0.85rem").set("color", "var(--aruclinic-text-muted)");
        signatureRow.add(doctorSignName, sigLabel);

        // Action Toolbar
        actionRow.addClassName("prescription-action-row");
        actionRow.getStyle().set("margin-top", "var(--aruclinic-spacing-xl)");

        sheet.add(brandRow, metaInfo, infoGrid, clinicalBlock, medBlock, adviceBlock, followUpBlock, signatureRow, actionRow);
        return sheet;
    }

    private void populateDetails() {
        if (prescription == null) return;

        idSpan.setText(prescription.getPrescriptionId());
        dateSpan.setText(prescription.getPrescriptionDate().toString());
        statusBadge.setText(prescription.getStatus());
        statusBadge.setClassName("status-badge " + prescription.getStatus().toLowerCase());

        patientName.setText(prescription.getPatientName());
        patientRepository.findById(prescription.getPatientId()).ifPresent(p -> {
            patientAgeGender.setText(p.getAge() + " yrs / " + p.getGender());
            patientMobile.setText(p.getMobileNumber());
        });

        doctorName.setText(prescription.getDoctorName());
        doctorName.getStyle().set("font-weight", "bold");
        doctorSignName.setText("Dr. " + prescription.getDoctorName());
        doctorRepository.findById(prescription.getDoctorId()).ifPresent(d -> {
            doctorQualSpec.setText(d.getQualification() + " (" + d.getSpecialization() + ")");
            doctorDept.setText(d.getDepartment());
        });

        diagnosis.setText(prescription.getDiagnosis());
        symptoms.setText(prescription.getSymptoms());
        advice.setText(prescription.getAdvice() != null && !prescription.getAdvice().trim().isEmpty() ? prescription.getAdvice() : "None");
        followUpDate.setText(prescription.getFollowUpDate() != null ? prescription.getFollowUpDate().toString() : "No follow-up scheduled");

        medList.clear();
        if (prescription.getItems() != null) {
            for (PrescriptionItemDto it : prescription.getItems()) {
                medList.add(parseDosage(it));
            }
        }
        medGrid.getDataProvider().refreshAll();

        configureActions();
    }

    private ParsedMedicine parseDosage(PrescriptionItemDto item) {
        ParsedMedicine parsed = new ParsedMedicine();
        parsed.setName(item.getMedicineName());
        parsed.setDuration(String.valueOf(item.getDuration()));

        String dosage = item.getDosage();
        if (dosage != null) {
            String[] parts = dosage.split(" \\| ");
            for (String part : parts) {
                if (part.startsWith("Strength: ")) {
                    parsed.setStrength(part.replace("Strength: ", ""));
                } else if (part.startsWith("Dose: ")) {
                    parsed.setDose(part.replace("Dose: ", ""));
                } else if (part.startsWith("Food: ")) {
                    parsed.setFood(part.replace("Food: ", ""));
                } else if (part.startsWith("Remarks: ")) {
                    parsed.setRemarks(part.replace("Remarks: ", ""));
                }
            }
        }
        return parsed;
    }

    private void configureActions() {
        actionRow.removeAll();

        Button printBtn = new Button("Print", new Icon(VaadinIcon.PRINT));
        printBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        printBtn.addClickListener(e -> printPrescription());

        Button downloadBtn = new Button("Download PDF", new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadBtn.addClickListener(e -> downloadPdf());

        actionRow.add(printBtn, downloadBtn);

        String userRole = getUserRole();
        if ("DOCTOR".equalsIgnoreCase(userRole)) {
            // Edit Draft Button
            if ("DRAFT".equalsIgnoreCase(prescription.getStatus())) {
                Button editBtn = new Button("Edit Draft", new Icon(VaadinIcon.EDIT));
                editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                editBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form/" + prescriptionId)));
                actionRow.add(editBtn);
            }

            // Duplicate Button
            Button dupBtn = new Button("Duplicate Rx", new Icon(VaadinIcon.COPY));
            dupBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dupBtn.addClickListener(e -> {
                VaadinSession.getCurrent().setAttribute("duplicate_prescription_id", prescriptionId);
                getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form"));
            });
            actionRow.add(dupBtn);
        }
    }

    private void printPrescription() {
        UI.getCurrent().getPage().executeJs("window.print();");
    }

    private void downloadPdf() {
        if (prescription == null) {
            Notification.show("Prescription not loaded yet", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        String filename = "Prescription_" + prescription.getPrescriptionId() + ".pdf";

        // Collect item descriptions
        String items = prescription.getItems() != null ? prescription.getItems().stream()
                .map(item -> item.getMedicineName() + " (" + item.getDosage() + ")")
                .collect(Collectors.joining(", ")) : "None";

        StreamResource resource = new StreamResource(filename, () -> {
            try {
                return com.aruclinic.util.PdfHelper.generatePrescriptionPdf(
                    prescription.getPrescriptionId(),
                    prescription.getPatientName(),
                    prescription.getDoctorName(),
                    prescription.getPrescriptionDate() != null ? prescription.getPrescriptionDate().toString() : java.time.LocalDate.now().toString(),
                    prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "N/A",
                    items
                );
            } catch (Exception e) {
                return new java.io.ByteArrayInputStream(new byte[0]);
            }
        });

        resource.setContentType("application/pdf");

        // Create anchor element for download
        com.vaadin.flow.component.html.Anchor downloadLink = new com.vaadin.flow.component.html.Anchor();
        downloadLink.setHref(resource);
        downloadLink.getElement().setAttribute("download", filename);

        // Trigger download
        downloadLink.getElement().executeJs("this.click()");

        Notification.show("Downloading prescription as PDF...", 2000, Notification.Position.TOP_CENTER);
    }

    private String generatePrescriptionText() {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("===================================================\n");
        sb.append("ARUCLINIC PRESCRIPTION\n");
        sb.append("===================================================\n\n");

        // Prescription Info
        sb.append("Prescription ID: ").append(prescription.getPrescriptionId()).append("\n");
        sb.append("Date: ").append(prescription.getPrescriptionDate()).append("\n");
        sb.append("Status: ").append(prescription.getStatus()).append("\n\n");

        // Patient Details
        sb.append("---------------------------------------------------\n");
        sb.append("PATIENT DETAILS\n");
        sb.append("---------------------------------------------------\n");
        sb.append("Name: ").append(prescription.getPatientName()).append("\n");

        Patient patient = patientRepository.findById(prescription.getPatientId()).orElse(null);
        if (patient != null) {
            sb.append("Age/Gender: ").append(patient.getAge()).append(" yrs / ").append(patient.getGender()).append("\n");
            sb.append("Mobile: ").append(patient.getMobileNumber()).append("\n");
        }
        sb.append("\n");

        // Doctor Details
        sb.append("---------------------------------------------------\n");
        sb.append("DOCTOR DETAILS\n");
        sb.append("---------------------------------------------------\n");
        sb.append("Name: ").append(prescription.getDoctorName()).append("\n");

        com.aruclinic.entity.Doctor doctor = doctorRepository.findById(prescription.getDoctorId()).orElse(null);
        if (doctor != null) {
            sb.append("Qualification: ").append(doctor.getQualification()).append("\n");
            sb.append("Specialization: ").append(doctor.getSpecialization()).append("\n");
            sb.append("Department: ").append(doctor.getDepartment()).append("\n");
        }
        sb.append("\n");

        // Clinical Details
        sb.append("---------------------------------------------------\n");
        sb.append("CLINICAL EXAMINATION\n");
        sb.append("---------------------------------------------------\n");
        sb.append("Symptoms: ").append(prescription.getSymptoms()).append("\n");
        sb.append("Diagnosis: ").append(prescription.getDiagnosis()).append("\n\n");

        // Medicines
        sb.append("---------------------------------------------------\n");
        sb.append("PRESCRIBED MEDICINES (Rx)\n");
        sb.append("---------------------------------------------------\n");

        if (prescription.getItems() != null && !prescription.getItems().isEmpty()) {
            int count = 1;
            for (PrescriptionItemDto item : prescription.getItems()) {
                sb.append(count).append(". ").append(item.getMedicineName()).append("\n");
                sb.append("   Dosage: ").append(item.getDosage()).append("\n");
                sb.append("   Duration: ").append(item.getDuration()).append(" days\n\n");
                count++;
            }
        } else {
            sb.append("No medicines prescribed\n\n");
        }

        // Advice
        sb.append("---------------------------------------------------\n");
        sb.append("INSTRUCTIONS & ADVICE\n");
        sb.append("---------------------------------------------------\n");
        sb.append(prescription.getAdvice() != null && !prescription.getAdvice().trim().isEmpty() ?
                   prescription.getAdvice() : "None").append("\n\n");

        // Follow-up
        sb.append("---------------------------------------------------\n");
        sb.append("FOLLOW-UP\n");
        sb.append("---------------------------------------------------\n");
        sb.append("Next Visit Date: ").append(prescription.getFollowUpDate() != null ?
                   prescription.getFollowUpDate().toString() : "No follow-up scheduled").append("\n");

        sb.append("\n===================================================\n");
        sb.append("Doctor Signature: _________________________\n");
        sb.append("===================================================\n");

        return sb.toString();
    }

    private void goBack() {
        String role = getUserRole();
        if ("DOCTOR".equalsIgnoreCase(role)) {
            getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions"));
        } else if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            getUI().ifPresent(ui -> ui.navigate("admin/prescriptions"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("patient/prescriptions"));
        }
    }

    private void syncSecurityContext() {
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
        } catch (Exception e) {
            // Ignore
        }
    }

    private String getUserRole() {
        syncSecurityContext();
        try {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("PATIENT");
        } catch (Exception e) {
            return "PATIENT";
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        syncSecurityContext();
        this.prescriptionId = parameter;
        try {
            this.prescription = prescriptionService.getPrescriptionById(prescriptionId);

            // Role-based security validation
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isPatient = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_PATIENT"));

            if (isPatient) {
                Patient patient = patientRepository.findByEmail(name).orElse(null);
                if (patient != null && !prescription.getPatientId().equals(patient.getId())) {
                    Notification.show("Access Denied: You can only view your own prescriptions", 3000, Notification.Position.TOP_CENTER);
                    event.forwardTo("patient");
                    return;
                }
            }

            populateDetails();
        } catch (Exception ex) {
            Notification.show("Error loading prescription: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            event.forwardTo("");
        }
    }

    // Parsed medicine display DTO
    public static class ParsedMedicine {
        private String name;
        private String strength = "N/A";
        private String dose = "N/A";
        private String food = "After Food";
        private String duration = "0";
        private String remarks = "None";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStrength() { return strength; }
        public void setStrength(String strength) { this.strength = strength; }
        public String getDose() { return dose; }
        public void setDose(String dose) { this.dose = dose; }
        public String getFood() { return food; }
        public void setFood(String food) { this.food = food; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}
