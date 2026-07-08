package com.aruclinic.view.patient;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;
import com.aruclinic.service.PatientService;
import com.aruclinic.entity.Patient;
import com.aruclinic.dto.MedicalHistoryItemDto;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Medical History view for displaying patient's medical records and history.
 */
@PageTitle("Medical History | AruClinic")
@Route(value = "patient/medical-history", layout = MainLayout.class)
@CssImport("./themes/aruclinic/patient.css")
public class MedicalHistoryView extends VerticalLayout implements BeforeEnterObserver {

    private final PatientService patientService;
    private Patient currentPatient = null;
    private final Div timeline = new Div();

    public MedicalHistoryView(PatientService patientService) {
        this.patientService = patientService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createMedicalHistoryContent());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        resolvePatient();
        refreshTimeline();
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

            // Fallback for blank setups during testing
            if (currentPatient == null) {
                List<Patient> patients = patientService.getAllPatientEntities();
                if (!patients.isEmpty()) {
                    currentPatient = patients.get(0);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
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

        header.add(title);

        // Medical history timeline
        timeline.addClassName("aruclinic-medical-history-timeline");

        content.add(header, timeline);
        return content;
    }

    private void refreshTimeline() {
        timeline.removeAll();

        if (currentPatient == null) {
            Div emptyMsg = new Div();
            emptyMsg.setText("No patient session resolved.");
            emptyMsg.getStyle().set("color", "var(--aruclinic-text-secondary)").set("font-style", "italic");
            timeline.add(emptyMsg);
            return;
        }

        List<MedicalHistoryItemDto> records = new ArrayList<>();
        String json = currentPatient.getMedicalHistory();
        if (json != null && !json.trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                records = mapper.readValue(json, new TypeReference<List<MedicalHistoryItemDto>>() {});
            } catch (Exception e) {
                // If parsing fails, log and show error or keep empty
            }
        }

        if (records.isEmpty()) {
            Div emptyMsg = new Div();
            emptyMsg.setText("No medical history records found.");
            emptyMsg.getStyle().set("color", "var(--aruclinic-text-secondary)")
                .set("font-style", "italic")
                .set("padding", "var(--aruclinic-spacing-md)")
                .set("text-align", "center");
            timeline.add(emptyMsg);
            return;
        }

        for (MedicalHistoryItemDto record : records) {
            timeline.add(createMedicalHistoryItem(
                record.getDate(),
                record.getTitle(),
                record.getDoctor(),
                record.getDetails(),
                record.getType() != null ? record.getType() : "primary"
            ));
        }
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
