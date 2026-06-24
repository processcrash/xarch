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
 * Contact entity. A customer can have many contacts; exactly one
 * is flagged as primary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crm_contact")
public class Contact implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long customerId;

    private String name;

    private String position;

    private String phone;

    private String email;

    /** True for the contact that should be used as the default addressee. */
    private Boolean isPrimary;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
