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

@PageTitle("Access Denied | AruClinic")
@Route("access-denied")
@CssImport("./themes/aruclinic/common.css")
public class AccessDeniedView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public AccessDeniedView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);

        add(createAccessDeniedContent());
    }

    private Component createAccessDeniedContent() {
        Div content = new Div();
        content.addClassName("aruclinic-error-page");

        Div iconDiv = new Div();
        iconDiv.addClassName("aruclinic-error-icon");
        iconDiv.add(new Icon(VaadinIcon.LOCK));

        H1 title = new H1("Access Denied");
        title.addClassName("aruclinic-error-title");

        Paragraph description = new Paragraph("You don't have permission to access this page.");
        description.addClassName("aruclinic-error-description");

        Div actions = new Div();
        actions.addClassName("aruclinic-error-actions");

        Button homeButton = new Button("Go to Home", new Icon(VaadinIcon.HOME));
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClassName("aruclinic-btn");
        homeButton.addClassName("aruclinic-btn-primary");
        homeButton.addClassName("aruclinic-btn-lg");
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        Button loginButton = new Button("Login", new Icon(VaadinIcon.SIGN_IN));
        loginButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        loginButton.addClassName("aruclinic-btn");
        loginButton.addClassName("aruclinic-btn-success");
        loginButton.addClassName("aruclinic-btn-lg");
        loginButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("auth/login")));

        actions.add(homeButton, loginButton);

        content.add(iconDiv, title, description, actions);
        return content;
    }
}