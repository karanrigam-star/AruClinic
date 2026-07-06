package com.aruclinic.frontend.views.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.frontend.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Admin Prescriptions | AruClinic")
@Route(value = "admin/prescriptions", layout = MainLayout.class)
@CssImport("./themes/aruclinic/prescription.css")
public class AdminPrescriptionListView extends VerticalLayout {

    private final PrescriptionService prescriptionService;
    private final Grid<PrescriptionDto> grid = new Grid<>();
    private final List<PrescriptionDto> allPrescriptions = new ArrayList<>();
    private final TextField searchField = new TextField();

    public AdminPrescriptionListView(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-prescription-list-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        refreshData();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-prescription-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();

        grid.addColumn(PrescriptionDto::getPrescriptionId)
            .setHeader("ID")
            .setWidth("120px")
            .setFlexGrow(0)
            .setClassNameGenerator(item -> "aruclinic-id-column");
        grid.addColumn(PrescriptionDto::getPrescriptionDate).setHeader("Date").setWidth("120px").setFlexGrow(0);
        grid.addColumn(PrescriptionDto::getPatientName).setHeader("Patient").setWidth("180px").setFlexGrow(1);
        grid.addColumn(PrescriptionDto::getDoctorName).setHeader("Doctor").setWidth("180px").setFlexGrow(1);
        grid.addColumn(PrescriptionDto::getDiagnosis).setHeader("Diagnosis").setWidth("200px").setFlexGrow(1);
        grid.addComponentColumn(this::getStatusBadge).setHeader("Status").setWidth("110px").setFlexGrow(0);
        grid.addComponentColumn(this::createActions).setHeader("Actions").setWidth("180px").setFlexGrow(0);
    }

    private Component getStatusBadge(PrescriptionDto item) {
        Span badge = new Span(item.getStatus());
        badge.addClassName("status-badge");
        badge.addClassName(item.getStatus().toLowerCase());
        return badge;
    }

    private Component createActions(PrescriptionDto item) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewBtn.setTooltipText("View Details");
        viewBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prescriptions/view/" + item.getId())));

        Button printBtn = new Button(new Icon(VaadinIcon.PRINT));
        printBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        printBtn.setTooltipText("Print");
        printBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prescriptions/view/" + item.getId())));

        actions.add(viewBtn, printBtn);
        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("All Patient Prescriptions");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button exportBtn = new Button("Export CSV", new Icon(VaadinIcon.DOWNLOAD));
        exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportBtn.addClickListener(e -> exportCsv());

        header.add(title, exportBtn);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search prescriptions by patient name, doctor name, or diagnosis...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGridList());

        bar.add(searchField);
        return bar;
    }

    private void refreshData() {
        List<PrescriptionDto> list = prescriptionService.getAllPrescriptions();
        allPrescriptions.clear();
        allPrescriptions.addAll(list);
        updateGridList();
    }

    private void updateGridList() {
        String query = searchField.getValue().trim().toLowerCase();
        List<PrescriptionDto> filtered = allPrescriptions.stream()
                .filter(p -> {
                    if (query.isEmpty()) return true;
                    return p.getPatientName().toLowerCase().contains(query) ||
                           p.getDoctorName().toLowerCase().contains(query) ||
                           p.getDiagnosis().toLowerCase().contains(query) ||
                           p.getPrescriptionId().toLowerCase().contains(query);
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }

    private void exportCsv() {
        Notification.show("Exporting prescriptions list to CSV file...", 2000, Notification.Position.TOP_CENTER);
    }
}
