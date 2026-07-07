package com.aruclinic.view.auth;

import com.aruclinic.dto.LoginRequestDto;
import com.aruclinic.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.aruclinic.security.CustomUserDetailsService;
import com.aruclinic.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@PageTitle("Login | AruClinic")
@Route("auth/login")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class LoginView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final com.aruclinic.service.AdminService adminService;
    private final com.aruclinic.repository.NotificationRepository notificationRepository;
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final Button loginButton = new Button("Login");
    private final Button forgotPasswordButton = new Button("Forgot Password?");
    private final Button registerButton = new Button("Create Account");

    public LoginView(UserService userService, 
                     CustomUserDetailsService customUserDetailsService,
                     UserRepository userRepository,
                     com.aruclinic.service.AdminService adminService,
                     com.aruclinic.repository.NotificationRepository notificationRepository) {
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.adminService = adminService;
        this.notificationRepository = notificationRepository;
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

            // 1. Verify credentials via User Service
            userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

            // 2. Load UserDetails for Spring Security context
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(emailValue);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            // 3. Set the authentication context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. Save to Vaadin Session to propagate security context to route observers
            VaadinSession.getCurrent().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            VaadinSession.getCurrent().setAttribute("SPRING_SECURITY_AUTHENTICATION", authentication);
            if (VaadinSession.getCurrent().getSession() != null) {
                VaadinSession.getCurrent().getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            }

            Notification.show("Login successful! Welcome back.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navigate to respective dashboard according to role
            String landingRoute = "";
            String roleName = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("PATIENT");

            if ("SUPER_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                landingRoute = "admin";
            } else if ("DOCTOR".equalsIgnoreCase(roleName)) {
                landingRoute = "doctor";
            } else if ("RECEPTIONIST".equalsIgnoreCase(roleName)) {
                landingRoute = "receptionist";
            } else {
                landingRoute = "patient";
            }

            final String targetRoute = landingRoute;
            getUI().ifPresent(ui -> ui.navigate(targetRoute));

        } catch (com.aruclinic.exception.UserDisabledException e) {
            showRequestToEnableDialog(emailValue);
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    private void showRequestToEnableDialog(String emailValue) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Account Disabled");
        dialog.setWidth("420px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Span info = new Span("Your account (" + emailValue + ") has been disabled. You cannot login until your account is re-enabled by both Admin and Receptionist staff.");
        info.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        content.add(info);

        Button requestBtn = new Button("Request Account Activation", new Icon(VaadinIcon.PAPERPLANE));
        requestBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        requestBtn.setWidthFull();
        requestBtn.addClickListener(event -> {
            try {
                com.aruclinic.entity.User user = userRepository.findByEmail(emailValue).orElse(null);
                if (user != null) {
                    // Check if already requested
                    String currentRequestStatus = adminService.getClinicSetting("enable_request_status_" + user.getId(), null);
                    if ("PENDING".equals(currentRequestStatus)) {
                        Notification.show("Activation request is already pending approval by staff.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_WARNING);
                        dialog.close();
                        return;
                    }

                    // 1. Save request status in settings
                    adminService.saveClinicSetting("enable_request_status_" + user.getId(), "PENDING");
                    adminService.saveClinicSetting("enable_request_admin_approved_" + user.getId(), "false");
                    adminService.saveClinicSetting("enable_request_receptionist_approved_" + user.getId(), "false");

                    // 2. Send notification to all Admins and Receptionists
                    String name = user.getFirstName() + " " + user.getLastName();
                    String msg = "User " + name + " (" + user.getEmail() + ") has requested to enable their account. Both Admin and Receptionist must approve to enable login.";
                    String title = "Account Enable Request: User #" + user.getId();

                    java.util.List<com.aruclinic.entity.User> staff = userRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> 
                            r.getName() != null && (r.getName().contains("ADMIN") || r.getName().contains("RECEPTIONIST"))
                        ))
                        .collect(java.util.stream.Collectors.toList());

                    for (com.aruclinic.entity.User u : staff) {
                        com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                        notif.setUser(u);
                        notif.setTitle(title);
                        notif.setMessage(msg);
                        notif.setRead(false);
                        notif.setCreatedAt(java.time.LocalDateTime.now());
                        notificationRepository.save(notif);
                    }

                    Notification.show("Activation request sent successfully to Admin and Receptionist staff!", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                } else {
                    Notification.show("User not found.", 3000, Notification.Position.TOP_CENTER);
                }
            } catch (Exception ex) {
                Notification.show("Error sending request: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER);
            }
        });

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(closeBtn, requestBtn);
        dialog.add(content);
        dialog.open();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
