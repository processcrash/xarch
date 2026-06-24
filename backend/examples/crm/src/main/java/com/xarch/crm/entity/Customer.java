package com.xarch.crm.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Customer entity. Type is the lifecycle: LEAD / PROSPECT / CUSTOMER / LOST.
 * Tags are stored as a JSON array of strings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crm_customer")
public class Customer implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    /** Lifecycle: LEAD / PROSPECT / CUSTOMER / LOST. */
    private String type;

    private String industry;

    /** Company size label: SMALL / MEDIUM / LARGE / ENTERPRISE. */
    private String scale;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String website;

    /** Sales owner id. */
    private Long ownerId;

    /** Acquisition source. */
    private String source;

    /** A / B / C / D - higher = hotter. */
    private String level;

    /** ACTIVE / INACTIVE. */
    private String status;

    /** JSON array of free-form tags. */
    private String tags;

    /** Last contact timestamp (epoch millis). */
    private Long lastContactTime;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
