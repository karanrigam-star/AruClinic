package com.aruclinic.frontend.views.receptionist;

import com.aruclinic.frontend.views.admin.AdminAppointmentListView;
import com.aruclinic.service.AdminService;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.frontend.views.MainLayout;

@PageTitle("Appointment Management | AruClinic")
@Route(value = "receptionist/appointments", layout = MainLayout.class)
public class ReceptionistAppointmentListView extends AdminAppointmentListView {
    
    private static final long serialVersionUID = 1L;

    public ReceptionistAppointmentListView(AdminService adminService,
                                           com.aruclinic.repository.AppointmentRepository appointmentRepository,
                                           com.aruclinic.repository.DoctorRepository doctorRepository) {
        super(adminService, appointmentRepository, doctorRepository);
    }
}
