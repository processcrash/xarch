package com.xarch.crm;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.OpportunityDTO;
import com.xarch.crm.entity.Opportunity;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.OpportunityMapper;
import com.xarch.crm.service.impl.OpportunityServiceImpl;
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
 * Unit tests for {@link OpportunityServiceImpl}. Covers funnel
 * transitions and filter-based pagination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Opportunity Service Tests")
class OpportunityServiceImplTest {

    @Mock
    private OpportunityMapper opportunityMapper;

    @InjectMocks
    private OpportunityServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should insert opportunity with OPEN status and default probability")
        void shouldInsertOpportunity() {
            OpportunityDTO dto = new OpportunityDTO(1L, "Big Deal",
                    new BigDecimal("10000"), null, OpportunityServiceImpl.STAGE_QUALIFICATION,
                    null, 1L, 2L, "desc");

            service.create(dto);

            ArgumentCaptor<Opportunity> captor = ArgumentCaptor.forClass(Opportunity.class);
            verify(opportunityMapper).insert(captor.capture());
            Opportunity saved = captor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(1L);
            assertThat(saved.getName()).isEqualTo("Big Deal");
            assertThat(saved.getAmount()).isEqualByComparingTo("10000");
            assertThat(saved.getCurrency()).isEqualTo("CNY");
            assertThat(saved.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_OPEN);
            assertThat(saved.getProbability()).isEqualTo(10);
            assertThat(saved.getIsDeleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("should accept positive amount")
        void shouldAcceptPositiveAmount() {
            OpportunityDTO dto = new OpportunityDTO(1L, "Positive",
                    new BigDecimal("1000"), "CNY",
                    OpportunityServiceImpl.STAGE_QUALIFICATION, null, 1L, 1L, null);

            service.create(dto);

            ArgumentCaptor<Opportunity> captor = ArgumentCaptor.forClass(Opportunity.class);
            verify(opportunityMapper).insert(captor.capture());
            assertThat(captor.getValue().getAmount()).isEqualByComparingTo("1000");
        }

        @Test
        @DisplayName("should accept zero amount at service layer (DTO enforces @NotNull)")
        void shouldAcceptZeroAmount() {
            OpportunityDTO dto = new OpportunityDTO(1L, "Zero", BigDecimal.ZERO, "CNY",
                    OpportunityServiceImpl.STAGE_QUALIFICATION, null, 1L, 1L, null);

            service.create(dto);

            ArgumentCaptor<Opportunity> captor = ArgumentCaptor.forClass(Opportunity.class);
            verify(opportunityMapper).insert(captor.capture());
            assertThat(captor.getValue().getAmount()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("changeStage / markWon / markLost")
    class StageTransitions {

        @Test
        @DisplayName("WON should set status=WON, probability=100")
        void shouldMarkWon() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.changeStage(1L, OpportunityServiceImpl.STAGE_WON);

            assertThat(opp.getStage()).isEqualTo(OpportunityServiceImpl.STAGE_WON);
            assertThat(opp.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_WON);
            assertThat(opp.getProbability()).isEqualTo(100);
        }

        @Test
        @DisplayName("LOST should set probability=0")
        void shouldMarkLost() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.changeStage(1L, OpportunityServiceImpl.STAGE_LOST);

            assertThat(opp.getStage()).isEqualTo(OpportunityServiceImpl.STAGE_LOST);
            assertThat(opp.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_LOST);
            assertThat(opp.getProbability()).isEqualTo(0);
        }

        @Test
        @DisplayName("non-terminal stage keeps status OPEN")
        void shouldKeepOpenForNonTerminal() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.changeStage(1L, OpportunityServiceImpl.STAGE_PROPOSAL);

            assertThat(opp.getStage()).isEqualTo(OpportunityServiceImpl.STAGE_PROPOSAL);
            assertThat(opp.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_OPEN);
        }

        @Test
        @DisplayName("markWon should stamp probability 100")
        void shouldMarkWonDirectly() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.markWon(1L);

            assertThat(opp.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_WON);
            assertThat(opp.getProbability()).isEqualTo(100);
        }

        @Test
        @DisplayName("markLost should stamp probability 0")
        void shouldMarkLostDirectly() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.markLost(1L);

            assertThat(opp.getStatus()).isEqualTo(OpportunityServiceImpl.STATUS_LOST);
            assertThat(opp.getProbability()).isEqualTo(0);
        }

        @Test
        @DisplayName("should throw when opportunity missing")
        void shouldThrow_whenMissing() {
            when(opportunityMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> service.changeStage(1L, OpportunityServiceImpl.STAGE_WON))
                    .isInstanceOf(CrmException.class);
        }
    }

    @Nested
    @DisplayName("update / delete")
    class Mutations {

        @Test
        @DisplayName("should update fields")
        void shouldUpdate() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);
            OpportunityDTO dto = new OpportunityDTO(1L, "Updated",
                    new BigDecimal("2000"), "USD", OpportunityServiceImpl.STAGE_PROPOSAL,
                    50, 999L, 2L, "new");

            service.update(1L, dto);

            assertThat(opp.getName()).isEqualTo("Updated");
            assertThat(opp.getAmount()).isEqualByComparingTo("2000");
        }

        @Test
        @DisplayName("delete should soft delete")
        void shouldSoftDelete() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            service.delete(1L);

            assertThat(opp.getIsDeleted()).isEqualTo(1);
            verify(opportunityMapper, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("page / listByCustomer")
    class Reads {

        @Test
        @DisplayName("should apply stage / customer / owner filters")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldPage() {
            com.mybatisflex.core.paginate.Page page = com.mybatisflex.core.paginate.Page.of(1, 10);
            page.setRecords(List.of(sampleOpp(1L)));
            page.setTotalRow(1L);
            when(opportunityMapper.paginate(anyLong(), anyLong(), any(QueryWrapper.class)))
                    .thenReturn(page);

            PageResult<Opportunity> result = service.page(
                    "deal", OpportunityServiceImpl.STAGE_PROPOSAL, 1L, 2L, 1, 10);

            assertThat(result.getList()).hasSize(1);
        }

        @Test
        @DisplayName("should get by id")
        void shouldGetById() {
            Opportunity opp = sampleOpp(1L);
            when(opportunityMapper.selectOneById(1L)).thenReturn(opp);

            assertThat(service.getById(1L)).isEqualTo(opp);
        }

        @Test
        @DisplayName("should list by customer")
        void shouldListByCustomer() {
            when(opportunityMapper.selectByCustomerId(1L)).thenReturn(List.of(sampleOpp(1L)));

            List<Opportunity> result = service.listByCustomer(1L);

            assertThat(result).hasSize(1);
        }
    }

    private Opportunity sampleOpp(long id) {
        return Opportunity.builder()
                .id(id)
                .customerId(1L)
                .name("Deal")
                .amount(new BigDecimal("1000"))
                .currency("CNY")
                .stage(OpportunityServiceImpl.STAGE_QUALIFICATION)
                .probability(10)
                .status(OpportunityServiceImpl.STATUS_OPEN)
                .build();
    }
}
