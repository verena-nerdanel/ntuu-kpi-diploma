package org.vg.markusbro.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vg.markusbro.admin.service.PluginsService;

@Controller
@RequestMapping(value = "/plugins")
public class PluginsController {

    @Autowired
    private PluginsService pluginsService;

    @GetMapping
    public String getDashboard(Model model) {
        model.addAttribute("plugins", pluginsService.getPluginsModel());
        return "plugins";
    }
}
