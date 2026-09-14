package com.shop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shop.common.BizException;
import com.shop.common.PageResult;
import com.shop.dao.UserMapper;
import com.shop.entity.User;
import com.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(String username, String password, String nickname) {
        if (!StringUtils.hasText(username) || username.length() < 3 || username.length() > 20) {
            throw new BizException("用户名长度需在3~20位之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BizException("密码至少6位");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new BizException("用户名已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(md5(password))) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateProfile(Long id, String nickname, String phone, String email) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
        userMapper.updateProfile(user);
    }

    @Override
    public void updatePassword(Long id, String oldPwd, String newPwd) {
        User user = userMapper.findById(id);
        if (user == null || !user.getPassword().equals(md5(oldPwd))) {
            throw new BizException("原密码错误");
        }
        if (!StringUtils.hasText(newPwd) || newPwd.length() < 6) {
            throw new BizException("新密码至少6位");
        }
        userMapper.updatePassword(id, md5(newPwd));
    }

    @Override
    public PageResult<User> pageForAdmin(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> list = userMapper.findList(keyword);
        PageInfo<User> info = new PageInfo<>(list);
        list.forEach(u -> u.setPassword(null));
        return PageResult.of(info.getTotal(), info.getPageNum(), info.getPageSize(), list);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    @Override
    public void updateRole(Long id, String role) {
        userMapper.updateRole(id, role);
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
