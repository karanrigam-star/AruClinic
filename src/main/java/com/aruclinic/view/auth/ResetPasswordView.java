package com.aruclinic.view.auth;

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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Reset Password view for setting a new password.
 */
@PageTitle("Reset Password | AruClinic")
@Route("auth/reset-password")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private String token;
    private final PasswordField newPassword = new PasswordField();
    private final PasswordField confirmPassword = new PasswordField();
    private final Button submitButton = new Button("Reset Password");
    private final Button backToLoginButton = new Button("Back to Login");

    public ResetPasswordView(UserService userService) {
        this.userService = userService;
        configureComponents();
        add(createResetPasswordContainer());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        java.util.Map<String, java.util.List<String>> parameters = event.getLocation().getQueryParameters().getParameters();
        if (parameters.containsKey("token")) {
            this.token = parameters.get("token").get(0);
        }
    }

    private void configureComponents() {
        setWidthFull();
        setMinHeight("100vh");
        setPadding(false);
        setSpacing(false);
        addClassName("aruclinic-login-page");

        // New password field
        newPassword.setPlaceholder("New Password");
        newPassword.setRequired(true);
        newPassword.setRequiredIndicatorVisible(true);
        newPassword.setWidthFull();
        newPassword.setClearButtonVisible(true);
        newPassword.addClassName("aruclinic-login-input");

        // Confirm password field
        confirmPassword.setPlaceholder("Confirm New Password");
        confirmPassword.setRequired(true);
        confirmPassword.setRequiredIndicatorVisible(true);
        confirmPassword.setWidthFull();
        confirmPassword.setClearButtonVisible(true);
        confirmPassword.addClassName("aruclinic-login-input");

        // Submit button
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClassName("aruclinic-login-button");
        submitButton.addClickListener(event -> handleResetPassword());

        // Back to login button
        backToLoginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginButton.addClassName("aruclinic-forgot-password");
        backToLoginButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/login")));
    }

    private Component createResetPasswordContainer() {
        Div container = new Div();
        container.addClassName("aruclinic-login-card");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-login-header");

        Div logo = new Div();
        logo.addClassName("aruclinic-login-logo");
        logo.add(new Icon(VaadinIcon.HOSPITAL));

        H1 title = new H1("Reset Password");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Enter your new password below");
        subtitle.addClassName("aruclinic-login-subtitle");

        header.add(logo, title, subtitle);

        // Form
        VerticalLayout form = new VerticalLayout();
        form.addClassName("aruclinic-login-form");
        form.setPadding(false);
        form.setSpacing(true);

        // New password field with icon
        Div newPasswordGroup = new Div();
        newPasswordGroup.addClassName("aruclinic-login-input-group");
        newPasswordGroup.add(new Icon(VaadinIcon.LOCK), newPassword);

        // Confirm password field with icon
        Div confirmPasswordGroup = new Div();
        confirmPasswordGroup.addClassName("aruclinic-login-input-group");
        confirmPasswordGroup.add(new Icon(VaadinIcon.LOCK), confirmPassword);

        // Password requirements
        Div requirements = new Div();
        requirements.addClassName("aruclinic-login-subtitle");
        requirements.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "var(--aruclinic-text-muted)")
            .set("margin-top", "var(--aruclinic-spacing-xs)");
        requirements.setText("Password must be at least 8 characters long");

        form.add(newPasswordGroup, confirmPasswordGroup, requirements, submitButton);

        // Back to login
        Div backSection = new Div();
        backSection.addClassName("aruclinic-signup-link");
        backSection.add(backToLoginButton);

        container.add(header, form, backSection);
        return container;
    }

    private void handleResetPassword() {
        String newPasswordValue = newPassword.getValue();
        String confirmPasswordValue = confirmPassword.getValue();

        newPassword.setErrorMessage(null);
        confirmPassword.setErrorMessage(null);

        if (newPasswordValue.isEmpty()) {
            newPassword.setErrorMessage("Please enter a new password");
            newPassword.focus();
            return;
        }

        if (confirmPasswordValue.isEmpty()) {
            confirmPassword.setErrorMessage("Please confirm your new password");
            confirmPassword.focus();
            return;
        }

        if (!newPasswordValue.equals(confirmPasswordValue)) {
            confirmPassword.setErrorMessage("Passwords do not match");
            confirmPassword.focus();
            return;
        }

        if (newPasswordValue.length() < 8) {
            newPassword.setErrorMessage("Password must be at least 8 characters long");
            newPassword.focus();
            return;
        }

        try {
            if (this.token == null || this.token.isEmpty()) {
                showError("Invalid reset token. Please request a new password reset link.");
                return;
            }

            userService.resetPassword(this.token, newPasswordValue);

            Notification.show(
                "Password has been reset successfully. Please login with your new password.",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            getUI().ifPresent(ui -> ui.navigate("auth/login"));

        } catch (Exception e) {
            showError("Failed to reset password: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
