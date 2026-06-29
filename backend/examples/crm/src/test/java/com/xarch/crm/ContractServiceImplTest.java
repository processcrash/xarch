package com.xarch.crm;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.ContractDTO;
import com.xarch.crm.entity.Contract;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.ContractMapper;
import com.xarch.crm.service.impl.ContractServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ContractServiceImpl}. Covers contract number
 * uniqueness and lifecycle transitions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contract Service Tests")
class ContractServiceImplTest {

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should reject duplicate contractNo")
        void shouldRejectDuplicate() {
            ContractDTO dto = new ContractDTO(1L, 2L, "C-001",
                    new BigDecimal("1000"), 1L, 2L, "monthly");
            Contract existing = Contract.builder().id(99L).contractNo("C-001").build();
            when(contractMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(existing);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(CrmException.class)
                    .hasMessageContaining("already exists");
            verify(contractMapper, never()).insert(any(Contract.class));
        }

        @Test
        @DisplayName("should insert DRAFT contract when contractNo is unique")
        void shouldInsert() {
            ContractDTO dto = new ContractDTO(1L, 2L, "C-001",
                    new BigDecimal("1000"), 1L, 2L, "monthly");
            when(contractMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

            service.create(dto);

            ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
            verify(contractMapper).insert(captor.capture());
            Contract saved = captor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(1L);
            assertThat(saved.getOpportunityId()).isEqualTo(2L);
            assertThat(saved.getContractNo()).isEqualTo("C-001");
            assertThat(saved.getAmount()).isEqualByComparingTo("1000");
            assertThat(saved.getStatus()).isEqualTo(ContractServiceImpl.STATUS_DRAFT);
            assertThat(saved.getIsDeleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("should allow blank contractNo (uniqueness not checked)")
        void shouldSkipUniqueness_whenContractNoBlank() {
            ContractDTO dto = new ContractDTO(1L, 2L, "",
                    new BigDecimal("100"), 1L, 2L, null);

            service.create(dto);

            verify(contractMapper, never()).selectOneByQuery(any(QueryWrapper.class));
            verify(contractMapper).insert(any(Contract.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update fields")
        void shouldUpdate() {
            Contract existing = sample(1L);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);
            ContractDTO dto = new ContractDTO(1L, 2L, "C-002",
                    new BigDecimal("2000"), 1L, 2L, "annual");

            service.update(1L, dto);

            assertThat(existing.getContractNo()).isEqualTo("C-002");
            assertThat(existing.getAmount()).isEqualByComparingTo("2000");
        }

        @Test
        @DisplayName("update should throw when missing")
        void shouldThrowOnUpdate() {
            when(contractMapper.selectOneById(1L)).thenReturn(null);
            ContractDTO dto = new ContractDTO(1L, 2L, "C-001", BigDecimal.ONE, 1L, 2L, null);

            assertThatThrownBy(() -> service.update(1L, dto))
                    .isInstanceOf(CrmException.class);
        }
    }

    @Nested
    @DisplayName("sign / terminate / delete")
    class Lifecycle {

        @Test
        @DisplayName("sign should set ACTIVE and stamp signedDate")
        void shouldSign() {
            Contract existing = sample(1L);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);
            long date = 1_700_000_000_000L;

            service.sign(1L, date);

            assertThat(existing.getStatus()).isEqualTo(ContractServiceImpl.STATUS_ACTIVE);
            assertThat(existing.getSignedDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("sign should default to now when null")
        void shouldSignToNow() {
            Contract existing = sample(1L);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);

            service.sign(1L, null);

            assertThat(existing.getSignedDate()).isNotNull();
        }

        @Test
        @DisplayName("terminate should set TERMINATED and stamp signedDate if not set")
        void shouldTerminate() {
            Contract existing = sample(1L);
            existing.setSignedDate(null);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);

            service.terminate(1L);

            assertThat(existing.getStatus()).isEqualTo(ContractServiceImpl.STATUS_TERMINATED);
            assertThat(existing.getSignedDate()).isNotNull();
        }

        @Test
        @DisplayName("terminate should preserve existing signedDate")
        void shouldPreserveSignedDate() {
            Contract existing = sample(1L);
            existing.setSignedDate(123L);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);

            service.terminate(1L);

            assertThat(existing.getSignedDate()).isEqualTo(123L);
        }

        @Test
        @DisplayName("delete should soft delete")
        void shouldDelete() {
            Contract existing = sample(1L);
            when(contractMapper.selectOneById(1L)).thenReturn(existing);

            service.delete(1L);

            assertThat(existing.getIsDeleted()).isEqualTo(1);
            verify(contractMapper).update(existing);
        }
    }

    @Nested
    @DisplayName("page / getById")
    class Reads {

        @Test
        @DisplayName("should filter by status and customerId")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldPage() {
            com.mybatisflex.core.paginate.Page page = com.mybatisflex.core.paginate.Page.of(1, 10);
            page.setRecords(java.util.List.of(sample(1L)));
            page.setTotalRow(1L);
            when(contractMapper.paginate(anyLong(), anyLong(), any(QueryWrapper.class)))
                    .thenReturn(page);

            PageResult<Contract> result = service.page(1L, ContractServiceImpl.STATUS_ACTIVE, 1, 10);

            assertThat(result.getList()).hasSize(1);
        }

        @Test
        @DisplayName("should get by id")
        void shouldGetById() {
            Contract c = sample(1L);
            when(contractMapper.selectOneById(1L)).thenReturn(c);

            assertThat(service.getById(1L)).isEqualTo(c);
        }
    }

    private Contract sample(long id) {
        return Contract.builder()
                .id(id)
                .customerId(1L)
                .opportunityId(2L)
                .contractNo("C-001")
                .amount(new BigDecimal("1000"))
                .status(ContractServiceImpl.STATUS_DRAFT)
                .isDeleted(0)
                .build();
    }
}
