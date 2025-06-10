package org.vg.markusbro.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class UserEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_NICKNAME")
    private String userNickname;

    @Column(name = "LAST_ACTIVE")
    private Date lastActive;

    @Column(name = "TOTAL_MESSAGES")
    private int totalMessages;

    @Column(name = "ACCESS_GENERAL")
    private boolean accessGeneral;

    @Column(name = "ACCESS_LLM")
    private boolean accessLlm;
}
