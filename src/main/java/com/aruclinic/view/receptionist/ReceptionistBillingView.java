package com.aruclinic.view.receptionist;

import com.aruclinic.entity.Bill;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.service.AdminService;
import com.aruclinic.service.PrescriptionService;
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receptionist-facing Invoicing & Billing view mapped to receptionist/billing.
 * Fully wired with realtime billing, payment method selections, dynamic UPI QR preview, and double side-by-side grids.
 */
@PageTitle("Billing Module | AruClinic")
@Route(value = "receptionist/billing", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class ReceptionistBillingView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final AdminService adminService;
    private final PrescriptionService prescriptionService;
    private final Grid<Bill> grid = new Grid<>();
    private final Grid<Appointment> pendingGrid = new Grid<>();
    private final Select<String> statusFilter = new Select<>();

    public ReceptionistBillingView(AdminService adminService, PrescriptionService prescriptionService) {
        this.adminService = adminService;
        this.prescriptionService = prescriptionService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-receptionist-billing-view");

        configureGrid();
        configurePendingGrid();

        add(createHeader(), createSummaryCards(), createFilterAndTablesLayout());
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-billing-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("450px");

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

    private void configurePendingGrid() {
        pendingGrid.addClassName("aruclinic-pending-billing-grid");
        pendingGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        pendingGrid.setHeight("450px");

        pendingGrid.addColumn(a -> a.getPatient() != null ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "N/A")
                .setHeader("Patient Name").setAutoWidth(true);
        pendingGrid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A")
                .setHeader("Doctor").setAutoWidth(true);
        pendingGrid.addColumn(a -> a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "N/A")
                .setHeader("Date").setAutoWidth(true);
        pendingGrid.addColumn(Appointment::getReason).setHeader("Reason").setAutoWidth(true);
        
        pendingGrid.addComponentColumn(a -> {
            Button invoiceBtn = new Button("Generate Invoice", new Icon(VaadinIcon.FILE_ADD));
            invoiceBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            invoiceBtn.addClickListener(e -> openGenerateInvoiceDialog(a));
            return invoiceBtn;
        }).setHeader("Action").setAutoWidth(true);
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
                Notification.show("Payment recorded successfully. Admin notified.", 2000, Notification.Position.TOP_CENTER);
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
            String date = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : LocalDate.now().toString();
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
        long pendingCount = adminService.getPendingBillsCount();

        // Displays Revenue Today and Pending Invoices; removes Revenue This Month
        layout.add(createSummaryCard("Revenue Today", "₹" + String.format("%.2f", revenueToday), "Paid Invoices"));
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

    private Component createFilterAndTablesLayout() {
        HorizontalLayout tablesLayout = new HorizontalLayout();
        tablesLayout.setSizeFull();
        tablesLayout.setSpacing(true);
        tablesLayout.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        // Left Container (Pending Invoices Grid)
        VerticalLayout leftContainer = new VerticalLayout();
        leftContainer.setPadding(false);
        leftContainer.setSpacing(true);
        leftContainer.getStyle()
            .set("background", "var(--aruclinic-card-bg)")
            .set("border", "1px solid var(--aruclinic-border)")
            .set("border-radius", "var(--aruclinic-radius-md)")
            .set("padding", "var(--aruclinic-spacing-lg)")
            .set("box-shadow", "var(--aruclinic-shadow-md)");

        H2 leftTitle = new H2("Pending Invoices (Completed Appointments)");
        leftTitle.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("font-weight", "600")
            .set("margin", "0")
            .set("color", "var(--aruclinic-text-primary)");
        
        leftContainer.add(leftTitle, pendingGrid);

        // Right Container (All Invoices Grid with Status Filter)
        VerticalLayout rightContainer = new VerticalLayout();
        rightContainer.setPadding(false);
        rightContainer.setSpacing(true);
        rightContainer.getStyle()
            .set("background", "var(--aruclinic-card-bg)")
            .set("border", "1px solid var(--aruclinic-border)")
            .set("border-radius", "var(--aruclinic-radius-md)")
            .set("padding", "var(--aruclinic-spacing-lg)")
            .set("box-shadow", "var(--aruclinic-shadow-md)");

        H2 rightTitle = new H2("All Invoices");
        rightTitle.getStyle()
            .set("font-size", "var(--aruclinic-font-size-lg)")
            .set("font-weight", "600")
            .set("margin", "0")
            .set("color", "var(--aruclinic-text-primary)");

        statusFilter.setPlaceholder("Filter by Billing Status");
        statusFilter.setItems("ALL", "PAID", "UNPAID");
        statusFilter.setValue("ALL");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout rightHeader = new HorizontalLayout(rightTitle, statusFilter);
        rightHeader.setWidthFull();
        rightHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        rightHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        rightContainer.add(rightHeader, grid);

        tablesLayout.add(leftContainer, rightContainer);
        tablesLayout.setFlexGrow(1.0, leftContainer, rightContainer);

        return tablesLayout;
    }

    private void updateQrCode(Image qrCodeImage, BigDecimalField amount, BigDecimalField tax, Select<String> methodSelect) {
        if ("UPI".equals(methodSelect.getValue())) {
            BigDecimal subtotal = amount.getValue() != null ? amount.getValue() : BigDecimal.ZERO;
            BigDecimal taxAmt = tax.getValue() != null ? tax.getValue() : BigDecimal.ZERO;
            BigDecimal total = subtotal.add(taxAmt);
            
            try {
                String payload = "upi://pay?pa=aruclinic@upi&pn=AruClinic&am=" + total + "&cu=INR&tn=Invoice";
                String encodedPayload = java.net.URLEncoder.encode(payload, java.nio.charset.StandardCharsets.UTF_8.toString());
                String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + encodedPayload;
                
                qrCodeImage.setSrc(qrUrl);
                qrCodeImage.setAlt("UPI Payment QR Code");
                qrCodeImage.setWidth("150px");
                qrCodeImage.setHeight("150px");
                qrCodeImage.getStyle().set("display", "block").set("margin", "12px auto");
                qrCodeImage.setVisible(true);
            } catch (Exception ex) {
                qrCodeImage.setVisible(false);
            }
        } else {
            qrCodeImage.setVisible(false);
        }
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

        Select<String> methodSelect = new Select<>();
        methodSelect.setLabel("Payment Method");
        methodSelect.setItems("Cash", "Card", "UPI", "Net Banking");
        methodSelect.setValue("Cash");

        Select<String> payStatusSelect = new Select<>();
        payStatusSelect.setLabel("Payment Status");
        payStatusSelect.setItems("PAID", "UNPAID");
        payStatusSelect.setValue("UNPAID");

        Image qrCodeImage = new Image();
        qrCodeImage.setVisible(false);

        amount.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));
        tax.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));
        methodSelect.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));

        form.add(patientSelect, doctorSelect, amount, tax, date, methodSelect, payStatusSelect, qrCodeImage);

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
            b.setStatus(payStatusSelect.getValue());
            b.setPaymentMethod(methodSelect.getValue());
            b.setDescription("General Clinic Billing");
            if ("PAID".equals(payStatusSelect.getValue())) {
                b.setPaidDate(LocalDate.now());
            }
            b.setInvoiceNumber("INV-" + System.currentTimeMillis());

            adminService.createBill(b);
            Notification.show("Invoice created successfully. Admins notified.", 2000, Notification.Position.TOP_CENTER);
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

    private void openGenerateInvoiceDialog(Appointment appt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Generate Invoice for Appointment #" + appt.getId());
        dialog.setWidth("480px");

        FormLayout form = new FormLayout();
        
        TextField patientField = new TextField("Patient");
        patientField.setValue(appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "N/A");
        patientField.setReadOnly(true);

        TextField doctorField = new TextField("Doctor");
        doctorField.setValue(appt.getDoctor() != null ? "Dr. " + appt.getDoctor().getName() : "N/A");
        doctorField.setReadOnly(true);

        form.add(patientField, doctorField);

        // Fetch prescription details for this patient to display in the dialog
        if (appt.getPatient() != null) {
            List<com.aruclinic.dto.PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByPatientId(appt.getPatient().getId());
            com.aruclinic.dto.PrescriptionDto matchedPrescription = prescriptions.stream()
                    .filter(p -> p.getPrescriptionDate() != null && p.getPrescriptionDate().isEqual(appt.getAppointmentDate()))
                    .findFirst()
                    .orElse(prescriptions.isEmpty() ? null : prescriptions.get(0));

            if (matchedPrescription != null) {
                Div prescriptionDetails = new Div();
                prescriptionDetails.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "var(--aruclinic-radius-sm)")
                    .set("padding", "var(--aruclinic-spacing-md)")
                    .set("margin-bottom", "var(--aruclinic-spacing-md)")
                    .set("border-left", "4px solid var(--aruclinic-primary)");

                Span pTitle = new Span("Prescription Details (Realtime)");
                pTitle.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)").set("display", "block").set("margin-bottom", "8px");

                Span diag = new Span("Diagnosis: " + matchedPrescription.getDiagnosis());
                diag.getStyle().set("display", "block").set("font-size", "var(--aruclinic-font-size-sm)");

                Span symp = new Span("Symptoms: " + matchedPrescription.getSymptoms());
                symp.getStyle().set("display", "block").set("font-size", "var(--aruclinic-font-size-sm)");

                String medsList = matchedPrescription.getItems().stream()
                        .map(com.aruclinic.dto.PrescriptionItemDto::getMedicineName)
                        .collect(Collectors.joining(", "));
                Span meds = new Span("Medicines: " + (medsList.isEmpty() ? "None" : medsList));
                meds.getStyle().set("display", "block").set("font-size", "var(--aruclinic-font-size-sm)").set("margin-top", "4px");

                prescriptionDetails.add(pTitle, diag, symp, meds);
                form.add(prescriptionDetails);
            }
        }

        BigDecimalField amount = new BigDecimalField("Subtotal Amount (₹)");
        amount.setValue(new BigDecimal("500.00")); // default consulting fee
        BigDecimalField tax = new BigDecimalField("Tax Amount (₹)");
        tax.setValue(BigDecimal.ZERO);

        DatePicker date = new DatePicker("Invoice Date");
        date.setValue(LocalDate.now());

        Select<String> methodSelect = new Select<>();
        methodSelect.setLabel("Payment Method");
        methodSelect.setItems("Cash", "Card", "UPI", "Net Banking");
        methodSelect.setValue("Cash");

        Select<String> payStatusSelect = new Select<>();
        payStatusSelect.setLabel("Payment Status");
        payStatusSelect.setItems("PAID", "UNPAID");
        payStatusSelect.setValue("PAID");

        Image qrCodeImage = new Image();
        qrCodeImage.setVisible(false);

        amount.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));
        tax.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));
        methodSelect.addValueChangeListener(e -> updateQrCode(qrCodeImage, amount, tax, methodSelect));

        form.add(amount, tax, date, methodSelect, payStatusSelect, qrCodeImage);

        Button saveBtn = new Button("Generate", e -> {
            if (appt.getPatient() == null || appt.getDoctor() == null || amount.getValue() == null) {
                Notification.show("Please fill in all details", 2000, Notification.Position.TOP_CENTER);
                return;
            }
            BigDecimal subtotal = amount.getValue();
            BigDecimal taxAmount = tax.getValue() != null ? tax.getValue() : BigDecimal.ZERO;
            BigDecimal totalAmount = subtotal.add(taxAmount);

            Bill b = new Bill();
            b.setPatient(appt.getPatient());
            b.setDoctor(appt.getDoctor());
            b.setAmount(subtotal);
            b.setTax(taxAmount);
            b.setTotal(totalAmount);
            b.setInvoiceDate(date.getValue());
            b.setStatus(payStatusSelect.getValue());
            b.setPaymentMethod(methodSelect.getValue());
            b.setDescription("Billing for Completed Appointment #" + appt.getId());
            if ("PAID".equals(payStatusSelect.getValue())) {
                b.setPaidDate(LocalDate.now());
            }
            b.setInvoiceNumber("INV-" + System.currentTimeMillis());

            adminService.createBill(b);
            Notification.show("Invoice created successfully. Admins notified.", 2000, Notification.Position.TOP_CENTER);
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

        List<Appointment> completedAppts = adminService.getAllAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .filter(a -> {
                    String matchToken = "Appointment #" + a.getId();
                    return adminService.getAllBills().stream()
                            .noneMatch(b -> b.getDescription() != null && b.getDescription().contains(matchToken));
                })
                .collect(Collectors.toList());
        pendingGrid.setItems(completedAppts);
    }
}
