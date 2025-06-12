package org.vg.markusbro.core.service.plugins.vitals;

import com.itextpdf.text.DocumentException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.Utils;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportGenerator;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportParams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PluginVitalBloodGlucose extends PluginVital {

    private static final String KEY_VITAL_BLOOD_GLUCOSE = "blood_glucose";
    private static final String KEY_SELECT_PERIOD = "PluginVitalBloodGlucoseView:pick_step";

    @Override
    public String getId() {
        return "vital.blood_glucose";
    }

    private static boolean isPluginRelated(Context context) {
        return context.contains("blood") &&
                context.containsAny("glucose", "sugar");
    }

    protected String formatValue(int value) {
        return getResource("format.value.integer", value);
    }

    protected String formatValue(double value) {
        return getResource("format.value.double", value);
    }

    @Component
    public static class PluginVitalBloodGlucoseAdd extends PluginVitalBloodGlucose {

        @Override
        public double getScore(Context context) {
            return new BloodGlucoseData(context).getScore();
        }

        @Override
        public void handle(Context context) {
            BloodGlucoseData data = new BloodGlucoseData(context);
            storeVital(context, KEY_VITAL_BLOOD_GLUCOSE, data.getBloodGlucose());

            context.reply(getResource("response.entryAdded", formatValue(data.getBloodGlucose())));
        }
    }

    @Value
    static class BloodGlucoseData {
        private static final Pattern PATTERN_BLOOD_GLUCOSE = Pattern.compile("(\\d+)");
        private static final double MMOLL_TO_MGDL = 18.018;

        int bloodGlucose;
        double score;

        public BloodGlucoseData(Context context) {

            if (context.containsAny("glucose", "sugar") ||
                    context.containsAny("mg") && context.containsAny("dl") ||
                    context.containsAny("mmol")) {

                final Matcher matcher = PATTERN_BLOOD_GLUCOSE.matcher(context.getMessage());

                if (matcher.find()) {
                    this.bloodGlucose = context.containsAny("mmol")
                            ? (int) Math.round(Utils.parseDouble(matcher.group(1)) * MMOLL_TO_MGDL)
                            : Integer.parseInt(matcher.group(1));
                    this.score = SCORE_ALWAYS;
                    return;
                }
            }

            this.bloodGlucose = -1;
            this.score = SCORE_NEVER;
        }
    }

    @Component
    public static class PluginVitalBloodGlucoseAverage extends PluginVitalBloodGlucose {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAll("average")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_GLUCOSE);

            if (!entries.isEmpty()) {
                final double average = entries.stream()
                        .mapToInt(e -> Integer.parseInt(e.value()))
                        .average()
                        .getAsDouble();

                context.reply(getResource("response.average", entries.size(), formatValue(average)));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalBloodGlucoseMinMax extends PluginVitalBloodGlucose {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("min", "max")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_GLUCOSE);

            if (!entries.isEmpty()) {
                final TemporalVital min = entries.stream()
                        .min(Comparator.comparing(e -> Integer.parseInt(e.value())))
                        .get();

                final TemporalVital max = entries.stream()
                        .max(Comparator.comparing(e -> Integer.parseInt(e.value())))
                        .get();

                context.reply(getResource("response.minMax",
                        entries.size(),
                        formatValue(Integer.parseInt(min.value())),
                        Utils.getDateTimeFormatter().format(min.time()),
                        formatValue(Integer.parseInt(max.value())),
                        Utils.getDateTimeFormatter().format(max.time())
                ));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalBloodGlucoseView_step1 extends PluginVitalBloodGlucose {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("all", "list")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_GLUCOSE);

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
    public static class PluginVitalBloodGlucoseView_step2 extends PluginVitalBloodGlucose {

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
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_BLOOD_GLUCOSE, start, end);

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
                    .min(Comparator.comparing(e -> Integer.parseInt(e.value())))
                    .get();

            final TemporalVital max = entries.stream()
                    .max(Comparator.comparing(e -> Integer.parseInt(e.value())))
                    .get();

            final String average = formatValue(entries.stream()
                    .mapToInt(e -> Integer.parseInt(e.value()))
                    .average()
                    .getAsDouble());

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
