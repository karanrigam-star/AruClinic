package com.aruclinic.view.appointment;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.dto.AppointmentStatus;
import com.aruclinic.service.AppointmentService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Calendar view for displaying appointments in a calendar format.
 */
@PageTitle("Calendar | AruClinic")
@Route("appointment/calendar")
@CssImport("./themes/aruclinic/appointment.css")
public class CalendarView extends VerticalLayout {

    private final AppointmentService appointmentService;
    private LocalDate currentDate = LocalDate.now();
    private String currentView = "month"; // month, week, day
    private H1 calendarTitle;
    private Div gridContainer;

    public CalendarView(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        calendarTitle = new H1();
        calendarTitle.addClassName("aruclinic-calendar-title");
        
        gridContainer = new Div();
        gridContainer.setWidthFull();

        add(createCalendarContent());
        refreshCalendar();
    }

    private Component createCalendarContent() {
        Div content = new Div();
        content.addClassName("aruclinic-appointment-calendar");
        content.setWidthFull();

        // Header
        content.add(createCalendarHeader());

        // Calendar grid
        content.add(gridContainer);

        return content;
    }

    private Component createCalendarHeader() {
        Div header = new Div();
        header.addClassName("aruclinic-calendar-header");

        Div leftSection = new Div();
        leftSection.addClassName("aruclinic-calendar-left");

        leftSection.add(calendarTitle);

        Div centerSection = new Div();
        centerSection.addClassName("aruclinic-calendar-nav");

        Button prevBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        prevBtn.addClassName("aruclinic-calendar-nav-btn");
        prevBtn.addClickListener(e -> navigatePrevious());

        Button todayBtn = new Button("Today");
        todayBtn.addClassName("aruclinic-calendar-nav-btn");
        todayBtn.addClassName("today");
        todayBtn.addClickListener(e -> navigateToToday());

        Button nextBtn = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        nextBtn.addClassName("aruclinic-calendar-nav-btn");
        nextBtn.addClickListener(e -> navigateNext());

        centerSection.add(prevBtn, todayBtn, nextBtn);

        Div rightSection = new Div();
        rightSection.addClassName("aruclinic-calendar-view-toggle");

        Button monthBtn = new Button("Month");
        monthBtn.addClassName("aruclinic-calendar-view-btn");
        if (currentView.equals("month")) {
            monthBtn.addClassName("active");
        }
        monthBtn.addClickListener(e -> setView("month"));

        Button weekBtn = new Button("Week");
        weekBtn.addClassName("aruclinic-calendar-view-btn");
        if (currentView.equals("week")) {
            weekBtn.addClassName("active");
        }
        weekBtn.addClickListener(e -> setView("week"));

        Button dayBtn = new Button("Day");
        dayBtn.addClassName("aruclinic-calendar-view-btn");
        if (currentView.equals("day")) {
            dayBtn.addClassName("active");
        }
        dayBtn.addClickListener(e -> setView("day"));

        rightSection.add(monthBtn, weekBtn, dayBtn);

        header.add(leftSection, centerSection, rightSection);
        return header;
    }

    private Component createCalendarGrid() {
        Div grid = new Div();
        grid.addClassName("aruclinic-calendar-grid");

        // Day headers
        for (DayOfWeek day : DayOfWeek.values()) {
            Div dayHeader = new Div();
            dayHeader.addClassName("aruclinic-calendar-day-header");
            dayHeader.setText(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            grid.add(dayHeader);
        }

        // Get the first day of the month
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        int offset = firstDayOfMonth.getDayOfWeek().getValue() - 1; // Monday = 1, Sunday = 7

        // Get the number of days in the month
        int daysInMonth = firstDayOfMonth.lengthOfMonth();

        // Get the number of days in the previous month
        LocalDate lastDayOfPrevMonth = firstDayOfMonth.minusDays(1);
        int daysInPrevMonth = lastDayOfPrevMonth.lengthOfMonth();

        // Add empty cells for days from previous month
        for (int i = 0; i < offset; i++) {
            Div emptyCell = new Div();
            emptyCell.addClassName("aruclinic-calendar-day");
            emptyCell.addClassName("other-month");
            grid.add(emptyCell);
        }

        // Add cells for days in the current month
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = firstDayOfMonth.plusDays(day - 1);
            Div dayCell = createDayCell(date);
            grid.add(dayCell);
        }

        // Add empty cells for days from next month to complete the grid
        int totalCells = offset + daysInMonth;
        int remainingCells = 42 - totalCells; // 6 rows x 7 columns = 42 cells
        for (int i = 0; i < remainingCells; i++) {
            Div emptyCell = new Div();
            emptyCell.addClassName("aruclinic-calendar-day");
            emptyCell.addClassName("other-month");
            grid.add(emptyCell);
        }

        return grid;
    }

