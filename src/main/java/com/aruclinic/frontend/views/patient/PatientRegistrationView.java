package com.aruclinic.frontend.views.patient;

import com.aruclinic.dto.PatientDto;
import com.aruclinic.service.PatientService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Patient Registration view for adding new patients to the system.
 */
@PageTitle("Patient Registration | AruClinic")
@Route("receptionist/patient-registration")
@CssImport("./themes/aruclinic/patient.css")
public class PatientRegistrationView extends VerticalLayout {

    private final PatientService patientService;

    private final TextField firstName = new TextField("First Name");
    private final TextField lastName = new TextField("Last Name");
    private final TextField patientId = new TextField("Patient ID");
    private final DatePicker dateOfBirth = new DatePicker("Date of Birth");
    private final ComboBox<String> gender = new ComboBox<>("Gender");
    private final TextField mobile = new TextField("Mobile Number");
    private final EmailField email = new EmailField("Email");
    private final TextField address = new TextField("Address");
    private final TextField city = new TextField("City");
    private final TextField state = new TextField("State");
    private final TextField zipCode = new TextField("ZIP Code");
    private final TextField emergencyContact = new TextField("Emergency Contact");
    private final TextField emergencyPhone = new TextField("Emergency Phone");
    private final ComboBox<String> bloodType = new ComboBox<>("Blood Type");
    private final TextField allergies = new TextField("Allergies");

    private final Button saveButton = new Button("Save Patient");
    private final Button cancelButton = new Button("Cancel");

    public PatientRegistrationView(PatientService patientService) {
        this.patientService = patientService;
        configureComponents();
        add(createRegistrationForm());
    }

    private void configureComponents() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Configure fields
        firstName.setRequired(true);
        firstName.setRequiredIndicatorVisible(true);
        firstName.setClearButtonVisible(true);
        firstName.setWidthFull();

        lastName.setRequired(true);
        lastName.setRequiredIndicatorVisible(true);
        lastName.setClearButtonVisible(true);
        lastName.setWidthFull();

        patientId.setRequired(true);
        patientId.setRequiredIndicatorVisible(true);
        patientId.setClearButtonVisible(true);
        patientId.setWidthFull();

        dateOfBirth.setRequired(true);
        dateOfBirth.setRequiredIndicatorVisible(true);
        dateOfBirth.setClearButtonVisible(true);
        dateOfBirth.setWidthFull();

        gender.setItems("Male", "Female", "Other");
        gender.setRequired(true);
        gender.setRequiredIndicatorVisible(true);
        gender.setWidthFull();

        mobile.setRequired(true);
        mobile.setRequiredIndicatorVisible(true);
        mobile.setClearButtonVisible(true);
        mobile.setPattern("\\d{10}");
        mobile.setWidthFull();

        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        email.setClearButtonVisible(true);
        email.setWidthFull();

        address.setClearButtonVisible(true);
        address.setWidthFull();

        city.setClearButtonVisible(true);
        city.setWidthFull();

        state.setClearButtonVisible(true);
        state.setWidthFull();

        zipCode.setClearButtonVisible(true);
        zipCode.setWidthFull();

        emergencyContact.setClearButtonVisible(true);
        emergencyContact.setWidthFull();

        emergencyPhone.setClearButtonVisible(true);
        emergencyPhone.setPattern("\\d{10}");
        emergencyPhone.setWidthFull();

        bloodType.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodType.setClearButtonVisible(true);
        bloodType.setWidthFull();

        allergies.setClearButtonVisible(true);
        allergies.setWidthFull();

