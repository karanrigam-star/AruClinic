package com.aruclinic.view.admin;

import com.aruclinic.service.AdminService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

@PageTitle("Clinic Settings | AruClinic")
@Route(value = "admin/settings", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminSettingsView extends VerticalLayout {

    private final AdminService adminService;

    private final TextField clinicName = new TextField("Clinic Name");
    private final TextField clinicEmail = new TextField("Contact Email");
    private final TextField clinicPhone = new TextField("Contact Phone");
    private final TextField clinicAddress = new TextField("Clinic Address");
    private final TextField consultFee = new TextField("Consultation Fee (₹)");
    
    private final Select<String> defaultTheme = new Select<>();
    
    private final TextField smtpHost = new TextField("SMTP Host");
    private final TextField smtpPort = new TextField("SMTP Port");
    private final TextField smsGateway = new TextField("SMS Gateway URL");

    public AdminSettingsView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-settings-view");

        add(createHeader(), createSettingsForm());
        loadSettings();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Clinic Settings & Configuration");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button saveBtn = new Button("Save Settings", new Icon(VaadinIcon.CHECK));
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> saveSettings());

        header.add(title, saveBtn);
        return header;
    }

    private Component createSettingsForm() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setPadding(false);

        // General settings
        Div generalSection = new Div();
        generalSection.getStyle().set("background", "white").set("border-radius", "8px").set("padding", "20px").set("width", "100%").set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)");
        H2 generalTitle = new H2("General Settings");
        generalTitle.getStyle().set("font-size", "18px").set("margin-top", "0");
        FormLayout generalForm = new FormLayout();
        
        defaultTheme.setLabel("System Theme");
        defaultTheme.setItems("Light Theme", "Dark Theme", "Fluent Medical Theme");
        defaultTheme.setValue("Light Theme");

        generalForm.add(clinicName, clinicEmail, clinicPhone, clinicAddress, consultFee, defaultTheme);
        generalSection.add(generalTitle, generalForm);

        // Integrations settings
        Div integrationsSection = new Div();
        integrationsSection.getStyle().set("background", "white").set("border-radius", "8px").set("padding", "20px").set("width", "100%").set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)");
        H2 integrationsTitle = new H2("Email & SMS Integrations");
        integrationsTitle.getStyle().set("font-size", "18px").set("margin-top", "0");
        FormLayout integrationsForm = new FormLayout();
        integrationsForm.add(smtpHost, smtpPort, smsGateway);
        integrationsSection.add(integrationsTitle, integrationsForm);

        container.add(generalSection, integrationsSection);
        return container;
    }

    private void loadSettings() {
        clinicName.setValue(adminService.getClinicSetting("clinic_name", "AruClinic Healthcare"));
        clinicEmail.setValue(adminService.getClinicSetting("clinic_email", "contact@aruclinic.com"));
        clinicPhone.setValue(adminService.getClinicSetting("clinic_phone", "+1 (555) 019-2834"));
        clinicAddress.setValue(adminService.getClinicSetting("clinic_address", "123 Fluent St, Health City"));
        consultFee.setValue(adminService.getClinicSetting("consultation_fee", "75.00"));
        defaultTheme.setValue(adminService.getClinicSetting("default_theme", "Light Theme"));
        smtpHost.setValue(adminService.getClinicSetting("smtp_host", "smtp.aruclinic.com"));
        smtpPort.setValue(adminService.getClinicSetting("smtp_port", "587"));
        smsGateway.setValue(adminService.getClinicSetting("sms_gateway_url", "https://api.sms-gateway.com/send"));
    }

    private void saveSettings() {
        adminService.saveClinicSetting("clinic_name", clinicName.getValue());
        adminService.saveClinicSetting("clinic_email", clinicEmail.getValue());
        adminService.saveClinicSetting("clinic_phone", clinicPhone.getValue());
        adminService.saveClinicSetting("clinic_address", clinicAddress.getValue());
        adminService.saveClinicSetting("consultation_fee", consultFee.getValue());
        adminService.saveClinicSetting("default_theme", defaultTheme.getValue());
        adminService.saveClinicSetting("smtp_host", smtpHost.getValue());
        adminService.saveClinicSetting("smtp_port", smtpPort.getValue());
        adminService.saveClinicSetting("sms_gateway_url", smsGateway.getValue());

        Notification.show("Clinic configurations saved successfully!", 2000, Notification.Position.TOP_CENTER);
    }
}
