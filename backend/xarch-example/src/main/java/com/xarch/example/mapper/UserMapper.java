package com.xarch.example.mapper;

import com.xarch.example.entity.User;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

/**
 * User mapper - includes user-role relation operations
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * Get user role IDs by user ID
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * Delete all roles for a user
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    /**
     * Insert a user-role relation
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * Get user with role IDs as comma-separated string
     */
    @Select("SELECT u.*, GROUP_CONCAT(ur.role_id) as roleIds " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE u.id = #{id} AND u.del_flag = 0 " +
            "GROUP BY u.id")
    User selectUserWithRoles(@Param("id") Long id);
}