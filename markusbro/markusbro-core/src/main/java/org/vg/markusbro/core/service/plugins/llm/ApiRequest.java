package org.vg.markusbro.core.service.plugins.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ApiRequest {
    private String model;
    private List<Message> input;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Message {
    private Role role;
    private String content;
}

enum Role {
    user,
    assistant
}