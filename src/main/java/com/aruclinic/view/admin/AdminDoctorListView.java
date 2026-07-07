package com.aruclinic.view.admin;

import com.aruclinic.entity.Doctor;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Doctor Management | AruClinic")
@Route(value = "admin/doctors", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminDoctorListView extends VerticalLayout {

    private final AdminService adminService;
    private final Grid<Doctor> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public AdminDoctorListView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-doctor-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-doctor-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(Doctor::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Doctor::getName).setHeader("Doctor Name").setAutoWidth(true);
        grid.addColumn(Doctor::getSpecialization).setHeader("Specialization").setAutoWidth(true);
        grid.addColumn(Doctor::getDepartment).setHeader("Department").setAutoWidth(true);
        grid.addColumn(Doctor::getQualification).setHeader("Qualification").setAutoWidth(true);
        grid.addColumn(Doctor::getExperience).setHeader("Experience (Yrs)").setAutoWidth(true);
        grid.addColumn(Doctor::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        grid.addColumn(Doctor::getEmail).setHeader("Email").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);
    }

    private Component createActions(Doctor doctor) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.addClickListener(e -> openEditDialog(doctor));

        Button deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            adminService.deleteDoctor(doctor.getId());
            Notification.show("Doctor deleted successfully", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Doctor Management");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Add Doctor", new Icon(VaadinIcon.PLUS));
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

        searchField.setPlaceholder("Search by name, specialization, or department...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("350px");

        bar.add(searchField);
        return bar;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Doctor");
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        TextField name = new TextField("Full Name");
        TextField specialization = new TextField("Specialization");
        TextField dept = new TextField("Department");
        TextField qual = new TextField("Qualification");
        IntegerField exp = new IntegerField("Experience (Years)");
        TextField mobile = new TextField("Mobile Number");
        TextField email = new TextField("Email Address");

        form.add(name, specialization, dept, qual, exp, mobile, email);

        Button saveBtn = new Button("Save", e -> {
            if (name.getValue().isEmpty() || email.getValue().isEmpty() || specialization.getValue().isEmpty()) {
                Notification.show("Please fill in all required fields", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            Doctor doc = new Doctor();
            doc.setName(name.getValue());
            doc.setSpecialization(specialization.getValue());
            doc.setDepartment(dept.getValue());
            doc.setQualification(qual.getValue());
            doc.setExperience(exp.getValue() != null ? exp.getValue() : 1);
            doc.setMobileNumber(mobile.getValue());
            doc.setEmail(email.getValue());

            adminService.createDoctor(doc);
            Notification.show("Doctor profile created successfully!", 2000, Notification.Position.TOP_CENTER);
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

    private void openEditDialog(Doctor doctor) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Doctor Details");
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        TextField name = new TextField("Full Name");
        name.setValue(doctor.getName() != null ? doctor.getName() : "");
        TextField specialization = new TextField("Specialization");
        specialization.setValue(doctor.getSpecialization() != null ? doctor.getSpecialization() : "");
        TextField dept = new TextField("Department");
        dept.setValue(doctor.getDepartment() != null ? doctor.getDepartment() : "");
        TextField qual = new TextField("Qualification");
        qual.setValue(doctor.getQualification() != null ? doctor.getQualification() : "");
        IntegerField exp = new IntegerField("Experience (Years)");
        exp.setValue(doctor.getExperience());
        TextField mobile = new TextField("Mobile Number");
        mobile.setValue(doctor.getMobileNumber() != null ? doctor.getMobileNumber() : "");
        TextField email = new TextField("Email Address");
        email.setValue(doctor.getEmail() != null ? doctor.getEmail() : "");

        form.add(name, specialization, dept, qual, exp, mobile, email);

        Button saveBtn = new Button("Save", e -> {
            Doctor doc = new Doctor();
            doc.setName(name.getValue());
            doc.setSpecialization(specialization.getValue());
            doc.setDepartment(dept.getValue());
            doc.setQualification(qual.getValue());
            doc.setExperience(exp.getValue() != null ? exp.getValue() : 1);
            doc.setMobileNumber(mobile.getValue());
            doc.setEmail(email.getValue());

            adminService.updateDoctor(doctor.getId(), doc);
            Notification.show("Doctor profile updated successfully!", 2000, Notification.Position.TOP_CENTER);
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

    private void refreshGrid() {
        List<Doctor> doctors = adminService.getAllDoctors();
        String query = searchField.getValue().trim().toLowerCase();
        if (!query.isEmpty()) {
            doctors = doctors.stream().filter(d ->
                (d.getName() != null && d.getName().toLowerCase().contains(query)) ||
                (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(query)) ||
                (d.getDepartment() != null && d.getDepartment().toLowerCase().contains(query))
            ).collect(Collectors.toList());
        }
        grid.setItems(doctors);
    }
}
