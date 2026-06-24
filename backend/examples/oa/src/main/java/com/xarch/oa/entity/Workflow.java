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
 * Workflow definition. The {@code definition} column holds a JSON
 * document describing the node graph, see
 * {@link com.xarch.oa.workflow.WorkflowDefinition} for the schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("oa_workflow")
public class Workflow implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** Display name. */
    private String name;

    /** LEAVE / EXPENSE / ... */
    private String businessType;

    /** JSON: nodes, edges, approval rule. */
    private String definition;

    /** Index of the node the engine is currently processing. */
    private Integer currentNode;

    /** ACTIVE / SUSPENDED / ARCHIVED. */
    private String status;

    @Column(onInsertValue = "UNIX_TIMESTAMP() * 1000")
    private Long createdAt;

    @Column(onUpdateValue = "UNIX_TIMESTAMP() * 1000")
    private Long updatedAt;
}
