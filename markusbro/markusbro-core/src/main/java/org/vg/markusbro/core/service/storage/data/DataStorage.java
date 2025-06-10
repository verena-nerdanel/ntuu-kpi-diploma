package org.vg.markusbro.core.service.storage.data;

import java.util.List;

public interface DataStorage {

    void put(long userId, String pluginId, String key, String value);

    String get(long userId, String pluginId, String key);

    List<Entry> getPrefix(long userId, String pluginId, String keyPrefix);

    void remove(long userId, String pluginId, String key);

    int count();

    int countForPlugin(String pluginId);

    int countForUser(long userId);
}
