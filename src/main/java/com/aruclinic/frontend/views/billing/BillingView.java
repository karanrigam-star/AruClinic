package com.aruclinic.frontend.views.billing;

import com.aruclinic.dto.BillDto;
import com.aruclinic.service.BillingService;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Billing List view for displaying all bills and invoices.
 */
@PageTitle("Billing | AruClinic")
@Route("patient/billing")
@CssImport("./themes/aruclinic/billing.css")
public class BillingView extends VerticalLayout {

    private final BillingService billingService;
    private final Grid<BillDto> billGrid = new Grid<>();

    public BillingView(BillingService billingService) {
        this.billingService = billingService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        add(createBillingContent());
    }

    private void configureGrid() {
        billGrid.addClassName("aruclinic-billing-table");
        billGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        billGrid.setHeight("auto");
        billGrid.setWidthFull();

        // Add columns
        billGrid.addColumn(BillDto::getInvoiceId)
            .setHeader("Invoice ID")
            .setWidth("150px")
            .setFlexGrow(0);

        billGrid.addColumn(BillDto::getInvoiceDate)
            .setHeader("Date")
            .setWidth("120px")
            .setFlexGrow(0);

        billGrid.addColumn(BillDto::getDescription)
            .setHeader("Description")
            .setWidth("300px")
            .setFlexGrow(0);

        billGrid.addColumn(bill -> bill.getAmount() != null ? "$" + bill.getAmount().toString() : "")
            .setHeader("Amount")
            .setWidth("120px")
            .setFlexGrow(0);

        billGrid.addColumn(this::getStatusBadge)
            .setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0);

        billGrid.addColumn(this::createActionButtons)
            .setHeader("Actions")
            .setWidth("150px")
            .setFlexGrow(0);

        // Set items (in a real app, this would come from the service)
        billGrid.setItems(getSampleBills());
    }

    private Component getStatusBadge(BillDto bill) {
        Span badge = new Span(bill.getStatus());
        badge.addClassName("aruclinic-billing-status-badge");
        badge.addClassName(bill.getStatus().toLowerCase());
        return badge;
    }

    private Component createActionButtons(BillDto bill) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addClassName("aruclinic-billing-action-btn-view");
        viewBtn.addClickListener(e -> viewInvoice(bill.getInvoiceId()));

        Button payBtn = new Button(new Icon(VaadinIcon.CREDIT_CARD));
        payBtn.addClassName("aruclinic-billing-action-btn-edit");
        payBtn.addClickListener(e -> payInvoice(bill.getInvoiceId()));
        payBtn.setVisible(bill.getStatus().equals("PENDING"));

        Button printBtn = new Button(new Icon(VaadinIcon.PRINT));
        printBtn.addClassName("aruclinic-billing-action-btn-print");
        printBtn.addClickListener(e -> printInvoice(bill.getInvoiceId()));

        actions.add(viewBtn);
        if (bill.getStatus().equals("PENDING")) {
            actions.add(payBtn);
        }
        actions.add(printBtn);

        return actions;
    }

    private List<BillDto> getSampleBills() {
        BillDto b1 = new BillDto();
        b1.setInvoiceId("INV-001");
        b1.setInvoiceDate(LocalDate.of(2025, 6, 1));
        b1.setDescription("Consultation with Dr. Smith");
        b1.setAmount(new BigDecimal("150.00"));
        b1.setStatus("PAID");

        BillDto b2 = new BillDto();
        b2.setInvoiceId("INV-002");
        b2.setInvoiceDate(LocalDate.of(2025, 5, 15));
        b2.setDescription("Blood Test and X-Ray");
        b2.setAmount(new BigDecimal("250.00"));
        b2.setStatus("PAID");

        BillDto b3 = new BillDto();
        b3.setInvoiceId("INV-003");
        b3.setInvoiceDate(LocalDate.of(2025, 5, 20));
        b3.setDescription("Follow-up Visit");
        b3.setAmount(new BigDecimal("100.00"));
        b3.setStatus("PENDING");

        BillDto b4 = new BillDto();
        b4.setInvoiceId("INV-004");
        b4.setInvoiceDate(LocalDate.of(2025, 4, 10));
        b4.setDescription("Annual Physical Exam");
        b4.setAmount(new BigDecimal("200.00"));
        b4.setStatus("PAID");

        return Arrays.asList(b1, b2, b3, b4);
    }

    private Component createBillingContent() {
        Div content = new Div();
        content.addClassName("aruclinic-billing-list");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-billing-list-header");

        H1 title = new H1("My Billing");
        title.addClassName("aruclinic-billing-list-title");

        Button newBillBtn = new Button("New Invoice", new Icon(VaadinIcon.PLUS));
        newBillBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBillBtn.addClassName("aruclinic-btn");
        newBillBtn.addClassName("aruclinic-btn-primary");
        newBillBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/billing/add")));

        header.add(title, newBillBtn);

        // Stats
        Div stats = new Div();
        stats.addClassName("aruclinic-billing-stats");

        stats.add(createStatCard("Total Invoices", "15", "primary"));
        stats.add(createStatCard("Paid", "12", "success"));
        stats.add(createStatCard("Pending", "2", "warning"));
        stats.add(createStatCard("Overdue", "1", "danger"));

        // Filter bar
        Div filterBar = new Div();
        filterBar.addClassName("aruclinic-billing-filter");

        // In a real app, you would add filter controls here
        Span filterLabel = new Span("Filter invoices by status, date, or amount");
        filterLabel.addClassName("aruclinic-billing-filter-label");
        filterBar.add(filterLabel);

        // Grid
        content.add(header, stats, filterBar, billGrid);
        return content;
    }

    private Component createStatCard(String label, String value, String type) {
        Div statCard = new Div();
        statCard.addClassName("aruclinic-billing-stat");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("aruclinic-billing-stat-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("aruclinic-billing-stat-value");
        valueSpan.addClassName(type);

        statCard.add(labelSpan, valueSpan);
        return statCard;
    }

    private void viewInvoice(String invoiceId) {
        getUI().ifPresent(ui -> ui.navigate("patient/billing/" + invoiceId));
    }

    private void payInvoice(String invoiceId) {
        getUI().ifPresent(ui -> ui.navigate("patient/billing/pay/" + invoiceId));
    }

    private void printInvoice(String invoiceId) {
        com.vaadin.flow.component.notification.Notification.show(
            "Printing invoice: " + invoiceId,
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }
}
