package com.xarch.cms.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Category entity. Forms a tree via {@link #parentId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cms_category")
public class Category implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Display name. */
    private String name;

    /** URL-friendly unique slug. */
    private String slug;

    /** Parent category id, 0 for root. */
    private Long parentId;

    /** Sort order (smaller = earlier). */
    private Integer sortOrder;

    /** Optional description. */
    private String description;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    /** Children, populated by the service layer when building a tree. */
    private transient List<Category> children;
}
