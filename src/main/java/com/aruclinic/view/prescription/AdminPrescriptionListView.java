package com.aruclinic.view.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("All Patient Prescriptions | AruClinic")
@Route(value = "admin/prescriptions", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminPrescriptionListView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final PrescriptionService prescriptionService;
    private final Grid<PrescriptionDto> grid = new Grid<>();
    private final List<PrescriptionDto> allPrescriptions = new ArrayList<>();
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    public AdminPrescriptionListView(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-user-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshData();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-user-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(PrescriptionDto::getPrescriptionId)
            .setHeader("ID")
            .setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getPrescriptionDate)
            .setHeader("Date")
            .setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getPatientName)
            .setHeader("Patient")
            .setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getDoctorName)
            .setHeader("Doctor")
            .setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getDiagnosis)
            .setHeader("Diagnosis")
            .setAutoWidth(true);

        grid.addComponentColumn(this::getStatusBadge)
            .setHeader("Status")
            .setAutoWidth(true);

        grid.addComponentColumn(this::createActions)
            .setHeader("Actions")
            .setAutoWidth(true);
    }

    private Component getStatusBadge(PrescriptionDto item) {
        String statusStr = item.getStatus() != null ? item.getStatus() : "ACTIVE";
        Span badge = new Span(statusStr);
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-size", "12px")
            .set("font-weight", "600")
            .set("display", "inline-block");

        if ("ACTIVE".equalsIgnoreCase(statusStr)) {
            badge.getStyle().set("background-color", "#e3f2fd").set("color", "#0288d1");
        } else if ("COMPLETED".equalsIgnoreCase(statusStr)) {
            badge.getStyle().set("background-color", "#e8f5e9").set("color", "#2e7d32");
        } else if ("CANCELLED".equalsIgnoreCase(statusStr)) {
            badge.getStyle().set("background-color", "#ffebee").set("color", "#c62828");
        } else {
            badge.getStyle().set("background-color", "#f5f5f5").set("color", "#616161");
        }
        return badge;
    }

    private Component createActions(PrescriptionDto item) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button viewBtn = new Button("View", new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        viewBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prescriptions/view/" + item.getId())));

        Button printBtn = new Button("Print", new Icon(VaadinIcon.PRINT));
        printBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        printBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prescriptions/view/" + item.getId())));

        Button deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> openDeleteConfirmationDialog(item));

        actions.add(viewBtn, printBtn, deleteBtn);
        return actions;
    }

    private void openDeleteConfirmationDialog(PrescriptionDto item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Prescription");
        dialog.setWidth("380px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        String displayId = item.getPrescriptionId() != null ? item.getPrescriptionId() : String.valueOf(item.getId());
        String patientName = item.getPatientName() != null ? item.getPatientName() : "Patient";
        Span warningText = new Span("Are you sure you want to delete prescription " + displayId + " for " + patientName + "? This action cannot be undone.");
        warningText.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-size", "14px");
        content.add(warningText);

        Button deleteBtn = new Button("Delete Permanently", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            try {
                prescriptionService.deletePrescription(item.getId());
                Notification.show("Prescription deleted successfully!", 3000, Notification.Position.TOP_CENTER);
                dialog.close();
                refreshData();
            } catch (Exception ex) {
                Notification.show("Error deleting prescription: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
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

        H1 title = new H1("All Patient Prescriptions");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button exportBtn = new Button("Export CSV", new Icon(VaadinIcon.DOWNLOAD));
        exportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportBtn.addClickListener(e -> exportCsv());

        header.add(title, exportBtn);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search by patient, doctor, diagnosis or ID...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGridList());
        searchField.setWidth("300px");

        statusFilter.setPlaceholder("Filter by status");
        statusFilter.setItems("ALL", "ACTIVE", "COMPLETED", "CANCELLED");
        statusFilter.setValue("ALL");
        statusFilter.addValueChangeListener(e -> updateGridList());

        bar.add(searchField, statusFilter);
        return bar;
    }

    private void refreshData() {
        List<PrescriptionDto> list = prescriptionService.getAllPrescriptions();
        allPrescriptions.clear();
        if (list != null) {
            allPrescriptions.addAll(list);
        }
        updateGridList();
    }

    private void updateGridList() {
        String query = searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
        String selectedStatus = statusFilter.getValue() != null ? statusFilter.getValue() : "ALL";

        List<PrescriptionDto> filtered = allPrescriptions.stream()
                .filter(p -> {
                    boolean matchesQuery = query.isEmpty() ||
                           (p.getPatientName() != null && p.getPatientName().toLowerCase().contains(query)) ||
                           (p.getDoctorName() != null && p.getDoctorName().toLowerCase().contains(query)) ||
                           (p.getDiagnosis() != null && p.getDiagnosis().toLowerCase().contains(query)) ||
                           (p.getPrescriptionId() != null && p.getPrescriptionId().toLowerCase().contains(query));

                    boolean matchesStatus = "ALL".equals(selectedStatus) ||
                           (p.getStatus() != null && p.getStatus().equalsIgnoreCase(selectedStatus));

                    return matchesQuery && matchesStatus;
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }

    private void exportCsv() {
        Notification.show("Exporting prescriptions list to CSV file...", 2000, Notification.Position.TOP_CENTER);
    }
}
