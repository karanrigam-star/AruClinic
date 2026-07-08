package com.aruclinic.view.settings;

import com.aruclinic.view.MainLayout;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Settings view for user preferences and account settings.
 */
@PageTitle("Settings | AruClinic")
@Route(value = "settings", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createSettingsContent());
    }

    private Component createSettingsContent() {
        Div content = new Div();
        content.addClassName("aruclinic-view");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-view-header");

        H1 title = new H1("Settings");
        title.addClassName("aruclinic-view-title");

        header.add(title);

        // Settings sections
        content.add(header);
        content.add(createProfileSettings());
        content.add(createNotificationSettings());
        content.add(createPrivacySettings());
        content.add(createSecuritySettings());

        return content;
    }

    private Component createProfileSettings() {
        Div section = new Div();
        section.addClassName("aruclinic-settings-section");

        H2 sectionTitle = new H2("Profile Settings");
        sectionTitle.addClassName("aruclinic-settings-section-title");

        Div sectionContent = new Div();
        sectionContent.addClassName("aruclinic-settings-section-content");

        // Settings items
        sectionContent.add(createSettingItem(
            "Edit Profile",
            "Update your personal information",
            "settings/profile",
            VaadinIcon.EDIT
        ));

        sectionContent.add(createSettingItem(
            "Change Password",
            "Update your account password",
            "settings/change-password",
            VaadinIcon.KEY
        ));

        sectionContent.add(createSettingItem(
            "Profile Picture",
            "Upload or change your profile picture",
            "settings/profile-picture",
            VaadinIcon.CAMERA
        ));

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createNotificationSettings() {
        Div section = new Div();
        section.addClassName("aruclinic-settings-section");

        H2 sectionTitle = new H2("Notification Settings");
        sectionTitle.addClassName("aruclinic-settings-section-title");

        Div sectionContent = new Div();
        sectionContent.addClassName("aruclinic-settings-section-content");

        // Settings items
        sectionContent.add(createSettingItem(
            "Email Notifications",
            "Configure email notification preferences",
            "settings/notifications/email",
            VaadinIcon.MAILBOX
        ));

        sectionContent.add(createSettingItem(
            "SMS Notifications",
            "Configure SMS notification preferences",
            "settings/notifications/sms",
            VaadinIcon.PHONE
        ));

        sectionContent.add(createSettingItem(
            "Push Notifications",
            "Configure browser push notifications",
            "settings/notifications/push",
            VaadinIcon.BELL
        ));

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createPrivacySettings() {
        Div section = new Div();
        section.addClassName("aruclinic-settings-section");

        H2 sectionTitle = new H2("Privacy Settings");
        sectionTitle.addClassName("aruclinic-settings-section-title");

        Div sectionContent = new Div();
        sectionContent.addClassName("aruclinic-settings-section-content");

        // Settings items
        sectionContent.add(createSettingItem(
            "Data Sharing",
            "Control how your data is shared",
            "settings/privacy/data-sharing",
            VaadinIcon.SHARE
        ));

        sectionContent.add(createSettingItem(
            "Activity History",
            "View and manage your activity history",
            "settings/privacy/activity",
            VaadinIcon.CLOCK
        ));

        sectionContent.add(createSettingItem(
            "Delete Account",
            "Permanently delete your account",
            "settings/privacy/delete",
            VaadinIcon.TRASH
        ));

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createSecuritySettings() {
        Div section = new Div();
        section.addClassName("aruclinic-settings-section");

        H2 sectionTitle = new H2("Security Settings");
        sectionTitle.addClassName("aruclinic-settings-section-title");

        Div sectionContent = new Div();
        sectionContent.addClassName("aruclinic-settings-section-content");

        // Settings items
        sectionContent.add(createSettingItem(
            "Two-Factor Authentication",
            "Enable or disable two-factor authentication",
            "settings/security/2fa",
            VaadinIcon.LOCK
        ));

        sectionContent.add(createSettingItem(
            "Login History",
            "View your login history and active sessions",
            "settings/security/login-history",
            VaadinIcon.LIST
        ));

        sectionContent.add(createSettingItem(
            "Security Questions",
            "Update your security questions",
            "settings/security/questions",
            VaadinIcon.QUESTION
        ));

        section.add(sectionTitle, sectionContent);
        return section;
    }

    private Component createSettingItem(String title, String description, String route, VaadinIcon icon) {
        Div item = new Div();
        item.addClassName("aruclinic-setting-item");
        item.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-setting-icon");
        iconDiv.add(new Icon(icon));

        Div content = new Div();
        content.addClassName("aruclinic-setting-content");

        Div titleDiv = new Div();
        titleDiv.addClassName("aruclinic-setting-title");
        titleDiv.setText(title);

        Div descDiv = new Div();
        descDiv.addClassName("aruclinic-setting-description");
        descDiv.setText(description);

        content.add(titleDiv, descDiv);

        Div arrow = new Div();
        arrow.addClassName("aruclinic-setting-arrow");
        arrow.add(new Icon(VaadinIcon.ARROW_RIGHT));

        item.add(iconDiv, content, arrow);
        return item;
    }
}
