package org.vg.markusbro.core.service.plugins.vitals;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.vg.markusbro.core.Utils;
import org.vg.markusbro.core.service.plugins.Context;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class PluginVitalBloodPressure extends PluginVital {

    private static final String KEY_VITAL_BLOOD_PRESSURE = "blood_pressure";

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
    public static class PluginVitalBloodPressureView extends PluginVitalBloodPressure {

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
                final SimpleDateFormat format = Utils.getDateTimeFormatter();

                final String result = entries.stream()
                        .sorted(Comparator.comparing(TemporalVital::time))
                        .map(e -> format.format(e.time()) + " " + e.value())
                        .collect(Collectors.joining("\n"));

                context.reply(String.format("Here are all your blood pressure measurements (%d):\n\n%s", entries.size(), result));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }
}
