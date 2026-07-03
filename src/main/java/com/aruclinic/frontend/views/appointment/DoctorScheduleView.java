package com.aruclinic.frontend.views.appointment;

import com.aruclinic.dto.DoctorDto;
import com.aruclinic.service.DoctorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Doctor Schedule view for displaying and managing doctor availability.
 */
@PageTitle("Doctor Schedule | AruClinic")
@Route("doctor/schedule")
@CssImport("./themes/aruclinic/appointment.css")
public class DoctorScheduleView extends VerticalLayout {

    private final DoctorService doctorService;
    private final ComboBox<String> doctorSelector = new ComboBox<>("Select Doctor");
    private String selectedDoctor = "Dr. Smith";

    public DoctorScheduleView(DoctorService doctorService) {
        this.doctorService = doctorService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureComponents();
        add(createScheduleContent());
    }

    private void configureComponents() {
        // Load doctors
        List<String> doctors = Arrays.asList("Dr. Smith", "Dr. Johnson", "Dr. Williams", "Dr. Brown");
        doctorSelector.setItems(doctors);
        doctorSelector.setValue(selectedDoctor);
        doctorSelector.setWidth("250px");
        doctorSelector.addValueChangeListener(e -> {
            selectedDoctor = e.getValue();
            refreshSchedule();
        });
    }

    private Component createScheduleContent() {
        Div content = new Div();
        content.addClassName("aruclinic-doctor-schedule");
        content.setWidthFull();

        // Header
        content.add(createScheduleHeader());

        // Schedule grid
        content.add(createScheduleGrid());

        return content;
    }

    private Component createScheduleHeader() {
        Div header = new Div();
        header.addClassName("aruclinic-schedule-header");

        Div leftSection = new Div();

        H1 title = new H1("Doctor Schedule");
        title.addClassName("aruclinic-schedule-title");

        leftSection.add(title);

        Div rightSection = new Div();
        rightSection.addClassName("aruclinic-schedule-doctor-selector");

        doctorSelector.addClassName("aruclinic-schedule-doctor-select");

        rightSection.add(doctorSelector);

        header.add(leftSection, rightSection);
        return header;
    }

    private Component createScheduleGrid() {
        Div grid = new Div();
        grid.addClassName("aruclinic-schedule-grid");

        // Get the current week
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday

        // Create a day card for each day of the week
        for (int i = 0; i < 5; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            grid.add(createDayCard(date));
        }

        return grid;
    }

    private Component createDayCard(LocalDate date) {
        Div dayCard = new Div();
        dayCard.addClassName("aruclinic-schedule-day-card");

        Div header = new Div();
        header.addClassName("aruclinic-schedule-day-header");

        Span dayName = new Span(date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()));
        dayName.addClassName("aruclinic-schedule-day-name");

        Span dayDate = new Span(date.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) + " " + date.getDayOfMonth());
        dayDate.addClassName("aruclinic-schedule-day-date");

        header.add(dayName, dayDate);

        Div slots = new Div();
        slots.addClassName("aruclinic-schedule-slots");

        // Add time slots for this day
        List<String> timeSlots = getTimeSlotsForDay(date);
        for (String slot : timeSlots) {
            slots.add(createTimeSlot(slot, date));
        }

        dayCard.add(header, slots);
        return dayCard;
    }

    private Component createTimeSlot(String time, LocalDate date) {
        Div slot = new Div();
        slot.addClassName("aruclinic-schedule-slot");

        Span timeSpan = new Span(time);
        timeSpan.addClassName("aruclinic-schedule-slot-time");

        Span statusSpan = new Span();
        statusSpan.addClassName("aruclinic-schedule-slot-status");

        // Determine status based on the slot
        String status = getSlotStatus(time, date);
        statusSpan.setText(status);
        statusSpan.addClassName(status.toLowerCase());

        slot.add(timeSpan, statusSpan);

        // Make slot clickable if available
        if (status.equals("Available")) {
            slot.addClickListener(e -> bookSlot(time, date));
        }

        return slot;
    }

    private List<String> getTimeSlotsForDay(LocalDate date) {
        // Return time slots for a day
        return Arrays.asList(
            "09:00 AM",
            "09:30 AM",
            "10:00 AM",
            "10:30 AM",
            "11:00 AM",
            "11:30 AM",
            "02:00 PM",
            "02:30 PM",
            "03:00 PM",
            "03:30 PM",
            "04:00 PM",
            "04:30 PM"
        );
    }

    private String getSlotStatus(String time, LocalDate date) {
        // In a real application, this would check the doctor's schedule from the database
        // For demo purposes, return sample statuses

        // If it's today, make some slots booked
        if (date.equals(LocalDate.now())) {
            if (time.equals("09:00 AM") || time.equals("10:30 AM") || time.equals("02:00 PM")) {
                return "Booked";
            }
        }

        // If it's tomorrow, make one slot booked
        if (date.equals(LocalDate.now().plusDays(1))) {
            if (time.equals("11:00 AM")) {
                return "Booked";
            }
        }

        // If it's in the past, mark as unavailable
        if (date.isBefore(LocalDate.now())) {
            return "Unavailable";
        }

        return "Available";
    }

    private void bookSlot(String time, LocalDate date) {
        // In a real application, this would open a booking form or book the slot directly
        com.vaadin.flow.component.notification.Notification.show(
            "Booking slot: " + date.toString() + " at " + time,
            2000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        );
    }

    private void refreshSchedule() {
        // Remove the old schedule grid
        getElement().executeJs("document.querySelector('.aruclinic-schedule-grid').remove()");

        // Add the new schedule grid
        Div newGrid = new Div();
        newGrid.addClassName("aruclinic-schedule-grid");
        newGrid.add(createScheduleGrid());

        add(newGrid);
    }
}
