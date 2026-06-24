package com.xarch.cms.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Join table for the many-to-many relationship between {@link Article} and {@link Tag}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cms_article_tag")
public class ArticleTag implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long articleId;

    private Long tagId;
}
