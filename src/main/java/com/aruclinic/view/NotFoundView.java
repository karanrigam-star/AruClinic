package com.aruclinic.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

/**
 * Not Found view (404) displayed when a page is not found.
 */
@PageTitle("Page Not Found | AruClinic")
@Route("404")
@CssImport("./themes/aruclinic/common.css")
public class NotFoundView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

	public NotFoundView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);

        add(createNotFoundContent());
    }

    private Component createNotFoundContent() {
        Div content = new Div();
        content.addClassName("aruclinic-error-page");

        // Icon
        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-error-icon");
        iconDiv.add(new Icon(VaadinIcon.SEARCH));

        // Error code
        H1 errorCode = new H1("404");
        errorCode.addClassName("aruclinic-error-code");

        // Title
        H1 title = new H1("Page Not Found");
        title.addClassName("aruclinic-error-title");

        // Description
        Paragraph description = new Paragraph("The page you're looking for doesn't exist or has been moved.");
        description.addClassName("aruclinic-error-description");

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-error-actions");

        Button homeButton = new Button("Go to Home", new Icon(VaadinIcon.HOME));
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClassName("aruclinic-btn");
        homeButton.addClassName("aruclinic-btn-primary");
        homeButton.addClassName("aruclinic-btn-lg");
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        Button contactButton = new Button("Contact Support", new Icon(VaadinIcon.ENVELOPE));
        contactButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        contactButton.addClassName("aruclinic-btn");
        contactButton.addClassName("aruclinic-btn-secondary");
        contactButton.addClassName("aruclinic-btn-lg");
        contactButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("contact")));

        actions.add(homeButton, contactButton);

        content.add(iconDiv, errorCode, title, description, actions);
        return content;
    }
}
