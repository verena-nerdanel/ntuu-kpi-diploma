package org.vg.markusbro.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STORAGE")
public class StorageEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "PLUGIN_ID")
    private String pluginId;

    @Column(name = "K")
    private String key;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "V", length = 65536)
    private String value;
}