        // Buttons
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("aruclinic-btn");
        saveButton.addClassName("aruclinic-btn-primary");
        saveButton.addClassName("aruclinic-btn-lg");
        saveButton.addClickListener(e -> handleSave());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClassName("aruclinic-btn");
        cancelButton.addClassName("aruclinic-btn-secondary");
        cancelButton.addClassName("aruclinic-btn-lg");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("receptionist")));
    }

    private Component createRegistrationForm() {
        Div formContainer = new Div();
        formContainer.addClassName("aruclinic-patient-registration");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-registration-header");

        H1 title = new H1("Patient Registration");
        title.addClassName("aruclinic-registration-title");

        Paragraph subtitle = new Paragraph("Enter patient details to register in the system");
        subtitle.addClassName("aruclinic-registration-subtitle");

        header.add(title, subtitle);

        // Form
        Div form = new Div();
        form.addClassName("aruclinic-registration-form");

        // Personal Information Section
        Div personalInfoSection = new Div();
        personalInfoSection.addClassName("aruclinic-registration-section");

        H2 sectionTitle = new H2("Personal Information");
        sectionTitle.addClassName("aruclinic-registration-section-title");

        FormLayout personalInfoLayout = new FormLayout();
        personalInfoLayout.setWidthFull();

        // Name row
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setWidthFull();
        nameRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        nameRow.add(firstName, lastName);

        // Patient ID and DOB row
        HorizontalLayout idDobRow = new HorizontalLayout();
        idDobRow.setWidthFull();
        idDobRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        idDobRow.add(patientId, dateOfBirth);

        // Gender and Mobile row
        HorizontalLayout genderMobileRow = new HorizontalLayout();
        genderMobileRow.setWidthFull();
        genderMobileRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        genderMobileRow.add(gender, mobile);

        // Email
        HorizontalLayout emailRow = new HorizontalLayout();
        emailRow.setWidthFull();
        emailRow.add(email);

        personalInfoLayout.add(nameRow, idDobRow, genderMobileRow, emailRow);
        personalInfoSection.add(sectionTitle, personalInfoLayout);

        // Address Information Section
        Div addressSection = new Div();
        addressSection.addClassName("aruclinic-registration-section");

        H2 addressTitle = new H2("Address Information");
        addressTitle.addClassName("aruclinic-registration-section-title");

        FormLayout addressLayout = new FormLayout();
        addressLayout.setWidthFull();

        addressLayout.add(address, city);
        addressLayout.add(state, zipCode);

        addressSection.add(addressTitle, addressLayout);

        // Emergency Contact Section
        Div emergencySection = new Div();
        emergencySection.addClassName("aruclinic-registration-section");

        H2 emergencyTitle = new H2("Emergency Contact");
        emergencyTitle.addClassName("aruclinic-registration-section-title");

        FormLayout emergencyLayout = new FormLayout();
        emergencyLayout.setWidthFull();

        emergencyLayout.add(emergencyContact, emergencyPhone);

        emergencySection.add(emergencyTitle, emergencyLayout);

        // Medical Information Section
        Div medicalSection = new Div();
        medicalSection.addClassName("aruclinic-registration-section");

        H2 medicalTitle = new H2("Medical Information");
        medicalTitle.addClassName("aruclinic-registration-section-title");

        FormLayout medicalLayout = new FormLayout();
        medicalLayout.setWidthFull();

        medicalLayout.add(bloodType, allergies);

        medicalSection.add(medicalTitle, medicalLayout);

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-registration-actions");
        actions.add(saveButton, cancelButton);

        form.add(personalInfoSection, addressSection, emergencySection, medicalSection, actions);
        formContainer.add(header, form);

        return formContainer;
    }

    private void handleSave() {
        // Validate all required fields
        if (!validateFields()) {
            return;
        }

        PatientDto patientDto = new PatientDto();
        patientDto.setFirstName(firstName.getValue().trim());
        patientDto.setLastName(lastName.getValue().trim());
        patientDto.setPatientId(patientId.getValue().trim());
        patientDto.setDateOfBirth(dateOfBirth.getValue());
        patientDto.setGender(gender.getValue());
        patientDto.setMobile(mobile.getValue().trim());
        patientDto.setEmail(email.getValue().trim());
        patientDto.setAddress(address.getValue().trim());
        patientDto.setCity(city.getValue().trim());
        patientDto.setState(state.getValue().trim());
        patientDto.setZipCode(zipCode.getValue().trim());
        patientDto.setEmergencyContact(emergencyContact.getValue().trim());
        patientDto.setEmergencyPhone(emergencyPhone.getValue().trim());
        patientDto.setBloodType(bloodType.getValue());
        patientDto.setAllergies(allergies.getValue().trim());

        try {
            patientService.createPatient(patientDto);

            Notification.show(
                "Patient registered successfully!",
                3000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear form
            clearForm();

            // Navigate back to receptionist dashboard
            getUI().ifPresent(ui -> ui.navigate("receptionist"));

        } catch (Exception e) {
            showError("Failed to register patient: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (firstName.getValue().trim().isEmpty()) {
            firstName.setErrorMessage("First name is required");
            firstName.setInvalid(true);
            isValid = false;
        } else {
            firstName.setErrorMessage(null);
            firstName.setInvalid(false);
        }

        if (lastName.getValue().trim().isEmpty()) {
            lastName.setErrorMessage("Last name is required");
            lastName.setInvalid(true);
            isValid = false;
        } else {
            lastName.setErrorMessage(null);
            lastName.setInvalid(false);
        }

        if (patientId.getValue().trim().isEmpty()) {
            patientId.setErrorMessage("Patient ID is required");
            patientId.setInvalid(true);
            isValid = false;
        } else {
            patientId.setErrorMessage(null);
            patientId.setInvalid(false);
        }

        if (dateOfBirth.getValue() == null) {
            dateOfBirth.setErrorMessage("Date of birth is required");
            dateOfBirth.setInvalid(true);
            isValid = false;
        } else {
            dateOfBirth.setErrorMessage(null);
            dateOfBirth.setInvalid(false);
        }

        if (gender.getValue() == null) {
            gender.setErrorMessage("Gender is required");
            gender.setInvalid(true);
            isValid = false;
        } else {
            gender.setErrorMessage(null);
            gender.setInvalid(false);
        }

        if (mobile.getValue().trim().isEmpty()) {
            mobile.setErrorMessage("Mobile number is required");
            mobile.setInvalid(true);
            isValid = false;
        } else if (!mobile.getValue().trim().matches("\\d{10}")) {
            mobile.setErrorMessage("Mobile number must be 10 digits");
            mobile.setInvalid(true);
            isValid = false;
        } else {
            mobile.setErrorMessage(null);
            mobile.setInvalid(false);
        }

        if (email.getValue().trim().isEmpty()) {
            email.setErrorMessage("Email is required");
            email.setInvalid(true);
            isValid = false;
        } else if (!email.getValue().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            email.setErrorMessage("Please enter a valid email address");
            email.setInvalid(true);
            isValid = false;
        } else {
            email.setErrorMessage(null);
            email.setInvalid(false);
        }

        return isValid;
    }

    private void clearForm() {
        firstName.clear();
        lastName.clear();
        patientId.clear();
        dateOfBirth.clear();
        gender.clear();
        mobile.clear();
        email.clear();
        address.clear();
        city.clear();
        state.clear();
        zipCode.clear();
        emergencyContact.clear();
        emergencyPhone.clear();
        bloodType.clear();
        allergies.clear();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
