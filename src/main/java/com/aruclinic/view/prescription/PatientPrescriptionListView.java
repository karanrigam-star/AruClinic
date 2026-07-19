package com.aruclinic.view.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.dto.PrescriptionItemDto;
import com.aruclinic.entity.Patient;
import com.aruclinic.service.PatientService;
import com.aruclinic.service.UserService;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.view.MainLayout;
import com.aruclinic.util.PdfHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated patient prescription list view, styled identically to the appointment list view.
 */
@PageTitle("My Prescriptions | AruClinic")
@Route(value = "patient/prescriptions", layout = MainLayout.class)
@CssImport("./themes/aruclinic/appointment.css")
@CssImport("./themes/aruclinic/patient.css")
public class PatientPrescriptionListView extends VerticalLayout implements BeforeEnterObserver {

    private final PrescriptionService prescriptionService;
    private final PatientService patientService;
    private final com.aruclinic.service.NotificationService notificationService;
    private final UserService userService;

    private Patient currentPatient = null;
    private final Grid<PrescriptionDto> grid = new Grid<>();
    private final List<PrescriptionDto> allPrescriptions = new ArrayList<>();
    
    private final DatePicker dateFilter = new DatePicker();
    private final Select<String> statusFilter = new Select<>();
    private final TextField searchField = new TextField();

    public PatientPrescriptionListView(PrescriptionService prescriptionService, 
                                       PatientService patientService,
                                       com.aruclinic.service.NotificationService notificationService,
                                       UserService userService) {
        this.prescriptionService = prescriptionService;
        this.patientService = patientService;
        this.notificationService = notificationService;
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-patient-prescription-list-view");

        resolvePatient();
        configureGrid();

        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
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
                currentPatient = patientService.getPatientEntityByEmail(email);
            }

