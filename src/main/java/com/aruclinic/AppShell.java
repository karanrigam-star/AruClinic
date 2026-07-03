package com.aruclinic;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * The AppShellConfigurator is the main configuration point for the Vaadin application.
 * It activates the custom "aruclinic" theme and configures application-level shell properties.
 */
@Theme("aruclinic")
public class AppShell implements AppShellConfigurator {
    private static final long serialVersionUID = 1L;
}
