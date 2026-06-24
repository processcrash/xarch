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

/**
 * Comment entity. Supports one level of nesting via {@link #parentId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cms_comment")
public class Comment implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long articleId;

    private Long userId;

    /** Comment text. */
    private String content;

    /** Parent comment id, 0 for top-level. */
    private Long parentId;

    /** Status: VISIBLE / HIDDEN / PENDING. */
    private String status;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
