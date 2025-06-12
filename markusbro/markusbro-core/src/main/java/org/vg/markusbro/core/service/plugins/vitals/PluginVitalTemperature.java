package org.vg.markusbro.core.service.plugins.vitals;

import com.itextpdf.text.DocumentException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vg.markusbro.core.Utils;
import org.vg.markusbro.core.service.plugins.Context;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportGenerator;
import org.vg.markusbro.core.service.plugins.vitals.report.VitalReportParams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.vg.markusbro.core.Utils.parseDouble;

public abstract class PluginVitalTemperature extends PluginVital {

    private static final String KEY_VITAL_TEMPERATURE = "temperature";
    private static final String KEY_SELECT_PERIOD = "PluginVitalTemperatureView:pick_step";

    @Override
    public String getId() {
        return "vital.temperature";
    }

    private static boolean isPluginRelated(Context context) {
        return context.contains("temp");
    }

    @Component
    public static class PluginVitalTemperatureAdd extends PluginVitalTemperature {

        @Override
        public double getScore(Context context) {
            return new TemperatureData(context.getMessage()).getScore();
        }

        @Override
        public void handle(Context context) {
            final TemperatureData data = new TemperatureData(context.getMessage());
            final double value = data.getTemperature();
            storeVital(context, KEY_VITAL_TEMPERATURE, value);
            context.reply(getResource("response.entryAdded", value));
        }
    }

    @Value
    static class TemperatureData {
        private static final Pattern PATTERN_TEMPERATURE = Pattern.compile("\\d{2}[.,]\\d|\\d{2}");
        private static final double TEMPERATURE_MIN = 35.0;
        private static final double TEMPERATURE_MAX = 43.0;

        double temperature;
        double score;

        public TemperatureData(String text) {
            if (text != null) {
                final Matcher matcher = PATTERN_TEMPERATURE.matcher(text);

                if (matcher.find()) {
                    this.temperature = parseDouble(matcher.group());
                    if (temperature >= TEMPERATURE_MIN && temperature < TEMPERATURE_MAX) {
                        this.score = SCORE_ALWAYS;
                    } else {
                        this.score = SCORE_NEVER;
                    }
                    return;
                }
            }

            this.temperature = 0;
            this.score = SCORE_NEVER;
        }
    }

    @Component
    public static class PluginVitalTemperatureAverage extends PluginVitalTemperature {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.contains("average")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_TEMPERATURE);

            if (!entries.isEmpty()) {
                final double avg = entries.stream()
                        .mapToDouble(s -> parseDouble(s.value()))
                        .average()
                        .getAsDouble();

                context.reply(getResource("response.minMax", entries.size(), avg));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalTemperatureMinMax extends PluginVitalTemperature {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("min", "max")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_TEMPERATURE);

            if (!entries.isEmpty()) {
                final double min = entries.stream()
                        .mapToDouble(e -> parseDouble(e.value()))
                        .min()
                        .getAsDouble();
                final double max = entries.stream()
                        .mapToDouble(e -> parseDouble(e.value()))
                        .max()
                        .getAsDouble();

                context.reply(getResource("response.minMax", entries.size(), min, max));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }

    @Component
    public static class PluginVitalTemperatureView_step1 extends PluginVitalTemperature {

        @Override
        public double getScore(Context context) {
            return isPluginRelated(context) && context.containsAny("all", "list")
                    ? SCORE_ALWAYS
                    : SCORE_NEVER;
        }

        @Override
        public void handle(Context context) {
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_TEMPERATURE);

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
    public static class PluginVitalTemperatureView_step2 extends PluginVitalTemperature {

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
            final List<TemporalVital> entries = getVitals(context, KEY_VITAL_TEMPERATURE, start, end);

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
                    .min(Comparator.comparing(e -> Utils.parseDouble(e.value())))
                    .get();

            final TemporalVital max = entries.stream()
                    .max(Comparator.comparing(e -> Utils.parseDouble(e.value())))
                    .get();

            final String average = String.format("%.1f", entries.stream()
                    .mapToDouble(e -> Utils.parseDouble(e.value()))
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
