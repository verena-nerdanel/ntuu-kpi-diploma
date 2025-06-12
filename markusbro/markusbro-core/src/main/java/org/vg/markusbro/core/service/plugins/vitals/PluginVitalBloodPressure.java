package org.vg.markusbro.core.service.plugins.vitals;

import com.itextpdf.text.DocumentException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.vg.markusbro.core.Utils;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportGenerator;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportParams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PluginVitalBloodPressure extends PluginVital {

    private static final String KEY_VITAL_BLOOD_PRESSURE = "blood_pressure";
    private static final String KEY_SELECT_PERIOD = "PluginVitalBloodPressureView:pick_step";

    @Override
    public String getId() {
        return "vital.blood_pressure";
    }

    private static boolean isPluginRelated(Context context) {
        return context.containsAll("blood", "press") || context.contains("tension");
    }

    private static int getPressureSys(TemporalVital e) {
        return Integer.parseInt(StringUtils.substringBefore(e.value(), "/"));
    }

    private static int getPressureDia(TemporalVital e) {
        return Integer.parseInt(StringUtils.substringAfter(e.value(), "/"));
    }

    @Component
    public static class PluginVitalBloodPressureAdd extends PluginVitalBloodPressure {

        @Override
        public double getScore(Context context) {
            return new BloodPressureData(context.getMessage()).getScore();
        }

        @Override
        public void handle(Context context) {
            final BloodPressureData data = new BloodPressureData(context.getMessage());
            final String value = data.getBloodSys() + "/" + data.getBloodDia();
            storeVital(context, KEY_VITAL_BLOOD_PRESSURE, value);
            context.reply(getResource("response.entryAdded", value));
        }
    }

    @Value
    static class BloodPressureData {
        private static final Pattern PATTERN_BLOOD_PRESSURE = Pattern.compile("(\\d+)/(\\d+)");

        int bloodSys;
        int bloodDia;
        double score;

        public BloodPressureData(String text) {
            if (text != null) {
                final Matcher matcher = PATTERN_BLOOD_PRESSURE.matcher(text);

                if (matcher.find()) {
                    this.bloodSys = Integer.parseInt(matcher.group(1));
                    this.bloodDia = Integer.parseInt(matcher.group(2));
                    this.score = SCORE_ALWAYS;
                    return;
                }
            }

            this.bloodSys = 0;
            this.bloodDia = 0;
            this.score = SCORE_NEVER;
        }
    }

    @Component
    public static class PluginVitalBloodPressureAverage extends PluginVitalBloodPressure {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.contains("average")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_PRESSURE);

            if (!entries.isEmpty()) {
                final double avgSys = entries.stream()
                        .mapToInt(PluginVitalBloodPressure::getPressureSys)
                        .average()
                        .getAsDouble();

                final double avgDia = entries.stream()
                        .mapToInt(PluginVitalBloodPressure::getPressureDia)
                        .average()
                        .getAsDouble();

                context.reply(getResource("response.average", entries.size(), avgSys, avgDia));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalBloodPressureMinMax extends PluginVitalBloodPressure {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("min", "max")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_PRESSURE);

            if (!entries.isEmpty()) {
                final List<Integer> valuesSys = entries.stream()
                        .map(PluginVitalBloodPressure::getPressureSys)
                        .toList();

                final List<Integer> valuesDia = entries.stream()
                        .map(PluginVitalBloodPressure::getPressureDia)
                        .toList();

                final int minSys = Collections.min(valuesSys);
                final int maxSys = Collections.max(valuesSys);
                final int minDia = Collections.min(valuesDia);
                final int maxDia = Collections.max(valuesDia);

                context.reply(getResource("response.minMax", entries.size(), minSys, minDia, maxSys, maxDia));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalBloodPressureView_step1 extends PluginVitalBloodPressure {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("all", "list")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_PRESSURE);

            if (!entries.isEmpty()) {
                context.replyWithOptions(getResource("report.selectPeriod"), Arrays.asList(
                        OPTION_PERIOD_LAST_WEEK,
                        OPTION_PERIOD_LAST_MONTH,
                        OPTION_PERIOD_ALL,
                        OPTION_CANCEL
                ));

                writeValue(context, KEY_SELECT_PERIOD, System.currentTimeMillis());
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Slf4j
    @Component
    public static class PluginVitalBloodPressureView_step2 extends PluginVitalBloodPressure {

        @Override
        public double getScore(Context context) {
            final String value = readValue(context, KEY_SELECT_PERIOD);
            if (value == null) {
                return SCORE_NEVER;
            }

            removeValue(context, KEY_SELECT_PERIOD);
            return SCORE_ALWAYS;
        }

        @Override
        public void handle(Context context) {
            if (OPTION_PERIOD_LAST_WEEK.equals(context.getMessage())) {
                sendReport(context, Utils.getDateLastWeek(), null);
            } else if (OPTION_PERIOD_LAST_MONTH.equals(context.getMessage())) {
                sendReport(context, Utils.getDateLastMonth(), null);
            } else if (OPTION_PERIOD_ALL.equals(context.getMessage())) {
                sendReport(context, null, null);
            } else {
                context.removeOptions(getResource("report.cancelled"));
            }
        }

        private void sendReport(Context context, Date start, Date end) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_PRESSURE, start, end);

            if (entries.isEmpty()) {
                context.removeOptions(getResource("response.nothingFound"));
                return;
            }

            final Date actualStart = start != null
                    ? start
                    : entries.stream().map(TemporalVital::time).min(Date::compareTo).get();

            final Date actualEnd = end != null
                    ? end
                    : entries.stream().map(TemporalVital::time).max(Date::compareTo).get();

            final TemporalVital min = entries.stream()
                    .min(Comparator.comparing(PluginVitalBloodPressure::getPressureDia))
                    .get();

            final TemporalVital max = entries.stream()
                    .max(Comparator.comparing(PluginVitalBloodPressure::getPressureSys))
                    .get();

            final double avgSys = entries.stream()
                    .mapToInt(PluginVitalBloodPressure::getPressureSys)
                    .average()
                    .getAsDouble();

            final double avgDia = entries.stream()
                    .mapToInt(PluginVitalBloodPressure::getPressureDia)
                    .average()
                    .getAsDouble();

            final String average = String.format("%.0f/%.0f", avgSys, avgDia);

            File tempFile = null;
            try {
                tempFile = Files.createTempFile(String.valueOf(context.getUserId()), "").toFile();

                VitalReportGenerator.generateReport(tempFile, VitalReportParams.builder()
                        .reportTitle(getResource("report.title"))
                        .reportPeriodStart(actualStart)
                        .reportPeriodEnd(actualEnd)
                        .minValue(min)
                        .maxValue(max)
                        .averageValue(average)
                        .data(entries)
                        .userId(context.getUserId())
                        .userName(context.getUserName())
                        .build());

                final String timestamp = LocalDateTime.now().format(Utils.getFileNameFormatter());
                final String fileName = getResource("report.title") + " (" + timestamp + ").pdf";
                context.sendFile(tempFile, fileName, true);
            } catch (IOException | DocumentException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (tempFile != null) {
                    tempFile.deleteOnExit();
                }
            }
        }
    }
}
