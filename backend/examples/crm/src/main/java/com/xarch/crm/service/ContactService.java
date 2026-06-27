package com.xarch.crm.service;

import com.xarch.crm.dto.ContactDTO;
import com.xarch.crm.entity.Contact;

import java.util.List;

/**
 * Contact business interface.
 */
public interface ContactService {

    List<Contact> listByCustomer(Long customerId);

    Contact getById(Long id);

    /** Create a new contact. The {@code isPrimary} flag is honoured. */
    void create(ContactDTO dto);

    void update(ContactDTO dto);

    void delete(Long id);

    /** Promote a contact to the primary contact for its customer. */
    void setPrimary(Long id);
}