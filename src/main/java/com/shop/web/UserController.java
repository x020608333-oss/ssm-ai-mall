package com.shop.web;

import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.User;
import com.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 个人资料 / 密码
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/update")
    public Result<Void> update(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        userService.updateProfile(user.getId(), body.get("nickname"), body.get("phone"), body.get("email"));
        return Result.ok();
    }

    @PostMapping("/password")
    public Result<Void> password(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        userService.updatePassword(user.getId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }
}
