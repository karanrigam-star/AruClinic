package com.aruclinic.view.patient;

import com.aruclinic.dto.PatientDto;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.BillingService;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.aruclinic.util.PdfHelper;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Patient Profile view for displaying and editing patient information.
 */
@PageTitle("Patient Profile | AruClinic")
@Route("patient/profile")
@CssImport("./themes/aruclinic/patient.css")
public class PatientProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final PatientService patientService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BillingService billingService;
    private final com.aruclinic.service.PrescriptionService prescriptionService;

    private com.aruclinic.entity.User currentUser = null;
    private PatientDto patientDto;
    private boolean editMode = false;

    // Form inputs for edit mode
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker dobField;
    private Select<String> genderField;
    private Select<String> bloodTypeField;
    private TextField addressField;
    private TextField cityField;
    private TextField stateField;
    private TextField zipCodeField;
    private TextField emailField;
    private TextField mobileField;
    private TextField emergencyNameField;
    private TextField emergencyPhoneField;
    private TextField allergiesField;

    public PatientProfileView(PatientService patientService,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository,
                              BillingService billingService,
                              com.aruclinic.service.PrescriptionService prescriptionService) {
        this.patientService = patientService;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.billingService = billingService;
        this.prescriptionService = prescriptionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolveCurrentPatient();
        removeAll();
        if (patientDto != null) {
            add(createProfileContent());
        } else {
            add(new Span("Patient record not found. Please log in."));
        }
    }

    private void resolveCurrentPatient() {
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
                currentUser = userRepository.findByEmail(email).orElse(null);
                patientDto = patientService.getPatientByEmail(email);
            }

            // Fallback for testing/debugging
            if (patientDto == null) {
                List<PatientDto> patients = patientService.getAllPatients();
                if (!patients.isEmpty()) {
                    patientDto = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private Component createProfileContent() {
        Div content = new Div();
        content.addClassName("aruclinic-patient-profile");
        content.setWidthFull();

        // Header with profile picture and info
        content.add(createProfileHeader());

        // Tabs
        content.add(createProfileTabs());

        return content;
    }

    private Component createProfileHeader() {
        Div header = new Div();
        header.addClassName("aruclinic-profile-header");
        header.setWidthFull();

        // Avatar section
        Div avatarSection = new Div();
        avatarSection.addClassName("aruclinic-profile-avatar-section");

        Div avatar = new Div();
        avatar.addClassName("aruclinic-profile-avatar");
        
        String initials = "P";
        if (patientDto.getFirstName() != null && !patientDto.getFirstName().isEmpty() &&
            patientDto.getLastName() != null && !patientDto.getLastName().isEmpty()) {
            initials = patientDto.getFirstName().substring(0,1).toUpperCase() + 
                       patientDto.getLastName().substring(0,1).toUpperCase();
        }
        avatar.setText(initials);

        Button editAvatarBtn = new Button(new Icon(VaadinIcon.CAMERA));
        editAvatarBtn.addClassName("aruclinic-profile-avatar-edit");

        avatarSection.add(avatar, editAvatarBtn);

        // Info section
        Div infoSection = new Div();
        infoSection.addClassName("aruclinic-profile-info-section");

        H1 name = new H1(patientDto.getFirstName() + " " + patientDto.getLastName());
        name.addClassName("aruclinic-profile-name");

        Span patientId = new Span("Patient ID: " + patientDto.getPatientId());
        patientId.addClassName("aruclinic-profile-id");

        // Status
        Div status = new Div();
        status.addClassName("aruclinic-profile-status");
        status.addClassName("active");
        status.add(new Icon(VaadinIcon.CHECK), new Span("Active"));

        // Contact info
        Div contact = new Div();
        contact.addClassName("aruclinic-profile-contact");

        Div emailItem = new Div();
        emailItem.addClassName("aruclinic-profile-contact-item");
        emailItem.add(new Icon(VaadinIcon.MAILBOX), new Span(patientDto.getEmail()));

        Div phoneItem = new Div();
        phoneItem.addClassName("aruclinic-profile-contact-item");
        phoneItem.add(new Icon(VaadinIcon.PHONE), new Span(patientDto.getMobile()));

        contact.add(emailItem, phoneItem);

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-profile-actions");

        if (editMode) {
            Button saveBtn = new Button("Save Changes", new Icon(VaadinIcon.CHECK));
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            saveBtn.addClassName("aruclinic-btn");
            saveBtn.addClassName("aruclinic-btn-primary");
            saveBtn.addClickListener(e -> handleSaveChanges());

            Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelBtn.addClassName("aruclinic-btn");
            cancelBtn.addClassName("aruclinic-btn-secondary");
            cancelBtn.addClickListener(e -> {
                editMode = false;
                removeAll();
                add(createProfileContent());
            });

            actions.add(saveBtn, cancelBtn);
        } else {
            Button editProfileBtn = new Button("Edit Profile", new Icon(VaadinIcon.EDIT));
            editProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editProfileBtn.addClassName("aruclinic-btn");
            editProfileBtn.addClassName("aruclinic-btn-primary");
            editProfileBtn.addClickListener(e -> {
                editMode = true;
                removeAll();
                add(createProfileContent());
            });

            Button printBtn = new Button("Print", new Icon(VaadinIcon.PRINT));
            printBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            printBtn.addClassName("aruclinic-btn");
            printBtn.addClassName("aruclinic-btn-secondary");

            Button backBtn = new Button("Back", new Icon(VaadinIcon.ARROW_LEFT));
            backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backBtn.addClassName("aruclinic-btn");
            backBtn.addClassName("aruclinic-btn-secondary");
            backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient")));

            actions.add(editProfileBtn, printBtn, backBtn);
        }

        infoSection.add(name, patientId, status, contact, actions);
        header.add(avatarSection, infoSection);

        return header;
    }

    private Component createProfileTabs() {
        Div tabsContainer = new Div();
        tabsContainer.setWidthFull();

        // Create tabs
        Tabs tabs = new Tabs();
        tabs.addClassName("aruclinic-profile-tabs");

        Tab overviewTab = new Tab("Overview");
        Tab medicalTab = new Tab("Medical History");
        Tab billingTab = new Tab("Billing");
        Tab documentsTab = new Tab("Documents");

        tabs.add(overviewTab, medicalTab, billingTab, documentsTab);

        // Create tab panels
        Div overviewPanel = createOverviewPanel();
        overviewPanel.addClassName("aruclinic-profile-tab-panel");
        overviewPanel.addClassName("active");

        Div medicalPanel = createMedicalPanel();
        medicalPanel.addClassName("aruclinic-profile-tab-panel");

        Div billingPanel = createBillingPanel();
        billingPanel.addClassName("aruclinic-profile-tab-panel");

        Div documentsPanel = createDocumentsPanel();
        documentsPanel.addClassName("aruclinic-profile-tab-panel");

        tabs.addSelectedChangeListener(event -> {
            overviewPanel.removeClassName("active");
            medicalPanel.removeClassName("active");
            billingPanel.removeClassName("active");
            documentsPanel.removeClassName("active");

            Tab selectedTab = event.getSelectedTab();
            if (selectedTab == overviewTab) {
                overviewPanel.addClassName("active");
            } else if (selectedTab == medicalTab) {
                medicalPanel.addClassName("active");
            } else if (selectedTab == billingTab) {
                billingPanel.addClassName("active");
            } else if (selectedTab == documentsTab) {
                documentsPanel.addClassName("active");
            }
        });

        tabsContainer.add(tabs, overviewPanel, medicalPanel, billingPanel, documentsPanel);

        return tabsContainer;
    }

    private Div createOverviewPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        // Personal Information
        panel.add(createDetailCard("Personal Information", createPersonalInfoContent()));

        // Contact Information
        panel.add(createDetailCard("Contact Information", createContactInfoContent()));

        // Emergency Contact
        panel.add(createDetailCard("Emergency Contact", createEmergencyContactContent()));

        return panel;
    }

    private Component createDetailCard(String title, Component content) {
        Div card = new Div();
        card.addClassName("aruclinic-profile-detail-card");
        card.setWidthFull();

        H2 cardTitle = new H2(title);
        cardTitle.addClassName("aruclinic-profile-detail-card-title");

        card.add(cardTitle, content);
        return card;
    }

    private Component createPersonalInfoContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        if (editMode) {
            firstNameField = new TextField();
            firstNameField.setValue(patientDto.getFirstName() != null ? patientDto.getFirstName() : "");
            lastNameField = new TextField();
            lastNameField.setValue(patientDto.getLastName() != null ? patientDto.getLastName() : "");
            dobField = new DatePicker();
            dobField.setValue(patientDto.getDateOfBirth());
            
            genderField = new Select<>();
            genderField.setItems("Male", "Female", "Other");
            genderField.setValue(patientDto.getGender() != null ? patientDto.getGender() : "Male");

            bloodTypeField = new Select<>();
            bloodTypeField.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
            bloodTypeField.setValue(patientDto.getBloodType() != null ? patientDto.getBloodType() : "O+");

            content.add(createEditableDetailItem("First Name", firstNameField));
            content.add(createEditableDetailItem("Last Name", lastNameField));
            content.add(createEditableDetailItem("Date of Birth", dobField));
            content.add(createEditableDetailItem("Gender", genderField));
            content.add(createEditableDetailItem("Blood Type", bloodTypeField));
        } else {
            content.add(createDetailItem("Date of Birth", patientDto.getDateOfBirth() != null ? patientDto.getDateOfBirth().toString() : "N/A"));
            content.add(createDetailItem("Gender", patientDto.getGender()));
            content.add(createDetailItem("Age", patientDto.getDateOfBirth() != null ? calculateAge(patientDto.getDateOfBirth()) + " years" : "N/A"));
            content.add(createDetailItem("Blood Type", patientDto.getBloodType()));
        }

        return content;
    }

    private Component createContactInfoContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        if (editMode) {
            addressField = new TextField();
            addressField.setValue(patientDto.getAddress() != null ? patientDto.getAddress() : "");
            cityField = new TextField();
            cityField.setValue(patientDto.getCity() != null ? patientDto.getCity() : "");
            stateField = new TextField();
            stateField.setValue(patientDto.getState() != null ? patientDto.getState() : "");
            zipCodeField = new TextField();
            zipCodeField.setValue(patientDto.getZipCode() != null ? patientDto.getZipCode() : "");
            emailField = new TextField();
            emailField.setValue(patientDto.getEmail() != null ? patientDto.getEmail() : "");
            mobileField = new TextField();
            mobileField.setValue(patientDto.getMobile() != null ? patientDto.getMobile() : "");

            content.add(createEditableDetailItem("Address", addressField));
            content.add(createEditableDetailItem("City", cityField));
            content.add(createEditableDetailItem("State", stateField));
            content.add(createEditableDetailItem("ZIP Code", zipCodeField));
            content.add(createEditableDetailItem("Email", emailField));
            content.add(createEditableDetailItem("Mobile", mobileField));
        } else {
            content.add(createDetailItem("Address", patientDto.getAddress()));
            content.add(createDetailItem("City", patientDto.getCity() != null ? patientDto.getCity() : "N/A"));
            content.add(createDetailItem("State", patientDto.getState() != null ? patientDto.getState() : "N/A"));
            content.add(createDetailItem("ZIP Code", patientDto.getZipCode() != null ? patientDto.getZipCode() : "N/A"));
        }

        return content;
    }

    private Component createEmergencyContactContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        if (editMode) {
            emergencyNameField = new TextField();
            emergencyNameField.setValue(patientDto.getEmergencyContact() != null ? patientDto.getEmergencyContact() : "");
            emergencyPhoneField = new TextField();
            emergencyPhoneField.setValue(patientDto.getEmergencyPhone() != null ? patientDto.getEmergencyPhone() : "");
            allergiesField = new TextField();
            allergiesField.setValue(patientDto.getAllergies() != null ? patientDto.getAllergies() : "");

            content.add(createEditableDetailItem("Emergency Contact Name", emergencyNameField));
            content.add(createEditableDetailItem("Emergency Phone", emergencyPhoneField));
            content.add(createEditableDetailItem("Allergies", allergiesField));
        } else {
            content.add(createDetailItem("Emergency Contact Name", patientDto.getEmergencyContact()));
            content.add(createDetailItem("Emergency Phone", patientDto.getEmergencyPhone() != null ? patientDto.getEmergencyPhone() : "N/A"));
        }

        return content;
    }

    private Component createDetailItem(String label, String value) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-profile-detail-label");

        Span valueSpan = new Span(value != null && !value.isEmpty() ? value : "N/A");
        valueSpan.addClassName("aruclinic-profile-detail-value");

        item.add(labelSpan, valueSpan);
        return item;
    }

    private Component createEditableDetailItem(String label, Component inputField) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-profile-detail-label");

        inputField.getElement().getStyle().set("width", "100%");
        inputField.getElement().getStyle().set("font-size", "14px");
        inputField.getElement().getStyle().set("min-height", "auto");
        inputField.getElement().getStyle().set("padding", "0");

        item.add(labelSpan, inputField);
        return item;
    }

    private Div createMedicalPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        // Medical History
        panel.add(createDetailCard("Medical History", createMedicalHistoryContent()));

        // Allergies
        panel.add(createDetailCard("Allergies", createAllergiesContent()));

        return panel;
    }

    private Component createMedicalHistoryContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        // Sample medical history items
        content.add(createDetailItem("Last Visit", "June 1, 2025"));
        content.add(createDetailItem("Diagnosis", "Hypertension"));
        content.add(createDetailItem("Treatment", "Lisinopril 10mg daily"));
        content.add(createDetailItem("Last Blood Test", "May 15, 2025"));

        return content;
    }

    private Component createAllergiesContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        content.add(createDetailItem("Known Allergies", patientDto.getAllergies()));

        return content;
    }

    private Div createBillingPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        // Billing Information
        panel.add(createDetailCard("Billing Information", createBillingContent()));

        // Payment History
        panel.add(createDetailCard("Payment History", createPaymentHistoryContent()));

        return panel;
    }

    private Component createBillingContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        content.add(createDetailItem("Insurance Provider", "HealthCare Inc. (Default)"));
        content.add(createDetailItem("Policy Number", "HC-" + (patientDto.getId() != null ? patientDto.getId() : "1023")));
        
        String address = patientDto.getAddress() != null ? patientDto.getAddress() : "";
        if (patientDto.getCity() != null) address += ", " + patientDto.getCity();
        if (patientDto.getState() != null) address += ", " + patientDto.getState();
        content.add(createDetailItem("Billing Address", address));

        return content;
    }

    private Component createPaymentHistoryContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        List<com.aruclinic.dto.BillDto> bills = new ArrayList<>();
        try {
            bills = billingService.getBillsByPatientId(patientDto.getId());
        } catch (Exception e) {
            // Ignore
        }

        com.aruclinic.dto.BillDto latestPaid = bills.stream()
            .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
            .sorted((b1, b2) -> {
                LocalDate d1 = b1.getInvoiceDate() != null ? b1.getInvoiceDate() : LocalDate.MIN;
                LocalDate d2 = b2.getInvoiceDate() != null ? b2.getInvoiceDate() : LocalDate.MIN;
                return d2.compareTo(d1);
            })
            .findFirst()
            .orElse(null);

        java.math.BigDecimal outstanding = bills.stream()
            .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()) || "UNPAID".equalsIgnoreCase(b.getStatus()))
            .map(b -> b.getTotal() != null ? b.getTotal() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        if (latestPaid != null) {
            String dateStr = latestPaid.getInvoiceDate() != null ? latestPaid.getInvoiceDate().toString() : "N/A";
            content.add(createDetailItem("Last Payment", dateStr + " - ₹" + latestPaid.getTotal() + " (" + latestPaid.getInvoiceId() + ")"));
            content.add(createDetailItem("Payment Method", latestPaid.getPaymentMethod() != null ? latestPaid.getPaymentMethod() : "N/A"));
        } else {
            content.add(createDetailItem("Last Payment", "No payments recorded"));
            content.add(createDetailItem("Payment Method", "N/A"));
        }

        content.add(createDetailItem("Outstanding Balance", "₹" + outstanding.toString()));

        return content;
    }

    private Div createDocumentsPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        Div documentsCard = new Div();
        documentsCard.addClassName("aruclinic-profile-detail-card");
        documentsCard.setWidthFull();

        H2 cardTitle = new H2("Documents");
        cardTitle.addClassName("aruclinic-profile-detail-card-title");

        Div documentsList = new Div();
        documentsList.addClassName("aruclinic-profile-detail-list");

        // Load all prescriptions as documents
        try {
            List<com.aruclinic.dto.PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByPatientId(patientDto.getId());
            for (com.aruclinic.dto.PrescriptionDto p : prescriptions) {
                String docName = "Prescription - " + p.getPrescriptionId() + " (" + (p.getDiagnosis() != null ? p.getDiagnosis() : "Diagnosis") + ")";
                String dateStr = p.getPrescriptionDate() != null ? p.getPrescriptionDate().toString() : "Date N/A";
                
                Anchor downloadAnchor = new Anchor(new StreamResource("Prescription_" + p.getPrescriptionId() + ".pdf", () -> {
                    String medsList = p.getItems().stream()
                            .map(it -> it.getMedicineName() + " (" + it.getDosage() + ")")
                            .collect(Collectors.joining(", "));
                    return PdfHelper.generatePrescriptionPdf(
                        p.getPrescriptionId(),
                        patientDto.getFirstName() + " " + patientDto.getLastName(),
                        p.getDoctorName() != null ? p.getDoctorName() : "Doctor",
                        p.getPrescriptionDate() != null ? p.getPrescriptionDate().toString() : "",
                        p.getDiagnosis() != null ? p.getDiagnosis() : "Routine Checkup",
                        medsList
                    );
                }), "");
                downloadAnchor.getElement().setAttribute("download", true);
                
                Button downloadBtn = new Button("Download", new Icon(VaadinIcon.DOWNLOAD));
                downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                downloadBtn.addClassName("aruclinic-btn");
                downloadBtn.addClassName("aruclinic-btn-outline");
                downloadAnchor.add(downloadBtn);

                documentsList.add(createDocumentRow(docName, "Prescription Document - Issued on " + dateStr, downloadAnchor));
            }
        } catch (Exception e) {
            // Ignore
        }

        // Load all billing invoices as documents
        try {
            List<com.aruclinic.dto.BillDto> bills = billingService.getBillsByPatientId(patientDto.getId());
            for (com.aruclinic.dto.BillDto bill : bills) {
                String docName = "Invoice - " + bill.getInvoiceId() + " (" + (bill.getDescription() != null ? bill.getDescription() : "Billing Invoice") + ")";
                String dateStr = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : "Date N/A";

                String safeInvoiceId = bill.getInvoiceId() != null ? bill.getInvoiceId() : String.valueOf(bill.getId());
                String safePatientName = patientDto.getFirstName() + " " + patientDto.getLastName();
                String desc = bill.getDescription() != null ? bill.getDescription() : "";
                String safeDoctorName = desc.contains("Dr.") ? desc : "Doctor";
                String safeInvoiceDate = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : LocalDate.now().toString();
                String safeTotal = bill.getTotal() != null ? "₹" + bill.getTotal().toString() : "₹0.00";

                Anchor downloadAnchor = new Anchor(new StreamResource("Invoice_" + safeInvoiceId + ".pdf", () -> {
                    return PdfHelper.generateInvoicePdf(
                        safeInvoiceId,
                        safePatientName,
                        safeDoctorName,
                        safeInvoiceDate,
                        safeTotal
                    );
                }), "");
                downloadAnchor.getElement().setAttribute("download", true);

                Button downloadBtn = new Button("Download", new Icon(VaadinIcon.DOWNLOAD));
                downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                downloadBtn.addClassName("aruclinic-btn");
                downloadBtn.addClassName("aruclinic-btn-outline");
                downloadAnchor.add(downloadBtn);

                documentsList.add(createDocumentRow(docName, "Billing Invoice - Created on " + dateStr, downloadAnchor));
            }
        } catch (Exception e) {
            // Ignore
        }

        if (documentsList.getChildren().count() == 0) {
            Div emptyMessage = new Div();
            emptyMessage.setText("No documents found for this patient.");
            emptyMessage.getStyle().set("padding", "var(--aruclinic-spacing-md)")
                    .set("color", "var(--aruclinic-text-secondary)")
                    .set("text-align", "center")
                    .set("font-style", "italic");
            documentsList.add(emptyMessage);
        }

        documentsCard.add(cardTitle, documentsList);
        panel.add(documentsCard);

        return panel;
    }

    private Component createDocumentRow(String name, String meta, Component actionComponent) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Div nameDiv = new Div();
        nameDiv.addClassName("aruclinic-profile-detail-value");
        nameDiv.setText(name);

        Div metaDiv = new Div();
        metaDiv.addClassName("aruclinic-profile-detail-label");
        metaDiv.setText(meta);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.add(nameDiv, actionComponent);

        item.add(layout, metaDiv);
        return item;
    }

    private int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return 0;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    private void handleSaveChanges() {
        // Validate fields
        if (firstNameField.getValue().trim().isEmpty() || lastNameField.getValue().trim().isEmpty()) {
            Notification.show("First and Last name are required.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Update DTO
            patientDto.setFirstName(firstNameField.getValue().trim());
            patientDto.setLastName(lastNameField.getValue().trim());
            patientDto.setDateOfBirth(dobField.getValue());
            patientDto.setGender(genderField.getValue());
            patientDto.setBloodType(bloodTypeField.getValue());
            patientDto.setAddress(addressField.getValue().trim());
            patientDto.setCity(cityField.getValue().trim());
            patientDto.setState(stateField.getValue().trim());
            patientDto.setZipCode(zipCodeField.getValue().trim());
            patientDto.setEmail(emailField.getValue().trim());
            patientDto.setMobile(mobileField.getValue().trim());
            patientDto.setEmergencyContact(emergencyNameField.getValue().trim());
            patientDto.setEmergencyPhone(emergencyPhoneField.getValue().trim());
            patientDto.setAllergies(allergiesField.getValue().trim());
            patientDto.setAge(calculateAge(dobField.getValue()));

            // Update Database
            patientService.updatePatient(patientDto.getId(), patientDto);

            // Send notification to Patient
            if (currentUser != null) {
                com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                notif.setUser(currentUser);
                notif.setTitle("Profile Updated");
                notif.setMessage("Your profile details have been successfully updated.");
                notif.setRead(false);
                notif.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notif);
            }

            // Send notification to Admin users
            try {
                List<com.aruclinic.entity.User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("ADMIN")))
                    .collect(Collectors.toList());
                for (com.aruclinic.entity.User adminUser : admins) {
                    com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                    notif.setUser(adminUser);
                    notif.setTitle("Patient Profile Updated");
                    notif.setMessage("Profile of patient " + patientDto.getFirstName() + " " + patientDto.getLastName() + " (ID: " + patientDto.getPatientId() + ") has been updated.");
                    notif.setRead(false);
                    notif.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notif);
                }
            } catch (Exception ex) {
                // Ignore admin notification errors
            }

            editMode = false;
            Notification.show("Profile updated successfully!", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            removeAll();
            add(createProfileContent());

        } catch (Exception ex) {
            Notification.show("Failed to update profile: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
