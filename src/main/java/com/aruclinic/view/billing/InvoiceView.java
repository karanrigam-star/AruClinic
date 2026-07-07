package com.aruclinic.view.billing;

import com.aruclinic.dto.BillDto;
import com.aruclinic.dto.BillItemDto;
import com.aruclinic.service.BillingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.aruclinic.util.PdfHelper;
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
 * Invoice View for displaying invoice details.
 */
@PageTitle("Invoice | AruClinic")
@Route("patient/billing/invoice")
@CssImport("./themes/aruclinic/billing.css")
public class InvoiceView extends VerticalLayout {

    private final BillingService billingService;
    private BillDto invoice;

    public InvoiceView(BillingService billingService) {
        this.billingService = billingService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Load invoice data
        loadInvoiceData();

        add(createInvoiceContent());
    }

    private void loadInvoiceData() {
        // In a real app, this would fetch the invoice from the service based on the route parameter
        invoice = new BillDto();
        invoice.setInvoiceId("INV-001");
        invoice.setInvoiceDate(LocalDate.of(2025, 6, 1));
        invoice.setDueDate(LocalDate.of(2025, 6, 15));
        invoice.setPatientName("John Doe");
        invoice.setPatientId("PAT-001");
        invoice.setDoctorName("Dr. Smith");
        invoice.setDescription("Consultation and Blood Test");
        invoice.setStatus("PAID");
        invoice.setSubtotal(new BigDecimal("250.00"));
        invoice.setTax(new BigDecimal("25.00"));
        invoice.setDiscount(new BigDecimal("0.00"));
        invoice.setTotal(new BigDecimal("275.00"));
    }

    private Component createInvoiceContent() {
        Div content = new Div();
        content.addClassName("aruclinic-invoice-view");
        content.setWidthFull();

        // Invoice card
        content.add(createInvoiceCard());

        return content;
    }

    private Component createInvoiceCard() {
        Div card = new Div();
        card.addClassName("aruclinic-invoice-card");
        card.setWidthFull();

        // Header with logo and invoice info
        card.add(createInvoiceHeader());

        // Divider
        Div divider = new Div();
        divider.addClassName("aruclinic-divider");

        // Meta information
        card.add(createInvoiceMeta());

        // Divider
        Div divider2 = new Div();
        divider2.addClassName("aruclinic-divider");
        card.add(divider2);

        // Items table
        card.add(createInvoiceItems());

        // Summary
        card.add(createInvoiceSummary());

        // Footer
        card.add(createInvoiceFooter());

        return card;
    }

    private Component createInvoiceHeader() {
        Div header = new Div();
        header.addClassName("aruclinic-invoice-header-info");

        // Logo section
        Div logoSection = new Div();
        logoSection.addClassName("aruclinic-invoice-logo-section");

        H1 logo = new H1("AruClinic");
        logo.addClassName("aruclinic-invoice-logo");

        Div companyInfo = new Div();
        companyInfo.addClassName("aruclinic-invoice-company-info");
        companyInfo.setText("123 Healthcare Street\nNew York, NY 10001\nPhone: (123) 456-7890\nEmail: info@aruclinic.com");

        logoSection.add(logo, companyInfo);

        // Invoice details section
        Div detailsSection = new Div();
        detailsSection.addClassName("aruclinic-invoice-details-section");

        H2 invoiceTitle = new H2("INVOICE");
        invoiceTitle.addClassName("aruclinic-invoice-title");

        Span invoiceNumber = new Span("#" + invoice.getId());
        invoiceNumber.addClassName("aruclinic-invoice-number");

        detailsSection.add(invoiceTitle, invoiceNumber);

        header.add(logoSection, detailsSection);
        return header;
    }

