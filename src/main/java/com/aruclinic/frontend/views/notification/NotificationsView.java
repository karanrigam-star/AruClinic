package com.aruclinic.frontend.views.notification;

import com.aruclinic.dto.NotificationDto;
import com.aruclinic.service.NotificationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Notifications view for displaying user notifications.
 */
@PageTitle("Notifications | AruClinic")
@Route("notifications")
@CssImport("./themes/aruclinic/common.css")
public class NotificationsView extends VerticalLayout {

    private final NotificationService notificationService;

    public NotificationsView(NotificationService notificationService) {
        this.notificationService = notificationService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createNotificationsContent());
    }

    private Component createNotificationsContent() {
        Div content = new Div();
        content.addClassName("aruclinic-view");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-view-header");

        H1 title = new H1("Notifications");
        title.addClassName("aruclinic-view-title");

        Button markAllReadBtn = new Button("Mark All as Read", new Icon(VaadinIcon.CHECK));
        markAllReadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        markAllReadBtn.addClassName("aruclinic-btn");
        markAllReadBtn.addClassName("aruclinic-btn-primary");
        markAllReadBtn.addClickListener(e -> markAllAsRead());

        header.add(title, markAllReadBtn);

        // Tabs
        Div tabs = new Div();
        tabs.addClassName("aruclinic-tabs");

        Button allBtn = new Button("All");
        allBtn.addClassName("aruclinic-tab");
        allBtn.addClassName("active");
        allBtn.addClickListener(e -> switchTab("all"));

        Button unreadBtn = new Button("Unread (3)");
        unreadBtn.addClassName("aruclinic-tab");
        unreadBtn.addClickListener(e -> switchTab("unread"));

        Button importantBtn = new Button("Important");
        importantBtn.addClassName("aruclinic-tab");
        importantBtn.addClickListener(e -> switchTab("important"));

        tabs.add(allBtn, unreadBtn, importantBtn);

        // Notifications list
        Div notificationsList = new Div();
        notificationsList.addClassName("aruclinic-notifications-list");

        // Sample notifications
        notificationsList.add(createNotificationItem(
            "New Appointment Booked",
            "Your appointment with Dr. Smith has been confirmed for June 5, 2025 at 09:00 AM.",
            LocalDateTime.now().minusHours(2),
            false,
            "primary"
        ));

        notificationsList.add(createNotificationItem(
            "Prescription Ready",
            "Your prescription from Dr. Johnson is now ready for pickup at the pharmacy.",
            LocalDateTime.now().minusHours(5),
            false,
            "success"
        ));

        notificationsList.add(createNotificationItem(
            "Payment Received",
            "We have received your payment of $150.00 for invoice #INV-001.",
            LocalDateTime.now().minusHours(12),
            false,
            "success"
        ));

        notificationsList.add(createNotificationItem(
            "Appointment Reminder",
            "Reminder: Your appointment with Dr. Smith is tomorrow at 09:00 AM.",
            LocalDateTime.now().minusDays(1),
            true,
            "warning"
        ));

        notificationsList.add(createNotificationItem(
            "New Message",
            "You have a new message from Dr. Smith regarding your test results.",
            LocalDateTime.now().minusDays(2),
            true,
            "primary"
        ));

        notificationsList.add(createNotificationItem(
            "Billing Statement",
            "Your monthly billing statement is now available for review.",
            LocalDateTime.now().minusDays(3),
            true,
            "info"
        ));

        content.add(header, tabs, notificationsList);
        return content;
    }

    private Component createNotificationItem(String title, String message, LocalDateTime timestamp, boolean isUnread, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-notification-item");
        if (isUnread) {
            item.addClassName("unread");
        }

        Div header = new Div();
        header.addClassName("aruclinic-notification-header");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-notification-icon");
        iconDiv.addClassName(type);
        iconDiv.add(new Icon(getIconForType(type)));

        Div titleDiv = new Div();
        titleDiv.addClassName("aruclinic-notification-title");
        titleDiv.setText(title);

        Div timeDiv = new Div();
        timeDiv.addClassName("aruclinic-notification-time");
        timeDiv.setText(formatTimestamp(timestamp));

        header.add(iconDiv, titleDiv, timeDiv);

        Div messageDiv = new Div();
        messageDiv.addClassName("aruclinic-notification-message");
        messageDiv.setText(message);

        Div actions = new Div();
        actions.addClassName("aruclinic-notification-actions");

        Button markReadBtn = new Button("Mark as Read");
        markReadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        markReadBtn.addClassName("aruclinic-btn");
        markReadBtn.addClassName("aruclinic-btn-outline");
        markReadBtn.addClickListener(e -> markAsRead(item));
        markReadBtn.setVisible(isUnread);

        Button viewBtn = new Button("View");
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        viewBtn.addClassName("aruclinic-btn");
        viewBtn.addClassName("aruclinic-btn-primary");
        viewBtn.addClickListener(e -> viewNotification(title));

        actions.add(markReadBtn, viewBtn);

        item.add(header, messageDiv, actions);
        return item;
    }

    private VaadinIcon getIconForType(String type) {
        switch (type) {
            case "success":
                return VaadinIcon.CHECK;
            case "warning":
                return VaadinIcon.WARNING;
            case "danger":
                return VaadinIcon.EXCLAMATION;
            case "info":
                return VaadinIcon.INFO;
            default:
                return VaadinIcon.BELL;
        }
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return timestamp.format(formatter);
    }

    private void markAsRead(Component notificationItem) {
        notificationItem.getElement().getClassList().remove("unread");
        notificationItem.getElement().getClassList().add("read");

        // In a real app, this would call the service to mark as read
        com.vaadin.flow.component.notification.Notification.show(
            "Notification marked as read",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
        );
    }

    private void markAllAsRead() {
        // In a real app, this would call the service to mark all as read
        com.vaadin.flow.component.notification.Notification.show(
            "All notifications marked as read",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
        );
    }

    private void switchTab(String tab) {
        // Update tab styles
        getElement().executeJs(
            "document.querySelectorAll('.aruclinic-tab').forEach(tab => tab.classList.remove('active'))"
        );
        getElement().executeJs(
            "document.querySelector('.aruclinic-tab:nth-child(' + (tab === 'all' ? 1 : tab === 'unread' ? 2 : 3) + ')').classList.add('active')"
        );

        // In a real app, this would filter the notifications list
    }

    private void viewNotification(String title) {
        com.vaadin.flow.component.notification.Notification.show(
            "Viewing: " + title,
            2000,
            com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
        );
    }
}
