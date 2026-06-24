package com.xarch.oa.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.oa.entity.Workflow;
import com.xarch.oa.exception.OaException;
import com.xarch.oa.mapper.ApprovalRecordMapper;
import com.xarch.oa.mapper.WorkflowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Tiny workflow engine.
 *
 * <p>The engine is intentionally framework-light: a workflow is a JSON
 * graph of nodes and edges, the engine holds a pointer to the current
 * node, and an approval action drives the pointer forward along an
 * edge matched by the action's verb.
 *
 * <p>Two hooks are exposed for the caller:
 * <ul>
 *   <li>{@link #routeApproval} is invoked synchronously when an
 *       approver acts; it returns the next node id (or terminal).</li>
 *   <li>Public methods like {@link #start} and {@link #act} tie the
 *       routing into the surrounding business services.</li>
 * </ul>
 *
 * <p>The engine is stateless across calls except for the current
 * node index stored on the {@link Workflow} row. This keeps it easy
 * to reason about and easy to replay.
 */
@Component
public class WorkflowEngine {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    /**
     * Find a workflow definition by business type. Throws if no active
     * workflow is registered for the type.
     */
    public Workflow requireWorkflow(String businessType) {
        List<Workflow> all = workflowMapper.selectList();
        return all.stream()
                .filter(w -> businessType.equals(w.getBusinessType()))
                .filter(w -> "ACTIVE".equals(w.getStatus()))
                .findFirst()
                .orElseThrow(() -> new OaException(
                        "No active workflow registered for " + businessType));
    }

    /**
     * Decode the JSON definition for a workflow.
     */
    public WorkflowDefinition parse(Workflow workflow) {
        try {
            return mapper.readValue(workflow.getDefinition(), WorkflowDefinition.class);
        } catch (JsonProcessingException e) {
            throw new OaException("Invalid workflow definition: " + e.getMessage());
        }
    }

    /**
     * Initialise a fresh workflow pointer on a workflow row and return
     * the first node.
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition.Node start(Workflow workflow) {
        WorkflowDefinition def = parse(workflow);
        workflow.setCurrentNode(def.startNode());
        workflowMapper.updateById(workflow);
        return nodeById(def, def.startNode());
    }

    /**
     * Route an approval action. Returns the node the workflow is now
     * pointing at, or null when the workflow has reached a terminal
     * state.
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinition.Node act(Workflow workflow,
                                       ApprovalDTO action,
                                       Consumer<ApprovalRecord> sink) {
        WorkflowDefinition def = parse(workflow);
        int from = workflow.getCurrentNode() == null ? def.startNode() : workflow.getCurrentNode();

        // Serial/parallel semantics: in this engine, "parallel" simply
        // means "all approvers of the current node must approve before
        // we move on". We track that on the approval record.
        recordAction(workflow, action, sink);

        // Move only on the terminal action of the node.
        if (!isNodeComplete(def, workflow, from)) {
            return nodeById(def, from);
        }

        WorkflowDefinition.Edge edge = findEdge(def, from, action.action());
        if (edge == null) {
            throw new OaException("No edge from node " + from + " on " + action.action());
        }
        if (edge.to() == WorkflowDefinition.Edge.TERMINAL_APPROVE
                || edge.to() == WorkflowDefinition.Edge.TERMINAL_REJECT) {
            workflow.setCurrentNode(edge.to());
            workflowMapper.updateById(workflow);
            return null;
        }
        workflow.setCurrentNode(edge.to());
        workflowMapper.updateById(workflow);
        return nodeById(def, edge.to());
    }

    /**
     * Convenience: record the approval row using the supplied sink.
     */
    private void recordAction(Workflow workflow, ApprovalDTO action, Consumer<ApprovalRecord> sink) {
        if (sink == null) {
            return;
        }
        ApprovalRecord record = ApprovalRecord.builder()
                .businessType(workflow.getBusinessType())
                .approverId(action.approverId())
                .approverName(Objects.requireNonNullElse(action.approverName(), ""))
                .action(action.action())
                .comment(action.comment())
                .build();
        approvalRecordMapper.insert(record);
        sink.accept(record);
    }

    /**
     * True when every approver on the current node has approved.
     * For simplicity we count unique approver ids and compare.
     */
    private boolean isNodeComplete(WorkflowDefinition def, Workflow workflow, int nodeId) {
        WorkflowDefinition.Node node = nodeById(def, nodeId);
        if (node.approvers() == null || node.approvers().isEmpty()) {
            return true;
        }
        List<ApprovalRecord> history = approvalRecordMapper.selectByBusiness(
                workflow.getBusinessType(), workflow.getId());
        List<Long> approversWhoActed = history.stream()
                .filter(r -> "APPROVE".equals(r.getAction()))
                .map(ApprovalRecord::getApproverId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return new java.util.HashSet<>(approversWhoActed).containsAll(node.approvers());
    }

    private WorkflowDefinition.Edge findEdge(WorkflowDefinition def, int from, String action) {
        Map<String, WorkflowDefinition.Edge> byAction = def.edges().stream()
                .filter(e -> e.from() == from)
                .collect(Collectors.toMap(WorkflowDefinition.Edge::on, e -> e, (a, b) -> a));
        return byAction.get(action);
    }

    private WorkflowDefinition.Node nodeById(WorkflowDefinition def, int id) {
        return def.nodes().stream()
                .filter(n -> n.id() == id)
                .findFirst()
                .orElseThrow(() -> new OaException("Node not found: " + id));
    }
}
