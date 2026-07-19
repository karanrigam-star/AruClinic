package com.aruclinic.view.notification;

import com.aruclinic.view.MainLayout;
import com.aruclinic.service.NotificationService;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.dto.PrescriptionItemDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.aruclinic.util.PdfHelper;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Notifications view for displaying user notifications in real-time.
 */
@PageTitle("Notifications | AruClinic")
@Route(value = "notifications", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class NotificationsView extends VerticalLayout implements BeforeEnterObserver {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final com.aruclinic.service.PrescriptionService prescriptionService;
    private final com.aruclinic.service.AdminService adminService;

    private com.aruclinic.entity.User currentUser = null;
    private List<com.aruclinic.entity.Notification> dbNotifications = new ArrayList<>();
    private Div notificationsList;
    private String activeTab = "all";
    private Button unreadBtn;
    private Button clearAllBtn;

    public NotificationsView(NotificationService notificationService, 
                             UserRepository userRepository,
                             com.aruclinic.service.PrescriptionService prescriptionService,
                             com.aruclinic.service.AdminService adminService) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.prescriptionService = prescriptionService;
        this.adminService = adminService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolveCurrentUser();
        removeAll();
        add(createNotificationsContent());
        refreshNotifications();
    }

    private void resolveCurrentUser() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    auth = (org.springframework.security.core.Authentication) 
                            session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                }
            }

            if (auth != null) {
                org.springframework.security.core.context.SecurityContext context = 
                        org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }

            String email = null;
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                    email = springUser.getUsername();
                } else if (principal instanceof String principalStr) {
                    email = principalStr;
                }
            }

            if (email != null) {
                currentUser = userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            // Ignore
        }
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

        clearAllBtn = new Button("Clear All", new Icon(VaadinIcon.TRASH));
        clearAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearAllBtn.addClassName("aruclinic-btn");
        clearAllBtn.addClassName("aruclinic-btn-danger");
        clearAllBtn.addClickListener(e -> clearAllNotifications());

        Button backBtn = new Button("Back", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClassName("aruclinic-btn");
        backBtn.addClassName("aruclinic-btn-secondary");
        backBtn.addClickListener(e -> {
            String landingRoute = "patient";
            if (currentUser != null && currentUser.getRoles() != null) {
                boolean isDoctor = currentUser.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("DOCTOR"));
                boolean isReceptionist = currentUser.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("RECEPTIONIST"));
                boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("ADMIN"));
                
                if (isAdmin) {
                    landingRoute = "admin";
                } else if (isDoctor) {
                    landingRoute = "doctor";
                } else if (isReceptionist) {
                    landingRoute = "receptionist";
                }
            }
            final String targetRoute = landingRoute;
            getUI().ifPresent(ui -> ui.navigate(targetRoute));
        });

        Div headerActions = new Div();
        headerActions.addClassName("aruclinic-section-actions");
        headerActions.add(markAllReadBtn, clearAllBtn, backBtn);

        header.add(title, headerActions);

        // Tabs
        Div tabs = new Div();
        tabs.addClassName("aruclinic-tabs");

        Button allBtn = new Button("All");
        allBtn.addClassName("aruclinic-tab");
        allBtn.addClassName("active");
        allBtn.addClickListener(e -> switchTab("all"));

        unreadBtn = new Button("Unread (0)");
        unreadBtn.addClassName("aruclinic-tab");
        unreadBtn.addClickListener(e -> switchTab("unread"));

        Button importantBtn = new Button("Important");
        importantBtn.addClassName("aruclinic-tab");
        importantBtn.addClickListener(e -> switchTab("important"));

        tabs.add(allBtn, unreadBtn, importantBtn);

        // Notifications list
        notificationsList = new Div();
        notificationsList.addClassName("aruclinic-notifications-list");

        content.add(header, tabs, notificationsList);
        return content;
    }

    private Component createNotificationItem(String title, String message, LocalDateTime timestamp, boolean isUnread, String type, com.aruclinic.entity.Notification n) {
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
        markReadBtn.addClickListener(e -> markAsRead(n));
        markReadBtn.setVisible(isUnread);

        Button viewBtn = new Button("View");
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        viewBtn.addClassName("aruclinic-btn");
        viewBtn.addClassName("aruclinic-btn-primary");
        viewBtn.addClickListener(e -> {
            if (isUnread) {
                markAsRead(n);
            }
            viewNotification(n, type);
        });

        Button clearBtn = new Button(new Icon(VaadinIcon.TRASH));
        clearBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        clearBtn.setTooltipText("Clear Notification");
        clearBtn.addClickListener(e -> deleteNotification(n));

        actions.add(markReadBtn, viewBtn, clearBtn);

        if (title != null && title.startsWith("Account Enable Request: User #")) {
            try {
                String userIdStr = title.substring(title.indexOf("User #") + 6).trim();
                Long targetUserId = Long.parseLong(userIdStr);

                // Get current user roles to check if they are ADMIN or RECEPTIONIST
                final boolean finalIsAdmin = currentUser != null && currentUser.getRoles() != null &&
                    currentUser.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("ADMIN"));
                final boolean finalIsReceptionist = currentUser != null && currentUser.getRoles() != null &&
                    currentUser.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("RECEPTIONIST"));

                if (finalIsAdmin || finalIsReceptionist) {
                    boolean currentlyEnabled = adminService.isUserEnabled(targetUserId);

                    if (!currentlyEnabled) {
                        Button approveBtn = new Button("Approve & Enable", new Icon(VaadinIcon.CHECK_CIRCLE));
                        approveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                        approveBtn.addClickListener(e -> {
                            try {
                                // 1. Enable user in database
                                adminService.toggleUserStatus(targetUserId, true);

                                // 2. Clear settings keys
                                adminService.saveClinicSetting("enable_request_status_" + targetUserId, null);
                                adminService.saveClinicSetting("enable_request_admin_approved_" + targetUserId, null);
                                adminService.saveClinicSetting("enable_request_receptionist_approved_" + targetUserId, null);

                                // 3. Delete this notification so it disappears immediately
                                notificationService.deleteNotificationById(n.getId());

                                // 4. Send notification to the newly enabled user
                                com.aruclinic.entity.User enabledUser = userRepository.findById(targetUserId).orElse(null);
                                if (enabledUser != null) {
                                    com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                                    notif.setUser(enabledUser);
                                    notif.setTitle("Account Enabled");
                                    notif.setMessage("Your request to enable your account has been approved. You can now log in.");
                                    notif.setRead(false);
                                    notif.setCreatedAt(LocalDateTime.now());
                                    notificationService.save(notif);
                                }

                                com.vaadin.flow.component.notification.Notification.show(
                                    "Account approved and enabled successfully!", 
                                    3000, 
                                    com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
                                );

                                refreshNotifications();
                            } catch (Exception ex) {
                                com.vaadin.flow.component.notification.Notification.show("Error approving: " + ex.getMessage(), 3000, com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER);
                            }
                        });

                        actions.add(approveBtn);
                    } else {
                        Span approvedSpan = new Span("Account Already Enabled");
                        approvedSpan.getStyle().set("color", "var(--aruclinic-success, #10b981)").set("font-size", "12px").set("font-weight", "600");
                        actions.add(approvedSpan);
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
        }

        if (title != null && title.contains("Invoice")) {
            String invId = title.contains("INV-") ? title.substring(title.indexOf("INV-")) : "INV-TEMP";
            Anchor pdfAnchor = new Anchor(new StreamResource("Invoice.pdf", () -> {
                String pName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Patient";
                String amt = message != null && message.contains("₹") ? message.substring(message.indexOf("₹")) : "₹0.00";
                return PdfHelper.generateInvoicePdf(invId, pName, "AruClinic", java.time.LocalDate.now().toString(), amt);
            }), "");
            pdfAnchor.getElement().setAttribute("download", true);
            Button pdfBtn = new Button("PDF", new Icon(VaadinIcon.DOWNLOAD));
            pdfBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            pdfAnchor.add(pdfBtn);
            actions.add(pdfAnchor);
        }

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

    private void refreshNotifications() {
        if (notificationsList == null) return;
        notificationsList.removeAll();

        if (currentUser != null) {
            dbNotifications = notificationService.findByUserId(currentUser.getId());
        } else {
            dbNotifications = new ArrayList<>();
        }

        // Update unread count label on tab button
        long unreadCount = dbNotifications.stream().filter(n -> !n.isRead()).count();
        if (unreadBtn != null) {
            unreadBtn.setText("Unread (" + unreadCount + ")");
        }

        // Dynamically toggle visibility of Clear All button based on notification presence
        if (clearAllBtn != null) {
            clearAllBtn.setVisible(dbNotifications != null && !dbNotifications.isEmpty());
        }

        // Always display default notice at the top for patients
        boolean isPatient = false;
        if (currentUser != null && currentUser.getRoles() != null) {
            isPatient = currentUser.getRoles().stream()
                    .anyMatch(r -> r.getName() != null && r.getName().contains("PATIENT"));
        }

        if (isPatient) {
            Div defaultNotice = new Div();
            defaultNotice.addClassName("aruclinic-notification-item");
            defaultNotice.addClassName("unread");
            defaultNotice.getStyle()
                .set("border-left", "4px solid var(--aruclinic-primary, #1e40af)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("margin-bottom", "var(--aruclinic-spacing-md)")
                .set("padding", "var(--aruclinic-spacing-md)")
                .set("border-radius", "var(--aruclinic-radius-md)");

            HorizontalLayout header = new HorizontalLayout();
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
            infoIcon.getStyle().set("color", "var(--aruclinic-primary, #1e40af)");
            
            Span noticeTitle = new Span("Important Notice");
            noticeTitle.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-text-primary)");
            header.add(infoIcon, noticeTitle);

            Div message = new Div(new Span("Save your important documents. Keep your prescriptions and bills downloaded for future reference."));
            message.getStyle().set("margin-top", "8px").set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");

            defaultNotice.add(header, message);
            notificationsList.add(defaultNotice);
        }

        List<com.aruclinic.entity.Notification> filtered = dbNotifications.stream()
            .filter(n -> {
                if ("unread".equals(activeTab)) {
                    return !n.isRead();
                }
                if ("important".equals(activeTab)) {
                    return n.getTitle() != null && (n.getTitle().contains("Cancel") || n.getTitle().contains("Urgent") || n.getTitle().contains("New"));
                }
                return true;
            })
            .sorted((n1, n2) -> {
                LocalDateTime t1 = n1.getCreatedAt() != null ? n1.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime t2 = n2.getCreatedAt() != null ? n2.getCreatedAt() : LocalDateTime.MIN;
                return t2.compareTo(t1);
            })
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Div emptyMessage = new Div();
            emptyMessage.setText("No notifications found.");
            emptyMessage.getStyle().set("padding", "var(--aruclinic-spacing-md)")
                    .set("color", "var(--aruclinic-text-secondary)")
                    .set("text-align", "center")
                    .set("font-style", "italic");
            notificationsList.add(emptyMessage);
        } else {
            for (com.aruclinic.entity.Notification n : filtered) {
                String type = "info";
                if (n.getTitle() != null) {
                    if (n.getTitle().contains("Cancel") || n.getTitle().contains("Failed")) {
                        type = "danger";
                    } else if (n.getTitle().contains("Booked") || n.getTitle().contains("Confirmed") || n.getTitle().contains("Scheduled")) {
                        type = "success";
                    } else if (n.getTitle().contains("Reminder") || n.getTitle().contains("Warning")) {
                        type = "warning";
                    } else if (n.getTitle().contains("Message")) {
                        type = "primary";
                    }
                }
                notificationsList.add(createNotificationItem(
                    n.getTitle(),
                    n.getMessage(),
                    n.getCreatedAt() != null ? n.getCreatedAt() : LocalDateTime.now(),
                    !n.isRead(),
                    type,
                    n
                ));
            }
        }
    }

    private void markAsRead(com.aruclinic.entity.Notification n) {
        notificationService.markAsRead(n.getId());
        refreshNotifications();
        com.vaadin.flow.component.notification.Notification.show(
            "Notification marked as read",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
        );
    }

    private void markAllAsRead() {
        if (currentUser != null && dbNotifications != null) {
            boolean updated = false;
            for (com.aruclinic.entity.Notification n : dbNotifications) {
                if (!n.isRead()) {
                    notificationService.markAsRead(n.getId());
                    updated = true;
                }
            }
            if (updated) {
                refreshNotifications();
            }
            com.vaadin.flow.component.notification.Notification.show(
                "All notifications marked as read",
                2000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
            );
        }
    }

    private void switchTab(String tab) {
        activeTab = tab;
        // Update tab styles
        getElement().executeJs(
            "document.querySelectorAll('.aruclinic-tab').forEach(tab => tab.classList.remove('active'))"
        );
        int index = "all".equals(tab) ? 1 : "unread".equals(tab) ? 2 : 3;
        getElement().executeJs(
            "document.querySelector('.aruclinic-tab:nth-child(' + " + index + " + ')').classList.add('active')"
        );

        refreshNotifications();
    }

    private void viewNotification(com.aruclinic.entity.Notification n, String type) {
        if (n == null) return;
        String title = n.getTitle();
        
        if (title != null && title.contains("Prescription Deleted: PRESC-")) {
            showDeletedPrescriptionDialog(title);
            return;
        }

        // Beautiful, responsive notification popup compatible with all devices (mobile & desktop)
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title != null ? title : "Notification Details");
        dialog.setWidth("450px");
        dialog.setMaxWidth("90vw"); // Responsive sizing for mobile views

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);

        // Circular background container for the icon
        Div iconCircle = new Div();
        iconCircle.getStyle()
            .set("width", "60px")
            .set("height", "60px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-bottom", "10px");

        String displayType = type != null ? type : "info";
        Icon icon = new Icon(getIconForType(displayType));
        icon.setSize("28px");
        
        if ("danger".equalsIgnoreCase(displayType) || "error".equalsIgnoreCase(displayType) || (title != null && (title.contains("Cancel") || title.contains("Delete") || title.contains("Error") || title.contains("Failed")))) {
            iconCircle.getStyle().set("background", "#fee2e2");
            icon.setColor("#ef4444");
        } else if ("success".equalsIgnoreCase(displayType) || (title != null && (title.contains("Paid") || title.contains("Enable") || title.contains("Success") || title.contains("Booked") || title.contains("Scheduled") || title.contains("Updated")))) {
            iconCircle.getStyle().set("background", "#dcfce7");
            icon.setColor("#22c55e");
        } else if ("warning".equalsIgnoreCase(displayType) || (title != null && (title.contains("Reschedule") || title.contains("Reassign") || title.contains("Assigned")))) {
            iconCircle.getStyle().set("background", "#fef3c7");
            icon.setColor("#f59e0b");
        } else { // info / primary
            iconCircle.getStyle().set("background", "#e0f2fe");
            icon.setColor("#0ea5e9");
        }
        iconCircle.add(icon);
        content.add(iconCircle);

        // Formatted timestamp
        Span timeSpan = new Span(formatTimestamp(n.getCreatedAt()));
        timeSpan.getStyle()
            .set("font-size", "var(--aruclinic-font-size-xs, 0.75rem)")
            .set("color", "var(--aruclinic-text-secondary, #64748b)")
            .set("margin-bottom", "15px");
        content.add(timeSpan);

        // Detailed message display block
        Div messageBlock = new Div();
        messageBlock.getStyle()
            .set("background", "var(--aruclinic-bg-light, #f8fafc)")
            .set("border", "1px solid var(--aruclinic-border-color, #e2e8f0)")
            .set("border-radius", "8px")
            .set("padding", "var(--aruclinic-spacing-md, 16px)")
            .set("width", "100%")
            .set("font-size", "var(--aruclinic-font-size-md, 0.95rem)")
            .set("color", "var(--aruclinic-text-primary, #0f172a)")
            .set("line-height", "1.5");
        messageBlock.setText(n.getMessage());
        content.add(messageBlock);

        dialog.add(content);

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.getStyle().set("width", "100%");
        dialog.getFooter().add(closeBtn);

        dialog.open();
    }

    private void showDeletedPrescriptionDialog(String title) {
        try {
            String prescIdStr = title.substring(title.indexOf("PRESC-")).trim();
            Long id = Long.parseLong(prescIdStr.substring(6));
            PrescriptionDto prescription = prescriptionService.getPrescriptionById(id);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Prescription Details (Backup)");
            dialog.setWidth("500px");

            VerticalLayout content = new VerticalLayout();
            content.setSpacing(true);
            content.setPadding(false);

            Div detailsBlock = new Div();
            detailsBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px").set("width", "100%");

            detailsBlock.add(createDetailItem("Prescription ID", prescription.getPrescriptionId()));
            detailsBlock.add(createDetailItem("Date", prescription.getPrescriptionDate() != null ? prescription.getPrescriptionDate().toString() : "N/A"));
            detailsBlock.add(createDetailItem("Doctor Name", prescription.getDoctorName() != null ? "Dr. " + prescription.getDoctorName() : "Unknown"));
            detailsBlock.add(createDetailItem("Symptoms", prescription.getSymptoms() != null ? prescription.getSymptoms() : "N/A"));
            detailsBlock.add(createDetailItem("Diagnosis", prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "N/A"));
            detailsBlock.add(createDetailItem("Advice", prescription.getAdvice() != null ? prescription.getAdvice() : "N/A"));

            Div medsTitle = new Div(new Span("Prescribed Medicines:"));
            medsTitle.getStyle().set("font-weight", "600").set("margin-top", "12px").set("color", "var(--aruclinic-text-primary)");
            detailsBlock.add(medsTitle);

            if (prescription.getItems() != null && !prescription.getItems().isEmpty()) {
                for (PrescriptionItemDto med : prescription.getItems()) {
                    String medDetails = med.getMedicineName() + " - " + med.getDosage() + " (" + med.getDuration() + " days)";
                    detailsBlock.add(createDetailItem("• Medicine", medDetails));
                }
            } else {
                detailsBlock.add(createDetailItem("• Medicines", "None prescribed"));
            }

            content.add(detailsBlock);
            dialog.add(content);

            Button closeButton = new Button("Close", e -> dialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Anchor downloadAnchor = new Anchor(new StreamResource("Prescription_" + prescription.getPrescriptionId() + ".pdf", () -> {
                String medsList = "";
                if (prescription.getItems() != null) {
                    medsList = prescription.getItems().stream()
                        .map(it -> it.getMedicineName() + " (" + it.getDosage() + ")")
                        .collect(Collectors.joining(", "));
                }
                java.io.ByteArrayInputStream pdfStream = PdfHelper.generatePrescriptionPdf(
                    prescription.getPrescriptionId(),
                    prescription.getPatientName() != null ? prescription.getPatientName() : 
                        (currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Patient"),
                    prescription.getDoctorName() != null ? prescription.getDoctorName() : "Doctor",
                    prescription.getPrescriptionDate() != null ? prescription.getPrescriptionDate().toString() : "",
                    prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "Routine Checkup",
                    medsList
                );

                try {
                    prescriptionService.deletePrescriptionReal(prescription.getId());
                    notificationService.deletePrescriptionNotification(currentUser.getId(), prescription.getPrescriptionId());
                    getUI().ifPresent(ui -> ui.access(() -> {
                        dialog.close();
                        refreshNotifications();
                    }));
                } catch (Exception ex) {
                    // Ignore
                }

                return pdfStream;
            }), "");
            downloadAnchor.getElement().setAttribute("download", true);

            Button downloadBtn = new Button("Download PDF", new Icon(VaadinIcon.DOWNLOAD));
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            downloadAnchor.add(downloadBtn);

            dialog.getFooter().add(closeButton, downloadAnchor);
            dialog.open();

        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show(
                "Error loading backup prescription details.",
                3000,
                com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
            );
        }
    }

    private Component createDetailItem(String label, String value) {
        Div row = new Div();
        row.getStyle().set("display", "flex").set("justify-content", "space-between").set("border-bottom", "1px solid var(--lumo-contrast-10pct)").set("padding", "6px 0");
        
        Span lbl = new Span(label);
        lbl.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        
        Span val = new Span(value);
        val.getStyle().set("color", "var(--aruclinic-text-primary)").set("font-weight", "500").set("font-size", "14px");
        
        row.add(lbl, val);
        return row;
    }

    private void clearAllNotifications() {
        if (currentUser != null) {
            notificationService.clearNotificationsByUserId(currentUser.getId());
            refreshNotifications();
            com.vaadin.flow.component.notification.Notification.show(
                "All notifications cleared",
                2000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
            );
        }
    }

    private void deleteNotification(com.aruclinic.entity.Notification n) {
        if (n != null && n.getId() != null) {
            notificationService.deleteNotificationById(n.getId());
            refreshNotifications();
            com.vaadin.flow.component.notification.Notification.show(
                "Notification cleared",
                2000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
            );
        }
    }
}
