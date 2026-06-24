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
 * Leave request entity.
 *
 * <p>The status field is the workflow's state machine:
 * {@code DRAFT -> SUBMITTED -> APPROVING -> APPROVED / REJECTED / CANCELLED}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("oa_leave_request")
public class LeaveRequest implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Applicant user id. */
    private Long userId;

    /** Type of leave: SICK / PERSONAL / ANNUAL / MARRIAGE / BEREAVEMENT. */
    private String type;

    /** Inclusive start date (epoch millis). */
    private Long startDate;

    /** Inclusive end date (epoch millis). */
    private Long endDate;

    /** Number of leave days (calculated at submit). */
    private Double days;

    /** Reason text. */
    private String reason;

    /** Workflow status. */
    private String status;

    /** The approver currently expected to act. Null when terminal. */
    private Long currentApproverId;

    /** JSON-encoded array of attachment ids. */
    private String attachments;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
