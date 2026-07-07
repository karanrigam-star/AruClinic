package com.aruclinic.view.doctor;

import com.aruclinic.entity.Doctor;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Doctor Profile view for displaying and editing logged-in doctor information.
 */
@PageTitle("Doctor Profile | AruClinic")
@Route(value = "doctor/profile", layout = MainLayout.class)
@CssImport("./themes/aruclinic/patient.css")
public class DoctorProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private Doctor currentDoctor = null;
    private com.aruclinic.entity.User currentUser = null;
    private boolean editMode = false;

    // Form inputs for edit mode
    private TextField nameField;
    private TextField qualificationField;
    private IntegerField experienceField;
    private TextField departmentField;
    private TextField specializationField;
    private TextField mobileField;
    private TextField emailField; // Read-only to protect login logic

    public DoctorProfileView(DoctorRepository doctorRepository,
                             UserRepository userRepository,
                             NotificationRepository notificationRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolveCurrentDoctor();
        removeAll();
        if (currentDoctor != null) {
            add(createProfileContent());
        } else {
            add(new Span("Doctor profile not found. Please log in."));
        }
    }

    private void resolveCurrentDoctor() {
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
                currentDoctor = doctorRepository.findByEmail(email).orElse(null);
                currentUser = userRepository.findByEmail(email).orElse(null);
            }

            // Fallback for SUPER_ADMIN or blanks
            if (currentDoctor == null) {
                List<Doctor> doctors = doctorRepository.findAll();
                if (!doctors.isEmpty()) {
                    currentDoctor = doctors.get(0);
                    currentUser = userRepository.findByEmail(currentDoctor.getEmail()).orElse(null);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private Component createProfileContent() {
        Div container = new Div();
        container.setSizeFull();
        container.addClassName("aruclinic-profile-container");

        container.add(createProfileHeader());
        container.add(createOverviewPanel());

        return container;
    }

    private Component createProfileHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.addClassName("aruclinic-profile-header");
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Div avatarSection = new Div();
        avatarSection.addClassName("aruclinic-profile-avatar-section");
        Div avatar = new Div();
        avatar.addClassName("aruclinic-profile-avatar");
        avatar.setText(currentDoctor.getName() != null && !currentDoctor.getName().isEmpty() ? 
                currentDoctor.getName().substring(0, 1).toUpperCase() : "D");
        avatarSection.add(avatar);

        Div infoSection = new Div();
        infoSection.addClassName("aruclinic-profile-info-section");

        H2 name = new H2(currentDoctor.getName());
        name.addClassName("aruclinic-profile-name");

        Span docId = new Span("Doctor ID: DOC-" + currentDoctor.getId());
        docId.addClassName("aruclinic-profile-id");

        Span status = new Span(currentDoctor.getSpecialization() != null ? currentDoctor.getSpecialization() : "Medical Doctor");
        status.addClassName("aruclinic-profile-status");

        Div contact = new Div();
        contact.addClassName("aruclinic-profile-contact");
        
        Span emailItem = new Span(new Icon(VaadinIcon.ENVELOPE), new Span(currentDoctor.getEmail()));
        emailItem.addClassName("aruclinic-profile-contact-item");
        
        Span phoneItem = new Span(new Icon(VaadinIcon.PHONE), new Span(currentDoctor.getMobileNumber()));
        phoneItem.addClassName("aruclinic-profile-contact-item");
        
        contact.add(emailItem, phoneItem);

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("aruclinic-profile-actions");

        if (editMode) {
            Button saveBtn = new Button("Save", new Icon(VaadinIcon.CHECK));
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            saveBtn.addClassName("aruclinic-btn");
            saveBtn.addClassName("aruclinic-btn-primary");
            saveBtn.addClickListener(e -> handleSaveChanges());

            Button cancelBtn = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            cancelBtn.addClassName("aruclinic-btn");
            cancelBtn.addClassName("aruclinic-btn-secondary");
            cancelBtn.addClickListener(e -> {
                editMode = false;
                removeAll();
                add(createProfileContent());
            });

            actions.add(saveBtn, cancelBtn);
        } else {
            Button editProfileBtn = new Button("Edit Profile", new Icon(VaadinIcon.EDIT));
            editProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editProfileBtn.addClassName("aruclinic-btn");
            editProfileBtn.addClassName("aruclinic-btn-primary");
            editProfileBtn.addClickListener(e -> {
                editMode = true;
                removeAll();
                add(createProfileContent());
            });

            Button backBtn = new Button("Back", new Icon(VaadinIcon.ARROW_LEFT));
            backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backBtn.addClassName("aruclinic-btn");
            backBtn.addClassName("aruclinic-btn-secondary");
            backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor")));

            actions.add(editProfileBtn, backBtn);
        }

        infoSection.add(name, docId, status, contact, actions);
        header.add(avatarSection, infoSection);

        return header;
    }

    private Div createOverviewPanel() {
        Div panel = new Div();
        panel.setWidthFull();

        // Professional Details
        panel.add(createDetailCard("Professional Information", createProfessionalInfoContent()));

        // Contact Information
        panel.add(createDetailCard("Contact Information", createContactInfoContent()));

        return panel;
    }

    private Component createDetailCard(String title, Component content) {
        Div card = new Div();
        card.addClassName("aruclinic-profile-detail-card");
        card.setWidthFull();

        H2 cardTitle = new H2(title);
        cardTitle.addClassName("aruclinic-profile-detail-card-title");

        card.add(cardTitle, content);
        return card;
    }

    private Component createProfessionalInfoContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        if (editMode) {
            nameField = new TextField();
            nameField.setValue(currentDoctor.getName() != null ? currentDoctor.getName() : "");
            nameField.setWidthFull();

            qualificationField = new TextField();
            qualificationField.setValue(currentDoctor.getQualification() != null ? currentDoctor.getQualification() : "");
            qualificationField.setWidthFull();

            experienceField = new IntegerField();
            experienceField.setValue(currentDoctor.getExperience() != null ? currentDoctor.getExperience() : 0);
            experienceField.setMin(0);
            experienceField.setWidthFull();

            specializationField = new TextField();
            specializationField.setValue(currentDoctor.getSpecialization() != null ? currentDoctor.getSpecialization() : "");
            specializationField.setWidthFull();

            departmentField = new TextField();
            departmentField.setValue(currentDoctor.getDepartment() != null ? currentDoctor.getDepartment() : "");
            departmentField.setWidthFull();

            content.add(createEditableDetailItem("Full Name", nameField));
            content.add(createEditableDetailItem("Qualification", qualificationField));
            content.add(createEditableDetailItem("Experience (Years)", experienceField));
            content.add(createEditableDetailItem("Specialization", specializationField));
            content.add(createEditableDetailItem("Department", departmentField));
        } else {
            content.add(createDetailItem("Qualification", currentDoctor.getQualification()));
            content.add(createDetailItem("Experience", currentDoctor.getExperience() != null ? currentDoctor.getExperience() + " years" : "N/A"));
            content.add(createDetailItem("Specialization", currentDoctor.getSpecialization()));
            content.add(createDetailItem("Department", currentDoctor.getDepartment()));
        }

        return content;
    }

    private Component createContactInfoContent() {
        Div content = new Div();
        content.addClassName("aruclinic-profile-detail-list");

        if (editMode) {
            mobileField = new TextField();
            mobileField.setValue(currentDoctor.getMobileNumber() != null ? currentDoctor.getMobileNumber() : "");
            mobileField.setWidthFull();

            emailField = new TextField();
            emailField.setValue(currentDoctor.getEmail() != null ? currentDoctor.getEmail() : "");
            emailField.setEnabled(false); // Login email is kept read-only for authentication safety
            emailField.setWidthFull();

            content.add(createEditableDetailItem("Mobile Number", mobileField));
            content.add(createEditableDetailItem("Email Address", emailField));
        } else {
            content.add(createDetailItem("Mobile Number", currentDoctor.getMobileNumber()));
            content.add(createDetailItem("Email Address", currentDoctor.getEmail()));
        }

        return content;
    }

    private Component createDetailItem(String label, String value) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-profile-detail-label");

        Span valueSpan = new Span(value != null ? value : "N/A");
        valueSpan.addClassName("aruclinic-profile-detail-value");

        item.add(labelSpan, valueSpan);
        return item;
    }

    private Component createEditableDetailItem(String label, Component field) {
        Div item = new Div();
        item.addClassName("aruclinic-profile-detail-item");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-profile-detail-label");

        item.add(labelSpan, field);
        return item;
    }

    private void handleSaveChanges() {
        try {
            // Field validations
            if (nameField.getValue() == null || nameField.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Full Name cannot be empty.");
            }
            if (qualificationField.getValue() == null || qualificationField.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Qualification cannot be empty.");
            }
            if (experienceField.getValue() == null || experienceField.getValue() < 0) {
                throw new IllegalArgumentException("Experience must be a positive integer.");
            }
            if (specializationField.getValue() == null || specializationField.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Specialization cannot be empty.");
            }
            if (departmentField.getValue() == null || departmentField.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Department cannot be empty.");
            }
            if (mobileField.getValue() == null || mobileField.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Mobile Number cannot be empty.");
            }

            // Update Doctor Entity
            currentDoctor.setName(nameField.getValue().trim());
            currentDoctor.setQualification(qualificationField.getValue().trim());
            currentDoctor.setExperience(experienceField.getValue());
            currentDoctor.setSpecialization(specializationField.getValue().trim());
            currentDoctor.setDepartment(departmentField.getValue().trim());
            currentDoctor.setMobileNumber(mobileField.getValue().trim());

            doctorRepository.save(currentDoctor);

            // Update User Entity display name if username matches doctor email
            if (currentUser != null) {
                currentUser.setFirstName(nameField.getValue().trim());
                currentUser.setLastName(""); // Split or keep simple
                userRepository.save(currentUser);

                // Add notification
                com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                notif.setUser(currentUser);
                notif.setTitle("Profile Updated");
                notif.setMessage("Your professional profile details have been successfully updated.");
                notif.setRead(false);
                notif.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notif);
            }

            editMode = false;
            Notification.show("Profile updated successfully!", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            removeAll();
            add(createProfileContent());

        } catch (Exception ex) {
            Notification.show("Failed to update profile: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
