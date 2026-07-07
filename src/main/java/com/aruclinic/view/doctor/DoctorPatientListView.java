package com.aruclinic.view.doctor;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Prescription;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.PrescriptionRepository;
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("My Patients | AruClinic")
@Route(value = "doctor/patients", layout = MainLayout.class)
@CssImport("./themes/aruclinic/patient.css")
public class DoctorPatientListView extends VerticalLayout {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;

    private Doctor currentDoctor = null;
    private final Grid<Patient> grid = new Grid<>();
    private final TextField searchField = new TextField();

    public DoctorPatientListView(PatientRepository patientRepository,
                                 AppointmentRepository appointmentRepository,
                                 PrescriptionRepository prescriptionRepository,
                                 DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.doctorRepository = doctorRepository;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-doctor-patient-list-view");

        resolveCurrentDoctor();
        configureGrid();

        add(createHeader(), createFilterBar(), grid);
        setFlexGrow(1.0, grid);
        
        refreshGrid();
    }

    private void configureGrid() {
        grid.addClassName("aruclinic-patient-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("550px");

        grid.addColumn(p -> "PAT-" + p.getId()).setHeader("Patient ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(p -> p.getFirstName() + " " + p.getLastName()).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Patient::getGender).setHeader("Gender").setAutoWidth(true);
        grid.addColumn(p -> p.getDateOfBirth() != null ? calculateAge(p.getDateOfBirth()) + " yrs" : "N/A").setHeader("Age").setAutoWidth(true);
        grid.addColumn(Patient::getMobileNumber).setHeader("Mobile").setAutoWidth(true);
        grid.addColumn(Patient::getEmail).setHeader("Email").setAutoWidth(true);

        // Display latest appointment status dynamically
        grid.addColumn(p -> {
            if (currentDoctor == null) return "N/A";
            List<Appointment> appts = appointmentRepository.findByPatientId(p.getId()).stream()
                    .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(currentDoctor.getId()))
                    .collect(Collectors.toList());
            if (appts.isEmpty()) {
                return "No Bookings";
            }
            Appointment latest = null;
            for (Appointment a : appts) {
                if (a.getAppointmentDate() != null) {
                    if (latest == null || latest.getAppointmentDate() == null || a.getAppointmentDate().isAfter(latest.getAppointmentDate())) {
                        latest = a;
                    }
                }
            }
            if (latest != null && latest.getStatus() != null) {
                return latest.getStatus().name();
            }
            return "N/A";
        }).setHeader("Appointment Status").setAutoWidth(true);

        grid.addComponentColumn(this::createActionsComponent).setHeader("Actions").setAutoWidth(true);
    }

    private void resolveCurrentDoctor() {
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

            // Fallback for SUPER_ADMIN or missing records
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

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("My Patients");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createFilterBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.getStyle().set("margin-top", "var(--aruclinic-spacing-md)");

        searchField.setPlaceholder("Search by name or phone...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());
        searchField.setWidth("300px");

        bar.add(searchField);
        return bar;
    }

    private int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private Component createActionsComponent(Patient patient) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        Button consultBtn = new Button("Consult", new Icon(VaadinIcon.FILE_TEXT));
        consultBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        consultBtn.addClickListener(e -> getUI().ifPresent(ui -> 
                ui.navigate("doctor/prescriptions/form/patient-" + patient.getId())));

        Button historyBtn = new Button("View Records", new Icon(VaadinIcon.DOCTOR));
        historyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        historyBtn.addClassName("aruclinic-btn-outline");
        historyBtn.addClickListener(e -> openMedicalRecordsDialog(patient));

        layout.add(consultBtn, historyBtn);
        return layout;
    }

    private void openMedicalRecordsDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setWidth("700px");
        dialog.setHeight("550px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Medical Records: " + patient.getFirstName() + " " + patient.getLastName());
        layout.add(title);

        // Appointments History
        Span appointmentsHeader = new Span("Appointment History");
        appointmentsHeader.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)");
        layout.add(appointmentsHeader);

        Grid<Appointment> apptGrid = new Grid<>();
        apptGrid.setHeight("180px");
        apptGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        apptGrid.setItems(appointments);
        
        apptGrid.addColumn(a -> {
            if (a.getAppointmentDate() != null && a.getAppointmentTime() != null) {
                return a.getAppointmentDate().toString() + " " + a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
            } else if (a.getAppointmentDate() != null) {
                return a.getAppointmentDate().toString();
            } else if (a.getAppointmentDateTime() != null) {
                return a.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
            }
            return "N/A";
        }).setHeader("Date/Time").setAutoWidth(true);

        apptGrid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A")
                .setHeader("Doctor").setAutoWidth(true);
                
        apptGrid.addColumn(a -> a.getStatus() != null ? a.getStatus().name() : "SCHEDULED").setHeader("Status").setAutoWidth(true);

        layout.add(apptGrid);

        // Prescription History
        Span rxHeader = new Span("Prescription History");
        rxHeader.getStyle().set("font-weight", "600").set("color", "var(--aruclinic-primary)");
        layout.add(rxHeader);

        Grid<Prescription> rxGrid = new Grid<>();
        rxGrid.setHeight("180px");
        rxGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patient.getId());
        rxGrid.setItems(prescriptions);
        rxGrid.addColumn(p -> "RX-" + p.getId()).setHeader("Rx ID").setAutoWidth(true);
        rxGrid.addColumn(p -> p.getPrescriptionDate() != null ? p.getPrescriptionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A").setHeader("Date").setAutoWidth(true);
        rxGrid.addColumn(Prescription::getDiagnosis).setHeader("Diagnosis").setAutoWidth(true);
        rxGrid.addColumn(Prescription::getStatus).setHeader("Status").setAutoWidth(true);

        layout.add(rxGrid);

        Button closeBtn = new Button("Close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.addClickListener(e -> dialog.close());

        HorizontalLayout footer = new HorizontalLayout(closeBtn);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        layout.add(footer);
        dialog.add(layout);
        dialog.open();
    }

    private void refreshGrid() {
        String filter = searchField.getValue().trim();
        List<Patient> patients;

        if (currentDoctor != null) {
            // Find patient IDs directly to avoid LazyInitializationException outside transactional context
            java.util.Set<Long> patientIds = new java.util.HashSet<>(
                appointmentRepository.findPatientIdsByDoctorId(currentDoctor.getId())
            );
            patientIds.addAll(
                prescriptionRepository.findPatientIdsByDoctorId(currentDoctor.getId())
            );

            if (patientIds.isEmpty()) {
                patients = new java.util.ArrayList<>();
            } else {
                patients = patientRepository.findAllById(patientIds);
            }
        } else {
            patients = patientRepository.findAll();
        }

        if (!filter.isEmpty()) {
            patients = patients.stream()
                    .filter(p -> (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(filter.toLowerCase())
                            || (p.getMobileNumber() != null && p.getMobileNumber().contains(filter)))
                    .collect(Collectors.toList());
        }

        grid.setItems(patients);
    }
}
