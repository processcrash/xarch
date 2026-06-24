package com.xarch.crm.service;

import com.xarch.crm.entity.Contact;

import java.util.List;

/**
 * Contact business interface.
 */
public interface ContactService {

    List<Contact> listByCustomer(Long customerId);

    Contact getById(Long id);

    /** Create a new contact. The {@code isPrimary} flag is honoured. */
    void create(Contact contact);

    void update(Contact contact);

    void delete(Long id);
}
