package org.vg.markusbro.core.service.plugins;

import org.vg.markusbro.core.entity.UserEntity;

import java.io.File;
import java.util.List;

public interface Context {

    long getUserId();

    UserEntity getUser();

    String getUserName();

    String getMessage();

    boolean contains(String keyword);

    boolean containsAll(String... keyword);

    boolean containsAny(String... keyword);

    void reply(String text);

    void replyWithOptions(String text, List<String> options);

    void removeOptions(String message);

    void sendFile(File file, String userFileName, boolean removeKeyboard);
}
