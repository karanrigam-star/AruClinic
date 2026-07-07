package com.aruclinic.view.admin;

import com.aruclinic.entity.Bill;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.aruclinic.util.PdfHelper;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Billing Module | AruClinic")
@Route(value = "admin/billing", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminBillingView extends VerticalLayout {

    private final AdminService adminService;
    private final Grid<Bill> grid = new Grid<>();
    private final Select<String> statusFilter = new Select<>();

    public AdminBillingView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-billing-view");

        configureGrid();
        add(createHeader(), createSummaryCards(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-billing-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("500px");

        grid.addColumn(b -> "INV-" + b.getId()).setHeader("Invoice ID").setAutoWidth(true);
        grid.addColumn(b -> b.getPatient() != null ? b.getPatient().getFirstName() + " " + b.getPatient().getLastName() : "N/A")
            .setHeader("Patient").setAutoWidth(true);
        grid.addColumn(b -> b.getDoctor() != null ? "Dr. " + b.getDoctor().getName() : "N/A")
            .setHeader("Doctor").setAutoWidth(true);
        grid.addColumn(b -> b.getInvoiceDate() != null ? b.getInvoiceDate().toString() : "N/A")
            .setHeader("Invoice Date").setAutoWidth(true);
        grid.addColumn(Bill::getAmount).setHeader("Subtotal (₹)").setAutoWidth(true);
        grid.addColumn(Bill::getTax).setHeader("Tax (₹)").setAutoWidth(true);
        grid.addColumn(Bill::getTotal).setHeader("Total Amount (₹)").setAutoWidth(true);
        grid.addColumn(Bill::getStatus).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true);
    }

    private Component createActions(Bill bill) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        if ("UNPAID".equalsIgnoreCase(bill.getStatus())) {
            Button payBtn = new Button("Record Payment", new Icon(VaadinIcon.MONEY));
            payBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            payBtn.addClickListener(e -> {
                adminService.payBill(bill.getId());
                Notification.show("Payment recorded successfully", 2000, Notification.Position.TOP_CENTER);
                refreshGrid();
            });
            actions.add(payBtn);
        } else {
            Span paidLabel = new Span("Paid");
            paidLabel.getStyle().set("color", "var(--aruclinic-success)").set("font-weight", "600");
            actions.add(paidLabel);
        }

        Anchor pdfAnchor = new Anchor(new StreamResource("Invoice_" + bill.getId() + ".pdf", () -> {
            String pName = bill.getPatient() != null ? bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName() : "Patient";
            String dName = bill.getDoctor() != null ? bill.getDoctor().getName() : "AruClinic";
            String date = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : java.time.LocalDate.now().toString();
            return PdfHelper.generateInvoicePdf("INV-" + bill.getId(), pName, dName, date, "₹" + bill.getTotal().toString());
        }), "");
        pdfAnchor.getElement().setAttribute("download", true);
        Button pdfBtn = new Button("PDF", new Icon(VaadinIcon.DOWNLOAD));
        pdfBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        pdfAnchor.add(pdfBtn);
        actions.add(pdfAnchor);

        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Invoicing & Billing");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button addBtn = new Button("Generate Invoice", new Icon(VaadinIcon.PLUS));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openGenerateInvoiceDialog());

        header.add(title, addBtn);
        return header;
    }

    private Component createSummaryCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.getStyle().set("margin", "16px 0");

        double revenueToday = adminService.getRevenueToday();
        double revenueMonth = adminService.getRevenueThisMonth();
        long pendingCount = adminService.getPendingBillsCount();

        layout.add(createSummaryCard("Revenue Today", "₹" + String.format("%.2f", revenueToday), "Paid Invoices"));
        layout.add(createSummaryCard("Revenue This Month", "₹" + String.format("%.2f", revenueMonth), "Month-to-Date"));
        layout.add(createSummaryCard("Pending Invoices", String.valueOf(pendingCount), "Unpaid Invoices"));

        return layout;
    }

    private Component createSummaryCard(String title, String value, String desc) {
        Div card = new Div();
        card.getStyle().set("background", "#F8FAFC").set("border-radius", "8px").set("padding", "16px").set("flex", "1");
        
        Span t = new Span(title);
        t.getStyle().set("font-size", "12px").set("color", "#64748B");
        
        Div v = new Div();
        v.setText(value);
        v.getStyle().set("font-size", "22px").set("font-weight", "700").set("margin", "4px 0");

        Span d = new Span(desc);
        d.getStyle().set("font-size", "11px").set("color", "#94A3B8");

        card.add(t, v, d);
        return card;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        statusFilter.setPlaceholder("Filter by Billing Status");
        statusFilter.setItems("ALL", "PAID", "UNPAID");
        statusFilter.setValue("ALL");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        bar.add(statusFilter);
        return bar;
    }

    private void openGenerateInvoiceDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Generate Invoice");
        dialog.setWidth("450px");

        FormLayout form = new FormLayout();
        Select<Patient> patientSelect = new Select<>();
        patientSelect.setLabel("Patient");
        patientSelect.setItems(adminService.getAllPatients());
        patientSelect.setItemLabelGenerator(p -> p.getFirstName() + " " + p.getLastName() + " (" + p.getEmail() + ")");

        Select<Doctor> doctorSelect = new Select<>();
        doctorSelect.setLabel("Doctor");
        doctorSelect.setItems(adminService.getAllDoctors());
        doctorSelect.setItemLabelGenerator(d -> "Dr. " + d.getName());

        BigDecimalField amount = new BigDecimalField("Subtotal Amount (₹)");
        BigDecimalField tax = new BigDecimalField("Tax Amount (₹)");
        tax.setValue(BigDecimal.ZERO);

        DatePicker date = new DatePicker("Invoice Date");
        date.setValue(LocalDate.now());

        form.add(patientSelect, doctorSelect, amount, tax, date);

        Button saveBtn = new Button("Generate", e -> {
            if (patientSelect.getValue() == null || doctorSelect.getValue() == null || amount.getValue() == null) {
                Notification.show("Please fill in all details", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            BigDecimal subtotal = amount.getValue();
            BigDecimal taxAmount = tax.getValue() != null ? tax.getValue() : BigDecimal.ZERO;
            BigDecimal totalAmount = subtotal.add(taxAmount);

            Bill b = new Bill();
            b.setPatient(patientSelect.getValue());
            b.setDoctor(doctorSelect.getValue());
            b.setAmount(subtotal);
            b.setTax(taxAmount);
            b.setTotal(totalAmount);
            b.setInvoiceDate(date.getValue());
            b.setStatus("UNPAID");
            b.setInvoiceNumber("INV-" + System.currentTimeMillis());

            adminService.createBill(b);
            Notification.show("Invoice created successfully!", 2000, Notification.Position.TOP_CENTER);
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
        List<Bill> bills = adminService.getAllBills();

        String status = statusFilter.getValue();
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            bills = bills.stream().filter(b -> b.getStatus() != null && b.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
        }

        grid.setItems(bills);
    }
}
