package com.xarch.example.auth.service;

import com.xarch.example.auth.entity.User;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * User management service contract for {@code service-auth}.
 *
 * <p>Methods mirror the original monolithic {@code UserService} so the
 * controller layer can be migrated without changes.
 */
public interface UserService {

    /**
     * Page-query users.
     *
     * @param username filter by username (optional)
     * @param status   filter by status (optional)
     * @param pageNum  1-based page number
     * @param pageSize page size
     * @return paged users
     */
    PageResult<User> page(String username, String status, int pageNum, int pageSize);

    /**
     * Fetch one user by id.
     *
     * @param id primary key
     * @return user or {@code null}
     */
    User getById(Long id);

    /**
     * Create a new user.
     *
     * @param user user to create (id ignored)
     */
    void create(User user);

    /**
     * Update an existing user.
     *
     * @param user user payload — must carry the target id
     */
    void update(User user);

    /**
     * Delete a user by id.
     *
     * @param id primary key
     */
    void delete(Long id);

    /**
     * List all users — typically used for option/dropdown components.
     *
     * @return full user list
     */
    List<User> list();

    /**
     * Resolve the role ids assigned to a user.
     *
     * @param id user id
     * @return role id list
     */
    List<Long> getRoleIds(Long id);

    /**
     * Replace the role assignments for a user.
     *
     * @param id      user id
     * @param roleIds new role id set
     */
    void assignRoles(Long id, List<Long> roleIds);
}