package com.aruclinic.frontend.views.settings;

import com.aruclinic.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
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

/**
 * Change Password view for updating user password.
 */
@PageTitle("Change Password | AruClinic")
@Route("settings/change-password")
@CssImport("./themes/aruclinic/login-view.css")
public class ChangePasswordView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

	private final UserService userService;

    private final PasswordField currentPassword = new PasswordField("Current Password");
    private final PasswordField newPassword = new PasswordField("New Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm New Password");

    private final Button saveButton = new Button("Save Changes");
    private final Button cancelButton = new Button("Cancel");

    public ChangePasswordView(UserService userService) {
        this.userService = userService;
        configureComponents();
        add(createChangePasswordForm());
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

        // Current password field
        currentPassword.setPlaceholder("Enter your current password");
        currentPassword.setRequired(true);
        currentPassword.setRequiredIndicatorVisible(true);
        currentPassword.setWidthFull();
        currentPassword.setClearButtonVisible(true);
        currentPassword.addClassName("aruclinic-login-input");

        // New password field
        newPassword.setPlaceholder("Enter new password (min 8 characters)");
        newPassword.setRequired(true);
        newPassword.setRequiredIndicatorVisible(true);
        newPassword.setWidthFull();
        newPassword.setClearButtonVisible(true);
        newPassword.addClassName("aruclinic-login-input");

        // Confirm password field
        confirmPassword.setPlaceholder("Confirm new password");
        confirmPassword.setRequired(true);
        confirmPassword.setRequiredIndicatorVisible(true);
        confirmPassword.setWidthFull();
        confirmPassword.setClearButtonVisible(true);
        confirmPassword.addClassName("aruclinic-login-input");

        // Save button
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClassName("aruclinic-login-button");
        saveButton.addClickListener(e -> handleChangePassword());

        // Cancel button
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClassName("aruclinic-forgot-password");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("settings")));
    }

    private Component createChangePasswordForm() {
        Div container = new Div();
        container.addClassName("aruclinic-login-card");
        container.setWidth("90%");
        container.setMaxWidth("500px");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-login-header");

        Div logo = new Div();
        logo.addClassName("aruclinic-login-logo");
        logo.add(new Icon(VaadinIcon.KEY));

        H1 title = new H1("Change Password");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Update your account password");
        subtitle.addClassName("aruclinic-login-subtitle");

        header.add(logo, title, subtitle);

        // Form
        VerticalLayout form = new VerticalLayout();
        form.addClassName("aruclinic-login-form");
        form.setPadding(false);
        form.setSpacing(true);

        // Current password with icon
        Div currentPasswordGroup = new Div();
        currentPasswordGroup.addClassName("aruclinic-login-input-group");
        currentPasswordGroup.add(new Icon(VaadinIcon.LOCK), currentPassword);

        // New password with icon
        Div newPasswordGroup = new Div();
        newPasswordGroup.addClassName("aruclinic-login-input-group");
        newPasswordGroup.add(new Icon(VaadinIcon.LOCK), newPassword);

        // Confirm password with icon
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

        form.add(currentPasswordGroup, newPasswordGroup, confirmPasswordGroup, requirements, saveButton);

        // Cancel link
        Div cancelSection = new Div();
        cancelSection.addClassName("aruclinic-signup-link");
        cancelSection.add(cancelButton);

        container.add(header, form, cancelSection);
        return container;
    }

    private void handleChangePassword() {
        String currentPasswordValue = currentPassword.getValue();
        String newPasswordValue = newPassword.getValue();
        String confirmPasswordValue = confirmPassword.getValue();

        // Clear previous errors
        currentPassword.setErrorMessage(null);
        newPassword.setErrorMessage(null);
        confirmPassword.setErrorMessage(null);

        // Validate all fields are filled
        if (currentPasswordValue.isEmpty()) {
            currentPassword.setErrorMessage("Please enter your current password");
            currentPassword.focus();
            return;
        }

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

        // Validate password match
        if (!newPasswordValue.equals(confirmPasswordValue)) {
            confirmPassword.setErrorMessage("New password and confirm password do not match");
            confirmPassword.focus();
            return;
        }

        // Validate password length
        if (newPasswordValue.length() < 8) {
            newPassword.setErrorMessage("Password must be at least 8 characters long");
            newPassword.focus();
            return;
        }

        // Validate that new password is different from current
        if (currentPasswordValue.equals(newPasswordValue)) {
            newPassword.setErrorMessage("New password must be different from current password");
            newPassword.focus();
            return;
        }

        try {
            // In a real app, this would call the user service to change the password
            userService.changePassword(currentPasswordValue, newPasswordValue);

            Notification.show(
                "Password changed successfully!",
                3000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear form and navigate back to settings
            clearForm();
            getUI().ifPresent(ui -> ui.navigate("settings"));

        } catch (Exception e) {
            showError("Failed to change password: " + e.getMessage());
        }
    }

    private void clearForm() {
        currentPassword.clear();
        newPassword.clear();
        confirmPassword.clear();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
