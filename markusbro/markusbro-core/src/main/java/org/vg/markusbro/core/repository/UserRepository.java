package org.vg.markusbro.core.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vg.markusbro.core.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity findByUserId(long userId);

    @Modifying
    @Query("UPDATE UserEntity u SET u.totalMessages = u.totalMessages + 1, u.lastActive = NOW() WHERE u.userId = :userId")
    void incrementCounters(long userId);

    @Query("SELECT IFNULL(SUM(totalMessages), 0) FROM UserEntity")
    int getTotalMessages();
}
