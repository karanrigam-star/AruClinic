package com.aruclinic.view.admin;

import com.aruclinic.service.AdminService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

@PageTitle("Reports Module | AruClinic")
@Route(value = "admin/reports", layout = MainLayout.class)
@CssImport("./themes/aruclinic/common.css")
public class AdminReportsView extends VerticalLayout {

    private final AdminService adminService;

    public AdminReportsView(AdminService adminService) {
        this.adminService = adminService;

        setSizeFull();
        setPadding(true);
        setClassName("aruclinic-admin-reports-view");

        add(createHeader(), createReportsGrid());
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Clinic Reports & Analytics");
        title.getStyle().set("margin", "0").set("font-size", "var(--aruclinic-font-size-2xl)");

        header.add(title);
        return header;
    }

    private Component createReportsGrid() {
        Div grid = new Div();
        grid.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "20px")
                .set("width", "100%")
                .set("margin-top", "24px");

        grid.add(createReportCard("Patients Summary Report", "Registered demographics, age groupings, and history summaries.", String.valueOf(adminService.getTotalPatients()) + " total records", VaadinIcon.HOSPITAL));
        grid.add(createReportCard("Doctors Workload Report", "Patient appointments per doctor, active consultants list.", String.valueOf(adminService.getTotalDoctors()) + " active doctors", VaadinIcon.DOCTOR));
        grid.add(createReportCard("Revenue & Finance Analytics", "Gross income, billing details, payments received, and tax reports.", "₹" + String.format("%.2f", adminService.getRevenueThisMonth()) + " MTD", VaadinIcon.MONEY));
        grid.add(createReportCard("Appointments & Schedule Report", "Booked vs completed consultation stats, cancellation logs.", String.valueOf(adminService.getTodaysAppointments()) + " today", VaadinIcon.CLOCK));

        return grid;
    }

    private Component createReportCard(String title, String desc, String stat, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle().set("background", "white")
                .set("border-radius", "12px")
                .set("padding", "24px")
                .set("box-shadow", "0 4px 6px -1px rgb(0 0 0 / 0.1)")
                .set("border", "1px solid #E2E8F0");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon i = new Icon(icon);
        i.getStyle().set("color", "var(--aruclinic-primary)");
        H2 t = new H2(title);
        t.getStyle().set("font-size", "18px").set("margin", "0");
        header.add(i, t);

        Span d = new Span(desc);
        d.getStyle().set("font-size", "13px").set("color", "#64748B").set("display", "block").set("margin", "12px 0");

        Div footer = new Div();
        footer.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center").set("margin-top", "16px");

        Span s = new Span(stat);
        s.getStyle().set("font-size", "14px").set("font-weight", "600").set("color", "var(--aruclinic-primary)");

        HorizontalLayout actions = new HorizontalLayout();
        Anchor pdfAnchor = new Anchor(new StreamResource(title.replace(" ", "_") + ".pdf", () -> {
            return PdfHelper.generateReportPdf(title, desc, stat);
        }), "");
        pdfAnchor.getElement().setAttribute("download", true);
        Button pdfBtn = new Button("PDF", new Icon(VaadinIcon.DOWNLOAD));
        pdfBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        pdfAnchor.add(pdfBtn);

        Button excelBtn = new Button("Excel");
        excelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        excelBtn.addClickListener(e -> Notification.show("Exporting report as Excel...", 2000, Notification.Position.TOP_CENTER));

        actions.add(pdfAnchor, excelBtn);
        footer.add(s, actions);

        card.add(header, d, footer);
        return card;
    }
}
