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
 * Tag entity. Tags are flat and many-to-many with {@link Article} via {@link ArticleTag}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cms_tag")
public class Tag implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Display name. */
    private String name;

    /** URL-friendly unique slug. */
    private String slug;

    /** Optional description. */
    private String description;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;
}
