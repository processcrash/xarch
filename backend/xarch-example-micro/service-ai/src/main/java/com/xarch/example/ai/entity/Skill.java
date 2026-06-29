package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI skill — a user-defined named procedure that the agent can execute
 * on demand. Skills combine system prompts, tool references and
 * optional parameters.
 */
@Data
@Table("xarch_ai_skill")
public class Skill implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String name;

    private String code;

    private String description;

    /** Category for grouping in the UI. */
    private String category;

    /** System prompt prepended to the chat when this skill is invoked. */
    private String systemPrompt;

    /** JSON-encoded list of tool names this skill can use. */
    private String toolList;

    /** JSON-encoded parameter schema. */
    private String parameters;

    /** Version string. */
    private String version;

    /** Install status: 0=draft, 1=installed, 2=disabled. */
    private Integer status;

    private Long createUserId;
    private String createUserName;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}
