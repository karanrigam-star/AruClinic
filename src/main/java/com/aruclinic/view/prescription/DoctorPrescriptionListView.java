package com.aruclinic.view.prescription;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.entity.Doctor;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.service.PrescriptionService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Doctor Prescriptions | AruClinic")
@Route(value = "doctor/prescriptions", layout = MainLayout.class)
@CssImport("./themes/aruclinic/prescription.css")
public class DoctorPrescriptionListView extends VerticalLayout implements com.vaadin.flow.router.BeforeEnterObserver {

    private final PrescriptionService prescriptionService;
    private final DoctorRepository doctorRepository;

    private Doctor currentDoctor = null;
    private final Grid<PrescriptionDto> grid = new Grid<>();
    private final List<PrescriptionDto> allPrescriptions = new ArrayList<>();
    private final TextField searchField = new TextField();

    // Stats variables
    private int totalCount = 0;
    private int draftCount = 0;
    private int issuedCount = 0;
    private int followUpCount = 0;

    private final Span totalVal = new Span("0");
    private final Span draftVal = new Span("0");
    private final Span issuedVal = new Span("0");
    private final Span followUpVal = new Span("0");

    private String activeFilter = "ALL"; // ALL, DRAFT, ISSUED, TODAY, FOLLOWUP

    public DoctorPrescriptionListView(PrescriptionService prescriptionService, DoctorRepository doctorRepository) {
        this.prescriptionService = prescriptionService;
        this.doctorRepository = doctorRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-doctor-prescription-list-view");

        resolveDoctor();
        configureGrid();
        
        add(createHeader(), createStatsBlock(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        refreshData();
    }

    private void resolveDoctor() {
        try {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                com.vaadin.flow.server.VaadinSession session = com.vaadin.flow.server.VaadinSession.getCurrent();
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
                currentDoctor = doctorRepository.findByEmail(email).orElse(null);
            }

            // Fallback for SUPER_ADMIN or blank setups during testing
            if (currentDoctor == null) {
                List<Doctor> doctors = doctorRepository.findAll();
                if (!doctors.isEmpty()) {
                    currentDoctor = doctors.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public void beforeEnter(com.vaadin.flow.router.BeforeEnterEvent event) {
        if (com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute("consult_navigation_active") != null) {
            com.vaadin.flow.server.VaadinSession.getCurrent().setAttribute("consult_navigation_active", null);
            event.forwardTo("doctor/appointments");
        }
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-prescription-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addComponentColumn(item -> {
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete Prescription");
            deleteBtn.getElement().addEventListener("click", e -> {
                getUI().ifPresent(ui -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.setHeaderTitle("Delete Prescription");
                    confirmDialog.add(new Span("Are you sure you want to delete prescription " + item.getPrescriptionId() + "?"));
                    
                    Button confirmBtn = new Button("Delete", click -> {
                        try {
                            prescriptionService.deletePrescription(item.getId());
                            Notification.show("Prescription deleted successfully", 2000, Notification.Position.TOP_CENTER);
                            refreshData();
                            confirmDialog.close();
                        } catch (Exception ex) {
                            Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                        }
                    });
                    confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                    
                    Button cancelBtn = new Button("Cancel", click -> confirmDialog.close());
                    cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                    
                    confirmDialog.getFooter().add(cancelBtn, confirmBtn);
                    confirmDialog.open();
                });
            }).addEventData("event.stopPropagation()");
            return deleteBtn;
        }).setHeader("Delete").setWidth("80px").setFlexGrow(0);

        grid.addColumn(PrescriptionDto::getPrescriptionId)
            .setHeader("ID")
            .setWidth("120px")
            .setFlexGrow(0)
            .setClassNameGenerator(item -> "aruclinic-id-column");
        grid.addColumn(PrescriptionDto::getPrescriptionDate).setHeader("Date").setWidth("120px").setFlexGrow(0);
        grid.addColumn(PrescriptionDto::getPatientName).setHeader("Patient").setWidth("180px").setFlexGrow(1);
        grid.addColumn(PrescriptionDto::getDiagnosis).setHeader("Diagnosis").setWidth("200px").setFlexGrow(1);
        grid.addComponentColumn(this::getStatusBadge).setHeader("Status").setWidth("110px").setFlexGrow(0);
        grid.addComponentColumn(this::createActions).setHeader("Actions").setWidth("250px").setFlexGrow(0);
        grid.addItemClickListener(event -> {
            PrescriptionDto item = event.getItem();
            if (item != null) {
                getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form/" + item.getId()));
            }
        });
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

        Button dupBtn = new Button(new Icon(VaadinIcon.COPY));
        dupBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dupBtn.setTooltipText("Duplicate Rx");
        dupBtn.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("duplicate_prescription_id", item.getId());
            getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form"));
        });

        actions.add(viewBtn, printBtn, dupBtn);

        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_PRIMARY);
        editBtn.setTooltipText("Edit Prescription");
        editBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form/" + item.getId())));
        actions.add(editBtn);

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.setTooltipText("Delete Prescription");
        deleteBtn.addClickListener(e -> {
            try {
                prescriptionService.deletePrescription(item.getId());
                Notification.show("Prescription deleted", 2000, Notification.Position.TOP_CENTER);
                refreshData();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        actions.add(deleteBtn);

        return actions;
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Prescriptions");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        Button newBtn = new Button("New Prescription", new Icon(VaadinIcon.PLUS));
        newBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("doctor/prescriptions/form")));

        header.add(title, newBtn);
        return header;
    }

    private Component createStatsBlock() {
        Div statsGrid = new Div();
        statsGrid.addClassName("aruclinic-prescription-stats");

        statsGrid.add(createStatCard("Total Written", totalVal, "primary", () -> filterGrid("ALL")));
        statsGrid.add(createStatCard("Active Drafts", draftVal, "warning", () -> filterGrid("DRAFT")));
        statsGrid.add(createStatCard("Issued Rx", issuedVal, "success", () -> filterGrid("ISSUED")));
        statsGrid.add(createStatCard("Follow-ups", followUpVal, "info", () -> filterGrid("FOLLOWUP")));

        return statsGrid;
    }

    private Component createStatCard(String label, Span valueSpan, String type, Runnable onClick) {
        Div card = new Div();
        card.addClassName("aruclinic-prescription-stat");
        card.addClassName(type);
        card.addClickListener(e -> onClick.run());
        card.getStyle().set("cursor", "pointer");

        Span lbl = new Span(label);
        lbl.addClassName("aruclinic-prescription-stat-label");

        valueSpan.addClassName("aruclinic-prescription-stat-value");

        card.add(lbl, valueSpan);
        return card;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search prescriptions by patient, diagnosis, or symptoms...");
        searchField.setWidth("60%");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGridList());

        bar.add(searchField);
        return bar;
    }

    private void refreshData() {
        List<PrescriptionDto> list = prescriptionService.getAllPrescriptions();

        totalCount = list.size();
        draftCount = (int) list.stream().filter(p -> "DRAFT".equalsIgnoreCase(p.getStatus())).count();
        issuedCount = (int) list.stream().filter(p -> "ISSUED".equalsIgnoreCase(p.getStatus())).count();
        
        LocalDate today = LocalDate.now();
        followUpCount = (int) list.stream()
                .filter(p -> p.getFollowUpDate() != null && !p.getFollowUpDate().isBefore(today))
                .count();

        totalVal.setText(String.valueOf(totalCount));
        draftVal.setText(String.valueOf(draftCount));
        issuedVal.setText(String.valueOf(issuedCount));
        followUpVal.setText(String.valueOf(followUpCount));

        updateGridList();
    }

    private void filterGrid(String filter) {
        this.activeFilter = filter;
        updateGridList();
    }

    private void updateGridList() {
        String query = searchField.getValue();
        List<PrescriptionDto> list = prescriptionService.searchPrescriptions(query);
        
        List<PrescriptionDto> filtered = list.stream()
                .filter(p -> {
                    if ("ALL".equalsIgnoreCase(activeFilter)) return true;
                    if ("DRAFT".equalsIgnoreCase(activeFilter)) return "DRAFT".equalsIgnoreCase(p.getStatus());
                    if ("ISSUED".equalsIgnoreCase(activeFilter)) return "ISSUED".equalsIgnoreCase(p.getStatus());
                    if ("FOLLOWUP".equalsIgnoreCase(activeFilter)) {
                        return p.getFollowUpDate() != null && !p.getFollowUpDate().isBefore(LocalDate.now());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }
}
