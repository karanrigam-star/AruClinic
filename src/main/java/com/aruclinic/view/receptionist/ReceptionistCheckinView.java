package com.aruclinic.view.receptionist;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.service.AppointmentService;
import com.aruclinic.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receptionist view for checking in patients for their scheduled appointments.
 */
@PageTitle("Patient Check In | AruClinic")
@Route(value = "receptionist/checkin", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class ReceptionistCheckinView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private final Grid<Appointment> grid = new Grid<>();

    public ReceptionistCheckinView(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Patient Check In");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");
        add(title);

        configureGrid();
        add(grid);
        
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(a -> a.getPatient() != null ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "N/A")
                .setHeader("Patient Name").setAutoWidth(true);

        grid.addColumn(a -> a.getDoctor() != null ? "Dr. " + a.getDoctor().getName() : "N/A")
                .setHeader("Doctor").setAutoWidth(true);

        grid.addColumn(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A")
                .setHeader("Scheduled Time").setAutoWidth(true);

        grid.addColumn(Appointment::getReason).setHeader("Reason for Visit").setAutoWidth(true);

        grid.addComponentColumn(this::createCheckInButton).setHeader("Action").setAutoWidth(true);
    }

    private Component createCheckInButton(Appointment appt) {
        Button checkInBtn = new Button("Check In", new Icon(VaadinIcon.CHECK));
        checkInBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkInBtn.addClickListener(e -> {
            appt.setStatus(AppointmentStatus.CHECKED_IN);
            appointmentService.saveAppointment(appt);
            Notification.show("Patient " + (appt.getPatient() != null ? appt.getPatient().getFirstName() : "Patient") + " checked in successfully!", 2000, Notification.Position.TOP_CENTER);
            refreshGrid();
        });
        return checkInBtn;
    }

    private void refreshGrid() {
        LocalDate today = LocalDate.now();
        List<Appointment> todayScheduled = appointmentService.findAll().stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().isEqual(today))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList());

        grid.setItems(todayScheduled);
    }
}
