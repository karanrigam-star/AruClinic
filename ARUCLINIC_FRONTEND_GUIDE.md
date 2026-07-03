# ARUCLINIC_FRONTEND_GUIDE.md

## Purpose

This document extends the existing design standards specifically for
**AruClinic**, a **Vaadin 24 + Spring Boot + Java** application.

The existing rules remain valid. The following rules are **mandatory**
for every generated screen.

# Technology

-   Vaadin 24 Flow
-   Java 21
-   Spring Boot
-   Spring Security
-   JWT
-   MySQL
-   CSS only
-   No React
-   No HTML templates
-   No Lit
-   No Polymer

Everything must be built using Java Vaadin components.

# UI Philosophy

Build a premium healthcare application similar in quality to:

-   Practo
-   Apollo 24/7
-   Cerner
-   Epic EMR
-   Microsoft Fluent Healthcare

Avoid CRUD-looking screens.

# Project Structure

``` text
com.aruclinic.frontend
├── components
├── layouts
├── views
│   ├── auth
│   ├── dashboard
│   ├── patient
│   ├── doctor
│   ├── receptionist
│   ├── appointment
│   ├── billing
│   ├── prescription
│   ├── notification
│   ├── settings
│   └── dev
├── css
└── util
```

# Mandatory Views

Authentication: - Login - Register - OTP Verification - Forgot
Password - Reset Password

Dashboards: - Super Admin - Doctor - Receptionist - Patient

Modules: - Patient Registration - Patient Profile - Appointment
Booking - Calendar - Doctor Schedule - Prescription - Medical History -
Billing - Invoice - Payments - Notifications - Profile - Settings -
Change Password - Developer OTP Console - 404 - Access Denied

# Design System

Primary: #0F6CBD

Success: #22C55E

Danger: #EF4444

Warning: #F59E0B

Background: #F8FAFC

Cards: White

Border Radius: 12px

Soft shadows only.

Spacing must be consistent.

Fully responsive: - 360 - 480 - 768 - 1024 - 1440 - 1920

# CSS

Create modular CSS.

Example:

-   common.css
-   login-view.css
-   dashboard.css
-   patient.css
-   appointment.css
-   billing.css

Do not place all CSS in styles.css.

styles.css should only import shared styles.

# Every View Must Include

-   Route
-   PageTitle
-   Responsive layout
-   Validation
-   Accessibility
-   Keyboard navigation
-   Loading indicator
-   Success/Error notifications
-   Icons
-   Mobile friendly design

# Backend Rules

Never create fake services.

Always use existing Spring Services.

If backend functionality is missing, create the proper service,
repository and DTO instead of mocking data.

# Quality Checklist

Before finishing every screen:

-   Project compiles.
-   No warnings.
-   No duplicate code.
-   Imports optimized.
-   Route works.
-   Responsive.
-   Uses existing CSS naming.
-   Clean Java architecture.

# Delivery Strategy

Generate ONE view at a time.

After each view:

1.  Verify compilation.
2.  Verify routing.
3.  Verify CSS.
4.  Verify responsiveness.
5.  Continue only after the current screen works.
