package com.aruclinic.view.receptionist;

import com.aruclinic.view.admin.AdminAppointmentListView;
import com.aruclinic.service.AdminService;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

@PageTitle("Appointment Management | AruClinic")
@Route(value = "receptionist/appointments", layout = MainLayout.class)
public class ReceptionistAppointmentListView extends AdminAppointmentListView {
    
    private static final long serialVersionUID = 1L;

    public ReceptionistAppointmentListView(AdminService adminService,
                                           com.aruclinic.service.AppointmentService appointmentService,
                                           com.aruclinic.service.DoctorService doctorService) {
         super(adminService, appointmentService, doctorService);
     }
}
