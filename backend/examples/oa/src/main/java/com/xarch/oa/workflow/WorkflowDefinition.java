package com.xarch.oa.workflow;

import java.util.List;

/**
 * Parsed view of a {@link com.xarch.oa.entity.Workflow#getDefinition()}
 * JSON document. The engine drives approvals node by node; each node
 * references a role and a list of approver candidates.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "startNode": 0,
 *   "nodes": [
 *     {"id": 0, "name": "manager", "role": "MANAGER", "approvers": [101]},
 *     {"id": 1, "name": "hr",      "role": "HR",      "approvers": [201]}
 *   ],
 *   "edges": [
 *     {"from": 0, "to": 1, "on": "APPROVE"},
 *     {"from": 0, "to": -1, "on": "REJECT"}
 *   ]
 * }
 * </pre>
 */
public record WorkflowDefinition(
        int startNode,
        List<Node> nodes,
        List<Edge> edges
) {
    /**
     * A single approval step.
     */
    public record Node(int id, String name, String role, List<Long> approvers) {
    }

    /**
     * A directed transition between nodes. {@code to == -1} is the
     * terminal REJECT sink; {@code to == -2} is the terminal APPROVE sink.
     */
    public record Edge(int from, int to, String on) {
        public static final int TERMINAL_REJECT = -1;
        public static final int TERMINAL_APPROVE = -2;
    }
}
