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
 * Contract entity. Status is a simple lifecycle:
 * DRAFT -> ACTIVE -> EXPIRED / TERMINATED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crm_contract")
public class Contract implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private Long opportunityId;

    private String contractNo;

    private BigDecimal amount;

    private Long startDate;

    private Long endDate;

    private String paymentTerms;

    /** DRAFT / ACTIVE / EXPIRED / TERMINATED. */
    private String status;

    private Long signedDate;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
