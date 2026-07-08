package com.aruclinic.view;

import com.aruclinic.entity.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Route("")
@AnonymousAllowed
@CssImport("./themes/aruclinic/styles.css")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;

    private boolean isAuthenticated = false;
    private String userEmail = "";
    private String userRole = "";

    public HomeView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#F8FAFC"); // Clean surface background
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        checkAuthentication();
        
        if (isAuthenticated) {
            // Check for logout query parameters first (though unlikely when authenticated)
            java.util.List<String> logoutParams = event.getLocation().getQueryParameters().getParameters().get("logout");
            boolean hasLogout = logoutParams != null && !logoutParams.isEmpty() && "true".equals(logoutParams.get(0));
            
            if (!hasLogout) {
                // Automatically redirect authenticated users to their respective dashboards
                switch (userRole) {
                    case "SUPER_ADMIN":
                    case "ADMIN":
                        event.forwardTo("admin");
                        break;
                    case "DOCTOR":
                        event.forwardTo("doctor");
                        break;
                    case "RECEPTIONIST":
                        event.forwardTo("receptionist");
                        break;
                    case "PATIENT":
                    default:
                        event.forwardTo("patient");
                        break;
                }
                return;
            }
        }
        
        // Clear old children and rebuild to update auth buttons dynamically
        removeAll();
        
        add(createHeader());
        add(createHeroSection());
        add(createServicesSection());
        add(createSecurityTestingSection());
        add(createFooter());

        // Check for logout query parameters
        java.util.List<String> logoutParams = event.getLocation().getQueryParameters().getParameters().get("logout");
        if (logoutParams != null && !logoutParams.isEmpty() && "true".equals(logoutParams.get(0))) {
            String name = "User";
            try {
                jakarta.servlet.http.Cookie[] cookies = com.vaadin.flow.server.VaadinService.getCurrentRequest().getCookies();
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie cookie : cookies) {
                        if ("aruclinic_logout_name".equals(cookie.getName())) {
                            name = java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                            // Immediately delete the cookie so it won't display again
                            jakarta.servlet.http.Cookie deleteCookie = new jakarta.servlet.http.Cookie("aruclinic_logout_name", "");
                            deleteCookie.setPath("/");
                            deleteCookie.setMaxAge(0);
                            com.vaadin.flow.server.VaadinService.getCurrentResponse().addCookie(deleteCookie);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }

            com.vaadin.flow.component.notification.Notification.show(
                    "Hi \"" + name + "\", you have successfully logged out.",
                    5000,
                    com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
            ).addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS);
        }
    }

    private org.springframework.security.core.Authentication getAuthentication() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Try VaadinSession attributes
                com.vaadin.flow.server.VaadinSession vSession = com.vaadin.flow.server.VaadinSession.getCurrent();
                if (vSession != null) {
                    // Try our custom token attribute first
                    auth = (org.springframework.security.core.Authentication) 
                            vSession.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                    
                    if (auth == null) {
                        // Try standard SPRING_SECURITY_CONTEXT in VaadinSession
                        Object ctxObj = vSession.getAttribute("SPRING_SECURITY_CONTEXT");
                        if (ctxObj instanceof org.springframework.security.core.context.SecurityContext sc) {
                            auth = sc.getAuthentication();
                        }
                    }
                    
                    if (auth == null && vSession.getSession() != null) {
                        // Try underlying HttpSession / WrappedSession
                        Object ctxObj = vSession.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
                        if (ctxObj instanceof org.springframework.security.core.context.SecurityContext sc) {
                            auth = sc.getAuthentication();
                        }
                    }
                }
                
                // Try VaadinRequest as fallback
                if (auth == null && com.vaadin.flow.server.VaadinRequest.getCurrent() != null) {
                    com.vaadin.flow.server.WrappedSession ws = com.vaadin.flow.server.VaadinRequest.getCurrent().getWrappedSession();
                    if (ws != null) {
                        Object ctxObj = ws.getAttribute("SPRING_SECURITY_CONTEXT");
                        if (ctxObj instanceof org.springframework.security.core.context.SecurityContext sc) {
                            auth = sc.getAuthentication();
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return auth;
    }

    private void checkAuthentication() {
        isAuthenticated = false;
        userEmail = "";
        userRole = "";
        try {
            org.springframework.security.core.Authentication auth = getAuthentication();
            
            // Sync resolved authentication back to SecurityContextHolder for the current thread
            if (auth != null) {
                org.springframework.security.core.context.SecurityContext context = 
                        org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                    userEmail = springUser.getUsername();
                } else if (principal instanceof User user) {
                    userEmail = user.getEmail();
                } else if (principal instanceof String principalStr) {
                    userEmail = principalStr;
                }
                
                if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                    userRole = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(a -> a.replace("ROLE_", ""))
                        .findFirst()
                        .orElse("PATIENT");
                } else if (principal instanceof User user) {
                    userRole = user.getRoles().stream()
                        .findFirst()
                        .map(com.aruclinic.entity.Role::getName)
                        .orElse("PATIENT");
                } else {
                    userRole = "PATIENT";
                }
                isAuthenticated = true;
            }
        } catch (Exception e) {
            isAuthenticated = false;
        }
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("aruclinic-home-header");
        header.setWidthFull();
        header.getStyle()
            .set("background-color", "#002b5c") // Apollo Deep Navy
            .set("padding", "0 var(--aruclinic-spacing-xl)")
            .set("height", "70px")
            .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)")
            .set("position", "sticky")
            .set("top", "0")
            .set("z-index", "100");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Logo
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.addClassName("aruclinic-home-logo-section");
        logoSection.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Div logoBadge = new Div();
        logoBadge.getStyle()
            .set("background-color", "#22C55E") // Apollo Clinical Green
            .set("color", "white")
            .set("font-weight", "800")
            .set("width", "36px")
            .set("height", "36px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border-radius", "8px")
            .set("font-size", "var(--aruclinic-font-size-lg)");
        logoBadge.setText("AC");

        H2 brandName = new H2("AruClinic");
        brandName.getStyle()
            .set("color", "white")
            .set("margin", "0")
            .set("font-size", "var(--aruclinic-font-size-xl)")
            .set("font-weight", "700");

        logoSection.add(logoBadge, brandName);

        // Auth Buttons
        HorizontalLayout authActions = new HorizontalLayout();
        authActions.addClassName("aruclinic-home-auth-actions");
        authActions.setAlignItems(FlexComponent.Alignment.CENTER);
        authActions.setSpacing(true);

        if (isAuthenticated) {
            Span welcomeText = new Span("Hello, " + welcomeName() + " (" + userRole + ")");
            welcomeText.getStyle()
                .set("color", "rgba(255, 255, 255, 0.85)")
                .set("font-size", "var(--aruclinic-font-size-sm)")
                .set("font-weight", "500");

            Button dashboardBtn = new Button("Go to Dashboard");
            dashboardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dashboardBtn.getStyle().set("background-color", "#22C55E"); // Accent green
            dashboardBtn.addClickListener(e -> navigateToDashboard());

            Button logoutBtn = new Button("Logout");
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutBtn.getStyle().set("color", "white");
            logoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("auth/logout")));

            authActions.add(welcomeText, dashboardBtn, logoutBtn);
        } else {
            Button loginBtn = new Button("Login");
            loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            loginBtn.getStyle().set("color", "white");
            loginBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("auth/login")));

            Button registerBtn = new Button("Register");
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.getStyle().set("background-color", "#22C55E");
            registerBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("auth/register")));

            authActions.add(loginBtn, registerBtn);
        }

        header.add(logoSection, authActions);
        return header;
    }

    private String welcomeName() {
        if (userEmail == null) return "User";
        int idx = userEmail.indexOf('@');
        return idx > 0 ? userEmail.substring(0, idx) : userEmail;
    }

    private void navigateToDashboard() {
        getUI().ifPresent(ui -> {
            switch (userRole) {
                case "SUPER_ADMIN":
                    ui.navigate("admin");
                    break;
                case "DOCTOR":
                    ui.navigate("doctor");
                    break;
                case "RECEPTIONIST":
                    ui.navigate("receptionist");
                    break;
                case "PATIENT":
                default:
                    ui.navigate("patient");
                    break;
            }
        });
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("aruclinic-home-hero");
        hero.getStyle()
            .set("background", "radial-gradient(circle at top right, #003b5c, #001e3d)")
            .set("padding", "var(--aruclinic-spacing-3xl) var(--aruclinic-spacing-xl)")
            .set("text-align", "center")
            .set("color", "white")
            .set("width", "100%");

        H1 mainHeading = new H1("Premium Healthcare Management System");
        mainHeading.addClassName("aruclinic-home-hero-title");
        mainHeading.getStyle()
            .set("font-size", "2.8rem")
            .set("font-weight", "800")
            .set("margin", "0 auto var(--aruclinic-spacing-md)")
            .set("max-width", "800px")
            .set("letter-spacing", "-0.5px");

        Paragraph subHeading = new Paragraph("A high-performance portal linking patients, clinical staff, practitioners, and management seamlessly.");
        subHeading.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("color", "rgba(255, 255, 255, 0.8)")
            .set("max-width", "600px")
            .set("margin", "0 auto var(--aruclinic-spacing-xl)");

        // Search Mockup
        Div searchBar = new Div();
        searchBar.addClassName("aruclinic-home-search-bar");
        searchBar.getStyle()
            .set("background", "white")
            .set("border-radius", "50px")
            .set("padding", "6px 12px 6px 24px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("max-width", "600px")
            .set("margin", "0 auto")
            .set("box-shadow", "0 8px 30px rgba(0,0,0,0.25)");

        Icon searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.getStyle().set("color", "#6B7280").set("margin-right", "12px");

        Span searchText = new Span("Search for doctors, specialities, or health services...");
        searchText.getStyle().set("color", "#9CA3AF").set("font-size", "var(--aruclinic-font-size-sm)").set("flex", "1").set("text-align", "left");

        Button searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.getStyle().set("background-color", "#002b5c").set("border-radius", "25px");

        searchBar.add(searchIcon, searchText, searchButton);
        hero.add(mainHeading, subHeading, searchBar);

        return hero;
    }

    private Component createServicesSection() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(true);
        container.getStyle().set("padding", "var(--aruclinic-spacing-3xl) var(--aruclinic-spacing-xl)");
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 sectionTitle = new H2("Our Clinical Solutions");
        sectionTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "var(--aruclinic-font-size-2xl)")
            .set("margin-bottom", "var(--aruclinic-spacing-xl)")
            .set("font-weight", "700");

        Div grid = new Div();
        grid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))")
            .set("gap", "var(--aruclinic-spacing-xl)")
            .set("width", "100%")
            .set("max-width", "1200px");

        grid.add(createServiceCard(VaadinIcon.DOCTOR, "Consult Specialists", "Connect with verified doctors, dentists, pediatricians and dermatologists online or in-clinic."));
        grid.add(createServiceCard(VaadinIcon.FILE_TEXT, "Prescription & History", "Keep track of active prescriptions, digital consultation notes, and health summaries instantly."));
        grid.add(createServiceCard(VaadinIcon.GLASSES, "Lab Diagnostics", "Schedule blood profiles, radiology scans, and track path-lab report values directly in your dashboard."));
        grid.add(createServiceCard(VaadinIcon.MONEY, "Instant Billing", "Transparent billing summaries, convenient online payment gateways, and insurance tracking portals."));

        container.add(sectionTitle, grid);
        return container;
    }

    private Component createServiceCard(VaadinIcon iconName, String titleText, String descText) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "var(--aruclinic-spacing-xl)")
            .set("box-shadow", "0 4px 15px rgba(0,0,0,0.04)")
            .set("border", "1px solid #E5E7EB")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("cursor", "pointer");

        card.getElement().addEventListener("mouseover", e -> {
            card.getStyle().set("transform", "translateY(-4px)").set("box-shadow", "0 10px 25px rgba(0,0,0,0.08)");
        });
        card.getElement().addEventListener("mouseout", e -> {
            card.getStyle().set("transform", "none").set("box-shadow", "0 4px 15px rgba(0,0,0,0.04)");
        });

        Div iconCircle = new Div();
        iconCircle.getStyle()
            .set("background-color", "rgba(15, 108, 189, 0.08)")
            .set("color", "var(--aruclinic-primary)")
            .set("width", "50px")
            .set("height", "50px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-bottom", "var(--aruclinic-spacing-md)");
        iconCircle.add(new Icon(iconName));

        H3 cardTitle = new H3(titleText);
        cardTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "var(--aruclinic-font-size-md)")
            .set("margin", "0 0 var(--aruclinic-spacing-xs)")
            .set("font-weight", "600");

        Paragraph cardDesc = new Paragraph(descText);
        cardDesc.getStyle()
            .set("color", "#6B7280")
            .set("font-size", "var(--aruclinic-font-size-sm)")
            .set("line-height", "1.5");

        card.add(iconCircle, cardTitle, cardDesc);
        return card;
    }

    private Component createSecurityTestingSection() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setPadding(true);
        container.getStyle()
            .set("background-color", "#EEF2F6")
            .set("padding", "var(--aruclinic-spacing-3xl) var(--aruclinic-spacing-xl)")
            .set("border-top", "1px solid #E2E8F0");
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Role-Based Security Testing Console");
        title.getStyle()
            .set("color", "#002b5c")
            .set("font-size", "var(--aruclinic-font-size-xl)")
            .set("margin-bottom", "4px")
            .set("font-weight", "700");

        Paragraph sub = new Paragraph("Select a clinical portal below to test authorization walls. If unauthorized, access will be blocked.");
        sub.getStyle()
            .set("color", "#4B5563")
            .set("font-size", "var(--aruclinic-font-size-sm)")
            .set("margin-bottom", "var(--aruclinic-spacing-xl)");

        Div grid = new Div();
        grid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
            .set("gap", "var(--aruclinic-spacing-md)")
            .set("width", "100%")
            .set("max-width", "1000px");

        grid.add(createDashboardLinkCard("Patient Dashboard", "patient", VaadinIcon.USER));
        grid.add(createDashboardLinkCard("Doctor Dashboard", "doctor", VaadinIcon.DOCTOR));
        grid.add(createDashboardLinkCard("Receptionist Dashboard", "receptionist", VaadinIcon.NOTEBOOK));
        grid.add(createDashboardLinkCard("Admin Dashboard", "admin", VaadinIcon.SHIELD));

        container.add(title, sub, grid);
        return container;
    }

    private Component createDashboardLinkCard(String name, String route, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "var(--aruclinic-spacing-lg)")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.03)")
            .set("text-align", "center")
            .set("border", "1px solid #E2E8F0");

        H4 title = new H4(name);
        title.getStyle().set("margin", "var(--aruclinic-spacing-sm) 0").set("font-size", "var(--aruclinic-font-size-sm)");

        Button btn = new Button("Enter Portal", new Icon(icon));
        btn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btn.setWidthFull();
        btn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        card.add(title, btn);
        return card;
    }

    private Component createFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("aruclinic-home-footer");
        footer.setWidthFull();
        footer.getStyle()
            .set("background-color", "#001e3d")
            .set("padding", "var(--aruclinic-spacing-xl)")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("display", "flex");

        Span copyright = new Span("© 2026 AruClinic Healthcare. All rights reserved. Designed to Fluent Healthcare Standards.");
        copyright.getStyle()
            .set("color", "rgba(255, 255, 255, 0.6)")
            .set("font-size", "var(--aruclinic-font-size-xs)");

        Anchor whatsappLink = new Anchor("https://wa.me/916909178159", "");
        whatsappLink.setTarget("_blank");
        whatsappLink.addClassName("aruclinic-whatsapp-btn");
        whatsappLink.getStyle()
            .set("display", "inline-flex")
            .set("align-items", "center")
            .set("background-color", "rgba(37, 211, 102, 0.12)")
            .set("border", "1px solid #25D366")
            .set("padding", "8px 16px")
            .set("border-radius", "30px")
            .set("text-decoration", "none")
            .set("transition", "all 0.2s ease");
            
        whatsappLink.getElement().setProperty("innerHTML", 
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" fill=\"#25D366\" class=\"bi bi-whatsapp\" viewBox=\"0 0 16 16\" style=\"margin-right: 8px; vertical-align: middle;\">" +
            "<path d=\"M13.601 2.326A7.85 7.85 0 0 0 7.994 0C3.627 0 .068 3.558.064 7.926c0 1.399.366 2.76 1.057 3.965L0 16l4.204-1.102a7.9 7.9 0 0 0 3.79.949h.004c4.368 0 7.927-3.561 7.928-7.928a7.89 7.89 0 0 0-2.325-5.645zM7.994 14.521a6.6 6.6 0 0 1-3.356-.92l-.24-.144-2.494.654.666-2.433-.156-.251a6.56 6.56 0 0 1-1.007-3.505c0-3.626 2.957-6.584 6.591-6.584a6.56 6.56 0 0 1 4.66 1.931 6.56 6.56 0 0 1 1.928 4.66c-.004 3.639-2.961 6.592-6.592 6.592m3.699-4.7c-.187-.093-1.102-.544-1.272-.605-.17-.061-.293-.093-.418.093-.125.187-.481.605-.59.728-.109.123-.217.138-.404.045-1.928-.963-2.733-1.579-3.907-2.605-.3-.263-.102-.242.09-.434.172-.172.383-.443.473-.564.09-.12.045-.226-.023-.32-.068-.094-.418-.997-.57-1.365-.148-.358-.299-.31-.41-.315-.1-.005-.216-.006-.33-.006a.65.65 0 0 0-.473.22c-.164.18-0.627.614-.627 1.498s.644 1.738.733 1.861c.09.124 1.266 1.933 3.07 2.71 1.507.647 2.128.536 2.5.5.373-.035 1.102-.45 1.258-.885.156-.435.156-.807.109-.885-.045-.078-.17-.123-.356-.216\"/>" +
            "</svg><span style=\"font-weight:600;color:white;vertical-align:middle;font-size:12px;\">24/7 Helpline: 6909178159</span>");

        footer.add(copyright, whatsappLink);
        return footer;
    }
}
