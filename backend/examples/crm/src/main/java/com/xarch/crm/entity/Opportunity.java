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
import java.math.BigDecimal;

/**
 * Opportunity entity.
 *
 * <p>Stage is the funnel position:
 * QUALIFICATION -> NEEDS_ANALYSIS -> PROPOSAL -> NEGOTIATION -> WON / LOST.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crm_opportunity")
public class Opportunity implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private String name;

    private BigDecimal amount;

    private String currency;

    /** Funnel stage. */
    private String stage;

    /** Win probability 0-100. */
    private Integer probability;

    /** Expected close date (epoch millis). */
    private Long expectedCloseDate;

    private Long ownerId;

    private String description;

    /** OPEN / WON / LOST. */
    private String status;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
