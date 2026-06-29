package com.xarch.crm;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.FollowUpDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.entity.FollowUp;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.CustomerMapper;
import com.xarch.crm.mapper.FollowUpMapper;
import com.xarch.crm.service.impl.FollowUpServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FollowUpServiceImpl}. Covers customer
 * lastContactTime stamping and scheduling helpers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUp Service Tests")
class FollowUpServiceImplTest {

    @Mock
    private FollowUpMapper followUpMapper;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private FollowUpServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should stamp customer lastContactTime")
        void shouldStampCustomerLastContact() {
            FollowUpDTO dto = new FollowUpDTO(1L, 2L, 3L, "PHONE", "Hello", "positive",
                    1_700_000_000_000L, List.of(), 42L);
            Customer customer = Customer.builder().id(1L).build();
            when(customerMapper.selectOneById(1L)).thenReturn(customer);

            service.create(dto);

            ArgumentCaptor<FollowUp> captor = ArgumentCaptor.forClass(FollowUp.class);
            verify(followUpMapper).insert(captor.capture());
            FollowUp saved = captor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(1L);
            assertThat(saved.getContactId()).isEqualTo(2L);
            assertThat(saved.getOpportunityId()).isEqualTo(3L);
            assertThat(saved.getType()).isEqualTo("PHONE");
            assertThat(saved.getCreateTime()).isNotNull();

            assertThat(customer.getLastContactTime()).isNotNull();
            verify(customerMapper).update(customer);
        }

        @Test
        @DisplayName("should not throw if customer missing")
        void shouldNotThrow_whenCustomerMissing() {
            FollowUpDTO dto = new FollowUpDTO(99L, null, null, "EMAIL", "Hi", null,
                    null, null, 1L);
            when(customerMapper.selectOneById(99L)).thenReturn(null);

            service.create(dto);

            verify(followUpMapper).insert(any(FollowUp.class));
            verify(customerMapper, never()).update(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("update / delete")
    class Mutations {

        @Test
        @DisplayName("should update fields")
        void shouldUpdate() {
            FollowUp existing = sample(1L, 1L, 2L);
            when(followUpMapper.selectOneById(1L)).thenReturn(existing);
            FollowUpDTO dto = new FollowUpDTO(1L, 2L, 3L, "MEETING", "Updated", "result",
                    9L, null, 5L);

            service.update(1L, dto);

            assertThat(existing.getType()).isEqualTo("MEETING");
            assertThat(existing.getContent()).isEqualTo("Updated");
            verify(followUpMapper).update(existing);
        }

        @Test
        @DisplayName("update should throw on missing")
        void shouldThrowOnUpdate() {
            when(followUpMapper.selectOneById(1L)).thenReturn(null);
            assertThatThrownBy(() -> service.update(1L,
                    new FollowUpDTO(1L, null, null, "X", "x", null, null, null, 1L)))
                    .isInstanceOf(CrmException.class);
        }

        @Test
        @DisplayName("delete should soft delete")
        void shouldDelete() {
            FollowUp f = sample(1L, 1L, null);
            when(followUpMapper.selectOneById(1L)).thenReturn(f);

            service.delete(1L);

            assertThat(f.getIsDeleted()).isEqualTo(1);
            verify(followUpMapper).update(f);
        }

        @Test
        @DisplayName("delete should throw when missing")
        void shouldThrowOnDelete() {
            when(followUpMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(CrmException.class);
        }
    }

    @Nested
    @DisplayName("scheduleNext / listByOpportunity")
    class Scheduling {

        @Test
        @DisplayName("scheduleNext returns follow-ups due on the given date")
        void shouldReturnDue() {
            when(followUpMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(sample(1L, 1L, 1L), sample(2L, 1L, 1L)));

            List<FollowUp> result = service.scheduleNext(1_700_000_000_000L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("scheduleNext returns empty when date is null")
        void shouldReturnEmpty_whenDateNull() {
            List<FollowUp> result = service.scheduleNext(null);

            assertThat(result).isEmpty();
            verify(followUpMapper, never()).selectListByQuery(any(QueryWrapper.class));
        }

        @Test
        @DisplayName("listByOpportunity returns opportunity-related follow-ups")
        void shouldListByOpportunity() {
            FollowUp f = sample(1L, 1L, 7L);
            when(followUpMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(f));

            List<FollowUp> result = service.listByOpportunity(7L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOpportunityId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("listByCustomer should delegate to mapper")
        void shouldListByCustomer() {
            when(followUpMapper.selectByCustomerId(1L)).thenReturn(List.of(sample(1L, 1L, null)));

            List<FollowUp> result = service.listByCustomer(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getById should return the follow-up")
        void shouldGetById() {
            FollowUp f = sample(1L, 1L, null);
            when(followUpMapper.selectOneById(1L)).thenReturn(f);

            assertThat(service.getById(1L)).isEqualTo(f);
        }
    }

    private FollowUp sample(long id, long customerId, Long opportunityId) {
        return FollowUp.builder()
                .id(id)
                .customerId(customerId)
                .opportunityId(opportunityId)
                .type("PHONE")
                .content("x")
                .isDeleted(0)
                .build();
    }
}
