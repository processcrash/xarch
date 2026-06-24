package com.xarch.oa;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.oa.entity.Workflow;
import com.xarch.oa.workflow.WorkflowDefinition;
import com.xarch.oa.workflow.WorkflowEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pure unit tests for the workflow engine. We construct a workflow
 * definition in memory and stub the persistence layer out.
 */
class WorkflowEngineTest {

    @Test
    void parsesStandardDefinition() {
        WorkflowEngine engine = newEngine();
        Workflow workflow = new Workflow();
        workflow.setBusinessType("LEAVE");
        workflow.setDefinition(sampleDefinition());
        WorkflowDefinition def = engine.parse(workflow);
        assertEquals(0, def.startNode());
        assertEquals(2, def.nodes().size());
        assertEquals(4, def.edges().size());
    }

    @Test
    void approveWalksAcrossNodes() {
        WorkflowEngine engine = newEngine();
        Workflow workflow = new Workflow();
        workflow.setId(1L);
        workflow.setBusinessType("LEAVE");
        workflow.setDefinition(sampleDefinition());
        workflow.setCurrentNode(0);

        // First approver approves -> move to node 1
        WorkflowDefinition.Node next = engine.act(workflow,
                new ApprovalDTO("APPROVE", 101L, "Alice", "ok", null), this::noopSink);
        assertNotNull(next);
        assertEquals(1, next.id());
        assertEquals(1, workflow.getCurrentNode());

        // Second approver approves -> terminal
        WorkflowDefinition.Node terminal = engine.act(workflow,
                new ApprovalDTO("APPROVE", 201L, "Bob", "approved", null), this::noopSink);
        assertNull(terminal);
        assertEquals(WorkflowDefinition.Edge.TERMINAL_APPROVE, workflow.getCurrentNode());
    }

    @Test
    void rejectTerminates() {
        WorkflowEngine engine = newEngine();
        Workflow workflow = new Workflow();
        workflow.setId(2L);
        workflow.setBusinessType("LEAVE");
        workflow.setDefinition(sampleDefinition());
        workflow.setCurrentNode(0);

        WorkflowDefinition.Node terminal = engine.act(workflow,
                new ApprovalDTO("REJECT", 101L, "Alice", "no", null), this::noopSink);
        assertNull(terminal);
        assertEquals(WorkflowDefinition.Edge.TERMINAL_REJECT, workflow.getCurrentNode());
    }

    private void noopSink(ApprovalRecord record) {
        // no-op, the engine already inserted through the stub
    }

    /**
     * Sample JSON: 0 (manager) -> 1 (hr) -> approve, reject available at every node.
     */
    private String sampleDefinition() {
        return """
                {
                  "startNode": 0,
                  "nodes": [
                    {"id": 0, "name": "manager", "role": "MANAGER", "approvers": [101]},
                    {"id": 1, "name": "hr",      "role": "HR",      "approvers": [201]}
                  ],
                  "edges": [
                    {"from": 0, "to":  1, "on": "APPROVE"},
                    {"from": 0, "to": -1, "on": "REJECT"},
                    {"from": 1, "to": -2, "on": "APPROVE"},
                    {"from": 1, "to": -1, "on": "REJECT"}
                  ]
                }
                """;
    }

    /**
     * Build an engine with stubbed mappers. The engine only needs the
     * approval record mapper to be set for this test, because the
     * workflow itself is supplied directly to {@code act()}.
     */
    private WorkflowEngine newEngine() {
        WorkflowEngine engine = new WorkflowEngine();
        com.xarch.oa.mapper.ApprovalRecordMapper stub = new com.xarch.oa.mapper.ApprovalRecordMapper() {
            @Override
            public List<ApprovalRecord> selectByBusiness(String businessType, Long businessId) {
                return List.of();
            }
        };
        org.springframework.test.util.AopTestUtils.setTarget(engine, engine);
        try {
            java.lang.reflect.Field f = WorkflowEngine.class.getDeclaredField("approvalRecordMapper");
            f.setAccessible(true);
            f.set(engine, stub);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return engine;
    }
}
