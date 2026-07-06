package com.aruclinic.frontend.views.billing;

import com.aruclinic.dto.BillDto;
import com.aruclinic.entity.Patient;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.service.BillingService;
import com.aruclinic.frontend.views.MainLayout;
import com.aruclinic.util.PdfHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Billing List view for displaying all bills and invoices for the logged-in patient,
 * designed to look identical to the patient prescription list view.
 */
@PageTitle("Billing | AruClinic")
@Route(value = "patient/billing", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
@CssImport("./themes/aruclinic/patient.css")
@CssImport("./themes/aruclinic/billing.css")
public class BillingView extends VerticalLayout implements BeforeEnterObserver {

    private final BillingService billingService;
    private final PatientRepository patientRepository;
    private final Grid<BillDto> billGrid = new Grid<>();
    private final List<BillDto> allBills = new ArrayList<>();

    private Patient currentPatient = null;
    private final DatePicker dateFilter = new DatePicker();
    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    public BillingView(BillingService billingService, PatientRepository patientRepository) {
        this.billingService = billingService;
        this.patientRepository = patientRepository;
        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-patient-prescription-list-view"); // Inherit prescription styling

        resolvePatient();
        configureGrid();
        
        add(createHeader(), createFilterBar(), billGrid);
        setFlexGrow(1.0, billGrid);
        refreshData();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolvePatient();
        refreshData();
    }

    private void resolvePatient() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    auth = (org.springframework.security.core.Authentication)
                            session.getAttribute("SPRING_SECURITY_AUTHENTICATION");
                }
            }

            if (auth != null) {
                org.springframework.security.core.context.SecurityContext context =
                        org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }

            String email = null;
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                    email = springUser.getUsername();
                } else if (principal instanceof String principalStr) {
                    email = principalStr;
                }
            }

            if (email != null) {
                currentPatient = patientRepository.findByEmail(email).orElse(null);
            }

            // Fallback for blank setups during testing
            if (currentPatient == null) {
                List<Patient> patients = patientRepository.findAll();
                if (!patients.isEmpty()) {
                    currentPatient = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void configureGrid() {
        billGrid.addClassName("aruclinic-prescription-grid");
        billGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        billGrid.setHeight("550px");

        billGrid.addColumn(a -> a.getInvoiceDate() != null ? a.getInvoiceDate().toString() : "N/A")
                .setHeader("Date").setSortable(true).setAutoWidth(true);
        billGrid.addColumn(BillDto::getInvoiceId).setHeader("Invoice ID").setAutoWidth(true);
        billGrid.addColumn(BillDto::getDescription).setHeader("Description").setAutoWidth(true);
        billGrid.addColumn(bill -> bill.getAmount() != null ? "₹" + bill.getAmount().toString() : "₹0.00")
                .setHeader("Amount").setAutoWidth(true);
        billGrid.addColumn(bill -> bill.getTotal() != null ? "₹" + bill.getTotal().toString() : "₹0.00")
                .setHeader("Total").setAutoWidth(true);
        billGrid.addComponentColumn(this::getStatusBadge).setHeader("Status").setAutoWidth(true);
        billGrid.addComponentColumn(this::createActionButtons).setHeader("Actions").setAutoWidth(true);
    }

    private Component getStatusBadge(BillDto bill) {
        Span badge = new Span(bill.getStatus());
        badge.addClassName("status-badge");
        badge.addClassName(bill.getStatus().toLowerCase());
        return badge;
    }

    private Component createActionButtons(BillDto bill) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button viewDetailsBtn = new Button("Details", new Icon(VaadinIcon.INFO_CIRCLE));
        viewDetailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewDetailsBtn.addClickListener(e -> viewInvoice(bill));

        String safeInvoiceId = bill.getInvoiceId() != null ? bill.getInvoiceId() : String.valueOf(bill.getId());
        String safePatientName = currentPatient != null ? currentPatient.getFirstName() + " " + currentPatient.getLastName() : "Patient";
        String desc = bill.getDescription() != null ? bill.getDescription() : "";
        String safeDoctorName = desc.contains("Dr.") ? desc : "Doctor";
        String safeInvoiceDate = bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : LocalDate.now().toString();
        String safeTotal = bill.getTotal() != null ? "₹" + bill.getTotal().toString() : "₹0.00";

        Anchor downloadAnchor = new Anchor(new StreamResource("Invoice_" + safeInvoiceId + ".pdf", () -> {
            return PdfHelper.generateInvoicePdf(
                safeInvoiceId,
                safePatientName,
                safeDoctorName,
                safeInvoiceDate,
                safeTotal
            );
        }), "");
        downloadAnchor.getElement().setAttribute("download", true);
        
        Button downloadBtn = new Button(new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        downloadBtn.setTooltipText("Download PDF");
        downloadAnchor.add(downloadBtn);

        actions.add(viewDetailsBtn, downloadAnchor);

        if ("PENDING".equals(bill.getStatus()) || "UNPAID".equals(bill.getStatus())) {
            Button payBtn = new Button("Pay Now", new Icon(VaadinIcon.CREDIT_CARD));
            payBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            payBtn.setTooltipText("Pay Now");
            payBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/billing/pay/" + safeInvoiceId)));
            actions.add(payBtn);
        }

        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Billing");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        dateFilter.setPlaceholder("Filter by Date");
        dateFilter.setWidth("200px");
        dateFilter.addValueChangeListener(e -> updateGridList());

        statusFilter.setPlaceholder("Filter by Status");
        statusFilter.setItems("ALL", "PAID", "PENDING", "UNPAID");
        statusFilter.setValue("ALL");
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> updateGridList());

        searchField.setPlaceholder("Search description...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGridList());

        Button findBtn = new Button("Find", new Icon(VaadinIcon.SEARCH));
        findBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        findBtn.addClickListener(e -> updateGridList());

        bar.add(dateFilter, statusFilter, searchField, findBtn);
        return bar;
    }

    private void viewInvoice(BillDto bill) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Invoice Details");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Div detailsBlock = new Div();
        detailsBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px").set("width", "100%");

        detailsBlock.add(createDetailItem("Invoice ID", bill.getInvoiceId()));
        detailsBlock.add(createDetailItem("Invoice Date", bill.getInvoiceDate() != null ? bill.getInvoiceDate().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Due Date", bill.getDueDate() != null ? bill.getDueDate().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Description", bill.getDescription() != null ? bill.getDescription() : "N/A"));
        detailsBlock.add(createDetailItem("Amount", bill.getAmount() != null ? "₹" + bill.getAmount().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Tax", bill.getTax() != null ? "₹" + bill.getTax().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Total Amount", bill.getTotal() != null ? "₹" + bill.getTotal().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Payment Method", bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A"));
        detailsBlock.add(createDetailItem("Status", bill.getStatus()));

        content.add(detailsBlock);
        dialog.add(content);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private Component createDetailItem(String label, String value) {
        Div item = new Div();
        item.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("border-bottom", "1px solid #f1f5f9")
            .set("padding-bottom", "8px")
            .set("width", "100%");
        Span l = new Span(label);
        l.getStyle().set("font-weight", "600").set("color", "#64748b");
        Span v = new Span(value != null ? value : "N/A");
        v.getStyle().set("font-weight", "500").set("color", "#0f172a");
        item.add(l, v);
        return item;
    }

    private void refreshData() {
        if (currentPatient == null) {
            allBills.clear();
            updateGridList();
            return;
        }

        List<BillDto> list = billingService.getBillsByPatientId(currentPatient.getId());
        allBills.clear();
        allBills.addAll(list);

        updateGridList();
    }

    private void updateGridList() {
        String search = searchField.getValue().trim().toLowerCase();
        String status = statusFilter.getValue();
        LocalDate dFilter = dateFilter.getValue();

        List<BillDto> filtered = allBills.stream()
                .filter(b -> {
                    if (dFilter == null) return true;
                    return b.getInvoiceDate() != null && b.getInvoiceDate().isEqual(dFilter);
                })
                .filter(b -> {
                    if (search.isEmpty()) return true;
                    return b.getDescription() != null && b.getDescription().toLowerCase().contains(search);
                })
                .filter(b -> {
                    if (status == null || "ALL".equalsIgnoreCase(status)) return true;
                    return b.getStatus() != null && b.getStatus().equalsIgnoreCase(status);
                })
                .collect(Collectors.toList());

        billGrid.setItems(filtered);
    }
}
