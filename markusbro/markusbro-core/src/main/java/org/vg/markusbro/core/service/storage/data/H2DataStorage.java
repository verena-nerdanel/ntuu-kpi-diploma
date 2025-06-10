package org.vg.markusbro.core.service.storage.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vg.markusbro.core.entity.StorageEntity;
import org.vg.markusbro.core.repository.StorageRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class H2DataStorage implements DataStorage {

    @Autowired
    private StorageRepository storageRepository;

    @Override
    public void put(long userId, String pluginId, String key, String value) {
        StorageEntity entity = storageRepository
                .findByUserIdAndPluginIdAndKey(userId, pluginId, key)
                .orElseGet(() -> StorageEntity.builder()
                        .userId(userId)
                        .pluginId(pluginId)
                        .key(key)
                        .build());

        entity.setValue(value);
        storageRepository.save(entity);
    }

    @Override
    public String get(long userId, String pluginId, String key) {
        return storageRepository.findByUserIdAndPluginIdAndKey(userId, pluginId, key)
                .map(StorageEntity::getValue)
                .orElse(null);
    }

    @Override
    public List<Entry> getPrefix(long userId, String pluginId, String keyPrefix) {
        return storageRepository.findByUserIdAndPluginIdAndKeyStarting(userId, pluginId, keyPrefix).stream()
                .map(e -> new Entry(e.getKey(), e.getValue()))
                .collect(toList());
    }

    @Override
    public void remove(long userId, String pluginId, String key) {
        storageRepository.deleteByUserIdAndPluginIdAndKey(userId, pluginId, key);
    }

    @Override
    public int count() {
        return (int) storageRepository.count();
    }

    @Override
    public int countForPlugin(String pluginId) {
        return (int) storageRepository.countByPluginId(pluginId);
    }

    @Override
    public int countForUser(long userId) {
        return (int) storageRepository.countByUserId(userId);
    }
}
