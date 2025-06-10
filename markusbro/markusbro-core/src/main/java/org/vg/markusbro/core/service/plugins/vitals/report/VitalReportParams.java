package org.vg.markusbro.core.service.plugins.vitals.report;

import lombok.Builder;
import lombok.Getter;
import org.vg.markusbro.core.service.plugins.vitals.TemporalVital;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class VitalReportParams {
    private Long userId;
    private String userName;

    private String reportTitle;
    private Date reportPeriodStart;
    private Date reportPeriodEnd;

    private String averageValue;
    private TemporalVital minValue;
    private TemporalVital maxValue;

    private List<TemporalVital> data;
}
