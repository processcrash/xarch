package com.xarch.oa.entity;

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
 * An audit log row for any approval action. Generic across business
 * types (LEAVE / EXPENSE / ...) so that a single query can render
 * a unified "my approvals" feed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("oa_approval_record")
public class ApprovalRecord implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Business type discriminator: LEAVE / EXPENSE. */
    private String businessType;

    /** Primary key of the underlying business row. */
    private Long businessId;

    /** The user who acted. */
    private Long approverId;

    /** Denormalized display name. */
    private String approverName;

    /** APPROVE / REJECT / TRANSFER. */
    private String action;

    /** Optional comment from the approver. */
    private String comment;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;
}
