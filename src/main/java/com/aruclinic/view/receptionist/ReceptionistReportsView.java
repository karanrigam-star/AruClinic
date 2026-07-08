package com.aruclinic.view.receptionist;

import com.aruclinic.service.AdminService;
import com.aruclinic.view.admin.AdminReportsView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.aruclinic.view.MainLayout;

/**
 * Receptionist-facing Reports view mapped to receptionist/reports.
 */
@PageTitle("Reports Module | AruClinic")
@Route(value = "receptionist/reports", layout = MainLayout.class)
public class ReceptionistReportsView extends AdminReportsView {
    
    private static final long serialVersionUID = 1L;

    public ReceptionistReportsView(AdminService adminService) {
        super(adminService);
    }
}
