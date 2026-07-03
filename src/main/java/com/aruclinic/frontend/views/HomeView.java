package com.aruclinic.frontend.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import com.aruclinic.entity.User;

/**
 * Root view of the application that handles role-based redirection.
 */
@Route("")
@AnonymousAllowed
public class HomeView extends Div implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String role = "PATIENT";
        boolean isAuthenticated = false;

        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                role = ((User) principal).getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName())
                    .orElse("PATIENT");
                isAuthenticated = true;
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                isAuthenticated = true;
            }
        } catch (Exception e) {
            // Not authenticated
        }

        if (!isAuthenticated) {
            event.forwardTo("auth/login");
        } else {
            switch (role) {
                case "SUPER_ADMIN":
                    event.forwardTo("admin");
                    break;
                case "DOCTOR":
                    event.forwardTo("doctor");
                    break;
                case "RECEPTIONIST":
                    event.forwardTo("receptionist");
                    break;
                case "PATIENT":
                default:
                    event.forwardTo("patient");
                    break;
            }
        }
    }
}
