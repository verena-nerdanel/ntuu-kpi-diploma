package org.vg.markusbro.core.service.plugins.vitals;

import lombok.Value;
import org.springframework.stereotype.Component;
import org.vg.markusbro.core.Utils;
import org.vg.markusbro.core.service.plugins.Context;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.vg.markusbro.core.Utils.parseDouble;

public abstract class PluginVitalTemperature extends PluginVital {

    private static final String KEY_VITAL_TEMPERATURE = "temperature";

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
    public static class PluginVitalTemperatureView extends PluginVitalTemperature {

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
                final SimpleDateFormat format = Utils.getDateTimeFormatter();

                final String result = entries.stream()
                        .sorted(Comparator.comparing(TemporalVital::time))
                        .map(e -> format.format(e.time()) + " " + e.value() + " C")
                        .collect(Collectors.joining("\n"));

                context.reply(String.format("Here are all your temperature measurements (%d):\n\n%s", entries.size(), result));
            } else {
                context.reply(getResource("response.nothingFound"));
            }
        }
    }
}
