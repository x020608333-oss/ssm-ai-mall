package com.shop.dao;

import com.shop.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    int insert(User user);

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    /** 管理端分页查询，PageHelper.startPage 后调用 */
    List<User> findList(@Param("keyword") String keyword);

    int countAll();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateRole(@Param("id") Long id, @Param("role") String role);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
