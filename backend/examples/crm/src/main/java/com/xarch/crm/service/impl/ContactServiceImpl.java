package com.xarch.crm.service.impl;

import com.xarch.crm.dto.ContactDTO;
import com.xarch.crm.entity.Contact;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.ContactMapper;
import com.xarch.crm.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Contact service implementation. Enforces the "one primary contact
 * per customer" invariant.
 */
@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMapper contactMapper;

    @Override
    public List<Contact> listByCustomer(Long customerId) {
        return contactMapper.selectByCustomerId(customerId);
    }

    @Override
    public Contact getById(Long id) {
        return contactMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ContactDTO dto) {
        if (dto.customerId() == null) {
            throw new CrmException("Contact.customerId is required");
        }
        long now = System.currentTimeMillis();
        Contact contact = Contact.builder()
                .customerId(dto.customerId())
                .name(dto.name())
                .position(dto.position())
                .phone(dto.phone())
                .email(dto.email())
                .isPrimary(Boolean.TRUE.equals(dto.isPrimary()))
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        if (Boolean.TRUE.equals(contact.getIsPrimary())) {
            demoteOtherPrimaries(contact.getCustomerId(), null);
        }
        contactMapper.insert(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ContactDTO dto) {
        Contact existing = requireContact(dto.id());
        existing.setName(dto.name());
        existing.setPosition(dto.position());
        existing.setPhone(dto.phone());
        existing.setEmail(dto.email());
        if (dto.isPrimary() != null) {
            existing.setIsPrimary(dto.isPrimary());
            if (Boolean.TRUE.equals(dto.isPrimary())) {
                demoteOtherPrimaries(existing.getCustomerId(), existing.getId());
            }
        }
        existing.setUpdateTime(System.currentTimeMillis());
        contactMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Contact contact = requireContact(id);
        contact.setIsDeleted(1);
        contact.setUpdateTime(System.currentTimeMillis());
        contactMapper.update(contact);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPrimary(Long id) {
        Contact contact = requireContact(id);
        demoteOtherPrimaries(contact.getCustomerId(), contact.getId());
        contact.setIsPrimary(true);
        contact.setUpdateTime(System.currentTimeMillis());
        contactMapper.update(contact);
    }

    /**
     * Unset {@code isPrimary} on every other contact of the given customer
     * so that exactly one remains primary.
     */
    private void demoteOtherPrimaries(Long customerId, Long keepId) {
        if (customerId == null) {
            return;
        }
        List<Contact> contacts = contactMapper.selectByCustomerId(customerId);
        for (Contact c : contacts) {
            if (keepId != null && keepId.equals(c.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(c.getIsPrimary())) {
                c.setIsPrimary(false);
                c.setUpdateTime(System.currentTimeMillis());
                contactMapper.update(c);
            }
        }
    }

    /**
     * Look up a contact or throw a domain exception.
     */
    private Contact requireContact(Long id) {
        if (id == null) {
            throw new CrmException("Contact id is required");
        }
        Contact contact = contactMapper.selectOneById(id);
        if (contact == null) {
            throw new CrmException("Contact not found: " + id);
        }
        return contact;
    }
}