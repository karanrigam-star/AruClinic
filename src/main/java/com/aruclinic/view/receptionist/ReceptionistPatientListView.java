package com.aruclinic.view.receptionist;

import com.aruclinic.service.AdminService;
import com.aruclinic.view.MainLayout;
import com.aruclinic.view.admin.AdminPatientListView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Patient Management | AruClinic")
@Route(value = "receptionist/patients", layout = MainLayout.class)
public class ReceptionistPatientListView extends AdminPatientListView {

    public ReceptionistPatientListView(AdminService adminService, com.aruclinic.service.LocationService locationService) {
        super(adminService, locationService);
    }
}
