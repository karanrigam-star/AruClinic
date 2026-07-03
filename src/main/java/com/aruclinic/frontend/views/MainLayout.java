package com.aruclinic.frontend.views;

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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

@CssImport("./themes/aruclinic/styles.css")
public class MainLayout extends VerticalLayout implements RouterLayout {

    private static final long serialVersionUID = 1L;
    private final JwtTokenProvider jwtTokenProvider;
    private SideNav sideNav;
    private boolean sidebarCollapsed = false;
    private FlexLayout mainContent;

    public MainLayout(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
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
        menuToggle.getStyle()
            .set("background", "none")
            .set("border", "none")
            .set("color", "white")
            .set("cursor", "pointer")
            .set("padding", "var(--aruclinic-spacing-xs)");
        menuToggle.addClickListener(e -> toggleSidebar());
        menuToggle.setVisible(false);

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

        leftSection.add(menuToggle, logo, title);

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

        Span notificationBadge = new Span("3");
        notificationBadge.getStyle()
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
        notificationsWrapper.add(notificationsBtn, notificationBadge);

        Div userAvatar = new Div();
        userAvatar.getStyle()
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
        userAvatar.setText("U");

        Div userInfo = new Div();
        userInfo.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("margin-right", "var(--aruclinic-spacing-sm)");

        Span userName = new Span("User");
        userName.getStyle()
            .set("font-size", "var(--aruclinic-font-size-sm)")
            .set("font-weight", "600")
            .set("color", "white");

        Span userRole = new Span("Role");
        userRole.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs)")
            .set("color", "rgba(255, 255, 255, 0.8)");

        userInfo.add(userName, userRole);

        rightSection.add(notificationsWrapper, userInfo, userAvatar);

        header.add(leftSection, rightSection);
        return header;
    }

    private Component createMainContent() {
        mainContent = new FlexLayout();
        mainContent.setSizeFull();
        mainContent.getStyle()
            .set("display", "flex")
            .set("overflow", "hidden");

        sideNav = new SideNav();
        sideNav.getStyle()
            .set("width", "280px")
            .set("background", "var(--aruclinic-primary-dark)")
            .set("color", "white")
            .set("min-height", "calc(100vh - 64px)")
            .set("position", "fixed")
            .set("left", "0")
            .set("top", "64px")
            .set("z-index", "1000")
            .set("overflow-y", "auto");

        Div contentArea = new Div();
        contentArea.setSizeFull();
        contentArea.getStyle()
            .set("margin-left", "280px")
            .set("padding", "var(--aruclinic-spacing-xl)")
            .set("overflow-y", "auto");

        mainContent.add(sideNav, contentArea);
        return mainContent;
    }

    private void updateNavigation() {
        sideNav.removeAll();

        String role = "PATIENT";
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                role = ((User) principal).getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName())
                    .orElse("PATIENT");
            }
        } catch (Exception e) {
            // User not authenticated
        }

        // Use navigation by route string in Vaadin 24
        switch (role) {
            case "SUPER_ADMIN":
                sideNav.addItem(new SideNavItem("Admin Dashboard", "admin"));
                sideNav.addItem(new SideNavItem("Users", "admin/users"));
                sideNav.addItem(new SideNavItem("Roles", "admin/roles"));
                sideNav.addItem(new SideNavItem("Audit Logs", "admin/audit"));
                sideNav.addItem(new SideNavItem("Settings", "admin/settings"));
                break;

            case "DOCTOR":
                sideNav.addItem(new SideNavItem("Doctor Dashboard", "doctor"));
                sideNav.addItem(new SideNavItem("My Schedule", "doctor/schedule"));
                sideNav.addItem(new SideNavItem("Appointments", "doctor/appointments"));
                sideNav.addItem(new SideNavItem("Patients", "doctor/patients"));
                sideNav.addItem(new SideNavItem("Prescriptions", "doctor/prescriptions"));
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
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        updateNavigation();
    }
}