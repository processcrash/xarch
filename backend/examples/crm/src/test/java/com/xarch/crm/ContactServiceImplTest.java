package com.xarch.crm;

import com.xarch.crm.dto.ContactDTO;
import com.xarch.crm.entity.Contact;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.ContactMapper;
import com.xarch.crm.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ContactServiceImpl}. Validates the
 * "single primary per customer" invariant.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contact Service Tests")
class ContactServiceImplTest {

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ContactServiceImpl service;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should throw when customerId is null")
        void shouldThrow_whenCustomerIdNull() {
            ContactDTO dto = new ContactDTO(null, null, "Joe", null, null, null, null);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(CrmException.class)
                    .hasMessageContaining("customerId");
            verify(contactMapper, never()).insert(any(Contact.class));
        }

        @Test
        @DisplayName("should insert non-primary contact without touching others")
        void shouldInsertNonPrimary() {
            ContactDTO dto = new ContactDTO(null, 1L, "Joe", "mgr", "555", "j@x.com", false);

            service.create(dto);

            ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
            verify(contactMapper).insert(captor.capture());
            Contact saved = captor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(1L);
            assertThat(saved.getName()).isEqualTo("Joe");
            assertThat(saved.getIsPrimary()).isFalse();
        }

        @Test
        @DisplayName("should demote other primaries when inserting primary contact")
        void shouldInsertPrimaryAndDemoteOthers() {
            ContactDTO dto = new ContactDTO(null, 1L, "Alice", "ceo", "555", "a@x.com", true);
            Contact existing = Contact.builder()
                    .id(10L).customerId(1L).name("Old").isPrimary(true).build();
            when(contactMapper.selectByCustomerId(1L)).thenReturn(new ArrayList<>(List.of(existing)));
            when(contactMapper.insert(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(11L);
                return 1;
            });

            service.create(dto);

            assertThat(existing.getIsPrimary()).isFalse();
            verify(contactMapper).update(existing);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update contact fields")
        void shouldUpdate() {
            Contact existing = Contact.builder()
                    .id(1L).customerId(1L).name("Joe").isPrimary(false).build();
            when(contactMapper.selectOneById(1L)).thenReturn(existing);
            ContactDTO dto = new ContactDTO(1L, 1L, "Joseph", "VP", "555", "j@x.com", false);

            service.update(dto);

            assertThat(existing.getName()).isEqualTo("Joseph");
            assertThat(existing.getPosition()).isEqualTo("VP");
        }

        @Test
        @DisplayName("should throw when id missing on update")
        void shouldThrow_whenIdMissing() {
            ContactDTO dto = new ContactDTO(null, 1L, "x", null, null, null, null);
            assertThatThrownBy(() -> service.update(dto))
                    .isInstanceOf(CrmException.class);
        }

        @Test
        @DisplayName("should throw when contact not found")
        void shouldThrow_whenMissing() {
            when(contactMapper.selectOneById(1L)).thenReturn(null);
            ContactDTO dto = new ContactDTO(1L, 1L, "x", null, null, null, null);
            assertThatThrownBy(() -> service.update(dto))
                    .isInstanceOf(CrmException.class);
        }
    }

    @Nested
    @DisplayName("setPrimary / delete")
    class Mutations {

        @Test
        @DisplayName("setPrimary should unset previous primary")
        void shouldSetPrimary() {
            Contact current = Contact.builder()
                    .id(10L).customerId(1L).isPrimary(true).build();
            Contact target = Contact.builder()
                    .id(20L).customerId(1L).isPrimary(false).build();
            when(contactMapper.selectOneById(20L)).thenReturn(target);
            when(contactMapper.selectByCustomerId(1L)).thenReturn(List.of(current, target));

            service.setPrimary(20L);

            assertThat(target.getIsPrimary()).isTrue();
            assertThat(current.getIsPrimary()).isFalse();
            verify(contactMapper).update(current);
        }

        @Test
        @DisplayName("setPrimary should throw when contact missing")
        void shouldThrowOnSetPrimary() {
            when(contactMapper.selectOneById(20L)).thenReturn(null);

            assertThatThrownBy(() -> service.setPrimary(20L))
                    .isInstanceOf(CrmException.class);
        }

        @Test
        @DisplayName("delete should soft delete")
        void shouldDelete() {
            Contact contact = Contact.builder().id(1L).build();
            when(contactMapper.selectOneById(1L)).thenReturn(contact);

            service.delete(1L);

            assertThat(contact.getIsDeleted()).isEqualTo(1);
            verify(contactMapper).update(contact);
        }
    }

    @Nested
    @DisplayName("listByCustomer / getById")
    class Reads {

        @Test
        @DisplayName("should list contacts by customer")
        void shouldListByCustomer() {
            when(contactMapper.selectByCustomerId(1L)).thenReturn(List.of(
                    Contact.builder().id(1L).customerId(1L).build()));

            List<Contact> result = service.listByCustomer(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should get contact by id")
        void shouldGetById() {
            Contact c = Contact.builder().id(1L).build();
            when(contactMapper.selectOneById(1L)).thenReturn(c);

            assertThat(service.getById(1L)).isEqualTo(c);
        }
    }
}
