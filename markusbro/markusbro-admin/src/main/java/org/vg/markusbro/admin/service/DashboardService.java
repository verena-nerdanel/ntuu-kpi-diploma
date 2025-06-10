package org.vg.markusbro.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vg.markusbro.admin.dto.DashboardModel;
import org.vg.markusbro.core.repository.UserRepository;
import org.vg.markusbro.core.service.plugins.Plugin;
import org.vg.markusbro.core.service.storage.data.DataStorage;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private List<Plugin> plugins;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private UserRepository userRepository;

    public DashboardModel getDashboard() {
        return DashboardModel.builder()
                .totalMessages(userRepository.getTotalMessages())
                .totalUsers((int) userRepository.count())
                .totalPlugins(plugins.size())
                .totalDbEntries(dataStorage.count())
                .build();
    }
}
