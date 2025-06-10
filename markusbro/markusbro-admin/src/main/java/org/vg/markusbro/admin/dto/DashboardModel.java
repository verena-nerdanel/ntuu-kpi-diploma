package org.vg.markusbro.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardModel {
    private int totalMessages;
    private int totalUsers;
    private int totalPlugins;
    private int totalDbEntries;
}
