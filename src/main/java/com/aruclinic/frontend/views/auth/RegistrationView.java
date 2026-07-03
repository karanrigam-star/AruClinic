package com.aruclinic.frontend.views.auth;

import com.aruclinic.dto.UserDto;
import com.aruclinic.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final EmailField email = new EmailField();
    private final PasswordField password = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final TextField mobile = new TextField();
    private final Button submitButton = new Button("Register");
    private final Button loginButton = new Button("Already have an account? Login");

    public RegistrationView(UserService userService) {
        this.userService = userService;
        configureComponents();
        add(createRegistrationContainer());
    }

    private void configureComponents() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
            .set("background", "linear-gradient(135deg, var(--aruclinic-primary) 0%, var(--aruclinic-primary-dark) 100%)")
            .set("min-height", "100vh")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

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
        container.setWidth("90%");
        container.setMaxWidth("550px");

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

        // Name fields
        Div nameGroup = new Div();
        nameGroup.addClassName("aruclinic-registration-grid");
        nameGroup.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(2, 1fr)")
            .set("gap", "var(--aruclinic-spacing-md)");

        Div firstNameGroup = new Div();
        firstNameGroup.addClassName("aruclinic-login-input-group");
        firstNameGroup.add(new Icon(VaadinIcon.USER), firstName);

        Div lastNameGroup = new Div();
        lastNameGroup.addClassName("aruclinic-login-input-group");
        lastNameGroup.add(new Icon(VaadinIcon.USER), lastName);

        nameGroup.add(firstNameGroup, lastNameGroup);

        // Email field with icon
        Div emailGroup = new Div();
        emailGroup.addClassName("aruclinic-login-input-group");
        emailGroup.add(new Icon(VaadinIcon.MAILBOX), email);

        // Password fields
        Div passwordGroup = new Div();
        passwordGroup.addClassName("aruclinic-login-input-group");
        passwordGroup.add(new Icon(VaadinIcon.LOCK), password);

        Div confirmPasswordGroup = new Div();
        confirmPasswordGroup.addClassName("aruclinic-login-input-group");
        confirmPasswordGroup.add(new Icon(VaadinIcon.LOCK), confirmPassword);

        // Mobile field with icon
        Div mobileGroup = new Div();
        mobileGroup.addClassName("aruclinic-login-input-group");
        mobileGroup.add(new Icon(VaadinIcon.PHONE), mobile);

        // Password requirements
        Div requirements = new Div();
        requirements.addClassName("aruclinic-login-subtitle");
        requirements.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "var(--aruclinic-text-muted)")
            .set("margin-top", "var(--aruclinic-spacing-xs)");
        requirements.setText("Password must be at least 8 characters long");

        form.add(nameGroup, emailGroup, passwordGroup, confirmPasswordGroup, mobileGroup, requirements, submitButton);

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

        UserDto userDto = new UserDto();
        userDto.setFirstName(firstNameValue);
        userDto.setLastName(lastNameValue);
        userDto.setEmail(emailValue);
        userDto.setPassword(passwordValue);
        userDto.setConfirmPassword(confirmPasswordValue);
        userDto.setMobileNumber(mobileValue);

        try {
            UserDto registeredUser = userService.registerUser(userDto);
            if (registeredUser != null) {
                Notification.show(
                    "Registration successful! Please check your email for verification.",
                    5000,
                    Notification.Position.BOTTOM_CENTER
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Redirect to OTP verification or login
                getUI().ifPresent(ui -> ui.navigate("auth/verify"));
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
