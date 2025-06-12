package org.vg.markusbro.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginModel {
    private String id;
    private List<String> classNames;
    private int totalMessages;
    private int totalEntries;
}
