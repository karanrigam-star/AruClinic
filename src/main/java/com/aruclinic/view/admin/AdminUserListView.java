package com.aruclinic.view.admin;

import com.aruclinic.entity.User;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Doctor;
import com.aruclinic.service.AdminService;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("User Management | AruClinic")
@Route(value = "admin/users", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminUserListView extends VerticalLayout {

    private final AdminService adminService;
    private final com.aruclinic.service.LocationService locationService;
    private final Grid<User> grid = new Grid<>();
    private final TextField searchField = new TextField();
    private final Select<String> roleFilter = new Select<>();

    public AdminUserListView(AdminService adminService,
                             com.aruclinic.service.LocationService locationService) {
        this.adminService = adminService;
        this.locationService = locationService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-user-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-user-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(u -> u.getFirstName() + " " + u.getLastName()).setHeader("Name").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(User::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        grid.addColumn(u -> u.getRoles().stream().findFirst().map(r -> r.getName()).orElse("PATIENT"))
            .setHeader("Role").setAutoWidth(true);
        
        grid.addColumn(u -> adminService.isUserEnabled(u.getId()) ? "Enabled" : "Disabled")
            .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);
    }

    private Component createActions(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.addClickListener(e -> openEditDialog(user));

        boolean isEnabled = adminService.isUserEnabled(user.getId());
        Button statusBtn = new Button(isEnabled ? "Disable" : "Enable");
        statusBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, isEnabled ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
        statusBtn.addClickListener(e -> {
            adminService.toggleUserStatus(user.getId(), !isEnabled);
            Notification.show("User status updated successfully", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        Button pwBtn = new Button("Reset PW");
        pwBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        pwBtn.addClickListener(e -> openResetPasswordDialog(user));

        Button deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> openDeleteConfirmationDialog(user));

        actions.add(editBtn, statusBtn, pwBtn, deleteBtn);
        return actions;
    }

    private void openDeleteConfirmationDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Account Permanently");
        dialog.setWidth("380px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Span warningText = new Span("Are you sure you want to delete user " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ") permanently? This will also remove any linked Doctor/Patient records. This action cannot be undone.");
        warningText.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        content.add(warningText);

        Button deleteBtn = new Button("Delete Permanently", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            try {
                adminService.deleteUser(user.getId());
                Notification.show("User deleted successfully!", 3000, Notification.Position.TOP_CENTER);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error deleting user: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, deleteBtn);
        dialog.add(content);
        dialog.open();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("User Management");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Add User", new Icon(VaadinIcon.PLUS));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openAddDialog());

        header.add(title, addBtn);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search by name or email...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("300px");

        roleFilter.setPlaceholder("Filter by role");
        roleFilter.setItems("ALL", "ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT");
        roleFilter.setValue("ALL");
        roleFilter.addValueChangeListener(e -> refreshGrid());

        bar.add(searchField, roleFilter);
        return bar;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New System User");
        dialog.setWidth("750px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        TextField ln = new TextField("Last Name");
        TextField email = new TextField("Email Address");
        TextField mobile = new TextField("Mobile Number");
        PasswordField pw = new PasswordField("Password");
        Select<String> roleSelect = new Select<>();
        roleSelect.setLabel("Role");
        roleSelect.setItems("ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT");

        // Patient-specific container
        Div patientFields = new Div();
        patientFields.setWidthFull();
        FormLayout patientFormLayout = new FormLayout();

        DatePicker dob = new DatePicker("Date of Birth");
        Select<String> gender = new Select<>();
        gender.setLabel("Gender");
        gender.setItems("Male", "Female", "Other");
        gender.setValue("Male");

        Select<String> blood = new Select<>();
        blood.setLabel("Blood Group");
        blood.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        blood.setValue("O+");

        TextField address = new TextField("Address");
        TextField state = new TextField("State");
        state.setValue("Arunachal Pradesh");
        state.setReadOnly(true);
        TextField zipCode = new TextField("ZIP Code");
        TextField district = new TextField("District");
        ComboBox<String> city = new ComboBox<>("City");
        TextField emergencyContact = new TextField("Emergency Contact");
        TextField emergencyPhone = new TextField("Emergency Phone");

        zipCode.setValueChangeMode(ValueChangeMode.EAGER);
        zipCode.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null && val.trim().length() == 6) {
                com.aruclinic.service.LocationService.LocationDetails details = locationService.lookupPincode(val.trim());
                if (details != null && details.state != null && !details.state.isEmpty()) {
                    district.setValue(details.district);
                    state.setValue(details.state);
                    city.setItems(details.cities);
                    if (!details.cities.isEmpty()) {
                        city.setValue(details.cities.get(0));
                    } else {
                        city.setValue(null);
                    }
                }
            } else {
                city.setItems(java.util.Collections.emptyList());
                city.setValue(null);
                district.setValue("");
            }
        });

        patientFormLayout.add(dob, gender, blood, address, zipCode, state, district, city, emergencyContact, emergencyPhone);
        patientFields.add(patientFormLayout);

        // Doctor-specific container
        Div doctorFields = new Div();
        doctorFields.setWidthFull();
        FormLayout doctorFormLayout = new FormLayout();
        TextField docSpec = new TextField("Specialization");
        
        ComboBox<String> docDept = new ComboBox<>("Department");
        docDept.setItems("General Outpatient", "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Orthopedics", "Gynaecology", "Neurology");
        docDept.setAllowCustomValue(true);
        docDept.addCustomValueSetListener(event -> docDept.setValue(event.getDetail()));
        docDept.setValue("General Outpatient");

        ComboBox<String> docQual = new ComboBox<>("Qualification");
        docQual.setItems("MBBS", "MD", "MS", "DNB", "PhD", "BDS", "MDS", "FACS", "FRCP");
        docQual.setRequiredIndicatorVisible(true);
        docQual.setValue("MBBS");

        IntegerField docExp = new IntegerField("Experience (Years)");
        docExp.setRequiredIndicatorVisible(true);
        docExp.setValue(2);

        doctorFormLayout.add(docSpec, docDept, docQual, docExp);
        doctorFields.add(doctorFormLayout);
        doctorFields.setVisible(false);

        roleSelect.addValueChangeListener(event -> {
            boolean isPatient = "PATIENT".equalsIgnoreCase(event.getValue());
            boolean isDoctor = "DOCTOR".equalsIgnoreCase(event.getValue());
            patientFields.setVisible(isPatient);
            doctorFields.setVisible(isDoctor);
            if (isPatient || isDoctor) {
                dialog.setWidth("750px");
            } else {
                dialog.setWidth("450px");
            }
        });

        // Set default role value (will trigger visibility of patient fields)
        roleSelect.setValue("PATIENT");

        form.add(fn, ln, email, mobile, pw, roleSelect, patientFields, doctorFields);

        Button saveBtn = new Button("Save", e -> {
            if ("DOCTOR".equalsIgnoreCase(roleSelect.getValue())) {
                if (docSpec.getValue().isEmpty() || docQual.getValue() == null || docQual.getValue().trim().isEmpty() || docExp.getValue() == null) {
                    Notification.show("Please fill in all required doctor fields (Specialization, Qualification, Experience)", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            }

            User u = new User();
            u.setFirstName(fn.getValue());
            u.setLastName(ln.getValue());
            u.setEmail(email.getValue());
            u.setMobileNumber(mobile.getValue());
            u.setPassword(pw.getValue());

            adminService.createUser(u, roleSelect.getValue());

            if ("PATIENT".equalsIgnoreCase(roleSelect.getValue())) {
                try {
                    java.util.Optional<Patient> patientOpt = adminService.findPatientByEmail(u.getEmail());
                    if (patientOpt.isPresent()) {
                        Patient patient = patientOpt.get();
                        patient.setDateOfBirth(dob.getValue());
                        if (dob.getValue() != null) {
                            patient.setAge(java.time.Period.between(dob.getValue(), java.time.LocalDate.now()).getYears());
                        }
                        patient.setGender(gender.getValue());
                        patient.setBloodGroup(blood.getValue());
                        patient.setAddress(address.getValue());
                        patient.setState(state.getValue());
                        patient.setZipCode(zipCode.getValue());
                        patient.setDistrict(district.getValue());
                        patient.setCity(city.getValue() != null ? city.getValue() : "");
                        patient.setEmergencyContact(emergencyContact.getValue());
                        patient.setEmergencyPhone(emergencyPhone.getValue());
                        
                        adminService.savePatient(patient);
                    }
                } catch (Exception ex) {
                    System.err.println("Error saving linked patient entity: " + ex.getMessage());
                }
            } else if ("DOCTOR".equalsIgnoreCase(roleSelect.getValue())) {
                try {
                    java.util.Optional<Doctor> doctorOpt = adminService.findDoctorByEmail(u.getEmail());
                    if (doctorOpt.isPresent()) {
                        Doctor doctor = doctorOpt.get();
                        doctor.setSpecialization(docSpec.getValue());
                        doctor.setDepartment(docDept.getValue() != null ? docDept.getValue() : "General Outpatient");
                        doctor.setQualification(docQual.getValue());
                        doctor.setExperience(docExp.getValue());
                        adminService.saveDoctor(doctor);
                    }
                } catch (Exception ex) {
                    System.err.println("Error saving linked doctor entity: " + ex.getMessage());
                }
            }

            Notification.show("User created successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshGrid();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openEditDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit User Details");
        dialog.setWidth("750px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        fn.setValue(user.getFirstName() != null ? user.getFirstName() : "");
        TextField ln = new TextField("Last Name");
        ln.setValue(user.getLastName() != null ? user.getLastName() : "");
        TextField email = new TextField("Email Address");
        email.setValue(user.getEmail() != null ? user.getEmail() : "");
        TextField mobile = new TextField("Mobile Number");
        mobile.setValue(user.getMobileNumber() != null ? user.getMobileNumber() : "");

        Select<String> roleSelect = new Select<>();
        roleSelect.setLabel("Role");
        roleSelect.setItems("ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT");
        roleSelect.setValue(user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("PATIENT"));

        // Patient-specific container
        Div patientFields = new Div();
        patientFields.setWidthFull();
        FormLayout patientFormLayout = new FormLayout();

        DatePicker dob = new DatePicker("Date of Birth");
        Select<String> gender = new Select<>();
        gender.setLabel("Gender");
        gender.setItems("Male", "Female", "Other");
        gender.setValue("Male");

        Select<String> blood = new Select<>();
        blood.setLabel("Blood Group");
        blood.setItems("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        blood.setValue("O+");

        TextField address = new TextField("Address");
        TextField state = new TextField("State");
        state.setValue("Arunachal Pradesh");
        state.setReadOnly(true);
        TextField zipCode = new TextField("ZIP Code");
        TextField district = new TextField("District");
        ComboBox<String> city = new ComboBox<>("City");
        TextField emergencyContact = new TextField("Emergency Contact");
        TextField emergencyPhone = new TextField("Emergency Phone");

        // Load existing patient details if any
        java.util.Optional<Patient> patientOpt = adminService.findPatientByEmail(user.getEmail());
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            dob.setValue(patient.getDateOfBirth());
            gender.setValue(patient.getGender() != null ? patient.getGender() : "Male");
            blood.setValue(patient.getBloodGroup() != null ? patient.getBloodGroup() : "O+");
            address.setValue(patient.getAddress() != null ? patient.getAddress() : "");
            state.setValue(patient.getState() != null ? patient.getState() : "Arunachal Pradesh");
            zipCode.setValue(patient.getZipCode() != null ? patient.getZipCode() : "");
            district.setValue(patient.getDistrict() != null ? patient.getDistrict() : "");
            if (patient.getCity() != null && !patient.getCity().isEmpty()) {
                city.setItems(patient.getCity());
                city.setValue(patient.getCity());
            }
            emergencyContact.setValue(patient.getEmergencyContact() != null ? patient.getEmergencyContact() : "");
            emergencyPhone.setValue(patient.getEmergencyPhone() != null ? patient.getEmergencyPhone() : "");
        }

        // Doctor-specific container
        Div doctorFields = new Div();
        doctorFields.setWidthFull();
        FormLayout doctorFormLayout = new FormLayout();
        TextField docSpec = new TextField("Specialization");
        
        ComboBox<String> docDept = new ComboBox<>("Department");
        docDept.setItems("General Outpatient", "General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Orthopedics", "Gynaecology", "Neurology");
        docDept.setAllowCustomValue(true);
        docDept.addCustomValueSetListener(event -> docDept.setValue(event.getDetail()));
        docDept.setValue("General Outpatient");

        ComboBox<String> docQual = new ComboBox<>("Qualification");
        docQual.setItems("MBBS", "MD", "MS", "DNB", "PhD", "BDS", "MDS", "FACS", "FRCP");
        docQual.setRequiredIndicatorVisible(true);
        docQual.setValue("MBBS");

        IntegerField docExp = new IntegerField("Experience (Years)");
        docExp.setRequiredIndicatorVisible(true);
        docExp.setValue(2);

        // Load existing doctor details if any
        java.util.Optional<Doctor> doctorOpt = adminService.findDoctorByEmail(user.getEmail());
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            docSpec.setValue(doctor.getSpecialization() != null ? doctor.getSpecialization() : "");
            docDept.setValue(doctor.getDepartment() != null ? doctor.getDepartment() : "General Outpatient");
            docQual.setValue(doctor.getQualification() != null ? doctor.getQualification() : "MBBS");
            docExp.setValue(doctor.getExperience() != null ? doctor.getExperience() : 2);
        }

        doctorFormLayout.add(docSpec, docDept, docQual, docExp);
        doctorFields.add(doctorFormLayout);
        doctorFields.setVisible(false);

        zipCode.setValueChangeMode(ValueChangeMode.EAGER);
        zipCode.addValueChangeListener(e -> {
            String val = e.getValue();
            if (val != null && val.trim().length() == 6) {
                com.aruclinic.service.LocationService.LocationDetails details = locationService.lookupPincode(val.trim());
                if (details != null && details.state != null && !details.state.isEmpty()) {
                    district.setValue(details.district);
                    state.setValue(details.state);
                    city.setItems(details.cities);
                    if (!details.cities.isEmpty()) {
                        city.setValue(details.cities.get(0));
                    } else {
                        city.setValue(null);
                    }
                }
            } else {
                city.setItems(java.util.Collections.emptyList());
                city.setValue(null);
                district.setValue("");
            }
        });

        patientFormLayout.add(dob, gender, blood, address, zipCode, state, district, city, emergencyContact, emergencyPhone);
        patientFields.add(patientFormLayout);

        roleSelect.addValueChangeListener(event -> {
            boolean isPatient = "PATIENT".equalsIgnoreCase(event.getValue());
            boolean isDoctor = "DOCTOR".equalsIgnoreCase(event.getValue());
            patientFields.setVisible(isPatient);
            doctorFields.setVisible(isDoctor);
            if (isPatient || isDoctor) {
                dialog.setWidth("750px");
            } else {
                dialog.setWidth("450px");
            }
        });

        // Trigger initial visibility
        boolean isPatientInit = "PATIENT".equalsIgnoreCase(roleSelect.getValue());
        boolean isDoctorInit = "DOCTOR".equalsIgnoreCase(roleSelect.getValue());
        patientFields.setVisible(isPatientInit);
        doctorFields.setVisible(isDoctorInit);
        if (isPatientInit || isDoctorInit) {
            dialog.setWidth("750px");
        } else {
            dialog.setWidth("450px");
        }

        form.add(fn, ln, email, mobile, roleSelect, patientFields, doctorFields);

        Button saveBtn = new Button("Save", e -> {
            User uDetails = new User();
            uDetails.setFirstName(fn.getValue());
            uDetails.setLastName(ln.getValue());
            uDetails.setEmail(email.getValue());
            uDetails.setMobileNumber(mobile.getValue());

            adminService.updateUser(user.getId(), uDetails, roleSelect.getValue());

            if ("DOCTOR".equalsIgnoreCase(roleSelect.getValue())) {
                if (docSpec.getValue().isEmpty() || docQual.getValue() == null || docQual.getValue().trim().isEmpty() || docExp.getValue() == null) {
                    Notification.show("Please fill in all required doctor fields (Specialization, Qualification, Experience)", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            }

            adminService.updateUser(user.getId(), uDetails, roleSelect.getValue());

            if ("PATIENT".equalsIgnoreCase(roleSelect.getValue())) {
                try {
                    java.util.Optional<Patient> pOpt = adminService.findPatientByEmail(user.getEmail());
                    if (pOpt.isPresent()) {
                        Patient patient = pOpt.get();
                        patient.setEmail(email.getValue());
                        patient.setFirstName(fn.getValue());
                        patient.setLastName(ln.getValue());
                        patient.setMobileNumber(mobile.getValue());
                        patient.setDateOfBirth(dob.getValue());
                        if (dob.getValue() != null) {
                            patient.setAge(java.time.Period.between(dob.getValue(), java.time.LocalDate.now()).getYears());
                        }
                        patient.setGender(gender.getValue());
                        patient.setBloodGroup(blood.getValue());
                        patient.setAddress(address.getValue());
                        patient.setState(state.getValue());
                        patient.setZipCode(zipCode.getValue());
                        patient.setDistrict(district.getValue());
                        patient.setCity(city.getValue() != null ? city.getValue() : "");
                        patient.setEmergencyContact(emergencyContact.getValue());
                        patient.setEmergencyPhone(emergencyPhone.getValue());
                        
                        adminService.savePatient(patient);
                    }
                } catch (Exception ex) {
                    System.err.println("Error updating linked patient entity: " + ex.getMessage());
                }
            } else if ("DOCTOR".equalsIgnoreCase(roleSelect.getValue())) {
                try {
                    java.util.Optional<Doctor> dOpt = adminService.findDoctorByEmail(user.getEmail());
                    if (dOpt.isPresent()) {
                        Doctor doctor = dOpt.get();
                        doctor.setEmail(email.getValue());
                        doctor.setName(fn.getValue() + " " + ln.getValue());
                        doctor.setMobileNumber(mobile.getValue());
                        doctor.setSpecialization(docSpec.getValue());
                        doctor.setDepartment(docDept.getValue() != null ? docDept.getValue() : "General Outpatient");
                        doctor.setQualification(docQual.getValue());
                        doctor.setExperience(docExp.getValue());
                        adminService.saveDoctor(doctor);
                    }
                } catch (Exception ex) {
                    System.err.println("Error updating linked doctor entity: " + ex.getMessage());
                }
            }

            Notification.show("User details updated successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
            refreshGrid();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(form);
        dialog.open();
    }

    private void openResetPasswordDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reset Password: " + user.getFirstName());
        dialog.setWidth("350px");

        PasswordField pw = new PasswordField("New Password");
        dialog.add(pw);

        Button confirmBtn = new Button("Reset", e -> {
            if (pw.getValue().isEmpty()) {
                Notification.show("Password cannot be empty", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            adminService.resetUserPassword(user.getId(), pw.getValue());
            Notification.show("Password reset successfully!", 2000, Notification.Position.TOP_CENTER);
            dialog.close();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }

    private void refreshGrid() {
        List<User> users = adminService.getAllUsers();
        String query = searchField.getValue().trim().toLowerCase();
        if (!query.isEmpty()) {
            users = users.stream().filter(u ->
                (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(query))
            ).collect(Collectors.toList());
        }

        String filterRole = roleFilter.getValue();
        if (filterRole != null && !"ALL".equalsIgnoreCase(filterRole)) {
            users = users.stream().filter(u ->
                u.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(filterRole))
            ).collect(Collectors.toList());
        }

        grid.setItems(users);
    }
}
