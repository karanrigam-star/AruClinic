# AruClinic Master Development Guide (AI Development Specification)

**Project:** AruClinic – Healthcare Management System  
**Frontend:** Vaadin 24 + Java  
**Backend:** Spring Boot 3.x  
**Database:** MySQL + Flyway  
**Authentication:** JWT + OTP Verification  
**Architecture:** Layered Architecture (View → Service → Repository → Database)

---

# Purpose

This document is the **single source of truth** for all AI-assisted development of AruClinic.

Every feature must integrate with the existing application without breaking any current functionality.

The AI must **analyze the existing codebase first**, reuse available classes, and extend the project incrementally.

No module should ever be rewritten unless explicitly requested.

---

# Core Development Principles

The AI must always:

- Inspect the existing project before writing code.
- Reuse existing entities.
- Reuse DTOs.
- Reuse repositories.
- Reuse services.
- Reuse utilities.
- Reuse validators.
- Reuse security configuration.
- Reuse CSS.
- Reuse layouts.
- Reuse components.

Never create duplicate implementations.

Always extend existing functionality.

---

# Zero Regression Policy

Before modifying any code:

1. Inspect the current implementation.
2. Understand existing dependencies.
3. Preserve existing APIs.
4. Preserve existing database schema.
5. Preserve existing routes.
6. Preserve existing UI styles.
7. Preserve existing security rules.

Nothing already working should stop working.

---

# Build Rule

After completing **every feature**, the AI must ensure:

- Project compiles successfully.
- No compilation errors.
- No warnings caused by new code.
- No duplicate beans.
- No duplicate routes.
- No duplicate CSS.
- No duplicate services.
- No duplicate repositories.
- No duplicate DTOs.

Never continue until build succeeds.

---

# Backend Rules

Views must NEVER access repositories.

Correct architecture:

View

↓

Service

↓

Repository

↓

Database

Views communicate only with Services.

Repositories communicate only with the database.

Business logic belongs only inside Services.

---

# Frontend Rules

Frontend technology:

- Vaadin 24
- Java
- CSS

Never introduce:

- React
- Angular
- Vue
- Thymeleaf
- JSP

unless explicitly requested.

---

# Authentication Workflow

Guest

↓

Register

↓

Generate OTP

↓

Verify OTP

↓

Activate Account

↓

Login

↓

JWT Generated

↓

Redirect According To Role

---

# Registration Rules

Only Patients may self-register.

Doctors cannot self-register.

Receptionists cannot self-register.

Admins cannot self-register.

Admin creates:

- Doctor
- Receptionist
- Admin

Patient registration must include:

- Full Name
- Mobile Number
- Email
- Date of Birth
- Gender
- Password
- Confirm Password

After registration:

Generate OTP.

Store OTP.

Verify OTP.

Activate account.

---

# Login Rules

Allow login using:

- Email
- Mobile Number

After successful login:

Redirect according to role.

---

# Role Redirect Rules

| Role | Landing Page |
|-------|--------------|
| Patient | /patient |
| Receptionist | /receptionist |
| Doctor | /doctor |
| Admin | /admin |

Unauthorized access must redirect to:

```
Access Denied
```

---

# Role Permissions

## Guest

Can access:

- Home
- About
- Contact
- Login
- Register

Cannot access secured pages.

---

## Patient

Can access:

- Dashboard
- My Profile
- Book Appointment
- Appointment History
- Bills
- Notifications
- Medical History (Read Only)
- Change Password

Cannot access:

- Admin
- Doctor
- Receptionist

---

## Receptionist

Can access:

- Dashboard
- Register Patient
- Search Patient
- Appointment Queue
- Billing
- Payments
- Notifications

Cannot:

- Manage Users
- Change Roles
- Access Admin Settings

---

## Doctor

Can access:

- Dashboard
- Today's Schedule
- Patients
- Consultation
- Diagnosis
- Medical Notes
- Prescriptions (when implemented)
- Notifications

Cannot:

- Billing Settings
- User Management
- System Configuration

---

## Admin

Full access.

Including:

- Dashboard
- User Management
- Role Management
- Doctor Management
- Receptionist Management
- Patient Management
- Appointments
- Billing
- Reports
- Notifications
- Clinic Settings
- Audit Logs
- Developer Tools
- OTP Console

---

# Module Development Order

Modules must be implemented in this exact order.

## Phase 1

Authentication

Includes:

- Registration
- OTP
- Login
- JWT
- Role Redirect

Compile.

Verify.

---

## Phase 2

Patient Module

Includes:

- Dashboard
- Profile
- Appointment Booking
- Appointment History
- Bills
- Notifications
- Medical History

Compile.

Verify.

---

## Phase 3

Reception Module

Includes:

- Dashboard
- Patient Registration
- Search Patient
- Appointment Queue
- Billing
- Payments

