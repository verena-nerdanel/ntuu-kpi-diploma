package org.vg.markusbro.core.service.plugins.vitals;

import org.apache.commons.lang3.StringUtils;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.service.plugins.AbstractPlugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class PluginVital extends AbstractPlugin {

    // UI
    protected final String OPTION_PERIOD_LAST_WEEK = getResource("report.period.lastWeek");
    protected final String OPTION_PERIOD_LAST_MONTH = getResource("report.period.lastMonth");
    protected final String OPTION_PERIOD_ALL = getResource("report.period.all");
    protected final String OPTION_CANCEL = getResource("report.cancel");

    protected void storeVital(Context context, String key, String value) {
        writeValue(context, buildTemporalKey(key), value);
    }

    protected void storeVital(Context context, String key, int value) {
        writeValue(context, buildTemporalKey(key), String.valueOf(value));
    }

    protected void storeVital(Context context, String key, double value) {
        writeValue(context, buildTemporalKey(key), String.valueOf(value));
    }

    protected List<TemporalVital> getVitals(Context context, String key) {
        return readValues(context, key).stream()
                .map(e -> {
                    try {
                        final String timestamp = StringUtils.substringAfter(e.key(), ":");
                        final Date time = getTimestampFormat().parse(timestamp);
                        return new TemporalVital(time, e.value());
                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(toList());
    }

    protected List<TemporalVital> getVitals(Context context, String key, Date start, Date end) {
        return getVitals(context, key).stream()
                .filter(e -> start == null || !e.time().before(start)) // inclusive
                .filter(e -> end == null || !e.time().after(end)) // inclusive
                .collect(Collectors.toList());
    }

    private String buildTemporalKey(String key) {
        final String timestamp = getTimestampFormat().format(new Date());
        return key + ":" + timestamp;
    }

    private static SimpleDateFormat getTimestampFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }
}
