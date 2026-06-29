package com.xarch.crm;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.CustomerDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.CustomerMapper;
import com.xarch.crm.service.impl.CustomerServiceImpl;
import com.xarch.starter.core.result.PageResult;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomerServiceImpl}. Covers lifecycle
 * transitions and filter-based pagination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should reject duplicate (name + phone)")
        void shouldRejectDuplicate() {
            CustomerDTO dto = new CustomerDTO("Acme", "LEAD", null, null, "Joe",
                    "555-1234", null, null, null, 1L, "WEB", "A", null);
            Customer existing = sampleCustomer(99L);
            when(customerMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(existing);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(CrmException.class)
                    .hasMessageContaining("already exists");
            verify(customerMapper, never()).insert(any(Customer.class));
        }

        @Test
        @DisplayName("should insert with LEAD type and ACTIVE status when unique")
        void shouldInsertCustomer() {
            CustomerDTO dto = new CustomerDTO("Acme", null, null, null, "Joe",
                    "555-1234", null, null, null, 1L, "WEB", "A", List.of("vip", "partner"));
            when(customerMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
            when(customerMapper.insert(any(Customer.class))).thenAnswer(inv -> {
                Customer c = inv.getArgument(0);
                c.setId(1L);
                return 1;
            });

            service.create(dto);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerMapper).insert(captor.capture());
            Customer saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("Acme");
            assertThat(saved.getType()).isEqualTo(CustomerServiceImpl.TYPE_LEAD);
            assertThat(saved.getStatus()).isEqualTo(CustomerServiceImpl.STATUS_ACTIVE);
            assertThat(saved.getTags()).contains("vip").contains("partner");
            assertThat(saved.getCreateTime()).isNotNull();
            assertThat(saved.getIsDeleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("should respect provided type")
        void shouldRespectType() {
            CustomerDTO dto = new CustomerDTO("Acme", "PROSPECT", null, null, null,
                    null, null, null, null, 1L, null, "B", null);
            when(customerMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

            service.create(dto);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerMapper).insert(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo("PROSPECT");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update existing customer")
        void shouldUpdate() {
            Customer existing = sampleCustomer(1L);
            when(customerMapper.selectOneById(1L)).thenReturn(existing);
            CustomerDTO dto = new CustomerDTO("Updated", null, null, null, null,
                    null, null, null, null, null, null, "C", List.of("x"));

            service.update(1L, dto);

            assertThat(existing.getName()).isEqualTo("Updated");
            assertThat(existing.getUpdateTime()).isNotNull();
            verify(customerMapper).update(existing);
        }

        @Test
        @DisplayName("should throw when customer missing")
        void shouldThrowOnUpdate() {
            when(customerMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> service.update(1L,
                    new CustomerDTO("x", null, null, null, null, null, null, null, null,
                            null, null, null, null)))
                    .isInstanceOf(CrmException.class);
        }
    }

    @Nested
    @DisplayName("lifecycle transitions")
    class Lifecycle {

        @Test
        @DisplayName("assignOwner should change ownerId")
        void shouldAssignOwner() {
            Customer existing = sampleCustomer(1L);
            when(customerMapper.selectOneById(1L)).thenReturn(existing);

            service.assignOwner(1L, 99L);

            assertThat(existing.getOwnerId()).isEqualTo(99L);
            verify(customerMapper).update(existing);
        }

        @Test
        @DisplayName("convert should move LEAD -> CUSTOMER")
        void shouldConvert() {
            Customer existing = sampleCustomer(1L);
            existing.setType(CustomerServiceImpl.TYPE_LEAD);
            when(customerMapper.selectOneById(1L)).thenReturn(existing);

            service.convert(1L);

            assertThat(existing.getType()).isEqualTo(CustomerServiceImpl.TYPE_CUSTOMER);
            assertThat(existing.getStatus()).isEqualTo(CustomerServiceImpl.STATUS_ACTIVE);
        }

        @Test
        @DisplayName("lose should set type=LOST and stamp lastContactTime")
        void shouldMarkLost() {
            Customer existing = sampleCustomer(1L);
            when(customerMapper.selectOneById(1L)).thenReturn(existing);

            service.lose(1L, "too expensive");

            assertThat(existing.getType()).isEqualTo(CustomerServiceImpl.TYPE_LOST);
            assertThat(existing.getStatus()).isEqualTo(CustomerServiceImpl.STATUS_INACTIVE);
            assertThat(existing.getLastContactTime()).isNotNull();
        }

        @Test
        @DisplayName("delete should soft delete (isDeleted=1)")
        void shouldSoftDelete() {
            Customer existing = sampleCustomer(1L);
            when(customerMapper.selectOneById(1L)).thenReturn(existing);

            service.delete(1L);

            assertThat(existing.getIsDeleted()).isEqualTo(1);
            verify(customerMapper).update(existing);
        }
    }

    @Nested
    @DisplayName("page")
    class PageTests {

        @Test
        @DisplayName("should accept all filters")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldPage() {
            com.mybatisflex.core.paginate.Page page = com.mybatisflex.core.paginate.Page.of(1, 10);
            page.setRecords(List.of(sampleCustomer(1L)));
            page.setTotalRow(1L);
            when(customerMapper.paginate(anyLong(), anyLong(), any(QueryWrapper.class)))
                    .thenReturn(page);

            PageResult<Customer> result = service.page("Acme", "LEAD", "A", 1L, 1, 10);

            assertThat(result.getList()).hasSize(1);
            verify(customerMapper).paginate(1, 10, any(QueryWrapper.class));
        }

        @Test
        @DisplayName("should handle null filters")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void shouldHandleNulls() {
            com.mybatisflex.core.paginate.Page page = com.mybatisflex.core.paginate.Page.of(1, 10);
            page.setRecords(List.of());
            page.setTotalRow(0L);
            when(customerMapper.paginate(anyLong(), anyLong(), any(QueryWrapper.class)))
                    .thenReturn(page);

            PageResult<Customer> result = service.page(null, null, null, null, 1, 10);

            assertThat(result.getList()).isEmpty();
        }

        @Test
        @DisplayName("should get by id")
        void shouldGetById() {
            Customer c = sampleCustomer(1L);
            when(customerMapper.selectOneById(1L)).thenReturn(c);
            assertThat(service.getById(1L)).isEqualTo(c);
        }
    }

    private Customer sampleCustomer(long id) {
        return Customer.builder()
                .id(id)
                .name("Acme")
                .type(CustomerServiceImpl.TYPE_LEAD)
                .contactPhone("555-1234")
                .status(CustomerServiceImpl.STATUS_ACTIVE)
                .isDeleted(0)
                .build();
    }
}
