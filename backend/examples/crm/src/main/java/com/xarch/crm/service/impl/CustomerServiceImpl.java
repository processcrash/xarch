package com.xarch.crm.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.CustomerDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.CustomerMapper;
import com.xarch.crm.service.CustomerService;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Customer service implementation. Validates uniqueness on
 * (name + contactPhone), manages soft delete and exposes
 * lifecycle transitions (convert / lose / assignOwner).
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    public static final String TYPE_LEAD = "LEAD";
    public static final String TYPE_PROSPECT = "PROSPECT";
    public static final String TYPE_CUSTOMER = "CUSTOMER";
    public static final String TYPE_LOST = "LOST";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public PageResult<Customer> page(String name, String type, String level, Long ownerId,
                                     int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(Customer.class);
        if (StringUtils.hasText(name)) {
            wrapper.and("name LIKE ?", "%" + name + "%");
        }
        if (StringUtils.hasText(type)) {
            wrapper.and("type = ?", type);
        }
        if (StringUtils.hasText(level)) {
            wrapper.and("level = ?", level);
        }
        if (ownerId != null) {
            wrapper.and("owner_id = ?", ownerId);
        }
        wrapper.orderBy("create_time", false);
        Page<Customer> page = customerMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public Customer getById(Long id) {
        return customerMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CustomerDTO dto) {
        // uniqueness on (name + contactPhone)
        if (StringUtils.hasText(dto.name()) && StringUtils.hasText(dto.contactPhone())) {
            Customer existing = customerMapper.selectOneByQuery(QueryWrapper.create()
                    .from(Customer.class)
                    .where("name = ?", dto.name())
                    .and("contact_phone = ?", dto.contactPhone()));
            if (existing != null) {
                throw new CrmException("Customer already exists: "
                        + dto.name() + " / " + dto.contactPhone());
            }
        }

        long now = System.currentTimeMillis();
        Customer customer = Customer.builder()
                .name(dto.name())
                .type(StringUtils.hasText(dto.type()) ? dto.type() : TYPE_LEAD)
                .industry(dto.industry())
                .scale(dto.scale())
                .contactName(dto.contactName())
                .contactPhone(dto.contactPhone())
                .contactEmail(dto.contactEmail())
                .address(dto.address())
                .website(dto.website())
                .ownerId(dto.ownerId())
                .source(dto.source())
                .level(dto.level())
                .status(STATUS_ACTIVE)
                .tags(dto.tags() == null ? null : String.join(",", dto.tags()))
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        customerMapper.insert(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CustomerDTO dto) {
        Customer customer = requireCustomer(id);
        if (StringUtils.hasText(dto.name())) {
            customer.setName(dto.name());
        }
        customer.setType(dto.type());
        customer.setIndustry(dto.industry());
        customer.setScale(dto.scale());
        customer.setContactName(dto.contactName());
        customer.setContactPhone(dto.contactPhone());
        customer.setContactEmail(dto.contactEmail());
        customer.setAddress(dto.address());
        customer.setWebsite(dto.website());
        customer.setSource(dto.source());
        customer.setLevel(dto.level());
        if (dto.tags() != null) {
            customer.setTags(String.join(",", dto.tags()));
        }
        // ownerId is not changed through the generic update - use assignOwner
        customer.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Customer customer = requireCustomer(id);
        customer.setIsDeleted(1);
        customer.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignOwner(Long id, Long ownerId) {
        Customer customer = requireCustomer(id);
        customer.setOwnerId(ownerId);
        customer.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convert(Long id) {
        Customer customer = requireCustomer(id);
        customer.setType(TYPE_CUSTOMER);
        customer.setStatus(STATUS_ACTIVE);
        customer.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lose(Long id, String reason) {
        Customer customer = requireCustomer(id);
        customer.setType(TYPE_LOST);
        customer.setStatus(STATUS_INACTIVE);
        customer.setLastContactTime(System.currentTimeMillis());
        customer.setUpdateTime(System.currentTimeMillis());
        customerMapper.update(customer);
        // reason is intentionally not persisted here - the follow-up log
        // holds the explanation
        if (reason == null) {
            // keep behaviour deterministic
        }
    }

    /**
     * Look up a customer or throw a domain exception.
     */
    private Customer requireCustomer(Long id) {
        Customer customer = customerMapper.selectOneById(id);
        if (customer == null) {
            throw new CrmException("Customer not found: " + id);
        }
        return customer;
    }

    /**
     * Unused helper kept for symmetry with other services that need to
     * iterate by owner. Avoids an unused-import warning on List.
     */
    @SuppressWarnings("unused")
    private List<Customer> listByOwner(Long ownerId) {
        return customerMapper.selectByOwner(ownerId);
    }
}