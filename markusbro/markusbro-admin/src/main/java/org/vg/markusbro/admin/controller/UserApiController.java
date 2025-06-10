package org.vg.markusbro.admin.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.vg.markusbro.admin.dto.AccessType;
import org.vg.markusbro.admin.service.UserService;

@RestController
@RequestMapping(value = "/users")
public class UserApiController {

    @Data
    public static class RequestDto {
        private AccessType access;
        private boolean value;
    }

    @Autowired
    private UserService userService;

    @PutMapping(value = "/{userId}", consumes = "application/json")
    public void updateUser(@PathVariable("userId") long userId, @RequestBody RequestDto body) {
        userService.updateUserAccess(userId, body.getAccess(), body.isValue());
    }
}
