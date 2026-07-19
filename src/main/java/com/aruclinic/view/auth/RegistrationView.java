package com.aruclinic.view.auth;

import com.aruclinic.dto.UserDto;
import com.aruclinic.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Enhanced Registration view for new user signup.
 * Follows AruClinic design system standards.
 */
@PageTitle("Register | AruClinic")
@Route("auth/register")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class RegistrationView extends VerticalLayout {

    private final UserService userService;
    private final com.aruclinic.service.LocationService locationService;
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final EmailField email = new EmailField();
    private final PasswordField password = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final TextField mobile = new TextField();
    private final DatePicker dateOfBirth = new DatePicker();
    private final Select<String> gender = new Select<>();
    private final Select<String> bloodGroup = new Select<>();
    private final TextField address = new TextField();
    private final ComboBox<String> city = new ComboBox<>();
    private final TextField state = new TextField();
    private final TextField district = new TextField();
    private final TextField zipCode = new TextField();
    private final TextField emergencyContactName = new TextField();
    private final TextField emergencyPhone = new TextField();
    private final Button submitButton = new Button("Register");
    private final Button loginButton = new Button("Already have an account? Login");

    public RegistrationView(UserService userService, com.aruclinic.service.LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
        configureComponents();
        add(createRegistrationContainer());
    }

    private void configureComponents() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("aruclinic-login-page");

        // First name field
        firstName.setPlaceholder("First Name");
        firstName.setRequired(true);
        firstName.setRequiredIndicatorVisible(true);
        firstName.setWidthFull();
        firstName.setClearButtonVisible(true);
        firstName.addClassName("aruclinic-login-input");

        // Last name field
        lastName.setPlaceholder("Last Name");
        lastName.setRequired(true);
        lastName.setRequiredIndicatorVisible(true);
        lastName.setWidthFull();
        lastName.setClearButtonVisible(true);
        lastName.addClassName("aruclinic-login-input");

        // Email field
        email.setPlaceholder("Email");
        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();
        email.setClearButtonVisible(true);
        email.addClassName("aruclinic-login-input");

        // Password field
        password.setPlaceholder("Password (min 8 characters)");
        password.setRequired(true);
        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();
        password.setClearButtonVisible(true);
        password.addClassName("aruclinic-login-input");

        // Confirm password field
        confirmPassword.setPlaceholder("Confirm Password");
        confirmPassword.setRequired(true);
        confirmPassword.setRequiredIndicatorVisible(true);
        confirmPassword.setWidthFull();
        confirmPassword.setClearButtonVisible(true);
        confirmPassword.addClassName("aruclinic-login-input");

        // Mobile field
        mobile.setPlaceholder("Mobile Number");
        mobile.setRequired(true);
        mobile.setRequiredIndicatorVisible(true);
        mobile.setWidthFull();
        mobile.setClearButtonVisible(true);
        mobile.setPattern("\\d{10}");
        mobile.addClassName("aruclinic-login-input");

        // Date of Birth
        dateOfBirth.setPlaceholder("Date of Birth");
        dateOfBirth.setRequired(true);
        dateOfBirth.setRequiredIndicatorVisible(true);
        dateOfBirth.setWidthFull();
        dateOfBirth.addClassName("aruclinic-login-input");

        // Gender
        gender.setPlaceholder("Gender");
        gender.setItems("Male", "Female", "Other");
        gender.setRequiredIndicatorVisible(true);
        gender.setWidthFull();
        gender.addClassName("aruclinic-login-input");

        // Blood Group
        bloodGroup.setPlaceholder("Blood Group");
        bloodGroup.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroup.setRequiredIndicatorVisible(true);
        bloodGroup.setWidthFull();
        bloodGroup.addClassName("aruclinic-login-input");

        // Address
        address.setPlaceholder("Address");
        address.setRequired(true);
        address.setRequiredIndicatorVisible(true);
        address.setWidthFull();
        address.addClassName("aruclinic-login-input");

        // City
        city.setPlaceholder("City");
        city.setRequired(true);
        city.setRequiredIndicatorVisible(true);
        city.setWidthFull();
        city.addClassName("aruclinic-login-input");

        // State
        state.setPlaceholder("State");
        state.setRequired(true);
        state.setRequiredIndicatorVisible(true);
        state.setWidthFull();
        state.addClassName("aruclinic-login-input");
        state.setValue("Arunachal Pradesh");
        state.setReadOnly(true);

        // District
        district.setPlaceholder("District");
        district.setRequired(true);
        district.setRequiredIndicatorVisible(true);
        district.setWidthFull();
        district.addClassName("aruclinic-login-input");

        // Zip Code
        zipCode.setPlaceholder("ZIP Code");
        zipCode.setRequired(true);
        zipCode.setRequiredIndicatorVisible(true);
        zipCode.setWidthFull();
        zipCode.addClassName("aruclinic-login-input");
        zipCode.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        zipCode.addValueChangeListener(event -> {
            String val = event.getValue();
            if (val != null && val.trim().length() == 6) {
                com.aruclinic.service.LocationService.LocationDetails details = locationService.lookupPincode(val.trim());
                if (details != null && details.state != null && !details.state.isEmpty()) {
                    district.setValue(details.district);
                    state.setValue(details.state);
                    city.setItems(details.cities);
                    if (!details.cities.isEmpty()) {
                        city.setValue(details.cities.get(0));
                    } else {
                        city.setValue(null);
                    }
                }
            } else {
                city.setItems(java.util.Collections.emptyList());
                city.setValue(null);
                district.setValue("");
            }
        });

        // Emergency Contact Name
        emergencyContactName.setPlaceholder("Emergency Contact Name");
        emergencyContactName.setRequired(true);
        emergencyContactName.setRequiredIndicatorVisible(true);
        emergencyContactName.setWidthFull();
        emergencyContactName.addClassName("aruclinic-login-input");

        // Emergency Phone
        emergencyPhone.setPlaceholder("Emergency Phone");
        emergencyPhone.setRequired(true);
        emergencyPhone.setRequiredIndicatorVisible(true);
        emergencyPhone.setWidthFull();
        emergencyPhone.setPattern("\\d{10}");
        emergencyPhone.addClassName("aruclinic-login-input");

        // Submit button
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClassName("aruclinic-login-button");
        submitButton.addClickListener(event -> handleRegistration());

        // Login button
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginButton.addClassName("aruclinic-forgot-password");
        loginButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/login")));
    }

    private Component createRegistrationContainer() {
        Div container = new Div();
        container.addClassName("aruclinic-login-card");
        container.addClassName("aruclinic-registration-card");
        container.setWidth("90%");
        container.setMaxWidth("650px");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-login-header");

        Div logo = new Div();
        logo.addClassName("aruclinic-login-logo");
        logo.add(new Icon(VaadinIcon.HOSPITAL));

        H1 title = new H1("Create Account");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Join AruClinic to manage your healthcare");
        subtitle.addClassName("aruclinic-login-subtitle");

        header.add(logo, title, subtitle);

        // Form
        VerticalLayout form = new VerticalLayout();
        form.addClassName("aruclinic-login-form");
        form.setPadding(false);
        form.setSpacing(true);

        // Form Fields Grid (3 rows, 2 columns)
        Div gridContainer = new Div();
        gridContainer.addClassName("aruclinic-registration-grid");

        // Row 1: First Name, Last Name
        Div firstNameGroup = new Div();
        firstNameGroup.addClassName("aruclinic-login-input-group");
        firstNameGroup.add(new Icon(VaadinIcon.USER), firstName);

        Div lastNameGroup = new Div();
        lastNameGroup.addClassName("aruclinic-login-input-group");
        lastNameGroup.add(new Icon(VaadinIcon.USER), lastName);

        // Row 2: Email, Mobile Number
        Div emailGroup = new Div();
        emailGroup.addClassName("aruclinic-login-input-group");
        emailGroup.add(new Icon(VaadinIcon.MAILBOX), email);

        Div mobileGroup = new Div();
        mobileGroup.addClassName("aruclinic-login-input-group");
        mobileGroup.add(new Icon(VaadinIcon.PHONE), mobile);

        // Row 3: Password, Confirm Password
        Div passwordGroup = new Div();
        passwordGroup.addClassName("aruclinic-login-input-group");
        passwordGroup.add(new Icon(VaadinIcon.LOCK), password);

        Div confirmPasswordGroup = new Div();
        confirmPasswordGroup.addClassName("aruclinic-login-input-group");
        confirmPasswordGroup.add(new Icon(VaadinIcon.LOCK), confirmPassword);

        // Date of Birth group
        Div dobGroup = new Div();
        dobGroup.addClassName("aruclinic-login-input-group");
        dobGroup.add(new Icon(VaadinIcon.DATE_INPUT), dateOfBirth);

        // Gender group
        Div genderGroup = new Div();
        genderGroup.addClassName("aruclinic-login-input-group");
        genderGroup.add(new Icon(VaadinIcon.MALE), gender);

        // Blood group group
        Div bloodGroupGroup = new Div();
        bloodGroupGroup.addClassName("aruclinic-login-input-group");
        bloodGroupGroup.add(new Icon(VaadinIcon.HEART), bloodGroup);

        // Address group
        Div addressGroup = new Div();
        addressGroup.addClassName("aruclinic-login-input-group");
        addressGroup.add(new Icon(VaadinIcon.HOME), address);

        // City group
        Div cityGroup = new Div();
        cityGroup.addClassName("aruclinic-login-input-group");
        cityGroup.add(new Icon(VaadinIcon.BUILDING), city);

        // State group
        Div stateGroup = new Div();
        stateGroup.addClassName("aruclinic-login-input-group");
        stateGroup.add(new Icon(VaadinIcon.MAP_MARKER), state);

        // District group
        Div districtGroup = new Div();
        districtGroup.addClassName("aruclinic-login-input-group");
        districtGroup.add(new Icon(VaadinIcon.MAP_MARKER), district);

        // Zip Code group
        Div zipGroup = new Div();
        zipGroup.addClassName("aruclinic-login-input-group");
        zipGroup.add(new Icon(VaadinIcon.DIPLOMA), zipCode);

        // Emergency Contact Name group
        Div emergencyContactNameGroup = new Div();
        emergencyContactNameGroup.addClassName("aruclinic-login-input-group");
        emergencyContactNameGroup.add(new Icon(VaadinIcon.USER_CARD), emergencyContactName);

        // Emergency Phone group
        Div emergencyPhoneGroup = new Div();
        emergencyPhoneGroup.addClassName("aruclinic-login-input-group");
        emergencyPhoneGroup.add(new Icon(VaadinIcon.PHONE_LANDLINE), emergencyPhone);

        gridContainer.add(
            firstNameGroup, lastNameGroup,
            emailGroup, mobileGroup,
            passwordGroup, confirmPasswordGroup,
            dobGroup, genderGroup,
            bloodGroupGroup, addressGroup,
            zipGroup, stateGroup,
            districtGroup, cityGroup,
            emergencyContactNameGroup, emergencyPhoneGroup
        );

        // Password requirements
        Div requirements = new Div();
        requirements.addClassName("aruclinic-login-subtitle");
        requirements.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "var(--aruclinic-text-muted)")
            .set("margin-top", "var(--aruclinic-spacing-xs)");
        requirements.setText("Password must be at least 8 characters long");

        form.add(gridContainer, requirements, submitButton);

        // Divider
        Div divider = new Div();
        divider.addClassName("aruclinic-login-divider");
        divider.add(new Span("or"));

        // Login section
        Div loginSection = new Div();
        loginSection.addClassName("aruclinic-signup-link");
        loginSection.add(loginButton);

        container.add(header, form, divider, loginSection);
        return container;
    }

    private void handleRegistration() {
        String firstNameValue = firstName.getValue().trim();
        String lastNameValue = lastName.getValue().trim();
        String emailValue = email.getValue().trim();
        String passwordValue = password.getValue();
        String confirmPasswordValue = confirmPassword.getValue();
        String mobileValue = mobile.getValue().trim();

        // Clear previous errors
        firstName.setErrorMessage(null);
        lastName.setErrorMessage(null);
        email.setErrorMessage(null);
        password.setErrorMessage(null);
        confirmPassword.setErrorMessage(null);
        mobile.setErrorMessage(null);
        dateOfBirth.setErrorMessage(null);
        gender.setErrorMessage(null);
        bloodGroup.setErrorMessage(null);
        address.setErrorMessage(null);
        city.setErrorMessage(null);
        state.setErrorMessage(null);
        district.setErrorMessage(null);
        zipCode.setErrorMessage(null);
        emergencyContactName.setErrorMessage(null);
        emergencyPhone.setErrorMessage(null);

        // Validate all fields are filled
        if (firstNameValue.isEmpty()) {
            firstName.setErrorMessage("Please enter your first name");
            firstName.focus();
            return;
        }

        if (lastNameValue.isEmpty()) {
            lastName.setErrorMessage("Please enter your last name");
            lastName.focus();
            return;
        }

        if (emailValue.isEmpty()) {
            email.setErrorMessage("Please enter your email");
            email.focus();
            return;
        }

        if (passwordValue.isEmpty()) {
            password.setErrorMessage("Please enter a password");
            password.focus();
            return;
        }

        if (confirmPasswordValue.isEmpty()) {
            confirmPassword.setErrorMessage("Please confirm your password");
            confirmPassword.focus();
            return;
        }

        if (mobileValue.isEmpty()) {
            mobile.setErrorMessage("Please enter your mobile number");
            mobile.focus();
            return;
        }

        // Validate password match
        if (!passwordValue.equals(confirmPasswordValue)) {
            confirmPassword.setErrorMessage("Password and confirm password do not match");
            confirmPassword.focus();
            return;
        }

        // Validate password length
        if (passwordValue.length() < 8) {
            password.setErrorMessage("Password must be at least 8 characters long");
            password.focus();
            return;
        }

        // Validate email format
        if (!emailValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            email.setErrorMessage("Please enter a valid email address");
            email.focus();
            return;
        }

        // Validate mobile number format (10 digits)
        if (!mobileValue.matches("\\d{10}")) {
            mobile.setErrorMessage("Mobile number must be 10 digits");
            mobile.focus();
            return;
        }

        // Validate new patient fields
        if (dateOfBirth.getValue() == null) {
            dateOfBirth.setErrorMessage("Please select your date of birth");
            dateOfBirth.focus();
            return;
        }
        if (gender.getValue() == null) {
            gender.setErrorMessage("Please select your gender");
            gender.focus();
            return;
        }
        if (bloodGroup.getValue() == null) {
            bloodGroup.setErrorMessage("Please select your blood group");
            bloodGroup.focus();
            return;
        }
        if (address.getValue().trim().isEmpty()) {
            address.setErrorMessage("Please enter your address");
            address.focus();
            return;
        }
        if (city.getValue() == null || city.getValue().trim().isEmpty()) {
            city.setErrorMessage("Please select your city");
            city.focus();
            return;
        }
        if (state.getValue().trim().isEmpty()) {
            state.setErrorMessage("Please enter your state");
            state.focus();
            return;
        }
        if (district.getValue().trim().isEmpty()) {
            district.setErrorMessage("Please enter your district");
            district.focus();
            return;
        }
        if (zipCode.getValue().trim().isEmpty()) {
            zipCode.setErrorMessage("Please enter your ZIP code");
            zipCode.focus();
            return;
        }
        if (emergencyContactName.getValue().trim().isEmpty()) {
            emergencyContactName.setErrorMessage("Please enter emergency contact name");
            emergencyContactName.focus();
            return;
        }
        if (emergencyPhone.getValue().trim().isEmpty()) {
            emergencyPhone.setErrorMessage("Please enter emergency phone number");
            emergencyPhone.focus();
            return;
        }
        if (!emergencyPhone.getValue().trim().matches("\\d{10}")) {
            emergencyPhone.setErrorMessage("Emergency phone number must be 10 digits");
            emergencyPhone.focus();
            return;
        }

        UserDto userDto = new UserDto();
        userDto.setFirstName(firstNameValue);
        userDto.setLastName(lastNameValue);
        userDto.setEmail(emailValue);
        userDto.setPassword(passwordValue);
        userDto.setConfirmPassword(confirmPasswordValue);
        userDto.setMobileNumber(mobileValue);
        userDto.setDateOfBirth(dateOfBirth.getValue());
        userDto.setGender(gender.getValue());
        userDto.setBloodGroup(bloodGroup.getValue());
        userDto.setAddress(address.getValue().trim());
        userDto.setCity(city.getValue() != null ? city.getValue().trim() : "");
        userDto.setState(state.getValue().trim());
        userDto.setDistrict(district.getValue().trim());
        userDto.setZipCode(zipCode.getValue().trim());
        userDto.setEmergencyContactName(emergencyContactName.getValue().trim());
        userDto.setEmergencyPhone(emergencyPhone.getValue().trim());

        try {
            UserDto registeredUser = userService.registerUser(userDto);
            if (registeredUser != null) {
                Notification.show(
                    "Registration successful! Please log in to verify your account.",
                    5000,
                    Notification.Position.BOTTOM_CENTER
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Redirect to login page
                getUI().ifPresent(ui -> ui.navigate("auth/login"));
            }
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
