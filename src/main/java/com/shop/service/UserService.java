package com.shop.service;

import com.shop.common.PageResult;
import com.shop.entity.User;

public interface UserService {

    void register(String username, String password, String nickname);

    User login(String username, String password);

    User findById(Long id);

    void updateProfile(Long id, String nickname, String phone, String email);

    void updatePassword(Long id, String oldPwd, String newPwd);

    PageResult<User> pageForAdmin(String keyword, int page, int size);

    void updateStatus(Long id, Integer status);

    void updateRole(Long id, String role);
}