    private Component createInvoiceMeta() {
        Div meta = new Div();
        meta.addClassName("aruclinic-invoice-meta");

        // Left side - Patient info
        Div patientInfo = new Div();
        patientInfo.addClassName("aruclinic-invoice-meta-group");

        Span patientLabel = new Span("Bill To:");
        patientLabel.addClassName("aruclinic-invoice-meta-label");

        Span patientValue = new Span(invoice.getPatientName() + " (ID: " + invoice.getPatientId() + ")");
        patientValue.addClassName("aruclinic-invoice-meta-value");

        patientInfo.add(patientLabel, patientValue);

        // Right side - Invoice dates
        Div dateInfo = new Div();
        dateInfo.addClassName("aruclinic-invoice-meta-group");

        Span dateLabel = new Span("Invoice Date:");
        dateLabel.addClassName("aruclinic-invoice-meta-label");

        Span dateValue = new Span(invoice.getInvoiceDate().toString());
        dateValue.addClassName("aruclinic-invoice-meta-value");

        Span dueLabel = new Span("Due Date:");
        dueLabel.addClassName("aruclinic-invoice-meta-label");

        Span dueValue = new Span(invoice.getDueDate().toString());
        dueValue.addClassName("aruclinic-invoice-meta-value");

        dateInfo.add(dateLabel, dateValue, dueLabel, dueValue);

        meta.add(patientInfo, dateInfo);
        return meta;
    }

    private Component createInvoiceItems() {
        Div items = new Div();
        items.addClassName("aruclinic-invoice-items");

        // Header row
        Div headerRow = new Div();
        headerRow.addClassName("aruclinic-invoice-items-header");

        Span descriptionHeader = new Span("Description");
        Span quantityHeader = new Span("Qty");
        Span unitPriceHeader = new Span("Unit Price");
        Span amountHeader = new Span("Amount");

        headerRow.add(descriptionHeader, quantityHeader, unitPriceHeader, amountHeader);
        items.add(headerRow);

        // Item rows
        List<BillItemDto> itemList = getSampleItems();
        for (BillItemDto item : itemList) {
            items.add(createInvoiceItemRow(item));
        }

        return items;
    }

    private Component createInvoiceItemRow(BillItemDto item) {
        Div row = new Div();
        row.addClassName("aruclinic-invoice-items-row");

        Span description = new Span(item.getDescription());
        description.addClassName("aruclinic-invoice-items-cell");
        description.addClassName("description");

        Span quantity = new Span(String.valueOf(item.getQuantity()));
        quantity.addClassName("aruclinic-invoice-items-cell");
        quantity.addClassName("quantity");

        Span unitPrice = new Span("₹" + item.getUnitPrice().toString());
        unitPrice.addClassName("aruclinic-invoice-items-cell");
        unitPrice.addClassName("unit-price");

        Span amount = new Span("₹" + item.getAmount().toString());
        amount.addClassName("aruclinic-invoice-items-cell");
        amount.addClassName("amount");

        row.add(description, quantity, unitPrice, amount);
        return row;
    }

    private List<BillItemDto> getSampleItems() {
        BillItemDto item1 = new BillItemDto();
        item1.setDescription("Consultation with Dr. Smith");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("150.00"));
        item1.setAmount(new BigDecimal("150.00"));

