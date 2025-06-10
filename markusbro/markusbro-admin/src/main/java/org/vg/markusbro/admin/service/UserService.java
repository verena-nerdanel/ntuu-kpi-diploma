package org.vg.markusbro.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vg.markusbro.admin.dto.AccessType;
import org.vg.markusbro.admin.dto.UserInfo;
import org.vg.markusbro.core.entity.UserEntity;
import org.vg.markusbro.core.repository.UserRepository;
import org.vg.markusbro.core.service.storage.data.DataStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataStorage dataStorage;

    public List<UserInfo> getUserInfos() {
        final List<UserInfo> data = new ArrayList<>();

        for (UserEntity entity : userRepository.findAll()) {
            data.add(UserInfo.builder()
                    .id(entity.getUserId())
                    .nickname(entity.getUserNickname())
                    .totalMessages(entity.getTotalMessages())
                    .totalEntries(dataStorage.countForUser(entity.getUserId()))
                    .lastActive(entity.getLastActive())
                    .accessGeneral(entity.isAccessGeneral())
                    .accessLlm(entity.isAccessLlm())
                    .build());
        }

        return data;
    }

    public void updateUserAccess(long userId, AccessType access, boolean value) {
        final UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        switch (access) {
            case GENERAL -> {
                user.setAccessGeneral(value);
                userRepository.save(user);
            }

            case LLM -> {
                user.setAccessLlm(value);
                userRepository.save(user);
            }

            default -> throw new UnsupportedOperationException("Unknown access type: " + access);
        }
    }
}
