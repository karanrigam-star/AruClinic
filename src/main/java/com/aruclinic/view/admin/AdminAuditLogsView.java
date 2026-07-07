package com.aruclinic.view.admin;

import com.aruclinic.entity.AuditLog;
import com.aruclinic.service.AdminService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("System Audit Logs | AruClinic")
@Route(value = "admin/audit", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminAuditLogsView extends VerticalLayout {

    private final AdminService adminService;
    private final Grid<AuditLog> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public AdminAuditLogsView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-audit-view");

        configureGrid();
        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-audit-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(AuditLog::getId).setHeader("Log ID").setAutoWidth(true);
        grid.addColumn(AuditLog::getAction).setHeader("Action").setAutoWidth(true);
        grid.addColumn(AuditLog::getEntityType).setHeader("Module").setAutoWidth(true);
        grid.addColumn(AuditLog::getEntityId).setHeader("Record ID").setAutoWidth(true);
        grid.addColumn(l -> l.getPerformedBy() != null ? l.getPerformedBy().getEmail() : "System").setHeader("Performed By").setAutoWidth(true);
        grid.addColumn(l -> l.getPerformedAt() != null ? l.getPerformedAt().toString().replace("T", " ") : "N/A").setHeader("Timestamp").setAutoWidth(true);
        grid.addColumn(AuditLog::getDetails).setHeader("Details").setAutoWidth(true);
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Security Audit Trail");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search audit trail...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("350px");

        bar.add(searchField);
        return bar;
    }

    private void refreshGrid() {
        List<AuditLog> logs = adminService.getAuditLogs();
        logs.sort((a, b) -> b.getPerformedAt().compareTo(a.getPerformedAt()));

        String query = searchField.getValue().trim().toLowerCase();
        if (!query.isEmpty()) {
            logs = logs.stream().filter(l ->
                (l.getAction() != null && l.getAction().toLowerCase().contains(query)) ||
                (l.getEntityType() != null && l.getEntityType().toLowerCase().contains(query)) ||
                (l.getDetails() != null && l.getDetails().toLowerCase().contains(query)) ||
                (l.getPerformedBy() != null && l.getPerformedBy().getEmail().toLowerCase().contains(query))
            ).collect(Collectors.toList());
        }

        grid.setItems(logs);
    }
}
