package com.aruclinic.frontend.views.patient;

import com.aruclinic.dto.PatientDto;
import com.aruclinic.service.PatientService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Patient Profile view for displaying and editing patient information.
 */
@PageTitle("Patient Profile | AruClinic")
@Route("patient/profile")
@CssImport("./themes/aruclinic/patient.css")
public class PatientProfileView extends VerticalLayout {

    private final PatientService patientService;
    private PatientDto patientDto;

    public PatientProfileView(PatientService patientService) {
        this.patientService = patientService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Load patient data (in a real app, this would come from the service)
        loadPatientData();

        add(createProfileContent());
    }

    private void loadPatientData() {
        // This is a placeholder - in a real application, you would fetch the patient data
        // based on the currently authenticated user
        patientDto = new PatientDto();
        patientDto.setFirstName("John");
        patientDto.setLastName("Doe");
        patientDto.setPatientId("PAT-001");
        patientDto.setDateOfBirth(java.time.LocalDate.of(1985, 5, 15));
        patientDto.setGender("Male");
        patientDto.setMobile("1234567890");
        patientDto.setEmail("john.doe@email.com");
        patientDto.setAddress("123 Main Street");
        patientDto.setCity("New York");
        patientDto.setState("NY");
        patientDto.setZipCode("10001");
        patientDto.setEmergencyContact("Jane Doe");
        patientDto.setEmergencyPhone("0987654321");
        patientDto.setBloodType("O+");
        patientDto.setAllergies("Penicillin");
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
        avatar.setText("JD"); // Initials from first and last name

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

        Button editProfileBtn = new Button("Edit Profile", new Icon(VaadinIcon.EDIT));
        editProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editProfileBtn.addClassName("aruclinic-btn");
        editProfileBtn.addClassName("aruclinic-btn-primary");
        editProfileBtn.addClickListener(e -> handleEditProfile());

        Button printBtn = new Button("Print", new Icon(VaadinIcon.PRINT));
        printBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        printBtn.addClassName("aruclinic-btn");
        printBtn.addClassName("aruclinic-btn-secondary");

        actions.add(editProfileBtn, printBtn);

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

        content.add(createDetailItem("Date of Birth", patientDto.getDateOfBirth().toString()));
        content.add(createDetailItem("Gender", patientDto.getGender()));
        content.add(createDetailItem("Age", calculateAge(patientDto.getDateOfBirth()) + " years"));
        content.add(createDetailItem("Blood Type", patientDto.getBloodType()));

        return content;
    }

    private Component createContactInfoContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        content.add(createDetailItem("Address", patientDto.getAddress()));
        content.add(createDetailItem("City", patientDto.getCity()));
        content.add(createDetailItem("State", patientDto.getState()));
        content.add(createDetailItem("ZIP Code", patientDto.getZipCode()));

        return content;
    }

    private Component createEmergencyContactContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        content.add(createDetailItem("Emergency Contact Name", patientDto.getEmergencyContact()));
        content.add(createDetailItem("Emergency Phone", patientDto.getEmergencyPhone()));

        return content;
    }

    private Component createDetailItem(String label, String value) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-profile-detail-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("aruclinic-profile-detail-value");

        item.add(labelSpan, valueSpan);
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

        content.add(createDetailItem("Insurance Provider", "HealthCare Inc."));
        content.add(createDetailItem("Policy Number", "HC-123456789"));
        content.add(createDetailItem("Billing Address", "Same as contact address"));

        return content;
    }

    private Component createPaymentHistoryContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        content.add(createDetailItem("Last Payment", "June 1, 2025 - $150.00"));
        content.add(createDetailItem("Payment Method", "Credit Card"));
        content.add(createDetailItem("Outstanding Balance", "$0.00"));

        return content;
    }

    private Div createDocumentsPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        // Documents List
        Div documentsCard = new Div();
        documentsCard.addClassName("aruclinic-profile-detail-card");
        documentsCard.setWidthFull();

        H2 cardTitle = new H2("Documents");
        cardTitle.addClassName("aruclinic-profile-detail-card-title");

        Div documentsList = new Div();
        documentsList.addClassName("aruclinic-profile-detail-list");

        // Sample documents
        documentsList.add(createDocumentItem("Medical Report - June 2025", "PDF", "1.2 MB"));
        documentsList.add(createDocumentItem("Blood Test Results", "PDF", "0.8 MB"));
        documentsList.add(createDocumentItem("X-Ray Report", "PDF", "2.5 MB"));
        documentsList.add(createDocumentItem("Prescription - Lisinopril", "PDF", "0.3 MB"));

        documentsCard.add(cardTitle, documentsList);
        panel.add(documentsCard);

        return panel;
    }

    private Component createDocumentItem(String name, String type, String size) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Div nameDiv = new Div();
        nameDiv.addClassName("aruclinic-profile-detail-value");
        nameDiv.setText(name);

        Div metaDiv = new Div();
        metaDiv.addClassName("aruclinic-profile-detail-label");
        metaDiv.setText(type + " - " + size);

        Button downloadBtn = new Button("Download", new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        downloadBtn.addClassName("aruclinic-btn");
        downloadBtn.addClassName("aruclinic-btn-outline");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.add(nameDiv, downloadBtn);

        item.add(layout, metaDiv);
        return item;
    }

    private int calculateAge(java.time.LocalDate dateOfBirth) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.getYear() - dateOfBirth.getYear();
    }

    private void handleEditProfile() {
        getUI().ifPresent(ui -> ui.navigate("patient/profile/edit"));
    }
}
