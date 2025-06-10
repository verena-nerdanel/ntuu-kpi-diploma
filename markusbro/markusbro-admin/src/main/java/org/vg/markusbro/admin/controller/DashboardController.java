package org.vg.markusbro.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vg.markusbro.admin.service.DashboardService;

@Controller
@RequestMapping
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping(path = {"/"})
    public String getDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping(path = {"/dashboard"})
    public String getDashboard(Model model) {
        model.addAttribute("data", dashboardService.getDashboard());
        return "dashboard";
    }
}
