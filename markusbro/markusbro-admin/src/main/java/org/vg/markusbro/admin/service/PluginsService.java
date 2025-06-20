package org.vg.markusbro.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vg.markusbro.admin.dto.PluginModel;
import org.vg.markusbro.core.service.plugins.Plugin;
import org.vg.markusbro.core.service.storage.data.DataStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PluginsService {

    @Autowired
    private List<Plugin> plugins;

    @Autowired
    private DataStorage dataStorage;

    public List<PluginModel> getPluginsModel() {
        final Map<String, List<String>> classes = new HashMap<>();

        plugins.forEach(p -> classes.computeIfAbsent(p.getId(), x -> new ArrayList<>()).add(p.getClass().getSimpleName()));

        return classes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> PluginModel.builder()
                        .id(e.getKey())
                        .classNames(e.getValue())
                        .totalEntries(dataStorage.countForPlugin(e.getKey()))
                        .build()
                )
                .toList();
    }
}