        BillItemDto item2 = new BillItemDto();
        item2.setDescription("Blood Test - Complete Panel");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("100.00"));
        item2.setAmount(new BigDecimal("100.00"));

        return Arrays.asList(item1, item2);
    }

    private Component createInvoiceSummary() {
        Div summary = new Div();
        summary.addClassName("aruclinic-invoice-summary");

        H2 summaryTitle = new H2("Summary");
        summaryTitle.addClassName("aruclinic-invoice-summary-title");

        // Subtotal
        Div subtotalRow = new Div();
        subtotalRow.addClassName("aruclinic-invoice-summary-row");

        Span subtotalLabel = new Span("Subtotal:");
        subtotalLabel.addClassName("aruclinic-invoice-summary-label");

        Span subtotalValue = new Span("₹" + invoice.getSubtotal().toString());
        subtotalValue.addClassName("aruclinic-invoice-summary-value");

        subtotalRow.add(subtotalLabel, subtotalValue);

        // Tax
        Div taxRow = new Div();
        taxRow.addClassName("aruclinic-invoice-summary-row");

        Span taxLabel = new Span("Tax (10%):");
        taxLabel.addClassName("aruclinic-invoice-summary-label");

        Span taxValue = new Span("₹" + invoice.getTax().toString());
        taxValue.addClassName("aruclinic-invoice-summary-value");

        taxRow.add(taxLabel, taxValue);

        // Discount
        Div discountRow = new Div();
        discountRow.addClassName("aruclinic-invoice-summary-row");

        Span discountLabel = new Span("Discount:");
        discountLabel.addClassName("aruclinic-invoice-summary-label");

        Span discountValue = new Span("-" + "₹" + invoice.getDiscount().toString());
        discountValue.addClassName("aruclinic-invoice-summary-value");

        discountRow.add(discountLabel, discountValue);

        // Total
        Div totalRow = new Div();
        totalRow.addClassName("aruclinic-invoice-summary-total");

        Span totalLabel = new Span("Total:");
        totalLabel.addClassName("aruclinic-invoice-summary-label");

        Span totalValue = new Span("₹" + invoice.getTotal().toString());
        totalValue.addClassName("aruclinic-invoice-summary-value");

        totalRow.add(totalLabel, totalValue);

        summary.add(summaryTitle, subtotalRow, taxRow, discountRow, totalRow);
        return summary;
    }

    private Component createInvoiceFooter() {
        Div footer = new Div();
        footer.addClassName("aruclinic-invoice-footer");

        // Status
        Div statusSection = new Div();

        Span statusLabel = new Span("Status:");
        statusLabel.addClassName("aruclinic-invoice-footer-label");

        Span statusBadge = new Span(invoice.getStatus());
        statusBadge.addClassName("aruclinic-billing-status-badge");
        statusBadge.addClassName(invoice.getStatus().toLowerCase());

        statusSection.add(statusLabel, statusBadge);

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-invoice-footer-actions");

        Button printBtn = new Button("Print", new Icon(VaadinIcon.PRINT));
        printBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        printBtn.addClassName("aruclinic-btn");
        printBtn.addClassName("aruclinic-btn-primary");
        printBtn.addClickListener(e -> printInvoice());

        Anchor downloadAnchor = new Anchor(new StreamResource("Invoice.pdf", () -> {
            return PdfHelper.generateInvoicePdf(
                "INV-" + invoice.getInvoiceId(),
                invoice.getPatientName(),
                invoice.getDoctorName(),
                invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : "",
                "₹" + invoice.getTotal().toString()
            );
        }), "");
        downloadAnchor.getElement().setAttribute("download", true);
        Button downloadBtn = new Button("Download PDF", new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadBtn.addClassName("aruclinic-btn");
        downloadBtn.addClassName("aruclinic-btn-secondary");
        downloadAnchor.add(downloadBtn);

        Button payBtn = new Button("Pay Now", new Icon(VaadinIcon.CREDIT_CARD));
        payBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        payBtn.addClassName("aruclinic-btn");
        payBtn.addClassName("aruclinic-btn-success");
        payBtn.addClickListener(e -> payInvoice());
        payBtn.setVisible(invoice.getStatus().equals("PENDING"));

        actions.add(printBtn, downloadAnchor);
        if (invoice.getStatus().equals("PENDING")) {
            actions.add(payBtn);
        }

        footer.add(statusSection, actions);
        return footer;
    }

    private void printInvoice() {
        com.vaadin.flow.component.notification.Notification.show(
            "Printing invoice...",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }

    private void downloadInvoice() {
        com.vaadin.flow.component.notification.Notification.show(
            "Downloading invoice as PDF...",
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }

    private void payInvoice() {
        getUI().ifPresent(ui -> ui.navigate("patient/billing/pay/" + invoice.getInvoiceId()));
    }
}
