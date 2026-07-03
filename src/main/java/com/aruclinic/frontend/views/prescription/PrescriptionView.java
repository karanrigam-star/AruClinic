package com.aruclinic.frontend.views.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.service.PrescriptionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Prescription List view for displaying all prescriptions.
 */
@PageTitle("Prescriptions | AruClinic")
@Route("patient/prescriptions")
@CssImport("./themes/aruclinic/prescription.css")
public class PrescriptionView extends VerticalLayout {

    private final PrescriptionService prescriptionService;
    private final Grid<PrescriptionDto> prescriptionGrid = new Grid<>();

    public PrescriptionView(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        add(createPrescriptionListContent());
    }

    private void configureGrid() {
        prescriptionGrid.addClassName("aruclinic-prescription-grid");
        prescriptionGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        prescriptionGrid.setHeight("auto");
        prescriptionGrid.setWidthFull();

        // Add columns
        prescriptionGrid.addColumn(PrescriptionDto::getPrescriptionId)
            .setHeader("Prescription ID")
            .setWidth("150px")
            .setFlexGrow(0);

        prescriptionGrid.addColumn(PrescriptionDto::getPrescriptionDate)
            .setHeader("Date")
            .setWidth("120px")
            .setFlexGrow(0);

        prescriptionGrid.addColumn(PrescriptionDto::getDoctorName)
            .setHeader("Doctor")
            .setWidth("180px")
            .setFlexGrow(0);

        prescriptionGrid.addColumn(PrescriptionDto::getDiagnosis)
            .setHeader("Diagnosis")
            .setWidth("250px")
            .setFlexGrow(0);

        prescriptionGrid.addColumn(this::getStatusBadge)
            .setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0);

        prescriptionGrid.addColumn(this::createActionButtons)
            .setHeader("Actions")
            .setWidth("150px")
            .setFlexGrow(0);

        // Set items (in a real app, this would come from the service)
        prescriptionGrid.setItems(getSamplePrescriptions());
    }

    private Component getStatusBadge(PrescriptionDto prescription) {
        Span badge = new Span(prescription.getStatus());
        badge.addClassName("aruclinic-prescription-status-badge");
        badge.addClassName(prescription.getStatus().toLowerCase());
        return badge;
    }

    private Component createActionButtons(PrescriptionDto prescription) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addClassName("aruclinic-prescription-action-btn");
        viewBtn.addClassName("view");
        viewBtn.addClickListener(e -> viewPrescription(prescription.getPrescriptionId()));

        Button printBtn = new Button(new Icon(VaadinIcon.PRINT));
        printBtn.addClassName("aruclinic-prescription-action-btn");
        printBtn.addClassName("print");
        printBtn.addClickListener(e -> printPrescription(prescription.getPrescriptionId()));

        actions.add(viewBtn, printBtn);
        return actions;
    }

    private List<PrescriptionDto> getSamplePrescriptions() {
        PrescriptionDto p1 = new PrescriptionDto();
        p1.setPrescriptionId("PRESC-001");
        p1.setPrescriptionDate(LocalDate.of(2025, 6, 1));
        p1.setDoctorName("Dr. Smith");
        p1.setDiagnosis("Hypertension");
        p1.setStatus("Active");

        PrescriptionDto p2 = new PrescriptionDto();
        p2.setPrescriptionId("PRESC-002");
        p2.setPrescriptionDate(LocalDate.of(2025, 5, 15));
        p2.setDoctorName("Dr. Johnson");
        p2.setDiagnosis("Diabetes");
        p2.setStatus("Active");

        PrescriptionDto p3 = new PrescriptionDto();
        p3.setPrescriptionId("PRESC-003");
        p3.setPrescriptionDate(LocalDate.of(2025, 4, 20));
        p3.setDoctorName("Dr. Smith");
        p3.setDiagnosis("Common Cold");
        p3.setStatus("Completed");

        PrescriptionDto p4 = new PrescriptionDto();
        p4.setPrescriptionId("PRESC-004");
        p4.setPrescriptionDate(LocalDate.of(2025, 3, 10));
        p4.setDoctorName("Dr. Williams");
        p4.setDiagnosis("Allergy");
        p4.setStatus("Expired");

        return Arrays.asList(p1, p2, p3, p4);
    }

    private Component createPrescriptionListContent() {
        Div content = new Div();
        content.addClassName("aruclinic-prescription-list");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-prescription-list-header");

        H1 title = new H1("My Prescriptions");
        title.addClassName("aruclinic-prescription-list-title");

        Button newPrescriptionBtn = new Button("New Prescription", new Icon(VaadinIcon.PLUS));
        newPrescriptionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newPrescriptionBtn.addClassName("aruclinic-btn");
        newPrescriptionBtn.addClassName("aruclinic-btn-primary");
        newPrescriptionBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/prescriptions/add")));

        header.add(title, newPrescriptionBtn);

        // Stats
        Div stats = new Div();
        stats.addClassName("aruclinic-prescription-stats");

        stats.add(createStatCard("Total Prescriptions", "12", "primary"));
        stats.add(createStatCard("Active", "5", "success"));
        stats.add(createStatCard("Completed", "5", "warning"));
        stats.add(createStatCard("Expired", "2", "danger"));

        // Filter bar
        Div filterBar = new Div();
        filterBar.addClassName("aruclinic-prescription-filter");

        // In a real app, you would add filter controls here
        Span filterLabel = new Span("Filter prescriptions by status, date, or doctor");
        filterLabel.addClassName("aruclinic-prescription-filter-label");
        filterBar.add(filterLabel);

        // Grid
        content.add(header, stats, filterBar, prescriptionGrid);
        return content;
    }

    private Component createStatCard(String label, String value, String type) {
        Div statCard = new Div();
        statCard.addClassName("aruclinic-prescription-stat");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-prescription-stat-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("aruclinic-prescription-stat-value");

        statCard.add(labelSpan, valueSpan);
        return statCard;
    }

    private void viewPrescription(String prescriptionId) {
        getUI().ifPresent(ui -> ui.navigate("patient/prescriptions/" + prescriptionId));
    }

    private void printPrescription(String prescriptionId) {
        com.vaadin.flow.component.notification.Notification.show(
            "Printing prescription: " + prescriptionId,
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }
}
