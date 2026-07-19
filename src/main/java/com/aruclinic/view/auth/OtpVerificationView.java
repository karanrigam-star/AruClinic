package com.aruclinic.view.auth;

import com.aruclinic.service.OtpService;
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
import com.aruclinic.service.UserService;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import com.aruclinic.security.CustomUserDetailsService;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.security.util.JwtTokenProvider;

/**
 * Enhanced OTP Verification view for email/SMS verification during registration.
 */
@PageTitle("Verify OTP | AruClinic")
@Route("auth/verify")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
@CssImport("./themes/aruclinic/login-view.css")
public class OtpVerificationView extends VerticalLayout {

    private final OtpService otpService;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.aruclinic.util.EmailUtil emailUtil;
    private final TextField email = new TextField();
    private final TextField mobile = new TextField();
    private final TextField otpCode = new TextField();
    private final Button verifyButton = new Button("Verify OTP");
    private final Button resendButton = new Button("Resend OTP");
    private final Button backToLoginButton = new Button("Back to Login");

    public OtpVerificationView(OtpService otpService, 
                               UserService userService,
                               CustomUserDetailsService customUserDetailsService,
                               UserRepository userRepository,
                               JwtTokenProvider jwtTokenProvider,
                               com.aruclinic.util.EmailUtil emailUtil) {
        this.otpService = otpService;
        this.userService = userService;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailUtil = emailUtil;
        configureComponents();
        add(createOtpVerificationContainer());
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
        email.setPlaceholder("Enter your email");
        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();
        email.setClearButtonVisible(true);
        email.addClassName("aruclinic-login-input");

        // Mobile field
        mobile.setPlaceholder("Enter your mobile number");
        mobile.setRequired(true);
        mobile.setRequiredIndicatorVisible(true);
        mobile.setWidthFull();
        mobile.setClearButtonVisible(true);
        mobile.setPattern("\\d{10}");
        mobile.addClassName("aruclinic-login-input");

        // OTP code field
        otpCode.setPlaceholder("6-digit OTP code");
        otpCode.setRequired(true);
        otpCode.setRequiredIndicatorVisible(true);
        otpCode.setWidthFull();
        otpCode.setClearButtonVisible(true);
        otpCode.setPattern("\\d{6}");
        otpCode.addClassName("aruclinic-login-input");

        // Verify button
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        verifyButton.setWidthFull();
        verifyButton.addClassName("aruclinic-login-button");
        verifyButton.addClickListener(event -> handleOtpVerification());

        // Resend button
        resendButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resendButton.addClassName("aruclinic-forgot-password");
        resendButton.addClickListener(event -> handleResendOtp());

        // Back to login button
        backToLoginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginButton.addClassName("aruclinic-forgot-password");
        backToLoginButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("auth/login")));
    }

    private Component createOtpVerificationContainer() {
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

        H1 title = new H1("OTP Verification");
        title.addClassName("aruclinic-login-title");

        Paragraph subtitle = new Paragraph("Enter the 6-digit code sent to your email and mobile");
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

        // Mobile field with icon
        Div mobileGroup = new Div();
        mobileGroup.addClassName("aruclinic-login-input-group");
        mobileGroup.add(new Icon(VaadinIcon.PHONE), mobile);

        // OTP field with icon
        Div otpGroup = new Div();
        otpGroup.addClassName("aruclinic-login-input-group");
        otpGroup.add(new Icon(VaadinIcon.KEY), otpCode);

        form.add(emailGroup, mobileGroup, otpGroup, verifyButton);

        // Resend OTP
        Div resendSection = new Div();
        resendSection.addClassName("aruclinic-signup-link");
        resendSection.add(new Span("Didn't receive OTP? "), resendButton);

        // Back to login
        Div backSection = new Div();
        backSection.addClassName("aruclinic-signup-link");
        backSection.add(backToLoginButton);

        container.add(header, form, resendSection, backSection);
        return container;
    }

    private void handleOtpVerification() {
        String emailValue = email.getValue().trim();
        String mobileValue = mobile.getValue().trim();
        String otpCodeValue = otpCode.getValue().trim();

        email.setErrorMessage(null);
        mobile.setErrorMessage(null);
        otpCode.setErrorMessage(null);

        if (emailValue.isEmpty()) {
            email.setErrorMessage("Please enter your email");
            email.focus();
            return;
        }

        if (mobileValue.isEmpty()) {
            mobile.setErrorMessage("Please enter your mobile number");
            mobile.focus();
            return;
        }

        if (otpCodeValue.isEmpty()) {
            otpCode.setErrorMessage("Please enter the OTP code");
            otpCode.focus();
            return;
        }

        if (!otpCodeValue.matches("\\d{6}")) {
            otpCode.setErrorMessage("OTP must be 6 digits");
            otpCode.focus();
            return;
        }

        try {
            // Validate the OTP using the secure service method
            if (userService.verifyOtp(emailValue, mobileValue, otpCodeValue)) {
                
                // 1. Load UserDetails for Spring Security context
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(emailValue);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 2. Set the authentication context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 3. Save to Vaadin Session to propagate security context to route observers
                VaadinSession.getCurrent().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                VaadinSession.getCurrent().setAttribute("SPRING_SECURITY_AUTHENTICATION", authentication);
                if (VaadinSession.getCurrent().getSession() != null) {
                    VaadinSession.getCurrent().getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                }

                // 4. Generate JWT Token and store in session for validation
                String token = jwtTokenProvider.generateToken(emailValue);
                VaadinSession.getCurrent().setAttribute("jwt_token", token);

                // 5. Clean up pending login variables
                VaadinSession.getCurrent().setAttribute("pending_login_email", null);
                VaadinSession.getCurrent().setAttribute("pending_login_mobile", null);

                Notification.show("OTP verified successfully! Welcome back.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // 6. Navigate to respective dashboard according to role
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
            } else {
                showError("Invalid OTP code or it has expired. Please try again.");
            }
        } catch (Exception e) {
            showError("OTP verification failed: " + e.getMessage());
        }
    }

    private void handleResendOtp() {
        String emailValue = email.getValue().trim();
        String mobileValue = mobile.getValue().trim();

        if (emailValue.isEmpty() || mobileValue.isEmpty()) {
            showError("Please enter your email and mobile number to resend OTP");
            return;
        }

        try {
            // Call the service to resend OTP
            com.aruclinic.entity.OtpVerification otpVerification = otpService.generateOtp(emailValue, mobileValue);

            // Send OTP email using SMTP
            emailUtil.sendEmail(emailValue, "Your AruClinic Verification OTP (Resent)", 
                "Hello,\n\n" +
                "You requested a new OTP. Your AruClinic Verification OTP is: " + otpVerification.getRawOtpCode() + "\n" +
                "This code is valid for 5 minutes.\n\n" +
                "Best regards,\n" +
                "AruClinic Team");

            Notification.show(
                "New OTP has been sent to your email. Please check.",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            showError("Failed to resend OTP: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        String pendingEmail = (String) VaadinSession.getCurrent().getAttribute("pending_login_email");
        String pendingMobile = (String) VaadinSession.getCurrent().getAttribute("pending_login_mobile");
        if (pendingEmail != null) {
            email.setValue(pendingEmail);
            email.setReadOnly(true);
        }
        if (pendingMobile != null) {
            mobile.setValue(pendingMobile);
            mobile.setReadOnly(true);
        }
    }
}
