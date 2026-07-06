package com.aruclinic.frontend.views.patient;

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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.frontend.views.MainLayout;

/**
 * Medical History view for displaying patient's medical records and history.
 */
@PageTitle("Medical History | AruClinic")
@Route(value = "patient/medical-history", layout = MainLayout.class)
@CssImport("./themes/aruclinic/patient.css")
public class MedicalHistoryView extends VerticalLayout {

    public MedicalHistoryView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createMedicalHistoryContent());
    }

    private Component createMedicalHistoryContent() {
        Div content = new Div();
        content.addClassName("aruclinic-medical-history");
        content.setWidthFull();

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-medical-history-header");

        H1 title = new H1("Medical History");
        title.addClassName("aruclinic-medical-history-title");

        Button addRecordBtn = new Button("Add Record", new Icon(VaadinIcon.PLUS));
        addRecordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRecordBtn.addClassName("aruclinic-btn");
        addRecordBtn.addClassName("aruclinic-btn-primary");
        addRecordBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/medical-history/add")));

        header.add(title, addRecordBtn);

        // Medical history timeline
        Div timeline = new Div();
        timeline.addClassName("aruclinic-medical-history-timeline");

        // Sample medical history items
        timeline.add(createMedicalHistoryItem(
            "June 1, 2025",
            "Follow-up Consultation",
            "Dr. Smith",
            "Patient reported improvement in blood pressure. Medication adjusted.",
            "primary"
        ));

        timeline.add(createMedicalHistoryItem(
            "May 15, 2025",
            "Blood Test Results",
            "Lab",
            "Cholesterol: 190 mg/dL, Glucose: 95 mg/dL, Blood Pressure: 120/80 mmHg",
            "success"
        ));

        timeline.add(createMedicalHistoryItem(
            "April 20, 2025",
            "Annual Physical Exam",
            "Dr. Johnson",
            "General health checkup. All vitals normal. Recommended annual follow-up.",
            "warning"
        ));

        timeline.add(createMedicalHistoryItem(
            "March 10, 2025",
            "X-Ray - Chest",
            "Radiology",
            "Chest X-ray showed no abnormalities. Clear lungs and heart.",
            "success"
        ));

        timeline.add(createMedicalHistoryItem(
            "February 5, 2025",
            "Vaccination - Flu Shot",
            "Nurse",
            "Seasonal flu vaccination administered. No adverse reactions reported.",
            "primary"
        ));

        timeline.add(createMedicalHistoryItem(
            "January 15, 2025",
            "Diagnosis - Hypertension",
            "Dr. Smith",
            "Diagnosed with stage 1 hypertension. Prescribed Lisinopril 10mg daily.",
            "danger"
        ));

        content.add(header, timeline);
        return content;
    }

    private Component createMedicalHistoryItem(String date, String title, String provider, String description, String type) {
        Div item = new Div();
        item.addClassName("aruclinic-medical-history-item");

        Div dateDiv = new Div();
        dateDiv.addClassName("aruclinic-medical-history-item-date");
        dateDiv.setText(date);

        Div card = new Div();
        card.addClassName("aruclinic-medical-history-item-card");

        Div cardTitle = new Div();
        cardTitle.addClassName("aruclinic-medical-history-item-title");
        cardTitle.setText(title);

        Div cardDescription = new Div();
        cardDescription.addClassName("aruclinic-medical-history-item-description");
        cardDescription.setText(description);

        Div cardMeta = new Div();
        cardMeta.addClassName("aruclinic-medical-history-item-meta");
        cardMeta.setText("Provider: " + provider);

        card.add(cardTitle, cardDescription, cardMeta);
        item.add(dateDiv, card);

        return item;
    }
}
