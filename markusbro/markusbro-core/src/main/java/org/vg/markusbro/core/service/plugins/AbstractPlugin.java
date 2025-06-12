package org.vg.markusbro.core.service.plugins;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vg.markusbro.core.service.storage.data.DataStorage;
import org.vg.markusbro.core.service.storage.data.Entry;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractPlugin implements Plugin {

    @Autowired
    private DataStorage dataStorage;

    protected void writeValue(Context context, String key, String value) {
        dataStorage.put(context.getUserId(), getId(), key, value);
    }

    protected void writeValue(Context context, String key, int value) {
        dataStorage.put(context.getUserId(), getId(), key, String.valueOf(value));
    }

    protected void writeValue(Context context, String key, long value) {
        dataStorage.put(context.getUserId(), getId(), key, String.valueOf(value));
    }

    protected void writeValue(Context context, String key, double value) {
        dataStorage.put(context.getUserId(), getId(), key, String.valueOf(value));
    }

    protected String readValue(Context context, String key) {
        return dataStorage.get(context.getUserId(), getId(), key);
    }

    protected List<Entry> readValues(Context context, String keyPrefix) {
        return dataStorage.getPrefix(context.getUserId(), getId(), keyPrefix);
    }

    protected void removeValue(Context context, String key) {
        dataStorage.remove(context.getUserId(), getId(), key);
    }

    protected String getResource(String key) {
        final Locale locale = Locale.ENGLISH; // TODO: automatic switch

        Class<?> c = getClass();

        while (Plugin.class.isAssignableFrom(c)) {
            try {
                final ResourceBundle bundle = ResourceBundle.getBundle("plugins/" + c.getSimpleName(), locale);
                return injectEnvVariables(bundle.getString(key));
            } catch (MissingResourceException e) {
                // ignore and go further
            }

            c = c.getSuperclass();
        }

        log.warn("No resource found for plugin {} and key \"{}\"", getClass().getCanonicalName(), key);
        return null;
    }

    private static String injectEnvVariables(String s) {
        return Pattern.compile("\\$\\{(.+)\\}")
                .matcher(s)
                .replaceAll(mr -> {
                    String envVal = System.getenv(mr.group(1));
                    return Matcher.quoteReplacement(envVal != null ? envVal : mr.group());
                });
    }

    protected String getResource(String key, Object... args) {
        final String value = getResource(key);
        return value != null
                ? String.format(value, args)
                : null;
    }
}
