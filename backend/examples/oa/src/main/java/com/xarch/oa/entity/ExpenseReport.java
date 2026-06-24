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
 * Expense report entity. The {@code items} column holds a JSON array of
 * {@link ExpenseItem} objects decoded by the service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("oa_expense_report")
public class ExpenseReport implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Applicant user id. */
    private Long userId;

    /** Category: TRAVEL / MEAL / OFFICE / TRAINING / OTHER. */
    private String category;

    /** Total amount, summed from {@code items} at submit. */
    private java.math.BigDecimal amount;

    /** Three-letter currency code, e.g. {@code CNY}. */
    private String currency;

    /** Free-form description. */
    private String description;

    /** JSON array of {@link ExpenseItem}. */
    private String items;

    /** Workflow status. */
    private String status;

    /** The approver currently expected to act. */
    private Long approverId;

    /** Date the report was reimbursed (epoch millis). */
    private Long reimbursementDate;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
