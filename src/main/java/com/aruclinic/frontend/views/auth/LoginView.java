package com.aruclinic.frontend.views.auth;

import com.aruclinic.dto.LoginRequestDto;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Enhanced Login view for user authentication.
 * Follows AruClinic design system standards.
 */
@PageTitle("Login | AruClinic")
@Route("auth/login")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class LoginView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
	private final UserService userService;
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final Button loginButton = new Button("Login");
    private final Button forgotPasswordButton = new Button("Forgot Password?");
    private final Button registerButton = new Button("Create Account");

    public LoginView(UserService userService) {
        this.userService = userService;
        configureComponents();
        add(createLoginContainer());
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

        // Email field
        email.setPlaceholder("Email");
        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();
        email.setClearButtonVisible(true);
        email.addClassName("aruclinic-login-input");

        // Password field
        password.setPlaceholder("Password");
        password.setRequired(true);
        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();
        password.addClassName("aruclinic-login-input");

        // Login button
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClassName("aruclinic-login-button");
        loginButton.addClickListener(event -> handleLogin());

        // Forgot password button
        forgotPasswordButton.addClassName("aruclinic-forgot-password");
        forgotPasswordButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/forgot-password")));

        // Register button
        registerButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        registerButton.addClassName("aruclinic-booking-btn");
        registerButton.addClassName("primary");
        registerButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/register")));
    }

    private Component createLoginContainer() {
        Div loginContainer = new Div();
        loginContainer.addClassName("aruclinic-login-card");
        loginContainer.setWidth("90%");
        loginContainer.setMaxWidth("480px");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-login-header");

        Div logo = new Div();
        logo.addClassName("aruclinic-login-logo");
        logo.add(new Icon(VaadinIcon.HOSPITAL));

        H1 title = new H1("AruClinic");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Your Healthcare Management System");
        subtitle.addClassName("aruclinic-login-subtitle");

        header.add(logo, title, subtitle);

        // Form
        VerticalLayout form = new VerticalLayout();
        form.addClassName("aruclinic-login-form");
        form.setPadding(false);
        form.setSpacing(true);

        // Email field with icon
        Div emailGroup = new Div();
        emailGroup.addClassName("aruclinic-login-input-group");
        emailGroup.add(new Icon(VaadinIcon.MAILBOX), email);

        // Password field with icon
        Div passwordGroup = new Div();
        passwordGroup.addClassName("aruclinic-login-input-group");
        passwordGroup.add(new Icon(VaadinIcon.LOCK), password);

        // Options
        Div options = new Div();
        options.addClassName("aruclinic-login-options");
        options.add(forgotPasswordButton);

        form.add(emailGroup, passwordGroup, options, loginButton);

        // Divider
        Div divider = new Div();
        divider.addClassName("aruclinic-login-divider");
        divider.add(new Span("or"));

        // Register section
        Div registerSection = new Div();
        registerSection.addClassName("aruclinic-signup-link");
        registerSection.add(new Span("Don't have an account?"), registerButton);

        loginContainer.add(header, form, divider, registerSection);
        return loginContainer;
    }

    private void handleLogin() {
        String emailValue = email.getValue().trim();
        String passwordValue = password.getValue();

        // Clear previous errors
        email.setErrorMessage(null);
        password.setErrorMessage(null);

        // Validate
        if (emailValue.isEmpty()) {
            email.setErrorMessage("Please enter your email");
            email.focus();
            return;
        }

        if (passwordValue.isEmpty()) {
            password.setErrorMessage("Please enter your password");
            password.focus();
            return;
        }

        // Validate email format
        if (!emailValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            email.setErrorMessage("Please enter a valid email address");
            email.focus();
            return;
        }

        try {
            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setEmail(emailValue);
            loginRequest.setPassword(passwordValue);

            userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

            Notification.show("Login successful! Welcome back.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate(""));

        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
