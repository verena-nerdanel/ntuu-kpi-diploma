package org.vg.markusbro.core.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vg.markusbro.core.entity.StorageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRepository extends CrudRepository<StorageEntity, Long> {

    Optional<StorageEntity> findByUserIdAndPluginIdAndKey(long userId, String pluginId, String key);

    @Query("SELECT e FROM StorageEntity e WHERE e.userId = :userId AND e.pluginId = :pluginId AND e.key LIKE :keyPrefix || '%'")
    List<StorageEntity> findByUserIdAndPluginIdAndKeyStarting(long userId, String pluginId, String keyPrefix);

    void deleteByUserIdAndPluginIdAndKey(long userId, String pluginId, String key);

    long countByPluginId(String pluginId);

    long countByUserId(long userId);
}
