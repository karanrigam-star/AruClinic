package com.aruclinic.view.dev;

import com.aruclinic.entity.OtpVerification;
import com.aruclinic.repository.OtpVerificationRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Developer OTP Console for viewing and managing OTP records.
 */
@PageTitle("Developer OTP Console | AruClinic")
@Route("dev/otp")
@CssImport("./themes/aruclinic/common.css")
public class DeveloperOtpView extends VerticalLayout {

    private static final long serialVersionUID = 4451911800664527310L;
	private final OtpVerificationRepository otpVerificationRepository;
    private final Grid<OtpVerification> grid = new Grid<>(OtpVerification.class, false);
    private final TextField searchField = new TextField();

    public DeveloperOtpView(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        add(createConsoleContent());
        refreshData();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-table");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.setHeight("auto");
        grid.setWidthFull();

        // Add columns
        grid.addColumn(OtpVerification::getEmail)
            .setHeader("Email")
            .setWidth("250px")
            .setFlexGrow(0);

        grid.addColumn(OtpVerification::getMobileNumber)
            .setHeader("Mobile Number")
            .setWidth("150px")
            .setFlexGrow(0);

        grid.addColumn(OtpVerification::getOtpCode)
            .setHeader("OTP Code")
            .setWidth("120px")
            .setFlexGrow(0);

        grid.addColumn(OtpVerification::getCreatedAt)
            .setHeader("Created Time")
            .setWidth("200px")
            .setFlexGrow(0);

        grid.addColumn(OtpVerification::getExpiresAt)
            .setHeader("Expiry Time")
            .setWidth("200px")
            .setFlexGrow(0);

        grid.addColumn(otp -> {
            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                return "Expired";
            } else {
                return "Valid";
            }
        }).setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0);

        grid.addColumn(this::createActionButtons)
            .setHeader("Actions")
            .setWidth("150px")
            .setFlexGrow(0);
    }

    private Component createActionButtons(OtpVerification otp) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button resendBtn = new Button(new Icon(VaadinIcon.REFRESH));
        resendBtn.addClassName("aruclinic-btn");
        resendBtn.addClassName("aruclinic-btn-sm");
        resendBtn.addClassName("aruclinic-btn-primary");
        resendBtn.addClickListener(e -> resendOtp(otp));

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addClassName("aruclinic-btn");
        deleteBtn.addClassName("aruclinic-btn-sm");
        deleteBtn.addClassName("aruclinic-btn-danger");
        deleteBtn.addClickListener(e -> deleteOtp(otp));

        actions.add(resendBtn, deleteBtn);
        return actions;
    }

    private Component createConsoleContent() {
        Div content = new Div();
        content.addClassName("aruclinic-view");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-view-header");

        H1 title = new H1("Developer OTP Console");
        title.addClassName("aruclinic-view-title");

        header.add(title);

        // Toolbar
        Div toolbar = new Div();
        toolbar.addClassName("aruclinic-toolbar");

        searchField.setPlaceholder("Search by email or mobile...");
        searchField.setWidth("300px");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshData());

        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClassName("aruclinic-btn");
        refreshButton.addClassName("aruclinic-btn-primary");
        refreshButton.addClickListener(event -> refreshData());

        Button generateOtpButton = new Button("Generate OTP", new Icon(VaadinIcon.PLUS));
        generateOtpButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        generateOtpButton.addClassName("aruclinic-btn");
        generateOtpButton.addClassName("aruclinic-btn-success");
        generateOtpButton.addClickListener(e -> generateNewOtp());

        toolbar.add(searchField, refreshButton, generateOtpButton);

        // Grid
        content.add(header, toolbar, grid);
        return content;
    }

    private void refreshData() {
        String searchTerm = searchField.getValue().trim().toLowerCase();
        List<OtpVerification> otpList;

        if (searchTerm.isEmpty()) {
            otpList = otpVerificationRepository.findAllByOrderByCreatedAtDesc();
        } else {
            // Search by email or mobile
            otpList = otpVerificationRepository.findByEmailContainingOrMobileNumberContaining(
                searchTerm, searchTerm
            );
        }

        grid.setItems(otpList);
    }

    private void resendOtp(OtpVerification otp) {
        // In a real app, this would call the OTP service to resend the OTP
        com.vaadin.flow.component.notification.Notification.show(
            "OTP resent to: " + otp.getEmail() + " / " + otp.getMobileNumber(),
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }

    private void deleteOtp(OtpVerification otp) {
        try {
            otpVerificationRepository.delete(otp);
            refreshData();

            com.vaadin.flow.component.notification.Notification.show(
                "OTP record deleted successfully",
                2000,
                com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
            );
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show(
                "Failed to delete OTP record: " + e.getMessage(),
                3000,
                com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
            );
        }
    }

    private void generateNewOtp() {
        // In a real app, this would open a dialog to generate a new OTP
        com.vaadin.flow.component.notification.Notification.show(
            "Generate OTP functionality would open a dialog here",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }
}
