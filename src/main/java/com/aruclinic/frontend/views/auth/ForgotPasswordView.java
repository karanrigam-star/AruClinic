package com.aruclinic.frontend.views.auth;

import com.aruclinic.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Forgot Password view for password recovery.
 */
@PageTitle("Forgot Password | AruClinic")
@Route("auth/forgot-password")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class ForgotPasswordView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
	private final UserService userService;
    private final TextField email = new TextField();
    private final Button submitButton = new Button("Send Reset Link");
    private final Button backToLoginButton = new Button("Back to Login");

    public ForgotPasswordView(UserService userService) {
        this.userService = userService;
        configureComponents();
        add(createForgotPasswordContainer());
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
        email.setPlaceholder("Enter your registered email");
        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();
        email.setClearButtonVisible(true);
        email.addClassName("aruclinic-login-input");

        // Submit button
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClassName("aruclinic-login-button");
        submitButton.addClickListener(event -> handleForgotPassword());

        // Back to login button
        backToLoginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginButton.addClassName("aruclinic-forgot-password");
        backToLoginButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/login")));
    }

    private Component createForgotPasswordContainer() {
        Div container = new Div();
        container.addClassName("aruclinic-login-card");
        container.setWidth("90%");
        container.setMaxWidth("480px");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-login-header");

        Div logo = new Div();
        logo.addClassName("aruclinic-login-logo");
        logo.add(new Icon(VaadinIcon.HOSPITAL));

        H1 title = new H1("Forgot Password");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Enter your email to receive a password reset link");
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

        form.add(emailGroup, submitButton);

        // Back to login
        Div backSection = new Div();
        backSection.addClassName("aruclinic-signup-link");
        backSection.add(backToLoginButton);

        container.add(header, form, backSection);
        return container;
    }

    private void handleForgotPassword() {
        String emailValue = email.getValue().trim();

        email.setErrorMessage(null);

        if (emailValue.isEmpty()) {
            email.setErrorMessage("Please enter your email");
            email.focus();
            return;
        }

        if (!emailValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            email.setErrorMessage("Please enter a valid email address");
            email.focus();
            return;
        }

        try {
            userService.forgotPassword(emailValue);

            Notification.show(
                "Password reset link has been sent to your email. Please check your inbox.",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate("auth/login"));

        } catch (Exception e) {
            showError("Failed to send reset link: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