Compile.

Verify.

---

## Phase 4

Doctor Module

Includes:

- Dashboard
- Schedule
- Patient List
- Consultation
- Diagnosis
- Medical Notes

Compile.

Verify.

---

## Phase 5

Prescription Module

Only after Doctor module is stable.

---

## Phase 6

Admin Module

Includes:

- Dashboard
- User Management
- Role Management
- Reports
- Settings
- Logs

Compile.

Verify.

---

## Phase 7

Billing

Compile.

Verify.

---

## Phase 8

Reports

Compile.

Verify.

---

## Phase 9

Notifications

Compile.

Verify.

---

## Phase 10

Clinic Settings

Compile.

Verify.

---

# SQL Verification Rules

After every backend change verify database.

Authentication

Check:

```
users
```

Verify:

- account created

---

Check:

```
user_roles
```

Verify:

- correct role assigned

---

Check:

```
otp_verifications
```

Verify:

- OTP generated
- verified
- expired correctly

---

Appointments

Verify:

```
appointments
```

---

Billing

Verify:

```
bills
payments
```

---

Prescription

Verify:

```
prescriptions
```

---

Audit

Verify:

```
audit_logs
```

---

# UI Standards

Use:

- Responsive Layout
- FlexLayout
- VerticalLayout
- HorizontalLayout
- Scroller
- AppLayout

Maintain existing theme.

Healthcare palette.

Consistent:

- Cards
- Buttons
- Fonts
- Icons
- Margins
- Spacing

Never redesign the application.

Only extend existing UI.

---

# CSS Rules

Reuse:

```
styles.css
```

Create modular CSS only if absolutely necessary.

Never duplicate styles.

Never override global theme unnecessarily.

---

# Validation Rules

Every form must use:

Binder Validation

Include:

- Required
- Email
- Mobile
- Password
- Date
- Numeric

Show friendly validation messages.

---

# Exception Handling

Never expose stack traces.

Display user-friendly notifications.

Log server errors.

Handle:

- Validation
- Database
- Security
- Authentication
- Authorization

---

# Navigation Rules

If a module is unfinished:

Do NOT create broken routes.

Instead:

Hide menu items.

Disable navigation.

Leave placeholders only if requested.

---

# Security Rules

Never bypass Spring Security.

Never expose admin routes.

Protect:

- APIs
- Views
- Services

Role-based authorization is mandatory.

---

# Flyway Rules

Every schema change requires:

New migration.

Never modify previous migrations already executed.

Migration names must be sequential.

Example:

```
V1__initial_schema.sql

V2__create_users.sql

V3__create_roles.sql
```

---

# Repository Rules

Repositories contain only database operations.

No business logic.

---

# Service Rules

Services contain:

- Business logic
- Validation
- Transactions

Views call only Services.

---

# View Rules

Views contain:

- UI
- Event Handling
- Binder

No database logic.

---

# Logging Rules

Log:

- Login
- Logout
- Registration
- Role Changes
- Billing
- Appointment Changes

Use proper log levels.

---

# Developer Console

Admin only.

Includes:

- OTP Viewer
- Audit Viewer
- Flyway Status
- Database Health
- Active Users
- Cache Status

---

# Code Quality Checklist

Before completing every feature verify:

- Build passes
- No compilation errors
- No duplicate code
- No duplicate entities
- No duplicate services
- No duplicate repositories
- No duplicate DTOs
- No duplicate CSS
- Routes work
- Security works
- SQL verified
- Responsive UI
- Existing functionality preserved

Only then continue.

---

# AI Development Workflow

For every feature follow this exact process:

1. Inspect existing codebase.
2. Understand architecture.
3. Reuse existing code.
4. Generate only the requested feature.
5. Compile project.
6. Fix compilation issues.
7. Verify backend with SQL.
8. Test UI navigation.
9. Verify role permissions.
10. Preserve existing functionality.
11. Commit feature mentally as complete.
12. Proceed to the next feature.

Never generate multiple unfinished modules simultaneously.

---

# Absolute AI Instructions

The AI MUST NOT:

- Rewrite existing working code.
- Duplicate classes.
- Duplicate DTOs.
- Duplicate CSS.
- Duplicate services.
- Duplicate repositories.
- Break existing APIs.
- Break navigation.
- Break authentication.
- Break role permissions.
- Introduce mock data.
- Introduce placeholder business logic.
- Create inaccessible routes.
- Replace existing backend implementations.

The AI MUST ALWAYS inspect the current codebase before making changes.

When a module is incomplete, hide navigation instead of exposing broken pages.

Generate one feature at a time.

Compile after every feature.

Verify SQL after every backend change.

Continue only when the current feature is fully functional.

This document is the master development specification for AruClinic and takes precedence over any generated assumptions.