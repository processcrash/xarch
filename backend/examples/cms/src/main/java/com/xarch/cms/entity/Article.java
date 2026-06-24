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
 * Article entity.
 *
 * <p>Represents a piece of content that goes through a lifecycle
 * (DRAFT -> PUBLISHED -> ARCHIVED) tracked via {@link #status}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cms_article")
public class Article implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Article title. */
    private String title;

    /** Full article body in markdown or HTML. */
    private String content;

    /** Short summary used in lists and previews. */
    private String summary;

    /** Category id (see {@link Category#getId()}). */
    private Long categoryId;

    /** Author user id. */
    private Long authorId;

    /** Lifecycle status: DRAFT / PUBLISHED / ARCHIVED. */
    private String status;

    /** Number of times the article was viewed. */
    private Long viewCount;

    /** Number of likes. */
    private Long likeCount;

    /** Creation timestamp (epoch millis). */
    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createTime;

    /** Last update timestamp (epoch millis). */
    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updateTime;

    /** Time at which the article was published (epoch millis). */
    private Long publishedTime;

    /** Soft delete flag (0 = active, 1 = deleted). */
    @Column(isLogicDelete = true)
    private Integer isDeleted;
}
