package org.vg.markusbro.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private long id;
    private String nickname;
    private int totalMessages;
    private int totalEntries;
    private Date lastActive;
    private boolean accessGeneral;
    private boolean accessLlm;
}
