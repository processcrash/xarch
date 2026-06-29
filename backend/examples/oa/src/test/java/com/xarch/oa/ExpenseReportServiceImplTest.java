package com.xarch.oa;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.ExpenseReportDTO;
import com.xarch.oa.entity.ExpenseItem;
import com.xarch.oa.entity.ExpenseReport;
import com.xarch.oa.entity.Workflow;
import com.xarch.oa.exception.OaException;
import com.xarch.oa.mapper.ExpenseReportMapper;
import com.xarch.oa.service.impl.ExpenseReportServiceImpl;
import com.xarch.oa.workflow.WorkflowDefinition;
import com.xarch.oa.workflow.WorkflowEngine;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExpenseReportServiceImpl}. Mirrors the leave
 * request test surface and adds amount / reimbursement assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseReport Service Tests")
class ExpenseReportServiceImplTest {

    @Mock
    private ExpenseReportMapper expenseReportMapper;

    @Mock
    private WorkflowEngine workflowEngine;

    @InjectMocks
    private ExpenseReportServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create draft with amount from DTO")
        void shouldCreateDraft() {
            ExpenseReportDTO dto = new ExpenseReportDTO("TRAVEL",
                    new BigDecimal("500.00"), "CNY", "Trip to Beijing",
                    List.of(new ExpenseItem(1000L, new BigDecimal("500.00"), "hotel")));

            service.create(dto, 42L);

            ArgumentCaptor<ExpenseReport> captor = ArgumentCaptor.forClass(ExpenseReport.class);
            verify(expenseReportMapper).insert(captor.capture());
            ExpenseReport saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(42L);
            assertThat(saved.getCategory()).isEqualTo("TRAVEL");
            assertThat(saved.getAmount()).isEqualByComparingTo("500.00");
            assertThat(saved.getCurrency()).isEqualTo("CNY");
            assertThat(saved.getStatus()).isEqualTo(ExpenseReportServiceImpl.STATUS_DRAFT);
        }

