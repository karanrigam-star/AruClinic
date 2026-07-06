package com.aruclinic.frontend.views.auth;

import com.aruclinic.repository.UserRepository;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles clearing security session contexts on logout request and redirects back to Home page.
 */
@Route("auth/logout")
@AnonymousAllowed
public class LogoutView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 1L;

    private final UserRepository userRepository;

    public LogoutView(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String name = "User";
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = null;
            if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                email = springUser.getUsername();
            } else if (principal instanceof com.aruclinic.entity.User user) {
                email = user.getEmail();
            } else if (principal instanceof String principalStr && !"anonymousUser".equals(principalStr)) {
                email = principalStr;
            }

            if (email != null) {
                name = userRepository.findByEmail(email)
                        .map(u -> u.getFirstName() + " " + u.getLastName())
                        .orElse(email);
            }
        } catch (Exception e) {
            // Ignore
        }

        // Clear Spring Security Context
        SecurityContextHolder.clearContext();
        
        // Save the user's name in a short-lived cookie to avoid exposing sensitive info in the URL
        try {
            String encodedName = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
            jakarta.servlet.http.Cookie logoutCookie = new jakarta.servlet.http.Cookie("aruclinic_logout_name", encodedName);
            logoutCookie.setPath("/");
            logoutCookie.setMaxAge(60); // 1 minute
            com.vaadin.flow.server.VaadinService.getCurrentResponse().addCookie(logoutCookie);
        } catch (Exception e) {
            // Ignore
        }

        // Invalidate Vaadin session and underlying HTTP session
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("SPRING_SECURITY_CONTEXT", null);
            session.setAttribute("SPRING_SECURITY_AUTHENTICATION", null);
            if (session.getSession() != null) {
                try {
                    session.getSession().invalidate();
                } catch (Exception e) {
                    // Already invalidated
                }
            }
            session.close();
        }
        
        // Redirect back to public Home Page
        event.getUI().getPage().setLocation("?logout=true");
    }
}
