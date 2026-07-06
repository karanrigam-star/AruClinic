package com.aruclinic.frontend.views.admin;

import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.Bill;
import com.aruclinic.service.AdminService;
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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.frontend.views.MainLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Patient Management | AruClinic")
@Route(value = "admin/patients", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminPatientListView extends VerticalLayout {

    private final AdminService adminService;
    private final com.aruclinic.service.LocationService locationService;
    private final Grid<Patient> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public AdminPatientListView(AdminService adminService, com.aruclinic.service.LocationService locationService) {
        this.adminService = adminService;
        this.locationService = locationService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-patient-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-patient-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(p -> "PAT-" + p.getId()).setHeader("Patient ID").setAutoWidth(true);
        grid.addColumn(p -> p.getFirstName() + " " + p.getLastName()).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Patient::getGender).setHeader("Gender").setAutoWidth(true);
        grid.addColumn(p -> p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "N/A").setHeader("DOB").setAutoWidth(true);
        grid.addColumn(p -> p.getAge() != null ? p.getAge() + " yrs" : "N/A").setHeader("Age").setAutoWidth(true);
        grid.addColumn(Patient::getBloodGroup).setHeader("Blood").setAutoWidth(true);
        grid.addColumn(Patient::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        grid.addColumn(Patient::getEmail).setHeader("Email").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);
    }

    private Component createActions(Patient patient) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button viewBtn = new Button("View", new Icon(VaadinIcon.INFO_CIRCLE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewBtn.addClickListener(e -> openViewDialog(patient));

        Button editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.addClickListener(e -> openEditDialog(patient));

        Button deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            adminService.deletePatient(patient.getId());
            Notification.show("Patient deleted successfully", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        actions.add(viewBtn, editBtn, deleteBtn);
        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Patient Management");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Register Patient", new Icon(VaadinIcon.PLUS));
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

        searchField.setPlaceholder("Search by name, email, or phone...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("350px");

        bar.add(searchField);
        return bar;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Register New Patient");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        TextField ln = new TextField("Last Name");
        TextField email = new TextField("Email Address");
        TextField mobile = new TextField("Mobile Number");
        DatePicker dob = new DatePicker("Date of Birth");
        IntegerField age = new IntegerField("Age");
        Select<String> gender = new Select<>();
        gender.setLabel("Gender");
        gender.setItems("Male", "Female", "Other");
        gender.setValue("Male");
        TextField blood = new TextField("Blood Group");
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

        form.add(fn, ln, email, mobile, dob, age, gender, blood, address, zipCode, state, district, city, emergencyContact, emergencyPhone);

        Button saveBtn = new Button("Register", e -> {
            if (fn.getValue().isEmpty() || ln.getValue().isEmpty() || email.getValue().isEmpty()) {
                Notification.show("Please fill in all required fields", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            Patient p = new Patient();
            p.setFirstName(fn.getValue());
            p.setLastName(ln.getValue());
            p.setEmail(email.getValue());
            p.setMobileNumber(mobile.getValue());
            p.setDateOfBirth(dob.getValue());
            p.setAge(age.getValue() != null ? age.getValue() : 30);
            p.setGender(gender.getValue());
            p.setBloodGroup(blood.getValue());
            p.setAddress(address.getValue());
            p.setState(state.getValue());
            p.setZipCode(zipCode.getValue());
            p.setDistrict(district.getValue());
            p.setCity(city.getValue() != null ? city.getValue() : "");
            p.setEmergencyContact(emergencyContact.getValue());
            p.setEmergencyPhone(emergencyPhone.getValue());

            adminService.createPatient(p);
            Notification.show("Patient registered successfully!", 2000, Notification.Position.TOP_CENTER);
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

    private void openEditDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Patient Details");
        dialog.setWidth("500px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        fn.setValue(patient.getFirstName() != null ? patient.getFirstName() : "");
        TextField ln = new TextField("Last Name");
        ln.setValue(patient.getLastName() != null ? patient.getLastName() : "");
        TextField email = new TextField("Email Address");
        email.setValue(patient.getEmail() != null ? patient.getEmail() : "");
        TextField mobile = new TextField("Mobile Number");
        mobile.setValue(patient.getMobileNumber() != null ? patient.getMobileNumber() : "");
        DatePicker dob = new DatePicker("Date of Birth");
        dob.setValue(patient.getDateOfBirth());
        IntegerField age = new IntegerField("Age");
        age.setValue(patient.getAge());
        Select<String> gender = new Select<>();
        gender.setLabel("Gender");
        gender.setItems("Male", "Female", "Other");
        gender.setValue(patient.getGender() != null ? patient.getGender() : "Male");
        TextField blood = new TextField("Blood Group");
        blood.setValue(patient.getBloodGroup() != null ? patient.getBloodGroup() : "");

        TextField address = new TextField("Address");
        address.setValue(patient.getAddress() != null ? patient.getAddress() : "");
        TextField state = new TextField("State");
        state.setValue(patient.getState() != null ? patient.getState() : "");
        state.setReadOnly(true);
        TextField zipCode = new TextField("ZIP Code");
        zipCode.setValue(patient.getZipCode() != null ? patient.getZipCode() : "");
        TextField district = new TextField("District");
        district.setValue(patient.getDistrict() != null ? patient.getDistrict() : "");
        ComboBox<String> city = new ComboBox<>("City");
        if (patient.getCity() != null && !patient.getCity().isEmpty()) {
            city.setItems(patient.getCity());
            city.setValue(patient.getCity());
        }
        TextField emergencyContact = new TextField("Emergency Contact");
        emergencyContact.setValue(patient.getEmergencyContact() != null ? patient.getEmergencyContact() : "");
        TextField emergencyPhone = new TextField("Emergency Phone");
        emergencyPhone.setValue(patient.getEmergencyPhone() != null ? patient.getEmergencyPhone() : "");

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

        form.add(fn, ln, email, mobile, dob, age, gender, blood, address, zipCode, state, district, city, emergencyContact, emergencyPhone);

        Button saveBtn = new Button("Save", e -> {
            Patient p = new Patient();
            p.setFirstName(fn.getValue());
            p.setLastName(ln.getValue());
            p.setEmail(email.getValue());
            p.setMobileNumber(mobile.getValue());
            p.setDateOfBirth(dob.getValue());
            p.setAge(age.getValue() != null ? age.getValue() : 30);
            p.setGender(gender.getValue());
            p.setBloodGroup(blood.getValue());
            p.setAddress(address.getValue());
            p.setState(state.getValue());
            p.setZipCode(zipCode.getValue());
            p.setDistrict(district.getValue());
            p.setCity(city.getValue() != null ? city.getValue() : "");
            p.setEmergencyContact(emergencyContact.getValue());
            p.setEmergencyPhone(emergencyPhone.getValue());

            adminService.updatePatient(patient.getId(), p);
            Notification.show("Patient profile updated successfully!", 2000, Notification.Position.TOP_CENTER);
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

    private void openViewDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Medical & Activity Records: " + patient.getFirstName() + " " + patient.getLastName());
        dialog.setWidth("750px");
        dialog.setHeight("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        // Demographic block
        Div demo = new Div();
        demo.getStyle().set("background", "#F8FAFC").set("padding", "16px").set("border-radius", "8px").set("width", "100%");
        demo.add(new Span("Email: " + patient.getEmail() + " | Mobile: " + patient.getMobileNumber() + " | Blood Group: " + patient.getBloodGroup()));
        layout.add(demo);

        // Appointments History
        H3 apptTitle = new H3("Appointment History");
        Grid<Appointment> apptGrid = new Grid<>();
        apptGrid.setHeight("180px");
        apptGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        
        List<Appointment> appointments = adminService.getAllAppointments().stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getId().equals(patient.getId()))
                .collect(Collectors.toList());
        apptGrid.setItems(appointments);
        
        apptGrid.addColumn(a -> a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "N/A").setHeader("Date");
        apptGrid.addColumn(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "N/A").setHeader("Time");
        apptGrid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A").setHeader("Doctor");
        apptGrid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "N/A").setHeader("Status");

        layout.add(apptTitle, apptGrid);

        // Billing History
        H3 billingTitle = new H3("Billing History");
        Grid<Bill> billGrid = new Grid<>();
        billGrid.setHeight("180px");
        billGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        List<Bill> bills = adminService.getAllBills().stream()
                .filter(b -> b.getPatient() != null && b.getPatient().getId().equals(patient.getId()))
                .collect(Collectors.toList());
        billGrid.setItems(bills);

        billGrid.addColumn(b -> "INV-" + b.getId()).setHeader("Invoice");
        billGrid.addColumn(b -> b.getInvoiceDate() != null ? b.getInvoiceDate().toString() : "N/A").setHeader("Date");
        billGrid.addColumn(Bill::getTotal).setHeader("Total (₹)");
        billGrid.addColumn(Bill::getStatus).setHeader("Status");

        layout.add(billingTitle, billGrid);

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.add(layout);
        dialog.open();
    }

    private void refreshGrid() {
        List<Patient> patients = adminService.getAllPatients();
        String query = searchField.getValue().trim().toLowerCase();
        if (!query.isEmpty()) {
            patients = patients.stream().filter(p ->
                (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(query) ||
                (p.getEmail() != null && p.getEmail().toLowerCase().contains(query)) ||
                (p.getMobileNumber() != null && p.getMobileNumber().contains(query))
            ).collect(Collectors.toList());
        }
        grid.setItems(patients);
    }
}