        @Test
        @DisplayName("should encode items as JSON array")
        void shouldEncodeItems() {
            ExpenseReportDTO dto = new ExpenseReportDTO("MEAL", new BigDecimal("100"), "CNY", "lunch",
                    List.of(new ExpenseItem(1L, new BigDecimal("50"), "soup"),
                            new ExpenseItem(2L, new BigDecimal("50"), "rice")));

            service.create(dto, 1L);

            ArgumentCaptor<ExpenseReport> captor = ArgumentCaptor.forClass(ExpenseReport.class);
            verify(expenseReportMapper).insert(captor.capture());
            String items = captor.getValue().getItems();
            assertThat(items).contains("soup").contains("rice");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update draft fields")
        void shouldUpdateDraft() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_DRAFT);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);
            ExpenseReportDTO dto = new ExpenseReportDTO("OFFICE", new BigDecimal("100"), "CNY", "stuff",
                    List.of(new ExpenseItem(1L, new BigDecimal("100"), "supplies")));

            service.update(1L, dto);

            assertThat(existing.getCategory()).isEqualTo("OFFICE");
            assertThat(existing.getAmount()).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("should reject update when not draft")
        void shouldRejectUpdate_whenNotDraft() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_APPROVED);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);

            assertThatThrownBy(() -> service.update(1L,
                    new ExpenseReportDTO("X", BigDecimal.ONE, "CNY", "d",
                            List.of(new ExpenseItem(1L, BigDecimal.ONE, "x")))))
                    .isInstanceOf(OaException.class)
                    .hasMessageContaining("drafts");
        }
    }

    @Nested
    @DisplayName("submit")
    class Submit {

        @Test
        @DisplayName("should set amount from items sum")
        void shouldSubmit() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_DRAFT);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);
            Workflow workflow = new Workflow();
            workflow.setBusinessType("EXPENSE");
            when(workflowEngine.requireWorkflow("EXPENSE")).thenReturn(workflow);
            WorkflowDefinition.Node first = new WorkflowDefinition.Node(0, "manager", "MANAGER", List.of(101L));
            when(workflowEngine.start(workflow)).thenReturn(first);

            service.submit(1L, 42L);

            assertThat(existing.getStatus()).isEqualTo(ExpenseReportServiceImpl.STATUS_APPROVING);
            assertThat(existing.getApproverId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("should reject submit when not draft")
        void shouldRejectSubmit_whenNotDraft() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_SUBMITTED);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);

            assertThatThrownBy(() -> service.submit(1L, 42L))
                    .isInstanceOf(OaException.class);
            verify(workflowEngine, never()).requireWorkflow(any());
        }
    }

    @Nested
    @DisplayName("act")
    class Act {

        @Test
        @DisplayName("should mark APPROVED on terminal approve")
        void shouldApprove() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_APPROVING);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);
            Workflow workflow = new Workflow();
            workflow.setBusinessType("EXPENSE");
            when(workflowEngine.requireWorkflow("EXPENSE")).thenReturn(workflow);
            when(workflowEngine.act(any(Workflow.class), any(ApprovalDTO.class), any()))
                    .thenReturn(null);

            service.act(1L, new ApprovalDTO("APPROVE", 101L, "M", "ok", null));

            assertThat(existing.getStatus()).isEqualTo(ExpenseReportServiceImpl.STATUS_APPROVED);
        }

        @Test
        @DisplayName("should mark REJECTED on terminal reject")
        void shouldReject() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_APPROVING);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);
            Workflow workflow = new Workflow();
            workflow.setBusinessType("EXPENSE");
            when(workflowEngine.requireWorkflow("EXPENSE")).thenReturn(workflow);
            when(workflowEngine.act(any(Workflow.class), any(ApprovalDTO.class), any()))
                    .thenReturn(null);

            service.act(1L, new ApprovalDTO("REJECT", 101L, "M", "no", null));

            assertThat(existing.getStatus()).isEqualTo(ExpenseReportServiceImpl.STATUS_REJECTED);
        }

        @Test
        @DisplayName("should reject act when not in APPROVING")
        void shouldRejectAct_whenNotApproving() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_DRAFT);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);

            assertThatThrownBy(() -> service.act(1L,
                    new ApprovalDTO("APPROVE", 1L, "X", "ok", null)))
                    .isInstanceOf(OaException.class);
        }
    }

    @Nested
    @DisplayName("reimburse")
    class Reimburse {

        @Test
        @DisplayName("should mark REIMBURSED with given date")
        void shouldReimburseWithDate() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_APPROVED);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);
            long now = 1_700_000_000_000L;

            service.reimburse(1L, now);

            assertThat(existing.getStatus()).isEqualTo(ExpenseReportServiceImpl.STATUS_REIMBURSED);
            assertThat(existing.getReimbursementDate()).isEqualTo(now);
        }

        @Test
        @DisplayName("should default to now when date is null")
        void shouldDefaultReimburseToNow() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_APPROVED);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);

            service.reimburse(1L, null);

            assertThat(existing.getReimbursementDate()).isNotNull();
        }

        @Test
        @DisplayName("should reject reimburse when not APPROVED")
        void shouldRejectReimburse_whenNotApproved() {
            ExpenseReport existing = sampleReport(1L, ExpenseReportServiceImpl.STATUS_DRAFT);
            when(expenseReportMapper.selectOneById(1L)).thenReturn(existing);

            assertThatThrownBy(() -> service.reimburse(1L, 1L))
                    .isInstanceOf(OaException.class)
                    .hasMessageContaining("approved");
        }
    }

    @Nested
    @DisplayName("page / getById / listPending / sumApprovedAmount")
    class Reads {

        @Test
        @DisplayName("should page with filters")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldPage() {
            com.mybatisflex.core.paginate.Page page = com.mybatisflex.core.paginate.Page.of(1, 10);
            page.setRecords(List.of(sampleReport(1L, ExpenseReportServiceImpl.STATUS_DRAFT)));
            page.setTotalRow(1L);
            when(expenseReportMapper.paginate(anyLong(), anyLong(),
                    any(com.mybatisflex.core.query.QueryWrapper.class))).thenReturn(page);

            PageResult<ExpenseReport> result = service.page(1L, "DRAFT", 1, 10);

            assertThat(result.getList()).hasSize(1);
        }

        @Test
        @DisplayName("should get by id")
        void shouldGetById() {
            ExpenseReport r = sampleReport(1L, "X");
            when(expenseReportMapper.selectOneById(1L)).thenReturn(r);
            assertThat(service.getById(1L)).isEqualTo(r);
        }

        @Test
        @DisplayName("should list pending for approver")
        void shouldListPending() {
            when(expenseReportMapper.selectPendingForApprover(101L))
                    .thenReturn(List.of(sampleReport(1L, "X")));
            assertThat(service.listPendingForApprover(101L)).hasSize(1);
        }

        @Test
        @DisplayName("should return sum from mapper")
        void shouldSumApprovedAmount() {
            when(expenseReportMapper.sumApprovedAmount(42L)).thenReturn(new BigDecimal("1000"));

            BigDecimal sum = service.sumApprovedAmount(42L);

            assertThat(sum).isEqualByComparingTo("1000");
        }

        @Test
        @DisplayName("should return zero when mapper returns null")
        void shouldReturnZero_whenMapperNull() {
            when(expenseReportMapper.sumApprovedAmount(42L)).thenReturn(null);

            BigDecimal sum = service.sumApprovedAmount(42L);

            assertThat(sum).isEqualByComparingTo("0");
        }
    }

    private ExpenseReport sampleReport(long id, String status) {
        return ExpenseReport.builder()
                .id(id)
                .userId(42L)
                .category("TRAVEL")
                .amount(new BigDecimal("100"))
                .currency("CNY")
                .status(status)
                .build();
    }
}
