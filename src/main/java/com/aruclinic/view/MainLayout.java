package com.aruclinic.view;

import com.aruclinic.entity.User;
import com.aruclinic.security.util.JwtTokenProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import com.aruclinic.repository.UserRepository;
import com.aruclinic.service.NotificationService;

@CssImport("./themes/aruclinic/styles.css")
public class MainLayout extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

    private static final long serialVersionUID = 1L;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private SideNav sideNav;
    private Div sideBarContainer;
    private boolean sidebarCollapsed = true;
    private FlexLayout mainContent;
    private Button backBtn;
    private String currentFallbackRoute = "";

    private Span notificationBadge;
    private Span userName;
    private Span userRole;
    private Div userAvatar;

    public MainLayout(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, NotificationService notificationService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createHeader(), createMainContent());
        updateNavigation();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.getStyle()
            .set("background", "var(--aruclinic-primary)")
            .set("color", "white")
            .set("padding", "0 var(--aruclinic-spacing-xl)")
            .set("height", "64px")
            .set("box-shadow", "var(--aruclinic-shadow-md)")
            .set("position", "sticky")
            .set("top", "0")
            .set("z-index", "100");

        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);

        Button menuToggle = new Button(new Icon(VaadinIcon.MENU));
        menuToggle.addClassName("aruclinic-mobile-menu-toggle");
        menuToggle.getStyle()
            .set("background", "none")
            .set("border", "none")
            .set("color", "white")
            .set("cursor", "pointer")
            .set("padding", "var(--aruclinic-spacing-xs)");
        menuToggle.addClickListener(e -> toggleSidebar());

        Div logo = new Div();
        logo.getStyle()
            .set("width", "40px")
            .set("height", "40px")
            .set("background", "white")
            .set("border-radius", "var(--aruclinic-radius-sm)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("color", "var(--aruclinic-primary)")
            .set("font-weight", "700")
            .set("font-size", "var(--aruclinic-font-size-xl)");
        logo.setText("AC");

        H1 title = new H1("AruClinic");
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--aruclinic-font-size-xl)")
            .set("font-weight", "600");

        backBtn = new Button("Back", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.getStyle()
            .set("background", "rgba(255, 255, 255, 0.15)")
            .set("border", "none")
            .set("color", "white")
            .set("cursor", "pointer")
            .set("font-weight", "500")
            .set("padding", "var(--aruclinic-spacing-xs) var(--aruclinic-spacing-md)")
            .set("border-radius", "var(--aruclinic-radius-sm)")
            .set("margin-left", "var(--aruclinic-spacing-md)");
        backBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        backBtn.addClickListener(e -> {
            if (currentFallbackRoute != null && !currentFallbackRoute.isEmpty()) {
                getUI().ifPresent(ui -> ui.navigate(currentFallbackRoute));
            } else {
                getUI().ifPresent(ui -> ui.getPage().getHistory().back());
            }
        });
        backBtn.setVisible(false);

        leftSection.add(menuToggle, logo, title, backBtn);

        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);

        Button notificationsBtn = new Button(new Icon(VaadinIcon.BELL));
        notificationsBtn.getStyle()
            .set("background", "none")
            .set("border", "none")
            .set("color", "white")
            .set("cursor", "pointer")
            .set("padding", "var(--aruclinic-spacing-xs)")
            .set("position", "relative");
        notificationsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("notifications")));

        this.notificationBadge = new Span("0");
        this.notificationBadge.setVisible(false);
        this.notificationBadge.getStyle()
            .set("position", "absolute")
            .set("top", "4px")
            .set("right", "4px")
            .set("width", "18px")
            .set("height", "18px")
            .set("background", "var(--aruclinic-danger)")
            .set("color", "white")
            .set("font-size", "10px")
            .set("font-weight", "600")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        // Wrap button and badge in a Div
        Div notificationsWrapper = new Div();
        notificationsWrapper.add(notificationsBtn, this.notificationBadge);

        this.userAvatar = new Div();
        this.userAvatar.getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "50%")
            .set("background", "var(--aruclinic-primary)")
            .set("color", "white")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-weight", "600")
            .set("font-size", "var(--aruclinic-font-size-sm)")
            .set("cursor", "pointer");
        this.userAvatar.setText("U");

        Div userInfo = new Div();
        userInfo.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("margin-right", "var(--aruclinic-spacing-sm)");

        this.userName = new Span("");
        this.userName.getStyle()
            .set("font-size", "var(--aruclinic-font-size-sm)")
            .set("font-weight", "600")
            .set("color", "white");

        this.userRole = new Span("");
        this.userRole.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "rgba(255, 255, 255, 0.8)");

        userInfo.add(this.userName, this.userRole);

        rightSection.add(notificationsWrapper, userInfo, this.userAvatar);

        header.add(leftSection, rightSection);
        return header;
    }

    private Component createMainContent() {
        mainContent = new FlexLayout();
        mainContent.setSizeFull();
        mainContent.getStyle()
            .set("display", "flex")
            .set("overflow", "hidden");

        sideBarContainer = new Div();
        sideBarContainer.addClassName("aruclinic-sidenav");
        // Close sidebar on click (useful for mobile navigation auto-collapse)
        sideBarContainer.addClickListener(e -> {
            if (!sidebarCollapsed) {
                toggleSidebar();
            }
        });

        sideNav = new SideNav();
        sideNav.setSizeFull();
        sideNav.getStyle().set("background", "transparent");

        Div trigger = new Div();
        trigger.addClassName("aruclinic-sidenav-trigger");
        trigger.add(new Icon(VaadinIcon.ANGLE_RIGHT));

        sideBarContainer.add(sideNav, trigger);

        Div contentArea = new Div();
        contentArea.setSizeFull();
        contentArea.addClassName("aruclinic-content-area");

        mainContent.add(sideBarContainer, contentArea);
        return mainContent;
    }

    private org.springframework.security.core.Authentication getAuthentication() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            try {
                // Try VaadinSession attributes
                VaadinSession vSession = VaadinSession.getCurrent();
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

    private String getUserRole() {
        String role = "PATIENT";
        try {
            org.springframework.security.core.Authentication auth = getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                    role = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(a -> a.replace("ROLE_", ""))
                        .findFirst()
                        .orElse("PATIENT");
                } else {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof User user) {
                        role = user.getRoles().stream()
                            .findFirst()
                            .map(r -> r.getName())
                            .orElse("PATIENT");
                    }
                }
            }
        } catch (Exception e) {
            // Not authenticated
        }
        return role;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isAuthenticated = false;
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
                if (principal instanceof org.springframework.security.core.userdetails.User || 
                    principal instanceof User || 
                    principal instanceof String) {
                    isAuthenticated = true;
                }
            }
        } catch (Exception e) {
            // Not authenticated
        }

        if (!isAuthenticated) {
            event.forwardTo("auth/login");
            return;
        }

        // Enforce role-based access checks on routes
        String path = event.getLocation().getPath();
        String role = getUserRole();

        // Update Back Button Visibility and Fallback Link
        if (backBtn != null) {
            if (path.isEmpty() || path.equals("admin") || path.equals("doctor") || path.equals("receptionist") || path.equals("patient")) {
                backBtn.setVisible(false);
            } else {
                backBtn.setVisible(true);
                String fallback = "";
                if (path.startsWith("admin")) {
                    fallback = "admin";
                } else if (path.startsWith("doctor/prescriptions/form")) {
                    fallback = "doctor/prescriptions";
                } else if (path.startsWith("doctor")) {
                    fallback = "doctor";
                } else if (path.startsWith("receptionist")) {
                    fallback = "receptionist";
                } else if (path.startsWith("patient")) {
                    fallback = "patient";
                } else {
                    fallback = "";
                }
                currentFallbackRoute = fallback;
            }
        }
        
        // 1. SUPER_ADMIN and ADMIN have full access to navigate anywhere in the app
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        // 2. Block non-admins from admin routes
        if (path.startsWith("admin")) {
            event.forwardTo("access-denied");
            return;
        }

        // 3. Block unauthorized users from doctor routes (only DOCTOR can access)
        if (path.startsWith("doctor") && !"DOCTOR".equalsIgnoreCase(role)) {
            event.forwardTo("access-denied");
            return;
        }

        // 4. Block unauthorized users from receptionist routes (only RECEPTIONIST can access)
        if (path.startsWith("receptionist") && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            event.forwardTo("access-denied");
            return;
        }

        // 5. Block unauthorized users from patient routes (PATIENT, DOCTOR, and RECEPTIONIST can access patient details)
        if (path.startsWith("patient")) {
            if (!"PATIENT".equalsIgnoreCase(role) && !"DOCTOR".equalsIgnoreCase(role) && !"RECEPTIONIST".equalsIgnoreCase(role)) {
                event.forwardTo("access-denied");
                return;
            }
        }
        // Dynamically update user details and notifications badge in header
        updateHeaderUserDetails();
    }

    private void updateHeaderUserDetails() {
        String email = null;
        try {
            org.springframework.security.core.Authentication auth = getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails springUser) {
                    email = springUser.getUsername();
                } else if (principal instanceof com.aruclinic.entity.User userEntity) {
                    email = userEntity.getEmail();
                } else if (principal instanceof String principalStr) {
                    email = principalStr;
                }
            }
        } catch (Exception e) {}

        if (email != null) {
            try {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    if (userName != null) {
                        userName.setText(user.getFirstName() + " " + user.getLastName());
                    }
                    if (userRole != null) {
                        userRole.setText(getUserRole());
                    }
                    if (userAvatar != null && user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                        userAvatar.setText(user.getFirstName().substring(0, 1).toUpperCase());
                    }
                    
                    if (notificationBadge != null) {
                        long unreadCount = notificationService.findByUserId(user.getId()).stream()
                            .filter(n -> !n.isRead())
                            .count();
                        notificationBadge.setText(String.valueOf(unreadCount));
                        notificationBadge.setVisible(unreadCount > 0);
                    }
                }
            } catch (Exception e) {
                // Ignore DB error during testing
            }
        }
    }

    private void updateNavigation() {
        sideNav.removeAll();

        String role = getUserRole();

        // Use navigation by route string in Vaadin 24
        switch (role) {
            case "SUPER_ADMIN":
            case "ADMIN":
                sideNav.addItem(new SideNavItem("Admin Dashboard", "admin"));
                sideNav.addItem(new SideNavItem("Users", "admin/users"));
                sideNav.addItem(new SideNavItem("Doctors", "admin/doctors"));
                sideNav.addItem(new SideNavItem("Receptionists", "admin/receptionists"));
                sideNav.addItem(new SideNavItem("Patients", "admin/patients"));
                sideNav.addItem(new SideNavItem("Appointments", "admin/appointments"));
                sideNav.addItem(new SideNavItem("Billing", "admin/billing"));
                sideNav.addItem(new SideNavItem("Reports", "admin/reports"));
                sideNav.addItem(new SideNavItem("Audit Logs", "admin/audit"));
                sideNav.addItem(new SideNavItem("Clinic Settings", "admin/settings"));
                break;

            case "DOCTOR":
                sideNav.addItem(new SideNavItem("Doctor Dashboard", "doctor"));
                sideNav.addItem(new SideNavItem("My Schedule", "doctor/schedule"));
                sideNav.addItem(new SideNavItem("Appointments", "doctor/appointments"));
                sideNav.addItem(new SideNavItem("Patients", "doctor/patients"));
                sideNav.addItem(new SideNavItem("Prescriptions", "doctor/prescriptions"));
                sideNav.addItem(new SideNavItem("Medical History", "doctor/medical-history"));
                sideNav.addItem(new SideNavItem("Profile", "doctor/profile"));
                break;

            case "RECEPTIONIST":
                sideNav.addItem(new SideNavItem("Receptionist Dashboard", "receptionist"));
                sideNav.addItem(new SideNavItem("Patient Registration", "receptionist/patient-registration"));
                sideNav.addItem(new SideNavItem("Appointments", "receptionist/appointments"));
                sideNav.addItem(new SideNavItem("Billing", "receptionist/billing"));
                sideNav.addItem(new SideNavItem("Reports", "receptionist/reports"));
                break;

            case "PATIENT":
            default:
                sideNav.addItem(new SideNavItem("Patient Dashboard", "patient"));
                sideNav.addItem(new SideNavItem("My Appointments", "patient/appointments"));
                sideNav.addItem(new SideNavItem("Prescriptions", "patient/prescriptions"));
                sideNav.addItem(new SideNavItem("Medical History", "patient/medical-history"));
                sideNav.addItem(new SideNavItem("Billing", "patient/billing"));
                sideNav.addItem(new SideNavItem("Profile", "patient/profile"));
                break;
        }

        sideNav.addItem(new SideNavItem("Notifications", "notifications"));
        sideNav.addItem(new SideNavItem("Settings", "settings"));
        sideNav.addItem(new SideNavItem("Change Password", "settings/change-password"));
        sideNav.addItem(new SideNavItem("Logout", "auth/logout"));
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        if (sidebarCollapsed) {
            sideBarContainer.removeClassName("expanded");
        } else {
            sideBarContainer.addClassName("expanded");
        }
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        updateNavigation();
        updateHeaderUserDetails();
    }
}