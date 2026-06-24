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
 * Follow-up entity. Logs an interaction with a customer, optionally
 * linked to a specific contact and / or opportunity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crm_follow_up")
public class FollowUp implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private Long contactId;

    private Long opportunityId;

    /** PHONE / EMAIL / MEETING / VISIT / OTHER. */
    private String type;

    private String content;

    private String result;

    /** Scheduled next follow-up (epoch millis). */
    private Long nextFollowUpDate;

    /** JSON array of attachment ids. */
    private String attachments;

    private Long userId;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
