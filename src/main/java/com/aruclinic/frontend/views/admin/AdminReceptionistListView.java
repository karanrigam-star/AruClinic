package com.aruclinic.frontend.views.admin;

import com.aruclinic.entity.User;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.frontend.views.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Receptionist Management | AruClinic")
@Route(value = "admin/receptionists", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminReceptionistListView extends VerticalLayout {

    private final AdminService adminService;
    private final Grid<User> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public AdminReceptionistListView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-receptionist-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-receptionist-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(u -> u.getFirstName() + " " + u.getLastName()).setHeader("Name").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(User::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        
        grid.addColumn(u -> adminService.isUserEnabled(u.getId()) ? "Active" : "Inactive")
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
            Notification.show("Receptionist status updated successfully", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });

        actions.add(editBtn, statusBtn);
        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Receptionist Management");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Add Receptionist", new Icon(VaadinIcon.PLUS));
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

        bar.add(searchField);
        return bar;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Receptionist");
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        TextField ln = new TextField("Last Name");
        TextField email = new TextField("Email Address");
        TextField mobile = new TextField("Mobile Number");
        PasswordField pw = new PasswordField("Password");

        form.add(fn, ln, email, mobile, pw);

        Button saveBtn = new Button("Save", e -> {
            if (fn.getValue().isEmpty() || email.getValue().isEmpty() || pw.getValue().isEmpty()) {
                Notification.show("Please fill in all required fields", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            User u = new User();
            u.setFirstName(fn.getValue());
            u.setLastName(ln.getValue());
            u.setEmail(email.getValue());
            u.setMobileNumber(mobile.getValue());
            u.setPassword(pw.getValue());

            adminService.createUser(u, "RECEPTIONIST");
            Notification.show("Receptionist created successfully!", 2000, Notification.Position.TOP_CENTER);
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
        dialog.setHeaderTitle("Edit Receptionist Details");
        dialog.setWidth("400px");

        FormLayout form = new FormLayout();
        TextField fn = new TextField("First Name");
        fn.setValue(user.getFirstName() != null ? user.getFirstName() : "");
        TextField ln = new TextField("Last Name");
        ln.setValue(user.getLastName() != null ? user.getLastName() : "");
        TextField email = new TextField("Email Address");
        email.setValue(user.getEmail() != null ? user.getEmail() : "");
        TextField mobile = new TextField("Mobile Number");
        mobile.setValue(user.getMobileNumber() != null ? user.getMobileNumber() : "");

        form.add(fn, ln, email, mobile);

        Button saveBtn = new Button("Save", e -> {
            User uDetails = new User();
            uDetails.setFirstName(fn.getValue());
            uDetails.setLastName(ln.getValue());
            uDetails.setEmail(email.getValue());
            uDetails.setMobileNumber(mobile.getValue());

            adminService.updateUser(user.getId(), uDetails, "RECEPTIONIST");
            Notification.show("Receptionist profile updated successfully!", 2000, Notification.Position.TOP_CENTER);
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
        List<User> receptionists = adminService.getReceptionists();
        String query = searchField.getValue().trim().toLowerCase();
        if (!query.isEmpty()) {
            receptionists = receptionists.stream().filter(u ->
                (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(query))
            ).collect(Collectors.toList());
        }
        grid.setItems(receptionists);
    }
}
