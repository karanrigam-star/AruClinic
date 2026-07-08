
# ARUCLINIC_ADMIN_MASTER_SPEC.md

# Purpose
This document is the master specification for the complete Admin Module of AruClinic.
Reuse the existing Vaadin theme, CSS, icons, layouts and backend architecture.
Never redesign the UI. Never use mock data.

---

# Global Rules

- Reuse existing DTOs, Services, Entities and Repositories.
- Never duplicate backend logic.
- Every page must use real database data.
- Compile after every feature.
- Verify SQL after every module.
- Preserve all existing functionality.
- Responsive on Desktop, Tablet and Mobile.

---

# Access

Only ROLE_ADMIN can access:

- Dashboard
- Users
- Doctors
- Receptionists
- Patients
- Appointments
- Billing
- Reports
- Notifications
- Audit Logs
- Clinic Settings

Other roles must receive Access Denied.

---

# Admin Dashboard

Cards:
- Total Patients
- Total Doctors
- Total Receptionists
- Total Users
- Today's Appointments
- Waiting Patients
- Completed Consultations
- Revenue Today
- Revenue This Month
- Pending Bills
- New Registrations

Charts:
- Daily Appointments
- Monthly Revenue
- Patient Registrations
- Doctor Workload

Quick Actions:
- Add Doctor
- Add Receptionist
- Add User
- Register Patient
- Book Appointment
- Generate Invoice
- Reports

Global Search:
Search Users, Patients, Doctors, Prescriptions, Appointments and Invoices using server-side database search.

---

# User Management

Features:
- Add User
- Edit User
- Disable User
- Activate User
- Reset Password
- Search
- Filter by Role
- Lock / Unlock Account
- Assign Roles

Roles:
- Admin
- Doctor
- Receptionist

Patients are managed in Patient Management.

---

# Doctor Management

Features:
- Add Doctor
- Edit Doctor
- Delete (Soft Delete)
- View Profile
- Specialization
- Consultation Fee
- Working Hours
- Weekly Schedule
- Availability
- Leave Management
- Status

---

# Receptionist Management

Features:
- Add Receptionist
- Edit
- Disable
- Shift Management
- Status
- Contact Information

---

# Patient Management

Features:
- Register Patient
- Search
- Edit
- View
- Appointment History
- Billing History
- Medical History (Read Only)
- Documents

---

# Appointment Management

Features:
- Calendar View
- Daily Schedule
- Weekly Schedule
- Monthly View
- Assign Doctor
- Reschedule
- Cancel
- Search
- Filters
- Queue Management

Statuses:
- Scheduled
- Checked In
- Waiting
- In Consultation
- Completed
- Cancelled
- No Show

---

# Billing Module

Features:
- Generate Invoice
- Payments
- Refunds
- Discounts
- Pending Bills
- Revenue
- Print Invoice
- Download PDF

---

# Reports Module

Reports:
- Patients
- Doctors
- Revenue
- Billing
- Appointments
- Prescriptions
- Daily Activity
- Monthly Activity

Export:
- PDF
- Excel

---

# Notifications

Features:
- System Notifications
- Appointment Notifications
- Billing Notifications
- Prescription Notifications

---

# Audit Logs

Track:
- User
- Action
- Module
- Timestamp
- Status

Examples:
- User Created
- Login
- Logout
- Appointment Updated
- Prescription Created
- Billing Generated

---

# Clinic Settings

Manage:
- Clinic Name
- Logo
- Address
- Phone
- Email
- Working Hours
- Consultation Fees
- Email Configuration
- SMS Configuration
- Language
- Theme

---

# Security

- Session Timeout
- Password Policy
- Login Attempts
- JWT Configuration
- Future Two-Factor Authentication

---

# UI Standards

- Reuse existing styles.css
- Create modular CSS only if required
- Keep same colors
- Same icons
- Same typography
- Same spacing
- Responsive layout
- Professional healthcare appearance

---

# Backend Rules

- Views call Services only.
- Services call Repositories.
- No Repository access from Views.
- No duplicate Entities, DTOs or Services.
- Transactional methods where required.

---

# SQL Verification

Verify:
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM patients;
SELECT COUNT(*) FROM doctors;
SELECT COUNT(*) FROM appointments;
SELECT COUNT(*) FROM prescriptions;
SELECT COUNT(*) FROM invoices;

---

# AI Development Rules

1. Inspect existing code before coding.
2. Reuse existing backend.
3. Preserve existing functionality.
4. Never create mock data.
5. Build one module at a time.
6. Compile after every feature.
7. Verify SQL after every feature.
8. Hide unfinished modules.
9. Keep the same AruClinic theme and CSS.
10. Finish one module completely before starting the next.

---

# Recommended Build Order

1. Admin Dashboard
2. User Management
3. Doctor Management
4. Receptionist Management
5. Patient Management
6. Appointment Management
7. Billing
8. Reports
9. Notifications
10. Audit Logs
11. Clinic Settings
12. Security Enhancements

---

# Definition of Done

- Builds successfully
- Uses real database data
- Role security enforced
- Responsive UI
- Existing functionality preserved
- No duplicate code
- Navigation works
- SQL verified
- Ready for production