    private Div createDayCell(LocalDate date) {
        Div dayCell = new Div();
        dayCell.addClassName("aruclinic-calendar-day");

        // Check if this is today
        if (date.equals(LocalDate.now())) {
            dayCell.addClassName("today");
        }

        // Check if this is the selected date
        if (date.equals(currentDate)) {
            dayCell.addClassName("selected");
        }

        // Day header
        Div dayHeader = new Div();
        dayHeader.addClassName("aruclinic-calendar-day-header");
        dayHeader.setText(String.valueOf(date.getDayOfMonth()));

        // Appointments for this day
        Div appointmentsDiv = new Div();
        appointmentsDiv.addClassName("aruclinic-calendar-appointments");

        // Get appointments for this date
        List<AppointmentDto> appointments = getAppointmentsForDate(date);
        for (AppointmentDto appointment : appointments) {
            Div appointmentDiv = (Div) createAppointmentDot(appointment.getStatus() != null ? appointment.getStatus().name() : "SCHEDULED");
            appointmentsDiv.add(appointmentDiv);
        }

        dayCell.add(dayHeader, appointmentsDiv);
        dayCell.addClickListener(e -> selectDate(date));

        return dayCell;
    }

    private Component createAppointmentDot(String status) {
        Div dot = new Div();
        dot.addClassName("aruclinic-calendar-appointment-dot");
        dot.addClassName(status.toLowerCase());
        return dot;
    }

    private List<AppointmentDto> getAppointmentsForDate(LocalDate date) {
        // In a real application, this would fetch appointments from the service
        // For demo purposes, return sample appointments
        List<AppointmentDto> appointments = new ArrayList<>();

        // Add some sample appointments for demonstration
        if (date.equals(LocalDate.now())) {
            AppointmentDto appt1 = new AppointmentDto();
            appt1.setStatus(AppointmentStatus.SCHEDULED);
            appointments.add(appt1);

            AppointmentDto appt2 = new AppointmentDto();
            appt2.setStatus(AppointmentStatus.CANCELLED);
            appointments.add(appt2);
        } else if (date.equals(LocalDate.now().plusDays(1))) {
            AppointmentDto appt = new AppointmentDto();
            appt.setStatus(AppointmentStatus.SCHEDULED);
            appointments.add(appt);
        } else if (date.equals(LocalDate.now().plusDays(3))) {
            AppointmentDto appt1 = new AppointmentDto();
            appt1.setStatus(AppointmentStatus.SCHEDULED);
            appointments.add(appt1);

            AppointmentDto appt2 = new AppointmentDto();
            appt2.setStatus(AppointmentStatus.SCHEDULED);
            appointments.add(appt2);

            AppointmentDto appt3 = new AppointmentDto();
            appt3.setStatus(AppointmentStatus.CANCELLED);
            appointments.add(appt3);
        }

        return appointments;
    }

    private void updateTitle() {
        String titleText;
        switch (currentView) {
            case "week":
                titleText = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
                break;
            case "day":
                titleText = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
                           currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " +
                           currentDate.getDayOfMonth() + ", " + currentDate.getYear();
                break;
            case "month":
            default:
                titleText = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentDate.getYear();
                break;
        }

        if (calendarTitle != null) {
            calendarTitle.setText(titleText);
        }
    }

    private void navigatePrevious() {
        switch (currentView) {
            case "week":
                currentDate = currentDate.minusWeeks(1);
                break;
            case "day":
                currentDate = currentDate.minusDays(1);
                break;
            case "month":
            default:
                currentDate = currentDate.minusMonths(1);
                break;
        }
        refreshCalendar();
    }

    private void navigateNext() {
        switch (currentView) {
            case "week":
                currentDate = currentDate.plusWeeks(1);
                break;
            case "day":
                currentDate = currentDate.plusDays(1);
                break;
            case "month":
            default:
                currentDate = currentDate.plusMonths(1);
                break;
        }
        refreshCalendar();
    }

    private void navigateToToday() {
        currentDate = LocalDate.now();
        refreshCalendar();
    }

    private void setView(String view) {
        currentView = view;
        refreshCalendar();
    }

    private void selectDate(LocalDate date) {
        currentDate = date;
        refreshCalendar();

        // In a real app, you might show a modal with appointments for this day
        showDateAppointments(date);
    }

    private void showDateAppointments(LocalDate date) {
        List<AppointmentDto> appointments = getAppointmentsForDate(date);

        if (appointments.isEmpty()) {
            com.vaadin.flow.component.notification.Notification.show(
                "No appointments on " + date.toString(),
                2000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
            );
        } else {
            com.vaadin.flow.component.notification.Notification.show(
                appointments.size() + " appointment(s) on " + date.toString(),
                2000,
                com.vaadin.flow.component.notification.Notification.Position.BOTTOM_CENTER
            );
        }
    }

    private void refreshCalendar() {
        gridContainer.removeAll();
        gridContainer.add(createCalendarGrid());
        updateTitle();
    }
}