            // Fallback for blank setups during testing (strictly disallowed for PATIENT role)
            boolean isPatient = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (currentPatient == null && !isPatient) {
                List<Patient> patients = patientService.getAllPatientEntities();
                if (!patients.isEmpty()) {
                    currentPatient = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-prescription-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(a -> a.getPrescriptionDate() != null ? a.getPrescriptionDate().toString() : "N/A")
                .setHeader("Date").setSortable(true).setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getPrescriptionId).setHeader("Prescription ID").setAutoWidth(true);

        grid.addColumn(a -> a.getDoctorId() != null ? "DOC-" + a.getDoctorId() : "N/A")
                .setHeader("Doctor ID").setAutoWidth(true);

        grid.addColumn(a -> a.getDoctorName() != null ? "Dr. " + a.getDoctorName() : "Unknown")
                .setHeader("Doctor Name").setSortable(true).setAutoWidth(true);

        grid.addColumn(PrescriptionDto::getDiagnosis).setHeader("Diagnosis").setAutoWidth(true);

        grid.addColumn(this::getMedicinesSummary).setHeader("Medicines").setAutoWidth(true);

        grid.addColumn(a -> a.getStatus() != null ? a.getStatus() : "ACTIVE")
                .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActionsComponent).setHeader("Actions").setAutoWidth(true);
    }

    private String getMedicinesSummary(PrescriptionDto item) {
        if (item.getItems() == null || item.getItems().isEmpty()) {
            return "0 items";
        }
        return item.getItems().size() + " medicine(s)";
    }

    private Component createActionsComponent(PrescriptionDto item) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button viewDetailsBtn = new Button("Details", new Icon(VaadinIcon.INFO_CIRCLE));
        viewDetailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewDetailsBtn.addClickListener(e -> showPrescriptionDetailsDialog(item));
        
        Anchor downloadAnchor = new Anchor(new StreamResource("Prescription_" + (item.getPrescriptionId() != null ? item.getPrescriptionId() : item.getId()) + ".pdf", () -> {
            String medsList = "";
            if (item.getItems() != null) {
                medsList = item.getItems().stream()
                    .map(it -> it.getMedicineName() + " (" + it.getDosage() + ")")
                    .collect(Collectors.joining(", "));
            }
            java.io.ByteArrayInputStream pdfStream = PdfHelper.generatePrescriptionPdf(
                item.getPrescriptionId() != null ? item.getPrescriptionId() : "RX-" + item.getId(),
                item.getPatientName() != null ? item.getPatientName() : 
                    (currentPatient != null ? currentPatient.getFirstName() + " " + currentPatient.getLastName() : "Patient"),
                item.getDoctorName() != null ? item.getDoctorName() : "Doctor",
                item.getPrescriptionDate() != null ? item.getPrescriptionDate().toString() : "",
                item.getDiagnosis() != null ? item.getDiagnosis() : "Routine Checkup",
                medsList
            );

            if ("DELETED_BY_DOCTOR".equalsIgnoreCase(item.getStatus())) {
                try {
                    prescriptionService.deletePrescriptionReal(item.getId());
                    if (currentPatient != null && currentPatient.getEmail() != null) {
                        com.aruclinic.entity.User patientUser = userService.getUserEntityByEmail(currentPatient.getEmail());
                        if (patientUser != null) {
                            notificationService.deletePrescriptionNotification(patientUser.getId(), item.getPrescriptionId());
                        }
                    }
                    getUI().ifPresent(ui -> ui.access(() -> {
                        refreshData();
                    }));
                } catch (Exception ex) {
                    // Ignore
                }
            }
            return pdfStream;
        }), "");
        downloadAnchor.getElement().setAttribute("download", true);

        Button downloadBtn = new Button(new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        downloadBtn.setTooltipText("Download PDF");
        downloadAnchor.add(downloadBtn);

        layout.add(viewDetailsBtn, downloadAnchor);
        return layout;
    }

    private void showPrescriptionDetailsDialog(PrescriptionDto item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Prescription Details");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Div detailsBlock = new Div();
        detailsBlock.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "12px").set("width", "100%");

        detailsBlock.add(createDetailItem("Prescription ID", item.getPrescriptionId()));
        detailsBlock.add(createDetailItem("Date", item.getPrescriptionDate() != null ? item.getPrescriptionDate().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Doctor Name", item.getDoctorName() != null ? "Dr. " + item.getDoctorName() : "Unknown"));
        detailsBlock.add(createDetailItem("Symptoms", item.getSymptoms() != null ? item.getSymptoms() : "N/A"));
        detailsBlock.add(createDetailItem("Diagnosis", item.getDiagnosis() != null ? item.getDiagnosis() : "N/A"));
        detailsBlock.add(createDetailItem("Advice", item.getAdvice() != null ? item.getAdvice() : "N/A"));
        detailsBlock.add(createDetailItem("Follow-up Date", item.getFollowUpDate() != null ? item.getFollowUpDate().toString() : "N/A"));
        detailsBlock.add(createDetailItem("Status", item.getStatus() != null ? item.getStatus() : "ACTIVE"));

        // Medicines section
        Div medsTitle = new Div(new Span("Prescribed Medicines:"));
        medsTitle.getStyle().set("font-weight", "600").set("margin-top", "12px").set("color", "var(--aruclinic-text-primary)");
        detailsBlock.add(medsTitle);

        if (item.getItems() != null && !item.getItems().isEmpty()) {
            for (PrescriptionItemDto med : item.getItems()) {
                String medDetails = med.getMedicineName() + " - " + med.getDosage() + " (" + med.getDuration() + " days)";
                detailsBlock.add(createDetailItem("• Medicine", medDetails));
            }
        } else {
            detailsBlock.add(createDetailItem("• Medicines", "None prescribed"));
        }

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
        l.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-text-secondary, #64748b)");
        Span v = new Span(value != null ? value : "N/A");
        v.getStyle().set("font-weight", "500").set("color", "var(--aruclinic-text-primary, #0f172a)");
        item.add(l, v);
        return item;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Prescriptions");
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
        statusFilter.setItems("ALL", "ACTIVE", "COMPLETED");
        statusFilter.setValue("ALL");
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> updateGridList());

        searchField.setPlaceholder("Search diagnosis or symptoms...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGridList());

        Button findBtn = new Button("Find", new Icon(VaadinIcon.SEARCH));
        findBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        findBtn.addClickListener(e -> updateGridList());

        bar.add(dateFilter, statusFilter, searchField, findBtn);
        return bar;
    }

    private void refreshData() {
        if (currentPatient == null) {
            allPrescriptions.clear();
            updateGridList();
            return;
        }

        List<PrescriptionDto> list = prescriptionService.getPrescriptionsByPatientId(currentPatient.getId());
        allPrescriptions.clear();
        allPrescriptions.addAll(list);

        updateGridList();
    }

    private void updateGridList() {
        LocalDate dFilter = dateFilter.getValue();
        String sFilter = statusFilter.getValue();
        String search = searchField.getValue().trim().toLowerCase();

        List<PrescriptionDto> filtered = allPrescriptions.stream()
                .filter(a -> {
                    if (dFilter == null) return true;
                    return a.getPrescriptionDate() != null && a.getPrescriptionDate().isEqual(dFilter);
                })
                .filter(a -> {
                    if (sFilter == null || "ALL".equalsIgnoreCase(sFilter)) return true;
                    return a.getStatus() != null && a.getStatus().equalsIgnoreCase(sFilter);
                })
                .filter(a -> {
                    if (search.isEmpty()) return true;
                    return (a.getDiagnosis() != null && a.getDiagnosis().toLowerCase().contains(search)) ||
                           (a.getSymptoms() != null && a.getSymptoms().toLowerCase().contains(search));
                })
                .collect(Collectors.toList());

        // Sort by date descending
        filtered.sort((a1, a2) -> {
            if (a1.getPrescriptionDate() == null && a2.getPrescriptionDate() == null) return 0;
            if (a1.getPrescriptionDate() == null) return 1;
            if (a2.getPrescriptionDate() == null) return -1;
            return a2.getPrescriptionDate().compareTo(a1.getPrescriptionDate());
        });

        grid.setItems(filtered);

        if (filtered.isEmpty()) {
            Notification.show("No prescriptions found matching the selected filters.", 3000, Notification.Position.MIDDLE);
        }
    }
}
